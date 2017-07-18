package cell

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import cell.core.CellCoreActor
import com.typesafe.config.ConfigFactory
import ontologies.messages.Location._
import ontologies.messages.MessageType.Topology.Subtype.Topology4Cell
import ontologies.messages.MessageType.{Init, Topology}
import ontologies.messages.{AriadneMessage, Location, _}
import spray.json.{JsValue, _}

import scala.io.Source
/**
  * Created by Alessandro on 28/06/2017.
  */

import ontologies.messages.AriannaJsonProtocol._

object TestCells extends App {
    /*val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/res/conf/akka/cell.conf"

    implicit val config = ConfigFactory.parseFile(new File(path2Config))
        .withFallback(ConfigFactory.load()).resolve()

    val system = ActorSystem("Arianna-Cluster", config)

    println("ActorSystem " + system.name + " where cells running on is activeted...")


    val publisher = system.actorOf(Props[CellPublisher], "CellPublisher")
    val subscriber = system.actorOf(Props[CellSubscriber], "CellSubscriber")
    //val actorsToInitialize = List(publisher, subscriber)


    //create and start the listener
    val clusterListener = system.actorOf(Props[ClusterMembersListener], "CellClusterListener")
    println("ClusterListener created")

    Thread.sleep(5000)
    //subscriber ! MyMessage(ontologies.Init, null)
    //Simulate a handshake message sending to the server
    publisher ! AriadneMessage(
        MessageType.Init,
        MessageType.Init.Subtype.Greetings,
        Location.Cell >> Location.Server, Greetings(List("Hello baby.")))
    println("[Cell] message sended!")*/

    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/res/conf/akka/cell.conf"

    implicit val config = ConfigFactory.parseFile(new File(path2Config))
        .withFallback(ConfigFactory.load()).resolve()

    val system = ActorSystem("Arianna-Cluster", config)

    var core = system.actorOf(Props[CellCoreActor], "CellCore")
    var server2Cell = Location.Server >> Location.Cell


    private def readJson(filename: String): JsValue = {
        val source: String = Source.fromFile(filename).getLines.mkString
        source.parseJson
    }

    def loadArea(): Area = {
        val area = readJson(s"res/json/map.json").convertTo[Area]
        area
    }

    def areaForCell: AreaForCell = {
        AreaForCell(area)
    }

    var area: Area = loadArea()

    Thread.sleep(5000)
    private val greetings: String = "Hello there, it's time to dress-up"
    core ! AriadneMessage(Init, Init.Subtype.Greetings,
        Location.Server >> Location.Self, Greetings(List(greetings)))
    println("Area sended to cell core")
    core ! AriadneMessage(Topology, Topology4Cell, server2Cell, areaForCell)
}
