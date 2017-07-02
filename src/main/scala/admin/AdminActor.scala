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

    override def receive: Receive = {
        case msg: Message.FromServer.ToAdmin.SAMPLE_UPDATE =>
            interfaceController.newText(msg.sampleUpdate)
        //TODO Update view
        case Message.FromServer.ToAdmin.SEND_ALARM_TO_ADMIN =>
        //TODO Update view
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
            //println(Platform.isFxApplicationThread)
            interfaceView.start(new Stage())
        }

    }
}