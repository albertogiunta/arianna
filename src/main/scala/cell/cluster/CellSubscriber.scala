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
        // subscribe to the topic named "content"
        mediator ! Subscribe(Topic.Alarm, self)
    
        mediator ! Subscribe(Topic.Topology, self)
        
        mediator ! Put(self)
    }
    
    def receive: PartialFunction[Any, Unit] = {
        case msg@AriadneMessage(MessageType.Alarm, _) =>
            println("[" + self.path.name + "]  I received an Alarm signal")
        case msg@AriadneMessage(MessageType.Topology, _) =>
            print("[" + self.path.name + "] I received a topology")
        case SubscribeAck(Subscribe(topic, None, `self`)) =>
            println("[" + self.path.name + "] Subscribing to " + topic + " topic")
    }
}

