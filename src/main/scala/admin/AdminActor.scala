package admin

import java.io.File
import java.nio.file.Paths
import javafx.embed.swing.JFXPanel
import javafx.stage.Stage

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import common.CustomActor
import ontologies.messages._

import scala.collection.mutable.ListBuffer
import scalafx.application.Platform

class AdminActor(interfaceView: InterfaceView) extends CustomActor {

    var area: Area = _
    Thread.sleep(4000)
    val interfaceController: InterfaceController = interfaceView.controller
    interfaceController.actorRef = self
    val serverActor = context.actorSelection("akka.tcp://serverSystem@127.0.0.1:4553/user/server")

    override def receive: Receive = {
        case msg@AriadneMessage(MessageType.Update, MessageType.Update.Subtype.UpdateForAdmin, _, adminUpdate: UpdateForAdmin) => {
            val updateCells: ListBuffer[CellForView] = new ListBuffer[CellForView]
            adminUpdate.list.foreach(c => updateCells += new CellForView(c.infoCell.id, c.infoCell.name, c.currentPeople, c.sensors))
            interfaceController.updateView(updateCells.toList)
        }
        //TODO Update view
        case msg@AriadneMessage(MessageType.Topology, MessageType.Topology.Subtype.Planimetrics, _, area: Area) => {
            val initialConfiguration: ListBuffer[CellForView] = new ListBuffer[CellForView]
            area.cells.foreach(c => initialConfiguration += CellForView(c.infoCell.id, c.infoCell.name, c.currentPeople, c.sensors))
            interfaceController.createCells(initialConfiguration.toList)
            //serverActor ! AriadneMessage(MessageType.Topology, MessageType.Topology.Subtype.Planimetrics, Location.Admin >> Location.Server, msg.content.toString)
        }
        case _ => println("none")

    }

}

object App {
    def main(args: Array[String]): Unit = {
        new JFXPanel
        val path2Project = Paths.get("").toFile.getAbsolutePath
        val path2Config = path2Project + "/res/conf/akka/application.conf"
        println(path2Config)
        var interfaceView: InterfaceView = new InterfaceView
        val config = ConfigFactory.parseFile(new File(path2Config))
        val system = ActorSystem.create("adminSystem", config.getConfig("admin"))
        var admin = system.actorOf(Props(new AdminActor(interfaceView)), "admin")
        Platform.runLater {
            interfaceView.start(new Stage())
        }

    }
}