package ontologies

import ontologies.messages.{MessageSubtype, MessageType}

/**
  * Created by Matteo Gabellini on 28/06/2017.
  */
trait Topic {

    def associatedMessageType: MessageType
    
    def associatedMessageSubtype: Option[MessageSubtype]
    
    val topic: String
    val subTopic: Option[String]
    
    override def toString: String = {
        topic + "/" + subTopic.getOrElse("")
    }

    override def equals(obj: scala.Any) = obj match {
        case that: Topic => that.toString == this.toString
        case _ => false
    }
}

case class AriadneTopic(override val topic: String, override val subTopic: Option[String]) extends Topic {
    override def associatedMessageType = MessageType.Factory(topic)
    
    override def associatedMessageSubtype = Option(MessageSubtype.Factory(subTopic.orNull))
}

object Topic {
    
    val Alarms = AriadneTopic(MessageType.Alarm, Option("All"))
    
    val Topologies = AriadneTopic(MessageType.Topology, Option("All"))
    
    val HandShakes = AriadneTopic(MessageType.Handshake, Option("All"))
    
    val Routes = AriadneTopic(MessageType.Route, Option("All"))
    
    val Updates = AriadneTopic(MessageType.Update, Option("All"))
    
    val Practicabilities = AriadneTopic(MessageType.Update, Option(MessageType.Update.Subtype.Practicability))

    implicit def Topic2String(topic: Topic): String = topic.toString
    
    implicit def Topic2MessageType(topic: Topic): MessageType = topic.associatedMessageType
}

object TestTopic extends App {
    println(Topic.Practicabilities)
}