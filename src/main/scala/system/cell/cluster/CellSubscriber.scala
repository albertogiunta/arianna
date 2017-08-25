package system.cell.cluster

import akka.actor.ActorRef
import com.actors.{ClusterMembersListener, TemplateSubscriber}
import system.exceptions.IncorrectInitMessageException
import system.names.NamingSystem
import system.ontologies._
import system.ontologies.messages.AriadneMessage
import system.ontologies.messages.MessageType._

/**
  * An actor that models a Cell receiver for the Cells-MasterServer
  * Publish-Subscribe interaction model
  *
  * Created by Matteo Gabellini on 29/06/2017.
  */
class CellSubscriber(mediator: ActorRef) extends TemplateSubscriber(mediator) {

    override protected val topics = Set(
        Topic.Alarms,
        Topic.Topologies,
        Topic.Practicabilities,
        Topic.ShutDown
    )
    
    override protected def init(args: List[String]): Unit = {
        super.init(args)
        if (args.head != ClusterMembersListener.greetings) throw IncorrectInitMessageException(this.name, args)
        log.info("Hello there from {}!", name)
    }

    override protected def subscribed = this.proactive orElse {
        case msg@AriadneMessage(Topology, Topology.Subtype.ViewedFromACell, _, cnt) =>
            log.debug("I received the topology: {} from {} of Type {}", cnt, sender.path.name, msg.supertype)
    
            this.parent ! msg
    
            context.become(behavior = cultured, discardOld = true)
            log.debug("I've Become Cultured...")

            log.debug("Unstashing the other messages that I could not manage")
            unstashAll

        case _ => stash
    }

    private def cultured: Receive = this.proactive orElse {
        case msg@AriadneMessage(Topology, Topology.Subtype.ViewedFromACell, _, cnt) =>
            log.info("Got an unexpected topology...")
            this.parent ! msg
        case msg@AriadneMessage(Alarm, Alarm.Subtype.End, _, _) =>
            this.parent ! msg
            log.debug("Got {} from {} of Type {}", msg.subtype, sender.path.name, msg.supertype)

        case msg@AriadneMessage(Alarm, _, _, cnt) =>
            log.info("Alarm triggered from ", sender.path.name)
            this.parent ! msg

        case msg@AriadneMessage(Update, Update.Subtype.Practicability, _, cnt) =>
            log.info("Got {} from {} of Type {}", cnt, sender.path.name, msg.supertype)
            this.parent ! msg
        case _ => desist _
    }


    private def proactive: Receive = ({
        case msg@AriadneMessage(Init, Init.Subtype.Goodbyes, _, _) =>
            this.parent ! msg
        case msg@AriadneMessage(Handshake, Handshake.Subtype.Acknowledgement, _, cnt) =>
            log.info("Got handshake ack from {}", sender.path.name)
            sibling(NamingSystem.Publisher).get ! msg
    }: Receive)
}

