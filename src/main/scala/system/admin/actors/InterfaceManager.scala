package system.admin.actors

import javafx.application.Platform

import akka.actor.{ActorRef, PoisonPill, Props}
import com.actors.TemplateActor
import com.utils.Counter
import system.admin.controller.InterfaceController
import system.admin.view.InterfaceView
import system.names.NamingSystem
import system.ontologies.messages.Location._
import system.ontologies.messages.MessageType.{Alarm, Error, Handshake, Init, Interface, Topology}
import system.ontologies.messages._

import scala.collection.mutable

/**
  * This actor keeps the interface updated when it receives messages from its parent, and receives the interface
  * event and inform the AdminSupervisor, his parent, about them.
  * It also creates a ChartManager for each chart window opened by the administrator and forward to it only
  * the updates about the correct cell.
  *
  **/
class InterfaceManager extends TemplateActor {

    private var interfaceController: InterfaceController = _
    private val chartActors: mutable.Map[RoomID, ActorRef] = new mutable.HashMap[RoomID, ActorRef]
    private var roomIDs: mutable.Map[String, RoomID] = new mutable.HashMap[String, RoomID]
    val counter: Counter = new Counter(0)
    
    
    protected override def init(args: List[String]): Unit = {
        Platform.runLater(() => {
            val view: InterfaceView = new InterfaceView
            view.start()
            interfaceController = view.controller
            interfaceController.interfaceActor = self
        })
    }

    /**
      * This method is the behaviour on which the BasicActor will be
      * after the Initialization has been completed.
      *
      * @return An Actor Receive Behaviour
      */

    override def receptive: Receive = {
        case msg@AriadneMessage(_, Topology.Subtype.Planimetrics, _, area: Area) =>
            area.rooms.foreach(r => roomIDs += ((r.cell.info.uri, r.info.id)))
            parent ! msg
            println("Rooms " + roomIDs.toString)
            context.become(initializer)


        case _ => desist _

    }

    def initializer: Receive = {
        case AriadneMessage(Topology, Topology.Subtype.Acknowledgement, _, _) => interfaceController connected true

        case AriadneMessage(Error, Error.Subtype.LostConnectionFromMaster, _, _) => interfaceController connected false

        case AriadneMessage(Handshake, Handshake.Subtype.CellToMaster, _, sensorsInfo: SensorsInfoUpdate) =>
            log.info("Received an handshake with sensor data")
            counter.++
            interfaceController.initializeSensors(sensorsInfo, roomIDs(sensorsInfo.cell.uri))
            if (counter.get == roomIDs.size) {
                context.become(operational)
                log.info("Finish initialize sensors, now become operational")
            }


        case msg@AriadneMessage(Init, Init.Subtype.Goodbyes, _, _) => parent ! msg

        case _ => desist _

    }

    def operational: Receive = {

        case AriadneMessage(Topology, Topology.Subtype.Acknowledgement, _, _) => interfaceController connected true

        case AriadneMessage(_, MessageType.Update.Subtype.Admin, _, adminUpdate: AdminUpdate) =>
            val updateCells: mutable.Map[RoomID, RoomDataUpdate] = new mutable.HashMap[RoomID, RoomDataUpdate]
            adminUpdate.list.foreach(update => updateCells += ((update.room, update)))
            interfaceController updateView updateCells.values.toList
            chartActors.foreach(actor => actor._2 ! AriadneMessage(Interface, Interface.Subtype.UpdateChart, Location.Admin >> Location.Self, updateCells(actor._1)))


        case msg@AriadneMessage(_, Alarm.Subtype.FromInterface, _, _) => parent ! msg

        case AriadneMessage(_, Alarm.Subtype.FromCell, _, content: AlarmContent) => interfaceController triggerAlarm content

        case msg@AriadneMessage(Interface, Interface.Subtype.OpenChart, _, cell: CellForChart) =>
            val chartActor = context.actorOf(Props[ChartManager], NamingSystem.ChartManager + chartActors.size.toString)
            chartActors += ((cell.cell.id, chartActor))
            chartActor ! msg


        case AriadneMessage(Interface, Interface.Subtype.CloseChart, _, info: RoomInfo) =>
            sender ! PoisonPill
            chartActors.remove(info.id)
            interfaceController.enableButton(info.id)


        case msg@AriadneMessage(Init, Init.Subtype.Goodbyes, _, _) => parent ! msg

        case AriadneMessage(Error, Error.Subtype.MapIdentifierMismatch, _, _) => interfaceController.showErrorDialog()

        case AriadneMessage(Error, Error.Subtype.LostConnectionFromMaster, _, _) => interfaceController connected false

        case msg@AriadneMessage(Alarm, Alarm.Subtype.End, _, _) => parent ! msg

        case _ => desist _

    }

}
