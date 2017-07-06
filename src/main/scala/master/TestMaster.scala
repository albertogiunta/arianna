package master

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import common.ClusterMembersListener
import master.cluster.{MasterPublisher, MasterSubscriber, TopologySupervisor}
import ontologies.messages.{AriadneLocalMessage, Location}
import ontologies.messages.Location._
import ontologies.messages.MessageType.Topology
import ontologies.messages.MessageType.Topology.Subtype.Planimetrics

import scala.io.Source

/**
  * Created by Alessandro on 29/06/2017.
  */
object TestMaster extends App {

    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/res/conf/akka/master.conf"

    implicit val config = ConfigFactory.parseFile(new File(path2Config))
        .withFallback(ConfigFactory.load()).resolve()

    implicit val system = ActorSystem("Arianna-Cluster", config)
    
    //    system.settings.config.getStringList("akka.cluster.seed-nodes").forEach(s => println(s))
    //    println()
    //
    //    println("ActorSystem " + system.name + " is now Active...")

    val listener = system.actorOf(Props[ClusterMembersListener], "Listener-Master")

    val subscriber = system.actorOf(Props[MasterSubscriber], "Subscriber-Master")

    val publisher = system.actorOf(Props[MasterPublisher], "Publisher-Master")
    
    val topologySupervisor = system.actorOf(Props[TopologySupervisor], "TopologySupervisor")
    
    val path2map = path2Project + "/res/json/map4test.json"
    
    val topology = Source.fromFile(new File(path2map)).getLines.mkString
    
    //    println(topology)
    //    println(Planimetrics.unmarshal(topology))
    
    Thread.sleep(500)
    
    topologySupervisor ! AriadneLocalMessage(
        Topology,
        Planimetrics,
        Location.Admin >> Location.Server,
        Planimetrics.unmarshal(topology)
    )
}