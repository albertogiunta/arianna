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

object WeightData extends MessageType {
  override def typeName: String = "WeightData"
}

object CellData extends MessageType {
  override def typeName: String = "CellData"
}

trait Message extends Serializable {

  def messageType: MessageType

  def content: Any

}

case class MyMessage(messageType: MessageType, content: Any) extends Message
