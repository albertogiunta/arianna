package cell.cluster

import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import common.BasicSubscriber
import ontologies._

/**
  * An actor that models a Cell receiver for the Cells-MasterServer
  * Publish-Subscribe interaction model
  *
  * Created by Matteo Gabellini on 29/06/2017.
  */
class CellSubscriber extends BasicSubscriber {
    
    override val topics = Set(Topic.Alarm, Topic.Topology)
    
    override protected def init(args: Any): Unit = {
        log.info("Hello there from {}!", name)
    }
    
    override protected def receptive = {
        case SubscribeAck(Subscribe(topic, None, `self`)) =>
            log.info("{} Successfully Subscribed to {}", name, topic)
        case msg@AriadneMessage(MessageType.Alarm, cnt) =>
            log.info("Got {} from {} of Type {}", cnt, sender.path.name, msg.messageType)
        case msg@AriadneMessage(MessageType.Topology, cnt) =>
            log.info("Got {} from {} of Type {}", cnt, sender.path.name, msg.messageType)
        case _ => desist _
    }
}

