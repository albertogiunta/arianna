package admin

import java.io.File
import javafx.embed.swing.JFXPanel

import akka.actor.{Actor, ActorSystem, Props}
import javafx.stage.Stage

import area.Message
import com.typesafe.config.ConfigFactory

import scalafx.application.Platform

/**
  * Created by lisamazzini on 30/06/17.
  */
class AdminActor(interfaceView: InterfaceView) extends Actor{

    val interfaceController : InterfaceController = new InterfaceController(interfaceView);

    def receive = {
        case msg : Message.FromServer.ToAdmin.SAMPLE_UPDATE => {
            interfaceController.newText(msg.sampleUpdate)
            //Update view
        }
        case Message.FromServer.ToAdmin.SEND_ALARM_TO_ADMIN => {
            //Update view
        }
    }

}


object App {
    def main(args: Array[String]): Unit = {
        new JFXPanel
        var interfaceView : InterfaceView = new InterfaceView
        val config = ConfigFactory.parseFile(new File("src/main/scala/application.conf"))
        val system = ActorSystem.create("adminSystem", config.getConfig("admin"))
        var admin = system.actorOf(Props(new AdminActor(interfaceView)), "admin")

       Platform.runLater {
           //println(Platform.isFxApplicationThread)
           interfaceView.start(new Stage())
       }

    }
}