package system.cell.cluster

import akka.actor.ActorRef
import com.actors.{ClusterMembersListener, TemplateSubscriber}
import system.exceptions.IncorrectInitMessageException
import system.names.NamingSystem
import system.ontologies._
import system.ontologies.messages.Location._
import system.ontologies.messages.MessageType._
import system.ontologies.messages.{AriadneMessage, Location, MessageDirection}

/**
  * An actor that models a Cell receiver for the Cells-MasterServer
  * Publish-Subscribe interaction model
  *
  * Created by Matteo Gabellini on 29/06/2017.
  */
class CellSubscriber(mediator: ActorRef) extends TemplateSubscriber(mediator) {

    override protected val topics = Set(
        //        Topic.HandShakes,
        Topic.Alarms,
        Topic.Topologies,
        Topic.Practicabilities,
        Topic.ShutDown
    )
    
    private val cell2Server: MessageDirection = Location.Master << Location.Cell
    private val server2Cell: MessageDirection = Location.Master >> Location.Cell
    
    override protected def init(args: List[String]): Unit = {
        super.init(args)
        if (args.head != ClusterMembersListener.greetings) throw IncorrectInitMessageException(this.name, args)
        log.info("Hello there from {}!", name)
    }

    override protected def subscribed = this.proactive orElse {
        case msg@AriadneMessage(Topology, Topology.Subtype.ViewedFromACell, _, cnt) =>
            log.info("I received the topology: {} from {} of Type {}", cnt, sender.path.name, msg.supertype)
    
            this.parent ! msg
    
            context.become(behavior = cultured, discardOld = true)
            log.info("I've Become Cultured...")

            log.info("Unstashing the other messages that I could not manage")
            unstashAll

        case _ => stash
    }

    private def cultured: Receive = this.proactive orElse {
        case msg@AriadneMessage(Alarm, Alarm.Subtype.End, _, _) =>
            this.parent ! msg
            log.info("Got {} from {} of Type {}", msg.subtype, sender.path.name, msg.supertype)

        case msg@AriadneMessage(Alarm, _, _, cnt) =>
            log.info("Got {} from {} of Type {}", cnt, sender.path.name, msg.supertype)
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
            log.info("Got ack {} from {} of Type {}", cnt, sender.path.name, msg.supertype)
            sibling(NamingSystem.Publisher).get ! msg
    }: Receive)
}

