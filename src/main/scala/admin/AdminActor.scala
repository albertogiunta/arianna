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

    private val chartActors: mutable.Map[Int, ActorRef] = new mutable.HashMap[Int, ActorRef]
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
            adminManager ! msg.copy(direction = toServer)
            context.become(operational)
        }

        case _ => desist _

    }

    def operational: Receive = {
        //Ricezione dell'aggiornamento delle celle
        case msg@AriadneMessage(_, MessageType.Update.Subtype.UpdateForAdmin, _, adminUpdate: UpdateForAdmin) => {
            val updateCells: mutable.Map[Int, CellForView] = new mutable.HashMap[Int, CellForView]
            adminUpdate.list.foreach(cell => updateCells += ((cell.info.id, new CellForView(cell.info.id, cell.info.name, cell.currentPeople, cell.sensors))))
            interfaceController updateView updateCells.values.toList
            chartActors.foreach(actor => actor._2 ! AriadneMessage(Interface, Interface.Subtype.UpdateChart, Location.Admin >> Location.Self, updateCells.get(actor._1).get))
        }

        case msg@AriadneMessage(_, Alarm.Subtype.FromInterface, _, _) => adminManager ! msg.copy(direction = toServer)

        case msg@AriadneMessage(_, Alarm.Subtype.Basic, _, content: AlarmContent) => interfaceController triggerAlarm content

        case msg@AriadneMessage(Handshake, Handshake.Subtype.CellToMaster, _, sensorsInfo: SensorsInfoUpdate) => {
            interfaceController initializeSensors sensorsInfo
        }

        case msg@AriadneMessage(Interface, Interface.Subtype.OpenChart, _, cell: CellForChart) => {
            var chartActor = context.actorOf(Props[ChartActor])
            chartActors += ((cell.info.id, chartActor))
            chartActor ! msg
        }

        case msg@AriadneMessage(Interface, Interface.Subtype.CloseChart, _, cell: InfoCell) => {
            context stop chartActors.get(cell.id).get
            interfaceController enableButton cell.id
        }

        case _ => desist _

    }

}
