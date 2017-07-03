package admin

import java.io.File
import javafx.embed.swing.JFXPanel
import javafx.stage.Stage

import akka.actor.{Actor, ActorSystem, Props}
import area.Message
import com.typesafe.config.ConfigFactory

import scalafx.application.Platform

class AdminActor(interfaceView: InterfaceView) extends Actor {

    val interfaceController: InterfaceController = new InterfaceController(interfaceView);
    interfaceView.controller = interfaceController
    interfaceController.actorRef = self
    val serverActor = context.actorSelection("akka.tcp://serverSystem@127.0.0.1:4553/user/server")

    override def receive: Receive = {
        case msg: Message.FromServer.ToAdmin.SAMPLE_UPDATE =>
            interfaceController.newText(msg.sampleUpdate)
        //TODO Update view
        case Message.FromServer.ToAdmin.SEND_ALARM_TO_ADMIN =>
        //TODO Update view
        case msg: Message.FromInterface.ToAdmin.MAP_CONFIG =>
            serverActor ! Message.FromAdmin.ToServer.MAP_CONFIG(msg.area)
        case Message.FromInterface.ToAdmin.ALARM =>
            serverActor ! Message.FromAdmin.ToServer.ALARM
    }
}


object App {
    def main(args: Array[String]): Unit = {
        new JFXPanel
        var interfaceView: InterfaceView = new InterfaceView
        val config = ConfigFactory.parseFile(new File("src/main/scala/application.conf"))
        val system = ActorSystem.create("adminSystem", config.getConfig("admin"))
        var admin = system.actorOf(Props(new AdminActor(interfaceView)), "admin")

        Platform.runLater {
            interfaceView.start(new Stage())
        }

    }
}