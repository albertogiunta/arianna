package ontologies

import ontologies.messages.MessageType

/**
  * Created by Matteo Gabellini on 28/06/2017.
  */
trait Topic {
    
    def associatedMessageType: MessageType
    
    val topicName: String
    
    override def toString: String = topicName

    override def equals(obj: scala.Any) = obj match {
        case o: Topic => o.toString == this.toString
        case o: String => o.toString == this.toString
        case _ => false
    }
}

case class AriadneTopic(override val topicName: String) extends Topic {
    override def associatedMessageType = MessageType.Factory(toString)
}

object Topic {
    
    val Alarm = AriadneTopic(MessageType.Alarm)
    
    val Topology = AriadneTopic(MessageType.Topology)
    
    val HandShake = AriadneTopic(MessageType.Handshake)
    
    val Route = AriadneTopic(MessageType.Route)
    
    val Update = AriadneTopic(MessageType.Update)

    implicit def Topic2String(topic: Topic): String = topic.toString

    implicit def String2Topic(string: String): Topic = AriadneTopic(string)
    
    implicit def Topic2MessageType(topic: Topic): MessageType = topic.associatedMessageType
}

object TestTopic extends App {

}