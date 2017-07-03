package ontologies

/**
  * Created by Matteo Gabellini on 28/06/2017.
  */
trait Topic {
    
    def messageType: MessageType
    
    def toString: String

    override def equals(obj: scala.Any) = obj match {
        case o: Topic => o.toString == this.toString
        case o: String => o.toString == this.toString
        case _ => false
    }
}

case class AriadneTopic(override val toString: String) extends Topic {
    override def messageType = MessageTypeFactory(toString)
}

object Topic {
    // Alarms from other Actors
    val Alarm = AriadneTopic("Alarm")
    val Topology = AriadneTopic("Topology")
    // Accept Handshakes from other Actors (Cells) and save Map them into the actual Topology,
    // broadcast the new topology to the other inhabitant of the cluster.
    val HandShake = AriadneTopic("Handshake")
    // Accept Data from Cells sensors, those data are useful for computing Practicability of those Cells.
    val Practicability = AriadneTopic("Practicability")
    // Accept Cells' Data to update the map.
    val Update = AriadneTopic("Update")

    implicit def Topic2String(topic: Topic): String = topic.toString

    implicit def String2Topic(string: String): Topic = AriadneTopic(string)
}

object TestTopic extends App {

    val s: String = Topic.Alarm

}