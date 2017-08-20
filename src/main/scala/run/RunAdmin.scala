package run

import java.io.File
import java.nio.file.Paths
import javafx.embed.swing.JFXPanel

import admin.actors.AdminManager
import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import ontologies.messages.Location._
import ontologies.messages.MessageType.Init
import ontologies.messages.{AriadneMessage, Greetings, Location}
import system.names.NamingSystem

object RunAdmin extends App {

    override def main(args: Array[String]): Unit = {
        new JFXPanel
        val path2Project = Paths.get("").toFile.getAbsolutePath
        val path2Config = path2Project + "/res/conf/akka/testAdmin.conf"
        val config = ConfigFactory.parseFile(new File(path2Config)).resolve
        val system = ActorSystem.create(NamingSystem.AdminActorSystem, config)
        var admin = system.actorOf(Props[AdminManager], NamingSystem.AdminManager)
        admin ! AriadneMessage(Init, Init.Subtype.Greetings, Location.Admin >> Location.Self, Greetings(List.empty))
    }

}