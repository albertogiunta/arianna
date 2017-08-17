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
trait MessageSerializer[C] {
    
    /**
      * Performs a transformation of the given message from its object view to an array of Byte
      *
      * @param message The Message object to be serialized
      * @return The Array of  Byte representing the object
      */
    def serialize(message: Message[C]): Array[Byte]
    
    /**
      * Performs a transformation of the given message from a Byte Array to its Object view
      *
      * @param array The Array of Byte representing the Message
      * @return The object view of the Message
      */
    def deserialize(array: Array[Byte]): Message[C]
}

/**
  * An Utility Companion Object that provides the logic to Serialize any object that implements the trait Message
  */
object MessageSerializer extends MessageSerializer[MessageContent] {
    
    override def serialize(message: Message[MessageContent]): Array[Byte] = {
    
        val char2byte: Char => Byte = c => c.toByte
        
        
        /*
            Create an Array[Byte] of the same length of the sum of the length in Byte of the fields
            the first byte are intLengthInByte that are needed in order to get the length of the messageType,
            which is variable. This is always recoverable since Int are always of fixed length 8 Byte / 32 bit
        */
        Array.concat(
            Array.fill(1) {
                message.subtype.toString.length.toByte
            },
            message.subtype.toString.map(char2byte).toArray,
            
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
        val fromBytes = retrieveBlock(fromOffset, toOffset - 1)
        val toBytes = retrieveBlock(toOffset, contentOffset)
        val contentBytes = retrieveBlock(contentOffset, array.length)
        val subtype = MessageSubtype.StaticFactory(subtypeBytes.mkString)
        
        AriadneMessage(
            subtype.superType,
            subtype,
            Location.Factory(fromBytes.mkString) >> Location.Factory(toBytes.mkString),
            subtype.unmarshal(contentBytes.mkString)
        )
    }
    
}