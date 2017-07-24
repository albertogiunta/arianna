package cell

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import cell.cluster.{CellPublisher, CellSubscriber}
import com.actors.ClusterMembersListener
import com.typesafe.config.ConfigFactory
import ontologies.messages.Location._
import ontologies.messages.MessageType.Update
import ontologies.messages._

/**
  * Created by Matteo Gabellini on 10/07/2017.
  */
class TestPracticabilityExchange extends App {

    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/conf/cell.conf"

    implicit val config = ConfigFactory.parseFile(new File(path2Config))
        .withFallback(ConfigFactory.load()).resolve()

    val system = ActorSystem("Arianna-Cluster", config)

    println("ActorSystem " + system.name + " where cells running on is activeted...")


    val publisher = system.actorOf(Props[CellPublisher], "CellPublisher")
    val subscriber = system.actorOf(Props[CellSubscriber], "CellSubscriber")

    val clusterListener = system.actorOf(Props[ClusterMembersListener], "CellClusterListener")
    println("ClusterListener created")


    private val cellID: Int = 12345
    private val cellUri: String = "uri"
    private val cellName: String = "Gondor"
    private val roomVertices: Coordinates = Coordinates(Point(1, 1),
        Point(-1, -1),
        Point(-1, 1),
        Point(1, -1))
    private val antennaPosition: Point = Point(0, 0)


    publisher ! AriadneMessage(
        Update,
        Update.Subtype.Practicability,
        Location.Cell >> Location.Server,
        PracticabilityUpdate(
            InfoCell(cellID,
                cellUri,
                cellName,
                roomVertices,
                antennaPosition),
            50.0)
    )
}
