package cell.cluster

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import ontologies._

/**
  * Actor that manages the sending of messages to the main server
  * Created by Matteo Gabellini on 29/06/2017.
  */
class CellPublisher extends Actor with ActorLogging {
    
    private val mediator: ActorRef = DistributedPubSub(context.system).mediator
    
    
    def receive = {
        case msg@AriadneMessage(MessageType.Handshake, _) =>
            mediator ! Publish(Topic.HandShake, msg)
    
        case msg@AriadneMessage(MessageType.SensorData, _) =>
            mediator ! Publish(Topic.SensorUpdate, msg)
    
        case msg@AriadneMessage(MessageType.Handshake, _) =>
            mediator ! Publish(Topic.HandShake, msg)
    
        case msg@AriadneMessage(MessageType.Practicability, _) =>
            mediator ! Publish(Topic.Practicability, msg)
    
        case msg@AriadneMessage(MessageType.CellData, _) =>
            mediator ! Publish(Topic.CellData, msg)
        case _ => // Ignore
    }
}