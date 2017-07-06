package master

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import common.{ClusterMembersListener, CustomActor}
import master.cluster.{DataStreamer, MasterPublisher, MasterSubscriber, TopologySupervisor}
import ontologies.messages.Location._
import ontologies.messages.MessageType.Topology
import ontologies.messages.MessageType.Topology.Subtype.Planimetrics
import ontologies.messages.{AriadneLocalMessage, Location}

import scala.io.Source

/**
  * Created by Alessandro on 29/06/2017.
  */
class Master extends CustomActor {
    
    override def preStart = {
        
        val listener = context.actorOf(Props[ClusterMembersListener], "ClusterListener")
        
        val subscriber = context.actorOf(Props[MasterSubscriber], "Subscriber")
        
        val publisher = context.actorOf(Props[MasterPublisher], "Publisher")
        
        val topologySupervisor = context.actorOf(Props[TopologySupervisor], "TopologySupervisor")
        
        val dataStreamer = context.actorOf(Props[DataStreamer], "DataStreamer")
    }
    
    override def receive: Receive = {
        case _ => log.info("Cazzo mi invii messaggi, stronzo!")
    }
}

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