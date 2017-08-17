package run

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import akka.cluster.pubsub.DistributedPubSub
import com.typesafe.config.{Config, ConfigFactory}
import master.Master
import system.names.NamingSystem

/**
  * Created by Xander_C on 09/07/2017.
  */
object RunMaster extends App {
    
    val REQUIRED_ARGS = 1
    
    if (args.length == REQUIRED_ARGS) {
        
        val path2Project: String = Paths.get("").toFile.getAbsolutePath
        
        val path2Config = if (!args(0).contains(path2Project)) path2Project + args(0) else args(0)
        
        implicit val config: Config = ConfigFactory.parseFile(new File(path2Config))
            .withFallback(ConfigFactory.load()).resolve()
        
        implicit val system: ActorSystem = ActorSystem(NamingSystem.ActorSystem, config)
        
        val middleware = DistributedPubSub(system).mediator
        
        val master = system.actorOf(Props(new Master(middleware)), NamingSystem.Master)
        
    } else {
        println(s"Wrong number of Arguments... Wanted $REQUIRED_ARGS, found " + args.length)
        System.exit(0)
    }
    
}