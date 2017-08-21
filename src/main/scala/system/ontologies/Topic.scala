package system.ontologies

import system.ontologies.messages.{MessageContent, MessageSubtype, MessageType}

/**
  * Created by Matteo Gabellini on 28/06/2017.
  */
trait Topic[C] {

    def associatedMessageType: MessageType
    
    def associatedMessageSubtype: Option[MessageSubtype[C]]
    
    val topic: String

    val subTopic: Option[String]
    
    override def toString: String = {
        topic + "/" + subTopic.getOrElse("All")
    }
    
    override def equals(obj: scala.Any): Boolean = obj match {
        case that: Topic[_] => that.toString == this.toString
        case _ => false
    }
}

case class AriadneTopic(override val topic: String, override val subTopic: Option[String]) extends Topic[MessageContent] {
    override def associatedMessageType = MessageType.StaticFactory(topic)
    
    override def associatedMessageSubtype = Option(MessageSubtype.StaticFactory(subTopic.orNull))
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
    
    implicit def Topic2String(topic: Topic[_]): String = topic.toString
    
    implicit def Topic2MessageType(topic: Topic[_]): MessageType = topic.associatedMessageType
    
    implicit def Topic2MessageSubtype(topic: Topic[_]): MessageSubtype[_] = topic.associatedMessageSubtype.orNull
}