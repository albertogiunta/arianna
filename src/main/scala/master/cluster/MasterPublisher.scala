package master.cluster

import akka.cluster.pubsub.DistributedPubSubMediator._
import common.BasicPublisher
import ontologies._
import ontologies.messages.AriadneRemoteMessage
import ontologies.messages.Location._
import ontologies.messages.MessageType._

/**
  * Created by Alessandro on 28/06/2017.
  */
class MasterPublisher extends BasicPublisher {
    
    override protected def init(args: List[Any]) = {
        log.info("Hello there from {}!", name)

        (1 to 1000).foreach(x => print(x + "=>"));
        println()
        
        mediator ! Publish(Topic.HandShake,
            AriadneRemoteMessage(Handshake, Handshake.Subtype.Basic, Server >> Self, args.toString))

        mediator ! Publish(Topic.Alarm,
            AriadneRemoteMessage(Alarm, Alarm.Subtype.Basic, Cell >> Self, args.toString))

        println(s"Message {} sent to Mediator for Publishing...", args.toString)

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

        case msg@AriadneRemoteMessage(Alarm, _, _, _) =>
            mediator ! Publish(Topic.Alarm, msg)

        case msg@AriadneRemoteMessage(Topology, _, _, _) =>
            mediator ! Publish(Topic.Topology, msg)
        case _ => desist _
    }
}
