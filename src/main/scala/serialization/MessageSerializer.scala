package serialization

import akka.serialization._
import ontologies._

/**
  * A Custom Serializer for Message(s) to be handled by the ActorSystem itself
  *
  */
class AriadneRemoteMessageSerializer extends SerializerWithStringManifest {
    
    override def identifier = 21040507
    
    override def manifest(obj: AnyRef): String = obj match {
        case _: AriadneRemoteMessage => AriadneRemoteMessage.getClass.getName
        case _: Message[_] => "ontologies.Message$"
        case _ => null
    }
    
    override def toBinary(obj: AnyRef): Array[Byte] = obj match {
        case msg: AriadneRemoteMessage => MessageSerializer.serialize(msg)
        case msg: Message[String] => MessageSerializer.serialize(msg)
        case _ => null
    }
    
    override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
        case man if man == AriadneRemoteMessage.getClass.getName || man == "ontologies.Message$" =>
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
object MessageSerializer extends MessageSerializer[String] {
    
    override def serialize(message: Message[String]): Array[Byte] = {
        
        val char2byte: Char => Byte = { c =>
            //            println(c.toString + "=>" + c.toByte)
            c.toByte
        }
        
        // Create an Array[Byte] of the same length of the sum of the length in Byte of the fields
        // the first byte are intLengthInByte that are needed in order to get the length of the messageType,
        // which is variable
        // This is always recoverable since Int are always of fixed length 8 Byte / 32 bit
        Array.concat(
            Array.fill(1) {
                message.supertype.toString.length.toByte
            },
            message.supertype.toString.toStream.map(char2byte).toArray,
            Array.fill(1) {
                message.subtype.toString.length.toByte
            },
            message.subtype.toString.toStream.map(char2byte).toArray,
            message.content.toStream.map(char2byte).toArray
        )
    }
    
    override def deserialize(array: Array[Byte]): Message[String] = {
        
        val typeLen = array(0)
        val subtypeLen = array(typeLen + 1)
        val contentOffset = typeLen + subtypeLen
        
        val supertype = for {
            i <- 1 until typeLen + 1
        } yield array(i).toChar
        
        val subtype = for {
            k <- typeLen + 2 until contentOffset + 2
        } yield array(k).toChar
        
        val content = for {
            j <- contentOffset + 2 until array.length
        } yield array(j).toChar
        
        AriadneRemoteMessage(
            MessageTypeFactory(supertype.mkString),
            MessageSubtypeFactory(subtype.mkString),
            content.mkString
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
    
    val serial = MessageSerializer.serialize(
        AriadneRemoteMessage(MessageType.Init, MessageType.Init.Subtype.Basic, "Se2i v21eramente orribb123ile")
    )
    
    println(serial.mkString("-"))
    
    val deserial = MessageSerializer.deserialize(serial)
    
    println(deserial)
}