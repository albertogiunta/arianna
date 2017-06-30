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
        mediator ! Subscribe(AlarmTopic.topicName, self)
        // Accept Data from Cells sensors, those data are useful for computing Practicability of those Cells.
        mediator ! Subscribe(SensorUpdateTopic.topicName, self)
        // Accept Handshakes from other Actors (Cells) and save Map them into the actual Topology,
        // broadcast the new topology to the other inhabitant of the cluster.
        mediator ! Subscribe(HandShakeTopic.topicName, self)
        // Accept Cells' Data to update the map.
        mediator ! Subscribe(CellDataTopic.topicName, self)
    
        mediator ! Put(self) // Point 2 Point Messaging with other Actors of the cluster
    }
    
    def receive = {
    
        case SubscribeAck(Subscribe(topic, None, `self`)) =>
            log.info("Successfully Subscribed to " + topic)

        case AriadneMessage(ontologies.Init, _) =>
            log.info("Hello there from {}!", self.path.name)
    
            this.context.become(receptive)
            log.info("I've become receptive!")
        
        case msg => log.info("Unhandled message while initializing... {}", msg) // Ignore
    }
    
    private val receptive: Actor.Receive = {

        case AriadneMessage(ontologies.Alarm, cnt) =>
            println("[MAIN SERVER] Got {}", cnt)
    
        case AriadneMessage(ontologies.SensorData, cnt) =>
            println("[MAIN SERVER] Got {}", cnt)
    
        case AriadneMessage(ontologies.Handshake, cnt) =>
            println("[MAIN SERVER] Got {}", cnt)
    
        case AriadneMessage(ontologies.CellData, cnt) =>
            println("[MAIN SERVER] Got {}", cnt)
    
        case AriadneMessage(ontologies.Practicability, cnt) =>
            println("[MAIN SERVER] Got {}", cnt)
    
        case SubscribeAck(Subscribe(topic, None, `self`)) =>
            println("[MAIN SERVER] Successfully Subscribed to " + topic)
            
        case msg => log.info("Unhandled message while receptive... {}", msg) // Ignore
    }
}