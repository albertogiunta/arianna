package ontologies

/**
  * Created by Matteo Gabellini on 28/06/2017.
  */
trait MessageType {
    
    def toString: String
    
    override def equals(obj: scala.Any) = obj match {
        case o: MessageType => o.toString == this.toString
        case o: String => o == this.toString
    }
}

final case class AriadneMessageType(override val toString: String) extends MessageType

object MessageType {
    
    val Init = AriadneMessageType("Init")
    val Alarm = AriadneMessageType("Alarm")
    val Topology = AriadneMessageType("Topology")
    val SensorData = AriadneMessageType("SensorData")
    val Handshake = AriadneMessageType("Handshake")
    val Practicability = AriadneMessageType("Practicability")
    val CellData = AriadneMessageType("CellData")
    val VariableType = AriadneMessageType("VariableType")
    
    implicit def MessageType2String(msg: MessageType): String = msg.toString
    
    implicit def String2MessageType(str: String): MessageType = AriadneMessageType(str)

}

object MessageTypeFactory {
    
    def apply(typeName: String): MessageType = typeName match {
        case MessageType.Init.toString => MessageType.Init
        case MessageType.Alarm.toString => MessageType.Alarm
        case MessageType.Topology.toString => MessageType.Topology
        case MessageType.SensorData.toString => MessageType.SensorData
        case MessageType.Handshake.toString => MessageType.Handshake
        case MessageType.Practicability.toString => MessageType.Practicability
        case MessageType.CellData.toString => MessageType.CellData
        case MessageType.VariableType.toString => MessageType.VariableType
        case _ => null
    }
}

trait Message {
    
    def messageType: MessageType
    
    def content: String
    
    override def toString =
        "Message of Type(" + messageType.toString + ") and Content was " + content.toString
    
    override def equals(obj: Any) = obj match {
        case msg: Message =>
            msg.messageType == this.messageType && msg.content == this.content
        case _ => false
    }
}

final case class AriadneMessage(messageType: MessageType, content: String) extends Message

object TestMessageType extends App {
    val s: String = MessageType.Init
    
    println(MessageType.Init == "Init")
}