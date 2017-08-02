package cell.cluster

import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import com.actors.BasicPublisher
import ontologies._
import ontologies.messages.Location._
import ontologies.messages.MessageType._
import ontologies.messages._

/**
  * Actor that manages the sending of messages to the main server
  * Created by Matteo Gabellini on 29/06/2017.
  */
class CellPublisher extends BasicPublisher {

    private val self2Self: MessageDirection = Location.Self >> Location.Self


    override protected def init(args: List[Any]) = {
        if (args(0) != "FROM CLUSTER MEMBERS LISTERNER Hello there, it's time to dress-up") throw new Exception()
        log.info("Hello there from {}!", name)

        parent ! AriadneMessage(
            Handshake,
            Handshake.Subtype.Acknowledgement,
            self2Self,
            SensorsInfoUpdate(InfoCell.empty, List.empty[SensorInfo])
        )

    }

    override protected def receptive = {
        case msg@AriadneMessage(Handshake, Handshake.Subtype.Acknowledgement, this.self2Self, sensorsInfoUpdate: SensorsInfoUpdate) => {
            log.info("Sending Handshake to Master...")
            mediator ! Publish(Topic.HandShakes,
                AriadneMessage(
                    Handshake,
                    Handshake.Subtype.CellToMaster,
                    Location.Cell >> Location.Master,
                    sensorsInfoUpdate
                )
            )
            this.context.become(cultured, discardOld = true)
        }
        case _ => //ignore
    }


    private def cultured: Receive = {
        case msg@AriadneMessage(Alarm, _, _, _) =>
            mediator ! Publish(Topic.Alarms, msg)

        case msg@AriadneMessage(Update, Update.Subtype.Sensors, _, _) =>
            mediator ! Publish(Topic.Updates, msg)

        case msg@AriadneMessage(Update, Update.Subtype.Practicability, _, _) =>

            mediator ! Publish(Topic.Practicabilities, msg)

        case msg@AriadneMessage(Update, Update.Subtype.CurrentPeople, _, _) =>
            mediator ! Publish(Topic.Updates, msg)

        case _ => // Ignore
    }
}