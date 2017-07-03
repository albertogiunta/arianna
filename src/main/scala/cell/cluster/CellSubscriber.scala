package cell.cluster

import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import common.BasicSubscriber
import ontologies.MessageType._
import ontologies._

/**
  * An actor that models a Cell receiver for the Cells-MasterServer
  * Publish-Subscribe interaction model
  *
  * Created by Matteo Gabellini on 29/06/2017.
  */
class CellSubscriber extends BasicSubscriber {

    override val topics = Set(Topic.Alarm, Topic.Topology)
    
    override protected def init(args: List[Any]) = {
        log.info("Hello there from {}!", name)
    }

    override protected def receptive = {
        case SubscribeAck(Subscribe(topic, None, `self`)) =>
            log.info("{} Successfully Subscribed to {}", name, topic)
        case msg@AriadneRemoteMessage(Alarm, Alarm.Subtype.Basic, cnt) =>
            log.info("Got {} from {} of Type {}", cnt, sender.path.name, msg.supertype)
        case msg@AriadneRemoteMessage(Topology, Topology.Subtype.RealTopology, cnt) =>
            log.info("Got {} from {} of Type {}", cnt, sender.path.name, msg.supertype)
        case _ => desist _
    }
}

