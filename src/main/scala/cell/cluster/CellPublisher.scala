package cell.cluster

import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import common.BasicPublisher
import ontologies.MessageType._
import ontologies._

/**
  * Actor that manages the sending of messages to the main server
  * Created by Matteo Gabellini on 29/06/2017.
  */
class CellPublisher extends BasicPublisher {
    
    override protected def init(args: List[Any]) = {
        log.info("Hello there from {}!", name)
    }

    override protected def receptive = {
        case msg@AriadneRemoteMessage(Handshake, Handshake.Subtype.Basic, _) =>
            mediator ! Publish(Topic.HandShake, msg)
    
        case msg@AriadneRemoteMessage(Update, Update.Subtype.Sensors, _) =>
            mediator ! Publish(Topic.Update, msg)
    
        case msg@AriadneRemoteMessage(Update, Update.Subtype.Practicability, _) =>
            mediator ! Publish(Topic.Update, msg)
    
        case msg@AriadneRemoteMessage(MessageType.Update, Update.Subtype.CellOccupation, _) =>
            mediator ! Publish(Topic.Update, msg)
        case _ => // Ignore
    }
}