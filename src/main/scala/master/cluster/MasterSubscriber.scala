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
    type Cell = Any

    private var topology: Map[String, Cell] = Map.empty

    override val topics: Set[Topic] =
        Set(Topic.Alarm, Topic.Update, Topic.HandShake,
            Topic.Update, Topic.Practicability)
    
    override protected def init(args: List[Any]) =
        log.info("Hello there from {}!", name)

    override protected def receptive = {
        case SubscribeAck(Subscribe(topic, None, this.self)) =>
            log.info("{} Successfully Subscribed to {}", name, topic)
    
        case msg@AriadneRemoteMessage(MessageType.Alarm, _, cnt: String) =>
            log.info("Got \"{}\" from {} of Type {}", cnt, sender.path.name, msg.supertype)
    
        case msg@AriadneRemoteMessage(MessageType.Update, _, cnt) =>
            log.info("Got \"{}\" from {} of Type {}", cnt, sender.path.name, msg.supertype)
    
        case msg@AriadneRemoteMessage(MessageType.Handshake, _, cnt) =>
            log.info("Got \"{}\" from {} of Type {}", cnt, sender.path.name, msg.supertype)

        case _ => desist _
    }
}