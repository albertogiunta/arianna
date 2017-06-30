package master

import akka.serialization._

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

object VariableType extends MyMessageType {
    override def typeName: String = "aaaaaaaaaaaaaaa"
}

object MyMessageTypeFactory {
    def apply(typeName: String): MyMessageType = typeName match {
        case t if t.equals(Init.typeName) => Init
        case t if t.equals(Alarm.typeName) => Alarm
        case t if t.equals(Topology.typeName) => Topology
        case t if t.equals(SensorData.typeName) => SensorData
        case t if t.equals(Handshake.typeName) => Handshake
        case t if t.equals(WeightData.typeName) => WeightData
        case t if t.equals(CellData.typeName) => CellData
        case t if t.equals(VariableType.typeName) => VariableType
        case _ => null
    }
}

trait Message {
    
    def messageType: MyMessageType
    
    def content: String
    
    override def toString =
        "Message Type is " + messageType.typeName + "\n" +
            "Content is " + content
}

case class MySerializableMessage(messageType: MyMessageType, content: String) extends Message

/**
  * A Custom Serializer that defines methods for an Object to be Serialized.
  *
  */
trait MessageSerializer {
    def serialize(message: Message): Array[Byte]
    
    def deserialize(array: Array[Byte]): Message
}

object MessageSerializer extends MessageSerializer {
    
    override def serialize(message: Message): Array[Byte] = {
        
        val char2byte: Char => Byte = c => {
            c.toByte
        }
        
        // Create an Array[Byte] of the same length of the sum of the length in Byte of the fields
        // the first byte are intLengthInByte that are needed in order to get the length of the messageType,
        // which is variable
        // This is always recoverable since Int are always of fixed length 8 Byte / 32 bit
        Array.concat(
            Array.fill(1) {
                message.messageType.typeName.length.toByte
            },
            message.messageType.typeName.toStream.map(char2byte).toArray,
            message.content.toStream.map(char2byte).toArray
        )
    }
    
    override def deserialize(array: Array[Byte]): Message = {
        
        MySerializableMessage(
            MyMessageTypeFactory(
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

/**
  * A Custom Serializer for Message(s) to be handled by the ActorSystem itself
  *
  */
class MessageSerializerWithManifest extends SerializerWithStringManifest {
    
    override def identifier = 21040507
    
    override def manifest(obj: AnyRef): String = obj match {
        case MySerializableMessage => MySerializableMessage.getClass.getName
        case _ => null
    }
    
    override def toBinary(obj: AnyRef): Array[Byte] = obj match {
        case msg: MySerializableMessage => MessageSerializer.serialize(msg)
        case _ => null
    }
    
    override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
        case msgClassType if msgClassType.equals(MySerializableMessage.getClass.getName) =>
            MessageSerializer.deserialize(bytes)
        case _ => null
    }
    
}

object TestSerializer extends App {
    
    println(MySerializableMessage.getClass.getName)
    
    println(MessageSerializer.deserialize(
        MessageSerializer.serialize(
            MySerializableMessage(VariableType, "ciao")
        )
    ))
}