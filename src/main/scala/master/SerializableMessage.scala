package master

import akka.serialization._
import master.DataTypeLengthConverter._

/**
  * Created by Matteo Gabellini on 28/06/2017.
  */
trait MyMessageType {
    def typeName: String
}

object Init extends MyMessageType {
    override def typeName: String = "Init"
}

object Alarm extends MyMessageType {
    override def typeName: String = "Alarm"
}

object Topology extends MyMessageType {
    override def typeName: String = "Topology"
}

object SensorData extends MyMessageType {
    override def typeName: String = "SensorData"
}

object Handshake extends MyMessageType {
    override def typeName: String = "Handshake"
}

object WeightData extends MyMessageType {
    override def typeName: String = "WeightData"
}

object CellData extends MyMessageType {
    override def typeName: String = "CellData"
}

/**
  * A Custom Serializable that defines methods for an Object to be Serialized.
  *
  * This will let the object itself serialize and deserialize itself
  */
trait SerializableMessage extends Serializable {
    
    def serialize: Array[Byte]
    
    def deserialize(array: Array[Byte]): Message
}

trait Message extends SerializableMessage {
    
    def messageType: MyMessageType
    
    def content: String
}

case class MySerializableMessage(messageType: MyMessageType, content: String) extends Message {
    
    override def serialize: Array[Byte] = {
        
        // Create an Array[Byte] of the same length of the sum of the length in Byte of the fields
        // the first byte are intLengthInByte that are needed in order to get the length of the messageType,
        // which is variable
        // This is always recoverable since Int are always of fixed length 8 Byte / 32 bit
        Array.concat[Byte](
            Array.fill(intLengthInByte + 1) {
                messageType.typeName.length.toByte
            },
            messageType.typeName.toStream.map(c => c.toByte).toArray,
            content.toStream.map(c => c.toByte).toArray
        )
    }
    
    override def deserialize(array: Array[Byte]): Message = ???
}

class MessageSerializer extends Serializer {
    
    override def identifier: Int = 70504012
    
    override def toBinary(o: AnyRef): Array[Byte] = ???
    
    override def includeManifest: Boolean = ???
    
    override def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]): AnyRef = ???
}

object DataTypeLengthConverter {
    
    val charLengthInByte = 4
    val intLengthInByte = 8
    val shortLengthInByte = 4
    val longLengthInByte = 16
    val floatLengthInByte = 32
    val doubleLengthInByte = 64
    val booleanLengthInByte = 1
    val stringLengthInByte: Int => Int = _ * charLengthInByte
}

/**
  * A Custom Serializer for Message(s) to be handled by the ActorSystem itself
  *
  */
trait MySerializer extends Serializer {
    
    // The manifest (type hint) that will be provided in the fromBinary method
    // Use `""` if manifest is not needed.
    def manifest(obj: AnyRef): String
    
}

class MessageSerializerWithManifest extends MySerializer {
    
    override def identifier = 21040507
    
    override def includeManifest: Boolean = true
    
    override def manifest(obj: AnyRef): String = obj match {
        case MySerializableMessage => "MyMessage"
        case _ => null
    }
    
    override def toBinary(obj: AnyRef): Array[Byte] = ???
    
    override def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]): AnyRef = ???
    
}

object TestSerializer extends App {
    MySerializableMessage(Init, "ciaone").serialize.foreach(b => print(b))
}