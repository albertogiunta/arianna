package run

import java.io.File

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
    
        val path2Config = args(0)
        
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