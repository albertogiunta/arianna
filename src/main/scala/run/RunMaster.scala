package run

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import akka.cluster.pubsub.DistributedPubSub
import com.typesafe.config.{Config, ConfigFactory}
import master.core.Master

/**
  * Created by Xander_C on 09/07/2017.
  */
object RunMaster extends App {

    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/res/conf/akka/testMaster.conf"
    
    implicit val config: Config = ConfigFactory.parseFile(new File(path2Config))
        .withFallback(ConfigFactory.load()).resolve()
    
    implicit val system: ActorSystem = ActorSystem("Arianna-Cluster", config)
    
    val middleware = DistributedPubSub(system).mediator
    
    val master = system.actorOf(Props(new Master(middleware)), "Master")
}
