package master.cluster

import akka.cluster.pubsub.DistributedPubSubMediator._
import common.BasicPublisher
import ontologies.Topic
import ontologies.messages.Location._
import ontologies.messages.MessageType.Alarm.Subtype.Basic
import ontologies.messages.MessageType.Topology.Subtype.Topology4Cell
import ontologies.messages.MessageType.Update.Subtype.Practicability
import ontologies.messages.MessageType._
import ontologies.messages.{AreaForCell, AriadneLocalMessage, AriadneRemoteMessage}

/**
  * Created by Alessandro on 28/06/2017.
  */
class MasterPublisher extends BasicPublisher {
    
    override protected def init(args: List[Any]) = {
        log.info("Hello there from {}!", name)
    
        TestBehaviour.test(this, args)
    }

    override protected val receptive = {
    
        case AriadneLocalMessage(Alarm, _, _, _) =>
        
            mediator ! Publish(
                Topic.Update,
                AriadneRemoteMessage(
                    Topic.Update,
                    Practicability,
                    Server >> Cell,
                    "Something"
                )
            )
    
        case msg@AriadneLocalMessage(Topology, Topology4Cell, _, cnt: AreaForCell) =>
            mediator ! Publish(
                Topic.Topology, // Forse sarebbe utile legare anche il Tipo di Messaggio al Topic
                AriadneRemoteMessage(
                    msg.supertype,
                    msg.subtype,
                    msg.direction,
                    Topology4Cell.marshal(cnt)
                )
            )
        case _ => desist _
    }
}


object TestBehaviour {
    def test(actor: MasterPublisher, args: List[Any]): Unit = {
        (1 to 1000).foreach(x => print(x + "=>"))
        println()
        
        actor.mediator ! Publish(Topic.HandShake,
            AriadneRemoteMessage(Handshake, Handshake.Subtype.Basic, Server >> Self, args.toString))
        
        actor.mediator ! Publish(Topic.Alarm,
            AriadneRemoteMessage(Alarm, Basic, Cell >> Self, args.toString))
        
        println(s"Message {} sent to Mediator for Publishing...", args.toString)
        
        // Point 2 Point communication using Akka Remoting service -- Orrible to see but practical
        actor.context.actorSelection("akka.tcp://Arianna-Cluster@127.0.0.1:25520/user/Subscriber-Master") ! "Ciao"
        
        // The Mediator Hierarchy is always /user/<Username>
        actor.mediator ! Send(
            path = "/user/Subscriber-Master",
            msg = AriadneRemoteMessage(Alarm, Basic, Self >> Self, args.mkString),
            localAffinity = true
        )
        
        actor.log.info(s"Message sent to Mediator for Point2Point relay...")
    }
}
