package run

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import cell.core.CellCoreActor
import com.typesafe.config.ConfigFactory
import ontologies.messages.Location._
import ontologies.messages.MessageType.Init
import ontologies.messages.{AriadneMessage, Greetings, Location}

/**
  * Created by Alessandro on 28/06/2017.
  */
object RunCells extends App {

    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/res/conf/akka/testCell.conf"

    implicit val config = ConfigFactory.parseFile(new File(path2Config))
        .withFallback(ConfigFactory.load()).resolve()

    val system = ActorSystem("Arianna-Cluster", config)

    var core = system.actorOf(Props[CellCoreActor], "CellCore")
    var server2Cell = Location.Master >> Location.Cell

    Thread.sleep(500)
    private val configPath: String = "res/json/cell/cell2.json"
    core ! AriadneMessage(Init, Init.Subtype.Greetings,
        Location.Master >> Location.Self, Greetings(List(configPath)))
}
