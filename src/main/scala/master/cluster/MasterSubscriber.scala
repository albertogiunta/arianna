package master.cluster

import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import common.BasicSubscriber
import ontologies._
import ontologies.messages.AriadneRemoteMessage
import ontologies.messages.MessageType._

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
            Topic.Update, Topic.Topology)
    
    override protected def init(args: List[Any]) =
        log.info("Hello there from {}!", name)

    override protected def receptive = {
        case SubscribeAck(Subscribe(topic, None, this.self)) =>
            log.info("{} Successfully Subscribed to {}", name, topic)

        case msg@AriadneRemoteMessage(Alarm, _, _, _: String) =>
            println("Got {} from {}", msg.toString, sender.path.name)

        case msg@AriadneRemoteMessage(Update, _, _, _) =>
            println("Got {} from {}", msg.toString, sender.path.name)

        case msg@AriadneRemoteMessage(Handshake, _, _, _) =>
            println("Got {} from {}", msg.toString, sender.path.name)

        case msg@AriadneRemoteMessage(Topology, _, _, _) =>
            println("Got {} from {}", msg.toString, sender.path.name)

        case _ => desist _
    }
}