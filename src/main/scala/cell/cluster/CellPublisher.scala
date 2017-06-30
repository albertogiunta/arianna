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
        
        case msg@AriadneMessage(Handshake, _) =>
            mediator ! Publish(HandShakeTopic.topicName, msg)
        case msg@AriadneMessage(SensorData, _) =>
            mediator ! Publish(SensorUpdateTopic.topicName, msg)
        case msg@AriadneMessage(Handshake, _) =>
            mediator ! Publish(HandShakeTopic.topicName, msg)
        case msg@AriadneMessage(Practicability, _) =>
            mediator ! Publish(PracticabilityTopic.topicName, msg)
        case msg@AriadneMessage(CellData, _) =>
            mediator ! Publish(CellDataTopic.topicName, msg)
        case _ => // Ignore
    }
}