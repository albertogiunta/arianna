package master

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import master.cluster.{ClusterEventListener, Subscriber}
import ontologies.MyMessage

object TestMaster extends App {
    
    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/conf/master.conf"
    
    implicit val config = ConfigFactory.parseFile(new File(path2Config))
        .withFallback(ConfigFactory.load()).resolve()
    
    implicit val system = ActorSystem("Arianna-Cluster", config)
    
    //    Cluster(system).join(Cluster(system).selfAddress)
    
    println("ActorSystem " + system.name + " is now Active...")
    
    val listener = system.actorOf(Props[ClusterEventListener], "Listener-Master")
    
    val subscriber = system.actorOf(Props[Subscriber], "Subscriber-Master")
    
    subscriber ! MyMessage(ontologies.Init, null)
    
    //    val publisher = system.actorOf(Props[Publisher], "Publisher-Master")
    //
    //    publisher ! MyMessage(ontologies.Init, "Hello baby.")
}