package master.tests

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import com.typesafe.config.ConfigFactory
import common.ClusterMembersListener
import master.cluster.{MasterPublisher, MasterSubscriber}
import ontologies.Topic
import ontologies.messages.Location._
import ontologies.messages.MessageType.Update
import ontologies.messages._

/**
  * Created by Alessandro on 29/06/2017.
  */
object TestClusterJoin extends App {
    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/res/conf/akka/cell.conf"

    implicit val config = ConfigFactory.parseFile(new File(path2Config))
        .withFallback(ConfigFactory.load()).resolve()

    implicit val system = ActorSystem("Arianna-Cluster", config)
    
    val listener = system.actorOf(Props[ClusterMembersListener], "ClusterMemberListener")
    
    val subscriber = system.actorOf(Props[MasterSubscriber], "CellSubscriber")
    
    val publisher = system.actorOf(Props[MasterPublisher], "CellPublisher")

    val remotemsg = AriadneMessage(
        Update,
        Update.Subtype.Sensors,
        Location.Cell >> Location.Server,
        SensorsInfoUpdate(
            InfoCell(0, "uri", "name",
                Coordinates(Point(1, 1), Point(-1, -1), Point(-1, 1), Point(1, -1)),
                Point(0, 0)
            ),
            List(SensorInfo(1, 2.0, 0.0, 0.0), SensorInfo(2, 1.55, 0.0, 0.0))
        )
    )
    
    Thread.sleep(3000)
    
    val mediator: ActorRef = DistributedPubSub(system).mediator
    
    system.log.info("Publishing {}", remotemsg)
    
    mediator ! Publish(Topic.Updates, remotemsg)
}