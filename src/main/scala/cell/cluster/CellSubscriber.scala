package cell.cluster

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.pubsub.DistributedPubSubMediator.Put
import akka.cluster.pubsub._
import ontologies._

/**
  * An actor that models a Cell receiver for the Cells-MasterServer
  * Publish-Subscribe interaction model
  *
  * Created by Matteo Gabellini on 29/06/2017.
  */
class CellSubscriber extends Actor with ActorLogging {
    
    import DistributedPubSubMediator.{Subscribe, SubscribeAck}
    
    val mediator: ActorRef = DistributedPubSub(context.system).mediator
    
    override def preStart(): Unit = {

        mediator ! Subscribe(Topic.Alarm, self)
    
        mediator ! Subscribe(Topic.Topology, self)
        
        mediator ! Put(self)
    }


    def receive = {
        case SubscribeAck(Subscribe(topic, None, `self`)) =>
            log.info("Successfully Subscribed to " + topic)
        case AriadneMessage(MessageType.Init, _) => {
            log.info("Hello there from {}!", self.path.name)

            this.context.become(receptive)
            log.info("[" + self.path.name + "] I've become receptive!")
        }
        case msg => log.info("Unhandled message while initializing... {}", msg)
    }


    private val receptive: Actor.Receive = {
        case msg@AriadneMessage(MessageType.Alarm, _) =>
            println("[" + self.path.name + "]  I received an Alarm signal")
        case msg@AriadneMessage(MessageType.Topology, _) =>
            print("[" + self.path.name + "] I received a topology")
    }
}

