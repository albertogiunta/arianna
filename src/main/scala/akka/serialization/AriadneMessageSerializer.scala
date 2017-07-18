package akka.serialization

import ontologies.messages.Location._
import ontologies.messages._

/**
  * A Custom Serializer for Message(s) to be handled by the ActorSystem itself
  *
  */
class AriadneMessageSerializer extends SerializerWithStringManifest {
    
    override def identifier = 21040507
    
    override def manifest(obj: AnyRef): String = obj match {
        case _: AriadneMessage[MessageContent] => AriadneMessage.getClass.getName
        case _: Message[MessageContent] => "ontologies.messages.Message$"
        case _ => null
    }
    
    override def toBinary(obj: AnyRef): Array[Byte] = obj match {
        case msg: AriadneMessage[MessageContent] => MessageSerializer.serialize(msg)
        case msg: Message[MessageContent] => MessageSerializer.serialize(msg)
        case _ => null
    }
    
    override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
        case man if man == AriadneMessage.getClass.getName ||
            man == "ontologies.Message$" =>
            MessageSerializer.deserialize(bytes)
        case _ => null
    }
}

/**
  * A Custom Serializer that defines methods for an Object to be Serialized.
  *
  * The actual implementation is statically provided by the Object companion MessageSerializer
  *
  */
trait MessageSerializer[T] {
    def serialize(message: Message[T]): Array[Byte]
    
    def deserialize(array: Array[Byte]): Message[T]
}

/**
  * An Utility Companion Object that provides the logic to Serialize any object that implements the trait Message
  */
object MessageSerializer extends MessageSerializer[MessageContent] {
    
    override def serialize(message: Message[MessageContent]): Array[Byte] = {
        
        val char2byte: Char => Byte = { c =>
            //println(c.toString + "=>" + c.toByte)
            c.toByte
        }
        
        /*
            Create an Array[Byte] of the same length of the sum of the length in Byte of the fields
            the first byte are intLengthInByte that are needed in order to get the length of the messageType,
            which is variable. This is always recoverable since Int are always of fixed length 8 Byte / 32 bit
        */
        Array.concat(
            Array.fill(1) {
                message.subtype.subtypeName.length.toByte
            },
            message.subtype.subtypeName.map(char2byte).toArray,
            
            Array.fill(1) {
                message.direction.iter.from.length.toByte
            },
            message.direction.iter.from.map(char2byte).toArray,
            
            Array.fill(1) {
                message.direction.iter.to.length.toByte
            },
            message.direction.iter.to.map(char2byte).toArray,
            
            message.subtype.marshal(message.content).map(char2byte).toArray
        )
    }
    
    override def deserialize(array: Array[Byte]): Message[MessageContent] = {
        
        val retrieveBlock: (Int, Int) => Seq[Char] =
            (from, to) => {
                for {
                    j <- from until to
                } yield array(j).toChar
            }
        
        val subtypeLen = array(0)
        val subtypeOffset = 1
        
        val fromLen = array(subtypeOffset + subtypeLen)
        val fromOffset = subtypeOffset + subtypeLen + 1
        
        val toLen = array(fromOffset + fromLen)
        val toOffset = fromOffset + fromLen + 1
        
        val contentOffset = toOffset + toLen
        
        /** ************************************************************/
        
        val subtypeBytes = retrieveBlock(subtypeOffset, fromOffset - 1)
        //println(subtype)
        val fromBytes = retrieveBlock(fromOffset, toOffset - 1)
        //println(from)
        val toBytes = retrieveBlock(toOffset, contentOffset)
        //println(to)
        val contentBytes = retrieveBlock(contentOffset, array.length)
        //println(content)
        
        val subtype = MessageSubtype.Factory(subtypeBytes.mkString)
        
        AriadneMessage(
            subtype.superType,
            subtype,
            Location.Factory(fromBytes.mkString) >> Location.Factory(toBytes.mkString),
            subtype.unmarshal(contentBytes.mkString)
        )
    }
    
}