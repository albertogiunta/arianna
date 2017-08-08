package cell.cluster

import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import com.actors.{BasicPublisher, ClusterMembersListener}
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
        super.init(args)
        if (args.head != ClusterMembersListener.greetings) throw new Exception()
        log.info("Hello there from {}!", name)

        //Ask to the core actor the cell information in order to continue the handshake task
        //println("aaa 0")
        parent ! AriadneMessage(
            Info,
            Info.Subtype.Request,
            self2Self,
            SensorsInfoUpdate(CellInfo.empty, List.empty[SensorInfo])
        )

    }

    override protected def receptive = {
        case msg@AriadneMessage(Info, Info.Subtype.Response, this.self2Self, sensorsInfoUpdate: SensorsInfoUpdate) => {
            println("aaa 3")
            //Thread.sleep(5000)
            mediator ! Publish(Topic.HandShakes,
                AriadneMessage(
                    Handshake,
                    Handshake.Subtype.CellToMaster,
                    Location.Cell >> Location.Master,
                    sensorsInfoUpdate
                )
            )
            log.info("Sending Handshake to Master...")
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