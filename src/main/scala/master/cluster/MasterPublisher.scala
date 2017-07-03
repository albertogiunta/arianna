package master.cluster

import akka.cluster.pubsub.DistributedPubSubMediator._
import common.BasicPublisher
import ontologies._

/**
  * Created by Alessandro on 28/06/2017.
  */
class MasterPublisher extends BasicPublisher {
    
    override protected def init(args: Any): Unit = {
        log.info("Hello there from {}!", name)
    
        Thread.sleep(500)
    
        mediator ! Publish(Topic.HandShake, AriadneMessage(MessageType.Handshake, args.toString))
    
        log.info(s"Message {} sent to Mediator for Publishing...", args.toString)
        
        //        // Point 2 Point communication using Akka Remoting service -- Orrible to see but practical
        //        this.context.actorSelection("akka.tcp://Arianna-Cluster@127.0.0.1:25520/user/Subscriber-Master") ! "Ciao"
        //
        //        // The Mediator Hierarchy is always /user/<Username>
        //        mediator ! Send(path = "/user/Subscriber-Master",
        //            msg = AriadneMessage(MessageType.Alarm, args.toString + "2"), localAffinity = true)
        //
        //        log.info(s"Message sent to Mediator for Point2Point relay...")
    }
    
    override protected val receptive = {
        
        case msg@AriadneMessage(MessageType.Alarm, _) =>
            mediator ! Publish(Topic.Alarm, msg)
        
        case msg@AriadneMessage(MessageType.Topology, _) =>
            mediator ! Publish(Topic.Topology, msg)
        case _ => desist _
    }
}
