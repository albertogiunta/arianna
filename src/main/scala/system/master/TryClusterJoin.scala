package system.master.tests

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import com.actors.ClusterMembersListener
import com.typesafe.config.ConfigFactory
import system.master.cluster.{MasterPublisher, MasterSubscriber}
import system.ontologies.Topic
import system.ontologies.messages.Location._
import system.ontologies.messages.MessageType.Handshake
import system.ontologies.messages._

/**
  * Created by Alessandro on 29/06/2017.
  */
object TryClusterJoin extends App {
    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/res/conf/akka/cell.conf"

    implicit val config = ConfigFactory.parseFile(new File(path2Config))
        .withFallback(ConfigFactory.load()).resolve()

    implicit val system = ActorSystem("Arianna-Cluster", config)
    
    val listener = system.actorOf(Props[ClusterMembersListener], "ClusterMemberListener")
    
    val subscriber = system.actorOf(Props[MasterSubscriber], "CellSubscriber")
    
    val publisher = system.actorOf(Props[MasterPublisher], "CellPublisher")

    val remotemsg = AriadneMessage(
        Handshake,
        Handshake.Subtype.CellToMaster,
        Location.Cell >> Location.Master,
        SensorsInfoUpdate(
            CellInfo("uri", 0),
            List(SensorInfo(1, 2.0), SensorInfo(2, 1.55))
        )
    )
    
    val mediator: ActorRef = DistributedPubSub(system).mediator
    
    Thread.sleep(3000)
    
    system.log.info("Publishing {}", remotemsg)
    
    mediator ! Publish(Topic.Updates, remotemsg)
}