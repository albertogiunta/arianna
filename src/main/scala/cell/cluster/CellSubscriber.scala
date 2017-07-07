package cell.cluster

import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import common.BasicSubscriber
import ontologies._
import ontologies.messages.Location._
import ontologies.messages.MessageType._
import ontologies.messages.{AriadneRemoteMessage, Location, MessageDirection}

/**
  * An actor that models a Cell receiver for the Cells-MasterServer
  * Publish-Subscribe interaction model
  *
  * Created by Matteo Gabellini on 29/06/2017.
  */
class CellSubscriber extends BasicSubscriber {

    override val topics = Set(Topic.Alarm, Topic.Topology)

    private val cell2Server: MessageDirection = Location.Server << Location.Cell
    private val server2Cell: MessageDirection = Location.Server >> Location.Cell

    override protected def init(args: List[Any]) = {
        log.info("Hello there from {}!", name)
    }

    override protected def receptive = {
        case SubscribeAck(Subscribe(topic, None, `self`)) =>
            log.info("{} Successfully Subscribed to {}", name, topic)
        case msg@AriadneRemoteMessage(Topology, Topology.Subtype.Topology4Cell, _, cnt) =>
            log.info("I received the topology: {} from {} of Type {}", cnt, sender.path.name, msg.supertype)

            context.become(behavior = cultured, discardOld = true)
            log.info("I've Become Cultured...")

            log.info("Unstashing the other messages that I could not manage")
            unstashAll

        case _ => stash
    }

    private def cultured: Receive = {
        case msg@AriadneRemoteMessage(Alarm, Alarm.Subtype.Basic, _, cnt) =>
            log.info("Got {} from {} of Type {}", cnt, sender.path.name, msg.supertype)
        case msg@AriadneRemoteMessage(Update, Update.Subtype.Practicability, _, cnt) =>
            log.info("Got {} from {} of Type {}", cnt, sender.path.name, msg.supertype)
        case msg@AriadneRemoteMessage(Route, Route.Subtype.Basic, _, cnt) =>
            log.info("Got {} from {} of Type {}", cnt, sender.path.name, msg.supertype)
        case msg@AriadneRemoteMessage(Handshake, Handshake.Subtype.Basic, _, cnt) =>
            log.info("Got {} from {} of Type {}", cnt, sender.path.name, msg.supertype)
        case _ => desist _
    }
}

