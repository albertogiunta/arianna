package run

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import master.core.Master

/**
  * Created by Xander_C on 09/07/2017.
  */
object RunMaster extends App {

    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/res/conf/akka/testMaster.conf"

    implicit val config = ConfigFactory.parseFile(new File(path2Config))
        .withFallback(ConfigFactory.load()).resolve()

    implicit val system = ActorSystem("Arianna-Cluster", config)
    
    val master = system.actorOf(Props[Master], "Master")
    
    //    val path2map = path2Project + "/res/json/map4test.json"
    //
    //    val topology = Source.fromFile(new File(path2map)).getLines.mkString
    //
    //    Thread.sleep(500)
    
    //    system.actorSelection("similUser/Master/TopologySupervisor") ! AriadneMessage(
    //        Topology,
    //        Planimetrics,
    //        Location.Admin >> Location.Master,
    //        Planimetrics.unmarshal(topology)
    //    )
    
    //    Thread.sleep(1000)
    //
    //    DistributedPubSub(system).mediator ! Publish(Topic.HandShakes,
    //        AriadneMessage(
    //            Handshake,
    //            Handshake.Subtype.Cell2Master,
    //            Location.Cell >> Location.Master,
    //            InfoCell(14321, "uri", "PancoPillo",
    //                Coordinates(Point(1, 1), Point(-1, -1), Point(-1, 1), Point(1, -1)),
    //                Point(0, 0)
    //            )
    //        )
    //    )
}
