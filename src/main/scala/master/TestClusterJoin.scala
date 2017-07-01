package master

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import common.ClusterMembersListener
import master.cluster.{MasterPublisher, MasterSubscriber}

/**
  * Created by Alessandro on 29/06/2017.
  */
object TestClusterJoin extends App {
    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/conf/cell.conf"
    
    implicit val config = ConfigFactory.parseFile(new File(path2Config))
        .withFallback(ConfigFactory.load()).resolve()
    
    implicit val system = ActorSystem("Arianna-Cluster", config)
    
    println("ActorSystem " + system.name + " is now Active...")
    
    val listener = system.actorOf(Props[ClusterMembersListener], "Listener-Cell")
    
    val subscriber = system.actorOf(Props[MasterSubscriber], "Subscriber-Cell")
    
    //    subscriber ! AriadneMessage(MessageType.Init, "Ciao")
    
    val publisher = system.actorOf(Props[MasterPublisher], "Publisher-Cell")
    
    //    Thread.sleep(5000)
    
    //    publisher ! AriadneMessage(MessageType.Init, "Ciao")
}