package cell

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import cell.cluster.{CellPublisher, CellSubscriber}
import com.typesafe.config.ConfigFactory
import common.ClusterMembersListener
import ontologies.messages.Location._
import ontologies.messages.{AriadneMessage, Location, _}
/**
  * Created by Alessandro on 28/06/2017.
  */
object TestCells extends App {
    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/conf/cell.conf"

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
    println("[Cell] message sended!")
}
