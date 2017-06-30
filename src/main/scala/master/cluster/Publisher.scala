package master.cluster

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator._
import ontologies._

/**
  * Created by Alessandro on 28/06/2017.
  */
class Publisher extends Actor with ActorLogging {
    
    // activate the extension
    private val mediator: ActorRef = DistributedPubSub(context.system).mediator
    
    override def preStart() = {
        mediator ! Put(self) // Point 2 Point Messaging with other Actors of the cluster
    }
    
    def receive = {
    
        case AriadneMessage(MessageType.Init, cnt) =>
            log.info("Hello there from {}!", self.path.name)
    
            this.context.become(receptive)
            log.info("I've become receptive!")
        
            mediator ! Publish(Topic.Alarm, AriadneMessage(MessageType.Alarm, cnt))
            //
            //            log.info(s"Message sent to Mediator for Publishing...")
            //            // Point 2 Point communication using Akka Remoting service -- Orrible to see but practical
            //            this.context.actorSelection("akka.tcp://Arianna-Cluster@127.0.0.1:25520/user/Subscriber-Master") ! "Ciao"
    
            //             // The Mediator Hierarchy is always /user/<Username>
            //                    mediator ! Send(path = "/user/Subscriber-Master",
            //                        msg = MyMessage(ontologies.Alarm, cnt + "2" ), localAffinity = true)
    
            log.info(s"Message sent to Mediator for Point2Point relay...")
            
        case _ => // Ignore
    }
    
    private val receptive: Actor.Receive = {
        case msg@AriadneMessage(MessageType.Alarm, _) =>
            mediator ! Publish(Topic.Alarm, msg)
    
        case msg@AriadneMessage(MessageType.Topology, _) =>
            mediator ! Publish(Topic.Topology, msg)
        case _: Any => // Ignore
    }
}
