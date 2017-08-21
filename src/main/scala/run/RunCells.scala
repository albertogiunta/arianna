package run

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import akka.cluster.pubsub.DistributedPubSub
import com.typesafe.config.ConfigFactory
import system.cell.core.CellCoreActor
import system.ontologies.messages.Location._
import system.ontologies.messages.MessageType.Init
import system.ontologies.messages.{AriadneMessage, Greetings, Location}

/**
  * Created by Alessandro on 28/06/2017.
  */
object RunCells extends App {

    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/res/conf/akka/testCell.conf"

    implicit val config = ConfigFactory.parseFile(new File(path2Config))
        .withFallback(ConfigFactory.load()).resolve()

    val system = ActorSystem("Arianna-Cluster", config)

    val middleware = DistributedPubSub(system).mediator
    (1 to 15) map {
        i =>
            val core = system.actorOf(Props(new CellCoreActor(middleware)), "CellCore" + i)
            val configPath: String = "res/json/system.cell/system.cell" + i + ".json"
            core ! AriadneMessage(Init, Init.Subtype.Greetings,
                Location.Master >> Location.Self, Greetings(List(configPath)))
    }
    
    
    //    var core = system.actorOf(Props(new CellCoreActor(middleware)), NamingSystem.CellCore)
    //    var server2Cell = Location.Master >> Location.Cell
    //    Thread.sleep(500)
    //    private val configPath: String = "res/json/system.cell/cell1.json"
    //    core ! AriadneMessage(Init, Init.Subtype.Greetings,
    //        Location.Master >> Location.Self, Greetings(List(configPath)))
}
