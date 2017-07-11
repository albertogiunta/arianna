package master.tests

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import master.Master
import ontologies.messages.MessageType.Topology
import ontologies.messages.MessageType.Topology.Subtype.Planimetrics
import ontologies.messages.{AriadneLocalMessage, Location}
import ontologies.messages.Location._

import scala.io.Source

/**
  * Created by Xander_C on 09/07/2017.
  */
object TestMaster extends App {

    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/res/conf/akka/master.conf"

    implicit val config = ConfigFactory.parseFile(new File(path2Config))
        .withFallback(ConfigFactory.load()).resolve()

    implicit val system = ActorSystem("Arianna-Cluster", config)
    
    val master = system.actorOf(Props[Master], "Master")
    
    val path2map = path2Project + "/res/json/map4test.json"
    
    val topology = Source.fromFile(new File(path2map)).getLines.mkString
    
    Thread.sleep(500)
    
    system.actorSelection("user/Master/TopologySupervisor") ! AriadneLocalMessage(
        Topology,
        Planimetrics,
        Location.Admin >> Location.Server,
        Planimetrics.unmarshal(topology)
    )
}
