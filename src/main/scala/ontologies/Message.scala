package ontologies

/**
  * Created by Matteo Gabellini on 28/06/2017.
  */
trait MessageType {
    def typeName: String
}

object Init extends MessageType {
    override def typeName: String = "Init"
}

object Alarm extends MessageType {
    override def typeName: String = "Alarm"
}

object Topology extends MessageType {
    override def typeName: String = "Topology"
}

object SensorData extends MessageType {
    override def typeName: String = "SensorData"
}

object Handshake extends MessageType {
    override def typeName: String = "Handshake"
}

object Practicability extends MessageType {
    override def typeName: String = "WeightData"
}

object CellData extends MessageType {
    override def typeName: String = "CellData"
}

object VariableType extends MessageType {
    override def typeName: String = "aaaaaaaaaaaaaaa"
}

object MessageTypeFactory {
    def apply(typeName: String): MessageType = typeName match {
        case t if t == Init.typeName => Init
        case t if t == Alarm.typeName => Alarm
        case t if t == Topology.typeName => Topology
        case t if t == SensorData.typeName => SensorData
        case t if t == Handshake.typeName => Handshake
        case t if t == Practicability.typeName => Practicability
        case t if t == CellData.typeName => CellData
        case t if t == VariableType.typeName => VariableType
        case _ => null
    }
}

trait Message {
    
    def messageType: MessageType
    
    def content: String
    
    override def toString =
        "Message Type is " + messageType.typeName + "\n" +
            "Content is " + content
    
    override def equals(obj: Any) = obj match {
        case msg: Message =>
            msg.messageType == this.messageType && msg.content == this.content
        case _ => false
    }
}

case class AriadneMessage(messageType: MessageType, content: String) extends Message
