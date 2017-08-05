package admin

import akka.actor.{ActorRef, Props}
import com.actors.BasicActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.{Alarm, Handshake, Interface, Topology}
import ontologies.messages._

import scala.collection.mutable
import scalafx.application.Platform

/**
  * This actor intermediates between the interface and the System. It sends the loaded map, handles the messages coming
  * from the System and update the Interface coherently and informs the System if the administrator sent an Alarm from
  * the interface. It also creates a ChartActor for each chart window opened by the administrator and forward to him only
  * the updates about the correct cell.
  *
  *
  */
class AdminActor() extends BasicActor {

    private var interfaceController: InterfaceController = _

    private var roomIDs: mutable.Map[CellInfo, RoomID] = new mutable.HashMap[CellInfo, RoomID]

    private val chartActors: mutable.Map[RoomID, ActorRef] = new mutable.HashMap[RoomID, ActorRef]
    //Se si fa partire solo l'admin manager
    private val adminManager = context.actorSelection("akka.tcp://Arianna-Cluster@127.0.0.1:25520/user/AdminManager")
    //Se si fa partire il master
    //val adminManager = context.actorSelection("akka.tcp://Arianna-Cluster@127.0.0.1:25520/user/Master/AdminManager")

    private val toServer: MessageDirection = Location.Admin >> Location.Master

    override def init(args: List[Any]): Unit = {
        Platform.runLater {
            val view: InterfaceView = new InterfaceView
            view.start
            interfaceController = view.controller
            interfaceController.adminActor = self
        }
    }

    override def receptive: Receive = {
        //Ricezione del messaggio iniziale dall'interfaccia con aggiornamento iniziale
        case msg@AriadneMessage(_, Topology.Subtype.Planimetrics, _, area: Area) => {
            area.rooms.foreach(r => roomIDs += ((r.cell.info, r.info.id)))
            adminManager ! msg.copy(direction = toServer)
            context.become(operational)
        }

        case _ => desist _

    }

    def operational: Receive = {
        //Ricezione dell'aggiornamento delle celle
        case msg@AriadneMessage(_, MessageType.Update.Subtype.Admin, _, adminUpdate: AdminUpdate) => {
            val updateCells: mutable.Map[RoomID, RoomDataUpdate] = new mutable.HashMap[RoomID, RoomDataUpdate]
            adminUpdate.list.foreach(update => updateCells += ((update.room, update)))
            interfaceController updateView updateCells.values.toList
            chartActors.foreach(actor => actor._2 ! AriadneMessage(Interface, Interface.Subtype.UpdateChart, Location.Admin >> Location.Self, updateCells.get(actor._1).get))
        }

        case msg@AriadneMessage(_, Alarm.Subtype.FromInterface, _, _) => adminManager ! msg.copy(direction = toServer)

        case msg@AriadneMessage(_, Alarm.Subtype.FromCell, _, content: AlarmContent) => interfaceController triggerAlarm content

        case msg@AriadneMessage(Handshake, Handshake.Subtype.CellToMaster, _, sensorsInfo: SensorsInfoUpdate) => {
            interfaceController.initializeSensors(sensorsInfo, roomIDs.get(sensorsInfo.cell).get)
        }

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
