package master.cluster

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Put, Subscribe, SubscribeAck}
import ontologies.MyMessage

/**
  * A Simple Subscriber for a Clustered Publish-Subscribe Model
  *
  * Created by Alessandro on 28/06/2017.
  */
class Subscriber extends Actor with ActorLogging {
    
    val mediator: ActorRef = DistributedPubSub(context.system).mediator
    
    override def receive = {
        case MyMessage(ontologies.Init, _) =>
            log.info("Hello!")
            // Accept Alarms from other Actors, then broadcast to every other Actor.
            mediator ! Subscribe(ontologies.Alarm.typeName, self)
            // Accept Data from Cells sensors, those data are useful for computing Practicability of those Cells.
            mediator ! Subscribe(ontologies.SensorData.typeName, self)
            // Accept Handshakes from other Actors (Cells) and save Map them into the actual Topology,
            // broadcast the new topology to the other inhabitant of the cluster.
            mediator ! Subscribe(ontologies.Handshake.typeName, self)
            // Accept Cells' Data to update the map.
            mediator ! Subscribe(ontologies.CellData.typeName, self)
            
            mediator ! Put(self) // Point 2 Point Messaging with other Actors of the cluster
            
            this.context.become(receptive)
            
            log.info("Become receptive!")
            
        case SubscribeAck(Subscribe(topic, None, `self`)) =>
            log.info("Subscribing to " + topic)
        
        case _ => // Ignore
    }
    
    val receptive: Actor.Receive = {
        case SubscribeAck(Subscribe(topic, None, `self`)) =>
            log.info("Subscribing to " + topic)
        
        //        case s: String =>
        //            log.info("Got {}", s)
        //
        //        case in: String =>
        //            val out = in.toUpperCase
        //            mediator ! Send(path = "/user/destination", msg = out, localAffinity = true)
        
        case _: Any => // Ignore
    }
}