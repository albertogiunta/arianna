package serialization

import akka.serialization._
import ontologies._

/**
  * A Custom Serializer for Message(s) to be handled by the ActorSystem itself
  *
  */
class AriadneMessageSerializer extends SerializerWithStringManifest {
    
    override def identifier = 21040507
    
    override def manifest(obj: AnyRef): String = obj match {
        case _: AriadneMessage => AriadneMessage.getClass.getName
        case _: Message => "ontologies.Message$"
        case _ => null
    }
    
    override def toBinary(obj: AnyRef): Array[Byte] = obj match {
        case msg: AriadneMessage => MessageSerializer.serialize(msg)
        case msg: Message => MessageSerializer.serialize(msg)
        case _ => null
    }
    
    override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
        case man if man == AriadneMessage.getClass.getName || man == "ontologies.Message$" =>
            MessageSerializer.deserialize(bytes)
        case _ => null
    }
}

/**
  * A Custom Serializer that defines methods for an Object to be Serialized.
  *
  * The actual implementation statically provided by the Object companion MessageSerializer
  *
  */
trait MessageSerializer {
    def serialize(message: Message): Array[Byte]
    
    def deserialize(array: Array[Byte]): Message
}

/**
  * An Utility Companion Object that provides the logic to Serialize any object that implements the trait Message
  */
object MessageSerializer extends MessageSerializer {
    
    override def serialize(message: Message): Array[Byte] = {
    
        val char2byte: Char => Byte = c => c.toByte
        
        // Create an Array[Byte] of the same length of the sum of the length in Byte of the fields
        // the first byte are intLengthInByte that are needed in order to get the length of the messageType,
        // which is variable
        // This is always recoverable since Int are always of fixed length 8 Byte / 32 bit
        Array.concat(
            Array.fill(1) {
                message.messageType.toString.length.toByte
            },
            message.messageType.toString.toStream.map(char2byte).toArray,
            message.content.toStream.map(char2byte).toArray
        )
    }
    
    override def deserialize(array: Array[Byte]): Message = {
    
        AriadneMessage(
            MessageTypeFactory(
                (for {
                    i <- 1 until array(0) + 1
                } yield array(i).toChar).toStream.mkString
            ),
            (for {
                j <- array(0) + 1 until array.length
            } yield array(j).toChar).toStream.mkString
        )
    }
}

object DataTypeLengthConverter {
    
    val charLengthInByte = 2
    val intLengthInByte = 4
    val shortLengthInByte = 2
    val longLengthInByte = 8
    val floatLengthInByte = 4
    val doubleLengthInByte = 8
    val booleanLengthInByte = 1
    val stringLengthInByte: Int => Int = _ * charLengthInByte
}

object TestSerializer extends App {
    
    println(MessageSerializer.deserialize(
        MessageSerializer.serialize(
            AriadneMessage(MessageType.VariableType, "ciao")
        )
    ))
}