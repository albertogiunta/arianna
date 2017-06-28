package ontologies

/**
  * Created by Alessandro on 28/06/2017.
  */
trait MessageType {
    
    def name: String
    
}

object Alarm extends MessageType {
    override def name: String = "Alarm"
}

object Topology extends MessageType {
    override def name: String = "Topology"
}

object SensorData extends MessageType {
    override def name: String = "SensorData"
}

object Handshake extends MessageType {
    override def name: String = "Handshake"
}

object WeightData extends MessageType {
    override def name: String = "WeightData"
}

object CellData extends MessageType {
    override def name: String = "CellData"
}

trait Message extends Serializable {
    
    def messageType: MessageType
    
    def content: Any
    
}

case class MyMessage(messageType: MessageType, content: Any) extends Message

//object Message {
//
//    def apply(contentType : String, content: Any) : Message = MyMessage(contentType, content)
//
//    def unapply(msg: Message) : Option[(String, Any)] = Some(msg.contentType, msg.content)
//}

object TestOntologies extends App {
    val msg = MyMessage(Alarm, 666)
    
    val checker: MyMessage => Unit = {
        case msg@MyMessage(Alarm, _) => println(msg.messageType.name)
        case msg@MyMessage(_, _) => println("Fuck-off")
        case _ => println("Ciao.")
    }
    
    checker(msg)
}
