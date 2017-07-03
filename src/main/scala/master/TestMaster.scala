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
object TestMaster extends App {

    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/conf/master.conf"

    implicit val config = ConfigFactory.parseFile(new File(path2Config))
        .withFallback(ConfigFactory.load()).resolve()

    implicit val system = ActorSystem("Arianna-Cluster", config)

    system.settings.config.getStringList("akka.cluster.seed-nodes").forEach(s => println(s))
    println()
    //entrySet.stream.filter(e => e.getKey.contains("akka.cluster")).forEach( e => println(e.getKey + " => " + e.getValue))

    println("ActorSystem " + system.name + " is now Active...")

    val listener = system.actorOf(Props[ClusterMembersListener], "Listener-Master")

    val subscriber = system.actorOf(Props[MasterSubscriber], "Subscriber-Master")

    //    subscriber ! AriadneMessage(MessageType.Init, "Ciao")

    val publisher = system.actorOf(Props[MasterPublisher], "Publisher-Master")

    Thread.sleep(1000)

    //    publisher ! AriadneMessage(MessageType.Init, "Ciao")
}