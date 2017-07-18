package admin

import java.io.File
import java.nio.file.Paths
import javafx.embed.swing.JFXPanel
import javafx.stage.Stage

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import common.CustomActor
import ontologies.messages.Location._
import ontologies.messages._

import scala.collection.mutable.ListBuffer
import scalafx.application.Platform

class AdminActor(interfaceView: InterfaceView) extends CustomActor {

    var area: Area = _
    val interfaceController: InterfaceController = interfaceView.controller
    interfaceController.actorRef = self
    val adminManager = context.actorSelection("akka.tcp://Arianna-Cluster@192.168.0.9:25520/user/AdminManager")
    val toServer: MessageDirection = Location.Admin >> Location.Server

    override def receive: Receive = {
        //Ricezione dell'aggiornamento delle celle
        case msg@AriadneMessage(_, MessageType.Update.Subtype.UpdateForAdmin, _, adminUpdate: UpdateForAdmin) => {
            println("Ricevuto update lato admin")
            val updateCells: ListBuffer[CellForView] = new ListBuffer[CellForView]
            adminUpdate.list.foreach(c => updateCells += new CellForView(c.infoCell.id, c.infoCell.name, c.currentPeople, c.sensors))
            interfaceController.updateView(updateCells.toList)
        }
        //Ricezione del messaggio iniziale dall'interfaccia con aggiornamento iniziale
        case msg@AriadneMessage(_, MessageType.Topology.Subtype.Planimetrics, _, area: Area) => {
            adminManager ! AriadneMessage(MessageType.Topology, MessageType.Topology.Subtype.Planimetrics, toServer, msg.content)
        }
        case msg@AriadneMessage(_, MessageType.Alarm.Subtype.FromInterface, _, _) => {
            println("Allarme ricevuto dall'attore")
            adminManager ! msg
        }
        case msg@AriadneMessage(_, MessageType.Alarm.Subtype.Basic, _, _) => {
            interfaceController.triggerAlarm()
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
            interfaceView.start(new Stage())
            var admin = system.actorOf(Props(new AdminActor(interfaceView)), "admin")
        }

    }
}