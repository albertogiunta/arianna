package run

import java.io.File
import javafx.application.Platform
import javafx.embed.swing.JFXPanel

import admin.actors.AdminManager
import admin.view.LoaderView
import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import ontologies.messages.Location._
import ontologies.messages.MessageType.Init
import ontologies.messages.{AriadneMessage, Greetings, Location}
import system.names.NamingSystem

object RunAdmin extends App with LoaderListener {

    override def main(args: Array[String]): Unit = {
        new JFXPanel
        Platform.runLater(() => {
            val loader = new LoaderView
            loader.start()
            loader.controller.listener = this
        })

    }

    override def onLoadConfig(path2Config: String): Unit = {
        val config = ConfigFactory.parseFile(new File(path2Config)).resolve
        val system = ActorSystem.create(NamingSystem.AdminActorSystem, config)
        var admin = system.actorOf(Props[AdminManager], NamingSystem.AdminManager)
        admin ! AriadneMessage(Init, Init.Subtype.Greetings, Location.Admin >> Location.Self, Greetings(List.empty))
    }
}