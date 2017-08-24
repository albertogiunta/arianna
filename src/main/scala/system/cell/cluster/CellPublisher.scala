package system.cell.cluster

import akka.actor.ActorRef
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import com.actors.{ClusterMembersListener, TemplatePublisher}
import com.utils.BasicWatchdog
import com.utils.Watchdog.WatchDogNotification
import system.ontologies._
import system.ontologies.messages.MessageType._
import system.ontologies.messages._

/**
  * Actor that manages the sending of messages to the main server
  * Created by Matteo Gabellini on 29/06/2017.
  */
class CellPublisher(mediator: ActorRef) extends TemplatePublisher(mediator) {
    
    private var watchdog: BasicWatchdog = _
    
    override protected def init(args: List[String]): Unit = {
        super.init(args)
        if (args.head != ClusterMembersListener.greetings) throw new Exception()
        log.info("Hello there from {}!", name)
    
        //Ask to the core actor the cell information in order to continue the handshake task
        parent ! AriadneMessage(
            Info,
            Info.Subtype.Request,
            Location.PreMade.selfToSelf,
            SensorsInfoUpdate.empty
        )

    }

    override protected def receptive = {
        case msg@AriadneMessage(Info, Info.Subtype.Response, Location.PreMade.selfToSelf, sensorsInfoUpdate: SensorsInfoUpdate) => {
            log.debug("Sensor Info " + sensorsInfoUpdate)
            val handshakeMsg = AriadneMessage(
                Handshake,
                Handshake.Subtype.CellToMaster,
                Location.PreMade.cellToMaster,
                sensorsInfoUpdate
            )
            mediator ! Publish(Topic.HandShakes,
                handshakeMsg
            )
            this.watchdog = new BasicWatchdog(self)
            this.watchdog.start()
            log.info("Sending Handshake to Master...")
        }
        case msg@AriadneMessage(Handshake, Handshake.Subtype.Acknowledgement, _, cnt) =>
            this.watchdog.notifyEventOccurred
            this.context.become(cultured, discardOld = true)
            log.debug("I've become cultured")
        case WatchDogNotification => {
            //Ask to the core actor the cell information in order to continue the handshake task
            parent ! AriadneMessage(
                Info,
                Info.Subtype.Request,
                Location.PreMade.selfToSelf,
                SensorsInfoUpdate.empty
            )
        }
        case _ => //ignore
    }


    private def cultured: Receive = {
        case msg@AriadneMessage(Alarm, _, _, _) =>
            mediator ! Publish(Topic.Alarms, msg.copy(direction = Location.PreMade.cellToCluster))
        case msg@AriadneMessage(Update, Update.Subtype.Sensors, _, _) =>
            mediator ! Publish(Topic.Updates, msg.copy(direction = Location.PreMade.cellToMaster))
        case msg@AriadneMessage(Update, Update.Subtype.Practicability, _, _) =>
            mediator ! Publish(Topic.Practicabilities, msg.copy(direction = Location.PreMade.cellToCell))
        case msg@AriadneMessage(Update, Update.Subtype.CurrentPeople, _, _) =>
            mediator ! Publish(Topic.Updates, msg.copy(direction = Location.PreMade.cellToMaster))
        case msg@AriadneMessage(Topology, Topology.Subtype.Acknowledgement, _, _) =>
            mediator ! Publish(Topic.TopologyACK, msg.copy(direction = Location.PreMade.cellToMaster))
        case _ => // Ignore
    }
}