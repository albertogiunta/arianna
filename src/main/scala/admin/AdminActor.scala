package admin

import java.io.File
import java.nio.file.Paths
import javafx.embed.swing.JFXPanel
import javafx.stage.Stage

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import common.CustomActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.{Alarm, Handshake, Topology}
import ontologies.messages._

import scala.collection.mutable.ListBuffer
import scalafx.application.Platform

/**
  * This actor intermediates between the interface and the System. It sends the loaded map, handles the messages coming
  * from the System and update the Interface coherently and informs the System if the administrator sent an Alarm from
  * the interface.
  *
  * @param interfaceView : the View of the Application
  *
  */
class AdminActor(interfaceView: InterfaceView) extends CustomActor {

    var area: Area = _
    val interfaceController: InterfaceController = interfaceView.controller
    interfaceController.actorRef = self
    //Se si fa partire solo l'admin manager
    val adminManager = context.actorSelection("akka.tcp://Arianna-Cluster@127.0.0.1:25520/user/AdminManager")
    //Se si fa partire il master
    //val adminManager = context.actorSelection("akka.tcp://Arianna-Cluster@127.0.0.1:25520/user/Master/AdminManager")
    val toServer: MessageDirection = Location.Admin >> Location.Server

    override def receive: Receive = {
        //Ricezione dell'aggiornamento delle celle
        case msg@AriadneMessage(_, MessageType.Update.Subtype.UpdateForAdmin, _, adminUpdate: UpdateForAdmin) => {
            val updateCells: ListBuffer[CellForView] = new ListBuffer[CellForView]
            adminUpdate.list.foreach(cell => updateCells += new CellForView(cell.info.id, cell.info.name, cell.currentPeople, cell.sensors))
            interfaceController updateView updateCells.toList
        }
        //Ricezione del messaggio iniziale dall'interfaccia con aggiornamento iniziale
        case msg@AriadneMessage(_, Topology.Subtype.Planimetrics, _, area: Area) => adminManager ! msg.copy(direction = toServer)

        case msg@AriadneMessage(_, Alarm.Subtype.FromInterface, _, _) => adminManager ! msg.copy(direction = toServer)

        case msg@AriadneMessage(_, Alarm.Subtype.Basic, _, content: AlarmContent) => interfaceController triggerAlarm content

        case msg@AriadneMessage(Handshake, Handshake.Subtype.CellToMaster, _, sensorsInfo: SensorsUpdate) => {
            interfaceController initializeSensors sensorsInfo
        }

        case _ => println("none")

    }

}

object App {
    def main(args: Array[String]): Unit = {
        new JFXPanel
        val path2Project = Paths.get("").toFile.getAbsolutePath
        val path2Config = path2Project + "/res/conf/akka/admin.conf"
        var interfaceView: InterfaceView = new InterfaceView
        val config = ConfigFactory.parseFile(new File(path2Config)).resolve
        val system = ActorSystem.create("adminSystem", config)
        Platform.runLater {
            interfaceView start new Stage()
            var admin = system.actorOf(Props(new AdminActor(interfaceView)), "admin")
        }

    }
}