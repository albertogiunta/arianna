package run

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import akka.cluster.pubsub.DistributedPubSub
import cell.core.CellCoreActor
import com.typesafe.config.ConfigFactory
import ontologies.messages.Location._
import ontologies.messages.MessageType.Init
import ontologies.messages.{AriadneMessage, Greetings, Location}
import system.names.NamingSystem

/**
  * Created by Alessandro on 28/06/2017.
  */
object RunCell1 extends App {
    
    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/res/conf/test/testCell1.conf"
    
    implicit val config = ConfigFactory.parseFile(new File(path2Config))
        .withFallback(ConfigFactory.load()).resolve()
    
    val system = ActorSystem("Arianna-Cluster", config)
    
    val middleware = DistributedPubSub(system).mediator
    
    var core = system.actorOf(Props(new CellCoreActor(middleware)), NamingSystem.CellCore)
    
    core ! AriadneMessage(Init, Init.Subtype.Greetings,
        Location.Master >> Location.Self, Greetings(List("res/json/cell/cell1.json")))
}