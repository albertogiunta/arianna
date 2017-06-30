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

        case AriadneMessage(MessageType.Init, _) =>
            log.info("Hello there from {}!", self.path.name)

            this.context.become(receptive)
            log.info("[" + self.path.name + "] I've become receptive!")

        case msg => log.info("Unhandled message while initializing... {}", msg)
    }

    private val receptive: Actor.Receive = {
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