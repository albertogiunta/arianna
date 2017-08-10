package cell.cluster

import akka.actor.ActorRef
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import com.actors.{BasicPublisher, ClusterMembersListener}
import com.utils.BasicWatchDog
import com.utils.WatchDog.WatchDogNotification
import ontologies._
import ontologies.messages.Location._
import ontologies.messages.MessageType._
import ontologies.messages._

/**
  * Actor that manages the sending of messages to the main server
  * Created by Matteo Gabellini on 29/06/2017.
  */
class CellPublisher(mediator: ActorRef) extends BasicPublisher(mediator) {

    private val self2Self: MessageDirection = Location.Self >> Location.Self

    private var watchDog: BasicWatchDog = _

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
            SensorsInfoUpdate.empty
        )

    }

    override protected def receptive = {
        case msg@AriadneMessage(Info, Info.Subtype.Response, this.self2Self, sensorsInfoUpdate: SensorsInfoUpdate) => {
            println("Sensor Info " + sensorsInfoUpdate)
            //Thread.sleep(1000)
            val handshakeMsg = AriadneMessage(
                Handshake,
                Handshake.Subtype.CellToMaster,
                Location.Cell >> Location.Master,
                sensorsInfoUpdate
            )
            mediator ! Publish(Topic.HandShakes,
                handshakeMsg
            )
            this.watchDog = new BasicWatchDog(self)
            this.watchDog.start()
            log.info("Sending Handshake to Master...")
        }
        case msg@AriadneMessage(Handshake, Handshake.Subtype.Acknowledgement, _, cnt) =>
            this.watchDog.notofyEventOccured
            this.context.become(cultured, discardOld = true)
            log.info("I've become cultured")
        case WatchDogNotification => {
            //Ask to the core actor the cell information in order to continue the handshake task
            //println("aaa 0")
            parent ! AriadneMessage(
                Info,
                Info.Subtype.Request,
                self2Self,
                SensorsInfoUpdate.empty
            )
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
        case msg@AriadneMessage(Topology, Topology.Subtype.Acknowledgement, _, _) =>
            mediator ! Publish(Topic.TopologyACK, msg)
        case _ => // Ignore
    }
}