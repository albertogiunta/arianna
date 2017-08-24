package run

import java.io.File
import javafx.application.Platform
import javafx.embed.swing.JFXPanel

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import system.admin.actors.AdminManager
import system.admin.view.LoaderView
import system.names.NamingSystem
import system.ontologies.messages.Location._
import system.ontologies.messages.MessageType.Init
import system.ontologies.messages.{AriadneMessage, Greetings, Location}

object RunAdmin extends App with LoaderListener {
    
    new JFXPanel
    Platform setImplicitExit false
    Platform.runLater(() => {
        val loader = new LoaderView
        loader.start()
        loader.controller.listener = this
    })
    
    override def onLoadConfig(path2Config: String): Unit = {
        val config = ConfigFactory.parseFile(new File(path2Config)).resolve
        val system = ActorSystem.create(NamingSystem.AdminActorSystem, config)
        val admin = system.actorOf(Props[AdminManager], NamingSystem.AdminManager)
        admin ! AriadneMessage(Init, Init.Subtype.Greetings, Location.Admin >> Location.Self, Greetings(List.empty))
    }
}