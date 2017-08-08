package cell.cluster

import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import com.actors.{BasicSubscriber, ClusterMembersListener}
import ontologies._
import ontologies.messages.Location._
import ontologies.messages.MessageType._
import ontologies.messages.{AriadneMessage, Location, MessageDirection}

/**
  * An actor that models a Cell receiver for the Cells-MasterServer
  * Publish-Subscribe interaction model
  *
  * Created by Matteo Gabellini on 29/06/2017.
  */
class CellSubscriber extends BasicSubscriber {

    override val topics = Set(Topic.HandShakes, Topic.Alarms, Topic.Topologies, Topic.Practicabilities)
    
    private val cell2Server: MessageDirection = Location.Master << Location.Cell
    private val server2Cell: MessageDirection = Location.Master >> Location.Cell

    override protected def init(args: List[Any]) = {
        super.init(args)
        if (args.head != ClusterMembersListener.greetings) throw new Exception()
        log.info("Hello there from {}!", name)
    }

    override protected def subscribed = {
        case SubscribeAck(Subscribe(topic, None, this.self)) =>
            log.info("{} Successfully Subscribed to {}", name, topic)
        case msg@AriadneMessage(Handshake, Handshake.Subtype.Acknowledgement, _, cnt) =>
            log.info("Got ack {} from {} of Type {}", cnt, sender.path.name, msg.supertype)
        case msg@AriadneMessage(Topology, Topology.Subtype.ViewedFromACell, _, cnt) =>
            log.info("I received the topology: {} from {} of Type {}", cnt, sender.path.name, msg.supertype)

            this.parent ! msg

            context.become(behavior = cultured, discardOld = true)
            log.info("I've Become Cultured...")

            log.info("Unstashing the other messages that I could not manage")
            unstashAll

        case _ => stash
    }

    private def cultured: Receive = {
        case msg@AriadneMessage(Alarm, _, _, cnt) =>
            log.info("Got {} from {} of Type {}", cnt, sender.path.name, msg.supertype)
            this.parent ! msg
        case msg@AriadneMessage(Update, Update.Subtype.Practicability, _, cnt) =>
            log.info("Got {} from {} of Type {}", cnt, sender.path.name, msg.supertype)
            this.parent ! msg
        case msg@AriadneMessage(Route, _, _, cnt) =>
            log.info("Got {} from {} of Type {}", cnt, sender.path.name, msg.supertype)
        case msg@AriadneMessage(Handshake, Handshake.Subtype.Acknowledgement, _, cnt) =>
            log.info("Got ack {} from {} of Type {}", cnt, sender.path.name, msg.supertype)
        case _ => desist _
    }
}

