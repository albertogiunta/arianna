package master

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import master.cluster.Publisher
import ontologies.MyMessage

/**
  * Created by Alessandro on 29/06/2017.
  */
object TestClusterJoin extends App {
    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/conf/cell.conf"
    
    implicit val config = ConfigFactory.parseFile(new File(path2Config))
        .withFallback(ConfigFactory.load()).resolve()
    
    implicit val system = ActorSystem("Arianna-Cluster", config)
    
    //    Cluster(system).join(Cluster(system).selfAddress)
    
    println("ActorSystem " + system.name + " is now Active...")
    
    //    val listener = system.actorOf(Props[ClusterEventListener], "Listener-Cell")
    
    //    val subscriber = system.actorOf(Props[Subscriber], "Subscriber-Cell")
    //    subscriber ! MyMessage(ontologies.Init, null)
    
    val publisher = system.actorOf(Props[Publisher], "Publisher-Cell")
    
    Thread.sleep(5000)
    publisher ! MyMessage(ontologies.Init, "Hello baby.")
    
    
}
