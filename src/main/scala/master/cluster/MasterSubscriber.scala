package master.cluster

import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import common.BasicSubscriber
import ontologies._

/**
  * A Simple Subscriber for a Clustered Publish-Subscribe Model
  *
  * Created by Alessandro on 28/06/2017.
  */
class MasterSubscriber extends BasicSubscriber {
    
    override val topics: Set[Topic] =
        Set(Topic.Alarm, Topic.SensorUpdate, Topic.HandShake,
            Topic.CellData, Topic.Practicability)
    
    override protected def init(args: Any): Unit =
        log.info("Hello there from {}!", name)
    
    override protected def receptive = {
        case SubscribeAck(Subscribe(topic, None, `self`)) =>
            log.info("{} Successfully Subscribed to {}", name, topic)
        
        case msg@AriadneMessage(MessageType.Alarm, cnt) =>
            log.info("Got \"{}\" from {} of Type {}", cnt, sender.path.name, msg.messageType)
        
        case msg@AriadneMessage(MessageType.SensorData, cnt) =>
            log.info("Got \"{}\" from {} of Type {}", cnt, sender.path.name, msg.messageType)
        
        case msg@AriadneMessage(MessageType.Handshake, cnt) =>
            log.info("Got \"{}\" from {} of Type {}", cnt, sender.path.name, msg.messageType)
        
        case msg@AriadneMessage(MessageType.CellData, cnt) =>
            log.info("Got \"{}\" from {} of Type {}", cnt, sender.path.name, msg.messageType)
        
        case msg@AriadneMessage(MessageType.Practicability, cnt) =>
            log.info("Got \"{}\" from {} of Type {}", cnt, sender.path.name, msg.messageType)
        
        case _ => desist _
    }
}