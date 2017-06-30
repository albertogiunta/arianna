package ontologies

/**
  * Created by Matteo Gabellini on 28/06/2017.
  */
trait Topic {
    
    def toString: String
    
    override def equals(obj: scala.Any) = obj match {
        case o: Topic => o.toString == this.toString
        case o: String => o.toString == this.toString
        case _ => false
    }
}

case class AriadneTopic(override val toString: String) extends Topic

object Topic {
    val Alarm = AriadneTopic("alarm")
    val Topology = AriadneTopic("topology")
    val HandShake = AriadneTopic("handshake")
    val SensorUpdate = AriadneTopic("sensor")
    val Practicability = AriadneTopic("practicability")
    val CellData = AriadneTopic("cell")
    
    implicit def Topic2String(topic: Topic): String = topic.toString
    
    implicit def String2Topic(string: String): Topic = AriadneTopic(string)
}

object TestTopic extends App {
    
    val s: String = Topic.Alarm
    
}