package run

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import akka.cluster.pubsub.DistributedPubSub
import com.typesafe.config.{Config, ConfigFactory}
import system.cell.core.CellCoreActor
import system.names.NamingSystem
import system.ontologies.messages.Location._
import system.ontologies.messages.MessageType.Init
import system.ontologies.messages.{AriadneMessage, Greetings, Location}

/**
  * Created by Alessandro on 28/06/2017.
  */
object RunCell2 extends App {
    
    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/res/conf/test/testCell2.conf"
    
    implicit val config: Config = ConfigFactory.parseFile(new File(path2Config))
        .withFallback(ConfigFactory.load()).resolve()
    
    val system = ActorSystem("Arianna-Cluster", config)
    
    val middleware = DistributedPubSub(system).mediator
    
    var core = system.actorOf(Props(new CellCoreActor(middleware)), NamingSystem.CellCore + 2)
    
    core ! AriadneMessage(Init, Init.Subtype.Greetings,
        Location.Master >> Location.Self, Greetings(List("res/json/system.cell/cell2.json")))
}
