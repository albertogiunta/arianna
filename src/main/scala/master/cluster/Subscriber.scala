package master.cluster

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator._
import ontologies._

/**
  * A Simple Subscriber for a Clustered Publish-Subscribe Model
  *
  * Created by Alessandro on 28/06/2017.
  */
class Subscriber extends Actor with ActorLogging {
    
    private val mediator: ActorRef = DistributedPubSub(context.system).mediator
    
    override def preStart() = {
        // Accept Alarms from other Actors, then broadcast to every other Actor.
        mediator ! Subscribe(Topic.Alarm, self)
        // Accept Data from Cells sensors, those data are useful for computing Practicability of those Cells.
        mediator ! Subscribe(Topic.SensorUpdate, self)
        // Accept Handshakes from other Actors (Cells) and save Map them into the actual Topology,
        // broadcast the new topology to the other inhabitant of the cluster.
        mediator ! Subscribe(Topic.HandShake, self)
        // Accept Cells' Data to update the map.
        mediator ! Subscribe(Topic.CellData, self)

        mediator ! Subscribe(Topic.Practicability, self)
    
        mediator ! Put(self) // Point 2 Point Messaging with other Actors of the cluster
    }
    
    def receive = {
    
        case SubscribeAck(Subscribe(topic, None, `self`)) =>
            log.info("Successfully Subscribed to " + topic)

        case AriadneMessage(MessageType.Init, _) =>
            log.info("Hello there from {}!", self.path.name)
    
            this.context.become(receptive)
            log.info("I've become receptive!")
        
        case msg => log.info("Unhandled message while initializing... {}", msg) // Ignore
    }
    
    private val receptive: Actor.Receive = {
    
        case AriadneMessage(MessageType.Alarm, cnt) =>
            log.info("Got {} from {}", cnt, sender.path.name)
    
        case AriadneMessage(MessageType.SensorData, cnt) =>
            log.info("Got {} from {}", cnt, sender.path.name)
    
        case AriadneMessage(MessageType.Handshake, cnt) =>
            log.info("Got {} from {}", cnt, sender.path.name)
    
        case AriadneMessage(MessageType.CellData, cnt) =>
            log.info("Got {} from {}", cnt, sender.path.name)
    
        case AriadneMessage(MessageType.Practicability, cnt) =>
            log.info("Got {} from {}", cnt, sender.path.name)
    
        case SubscribeAck(Subscribe(topic, None, `self`)) =>
            log.info("Successfully Subscribed to " + topic)
            
        case msg => log.info("Unhandled message while receptive... {}", msg) // Ignore
    }
}