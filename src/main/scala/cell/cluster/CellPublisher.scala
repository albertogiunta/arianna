package cell.cluster

import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import common.BasicPublisher
import ontologies._

/**
  * Actor that manages the sending of messages to the main server
  * Created by Matteo Gabellini on 29/06/2017.
  */
class CellPublisher extends BasicPublisher {

    override protected def init(args: Any): Unit = {
        log.info("Hello there from {}!", name)
    }

    override protected def receptive = {
        case msg@AriadneMessage(MessageType.Handshake, _) =>
            mediator ! Publish(Topic.HandShake, msg)

        case msg@AriadneMessage(MessageType.SensorData, _) =>
            mediator ! Publish(Topic.SensorUpdate, msg)

        case msg@AriadneMessage(MessageType.Practicability, _) =>
            mediator ! Publish(Topic.Practicability, msg)

        case msg@AriadneMessage(MessageType.CellData, _) =>
            mediator ! Publish(Topic.CellData, msg)
        case _ => // Ignore
    }
}