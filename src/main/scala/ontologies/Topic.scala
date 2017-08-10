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
        topic + "/" + subTopic.getOrElse("All")
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
    
    val ShutDown = AriadneTopic(MessageType.Init, Option(MessageType.Init.Subtype.Goodbyes))
    
    val Alarms = AriadneTopic(MessageType.Alarm, Option.empty)

    val Topologies = AriadneTopic(MessageType.Topology, Option.empty)

    val HandShakes = AriadneTopic(MessageType.Handshake, Option.empty)

    val Routes = AriadneTopic(MessageType.Route, Option.empty)

    val Updates = AriadneTopic(MessageType.Update, Option.empty)
    
    val Practicabilities = AriadneTopic(MessageType.Update, Option(MessageType.Update.Subtype.Practicability))
    
    val TopologyACK = AriadneTopic(MessageType.Topology, Option(MessageType.Topology.Subtype.Acknowledgement))
    
    implicit def Topic2String(topic: Topic): String = topic.toString
    
    implicit def Topic2MessageType(topic: Topic): MessageType = topic.associatedMessageType

    implicit def Topic2MessageSubtype(topic: Topic): MessageSubtype = topic.associatedMessageSubtype.orNull
}