package master.tests

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import com.typesafe.config.ConfigFactory
import master.Master
import ontologies.Topic
import ontologies.messages.Location._
import ontologies.messages.MessageType.Topology.Subtype.Planimetrics
import ontologies.messages.MessageType.{Handshake, Topology}
import ontologies.messages._

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

    system.actorSelection("user/Master/TopologySupervisor") ! AriadneMessage(
        Topology,
        Planimetrics,
        Location.Admin >> Location.Server,
        Planimetrics.unmarshal(topology)
    )
    
    Thread.sleep(1000)
    
    DistributedPubSub(system).mediator ! Publish(Topic.HandShakes,
        AriadneMessage(
            Handshake,
            Handshake.Subtype.Cell2Master,
            Location.Cell >> Location.Server,
            InfoCell(14321, "uri", "PancoPillo",
                Coordinates(Point(1, 1), Point(-1, -1), Point(-1, 1), Point(1, -1)),
                Point(0, 0)
            )
        )
    )
}