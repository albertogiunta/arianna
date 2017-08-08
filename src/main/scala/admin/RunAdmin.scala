package admin

import java.io.File
import java.nio.file.Paths
import javafx.embed.swing.JFXPanel

import admin.actors.AdminActor
import admin.view.InterfaceView
import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import ontologies.messages.Location._
import ontologies.messages.MessageType.Init
import ontologies.messages.{AriadneMessage, Greetings, Location}

object RunAdmin extends App {

    override def main(args: Array[String]): Unit = {
        new JFXPanel
        val path2Project = Paths.get("").toFile.getAbsolutePath
        val path2Config = path2Project + "/res/conf/akka/admin.conf"
        var interfaceView: InterfaceView = new InterfaceView
        val config = ConfigFactory.parseFile(new File(path2Config)).resolve
        val system = ActorSystem.create("adminSystem", config)
        var admin = system.actorOf(Props[AdminActor], "admin")
        admin ! AriadneMessage(Init, Init.Subtype.Greetings, Location.Admin >> Location.Self, Greetings(List.empty))
    }

}
