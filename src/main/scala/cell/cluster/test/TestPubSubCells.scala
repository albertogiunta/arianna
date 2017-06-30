package cell.cluster.test

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import akka.cluster.Cluster
import cell.cluster.{CellSubscriber, TestPublisher}
import com.typesafe.config.ConfigFactory
import ontologies.MyMessage
;

/**
  * Created by Matteo Gabellini on 29/06/2017.
  */
object TestPubSubCells extends App {

  val path2Project = Paths.get("").toFile.getAbsolutePath
  val path2Config = path2Project + "/src/main/scala/cell/cluster/test/testCell.conf"

  implicit val config = ConfigFactory.parseFile(new File(path2Config))
    .withFallback(ConfigFactory.load()).resolve()

  val system1 = ActorSystem("Arianna-Cluster-Master", config)

  println("ActorSystem " + system1.name + " activeted...")

  val joinAddress = Cluster(system1).selfAddress
  Cluster(system1).join(joinAddress)


  val subscriber1 = system1.actorOf(Props[CellSubscriber], "Subscriber1")
  val subscriber2 = system1.actorOf(Props[CellSubscriber], "Subscriber2")

  Thread.sleep(5000)
  //implicit val system2 = ActorSystem("Arianna-Cluster-Master", config)
  //Cluster(system2).join(joinAddress)
  val publisher = system1.actorOf(Props[TestPublisher], "Publisher")
  publisher ! MyMessage(ontologies.Init, "Hello baby.")

}
