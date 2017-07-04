package cell.cluster

import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import common.BasicPublisher
import ontologies._
import ontologies.messages.MessageType._
import ontologies.messages.{AriadneRemoteMessage, MessageType}

/**
  * Actor that manages the sending of messages to the main server
  * Created by Matteo Gabellini on 29/06/2017.
  */
class CellPublisher extends BasicPublisher {
    
    override protected def init(args: List[Any]) = {
        log.info("Hello there from {}!", name)
    }

    override protected def receptive = {
        case msg@AriadneRemoteMessage(Handshake, Handshake.Subtype.Basic, _, _) =>
            mediator ! Publish(Topic.HandShake, msg)

        case msg@AriadneRemoteMessage(Update, Update.Subtype.Sensors, _, _) =>
            mediator ! Publish(Topic.Update, msg)

        case msg@AriadneRemoteMessage(Update, Update.Subtype.Practicability, _, _) =>
            mediator ! Publish(Topic.Update, msg)

        case msg@AriadneRemoteMessage(MessageType.Update, Update.Subtype.CellOccupation, _, _) =>
            mediator ! Publish(Topic.Update, msg)
        case _ => // Ignore
    }
}