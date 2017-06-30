package cell

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import cell.cluster.{CellPublisher, CellSubscriber}
import com.typesafe.config.ConfigFactory
import ontologies.MyMessage

/**
  * Created by Alessandro on 28/06/2017.
  */
object TestCells extends App {
    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/conf/cell.conf"

    implicit val config = ConfigFactory.parseFile(new File(path2Config))
        .withFallback(ConfigFactory.load()).resolve()

    val system1 = ActorSystem("Arianna-Cluster", config)

    println("ActorSystem " + system1.name + " where cells running on is activeted...")

    val subscriber = system1.actorOf(Props[CellSubscriber], "Subscriber")

    //subscriber ! MyMessage(ontologies.Init, null)

    val publisher = system1.actorOf(Props[CellPublisher], "Publisher")

    //Simulate a handshake message sending to the server
    publisher ! MyMessage(ontologies.Handshake, "Hello baby.")
}
