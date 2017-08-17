package run

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import akka.cluster.pubsub.DistributedPubSub
import com.typesafe.config.{Config, ConfigFactory}
import master.Master

/**
  * Created by Xander_C on 09/07/2017.
  */
object RunMaster extends App {
    
    val path2Project: String = Paths.get("").toFile.getAbsolutePath
    
    val path2Config = if (!args(0).contains(path2Project)) path2Project + args(0) else args(0)
    
    implicit val config: Config = ConfigFactory.parseFile(new File(path2Config))
        .withFallback(ConfigFactory.load()).resolve()
    
    implicit val system: ActorSystem = ActorSystem("Arianna-Cluster", config)
    
    val middleware = DistributedPubSub(system).mediator
    
    val master = system.actorOf(Props(new Master(middleware)), "Master")
}