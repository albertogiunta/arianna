package admin

import akka.actor.{ActorRef, Props}
import com.actors.BasicActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.{Alarm, Handshake, Interface, Topology}
import ontologies.messages._

import scala.collection.mutable
import scalafx.application.Platform

class InterfaceActor extends BasicActor {

    private var interfaceController: InterfaceController = _
    private val chartActors: mutable.Map[RoomID, ActorRef] = new mutable.HashMap[RoomID, ActorRef]
    private var roomIDs: mutable.Map[CellInfo, RoomID] = new mutable.HashMap[CellInfo, RoomID]


    override def init(args: List[Any]): Unit = {
        Platform.runLater {
            val view: InterfaceView = new InterfaceView
            view.start
            interfaceController = view.controller
            interfaceController.adminActor = self
        }
    }

    /**
      * This method is the behaviour on which the BasicActor will be
      * after the Initialization has been completed.
      *
      * @return An Actor Receive Behaviour
      */

    override def receptive: Receive = {
        //Ricezione del messaggio iniziale dall'interfaccia con aggiornamento iniziale
        case msg@AriadneMessage(_, Topology.Subtype.Planimetrics, _, area: Area) => {
            area.rooms.foreach(r => roomIDs += ((r.cell.info, r.info.id)))
            parent ! msg
            context.become(operational)
        }

        case _ => desist _

    }

    def operational: Receive = {

        case msg@AriadneMessage(_, MessageType.Update.Subtype.Admin, _, adminUpdate: AdminUpdate) => {
            val updateCells: mutable.Map[RoomID, RoomDataUpdate] = new mutable.HashMap[RoomID, RoomDataUpdate]
            adminUpdate.list.foreach(update => updateCells += ((update.room, update)))
            interfaceController updateView updateCells.values.toList
            chartActors.foreach(actor => actor._2 ! AriadneMessage(Interface, Interface.Subtype.UpdateChart, Location.Admin >> Location.Self, updateCells.get(actor._1).get))
        }

        case msg@AriadneMessage(_, Alarm.Subtype.FromInterface, _, _) => parent ! msg

        case msg@AriadneMessage(_, Alarm.Subtype.FromCell, _, content: AlarmContent) => interfaceController triggerAlarm content

        case msg@AriadneMessage(Handshake, Handshake.Subtype.CellToMaster, _, sensorsInfo: SensorsInfoUpdate) => interfaceController.initializeSensors(sensorsInfo, roomIDs.get(sensorsInfo.cell).get)

        case msg@AriadneMessage(Interface, Interface.Subtype.OpenChart, _, cell: CellForChart) => {
            var chartActor = context.actorOf(Props[ChartActor])
            chartActors += ((cell.cell.id, chartActor))
            chartActor ! msg
        }

        case msg@AriadneMessage(Interface, Interface.Subtype.CloseChart, _, cell: RoomInfo) => {
            context stop chartActors.get(cell.id).get
            interfaceController enableButton cell.id
        }

        case _ => desist _

    }

}
