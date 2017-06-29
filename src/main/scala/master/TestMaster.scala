package master

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import master.cluster.{Publisher, Subscriber}
import ontologies.MyMessage

object TestMaster extends App {
    
    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/src/main/scala/master.conf"
    
    implicit val config = ConfigFactory.parseFile(new File(path2Config))
        .withFallback(ConfigFactory.load()).resolve()
    
    implicit val system = ActorSystem("Arianna-Cluster-Master", config)
    
    println("ActorSystem $system.name is now Active...")
    
    val subscriber = system.actorOf(Props[Subscriber], "Subscriber")
    
    subscriber ! MyMessage(ontologies.Init, null)
    
    val publisher = system.actorOf(Props[Publisher], "Publisher")
    
    publisher ! MyMessage(ontologies.Init, "Hello baby.")
}
