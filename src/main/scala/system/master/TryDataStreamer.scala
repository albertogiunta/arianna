package system.master

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import system.master.core.{AdminSupervisor, DataStreamer}
import system.ontologies.messages._

import scala.collection.immutable.HashMap
import scala.util.Random

/**
  * Created by Xander_C on 09/07/2017.
  */
object TryDataStreamer extends App {
    
    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/res/conf/akka/application.conf"
    val config = ConfigFactory.parseFile(new File(path2Config))
    val system = ActorSystem.create("serverSystem", config.getConfig("adminManager"))

    val adminManager = system.actorOf(Props[AdminSupervisor], "AdminManager")
    
    Thread.sleep(5000)
    
    val streamer = system.actorOf(Props(
        new DataStreamer(system.actorSelection(adminManager.path))), "DataStreamer")
    
    val roomsInfo = HashMap((0 to 5).map(id =>
        id -> RoomInfo(
            RoomID(id, "Room" + id),
            isExitPoint = true,
            isEntryPoint = true,
            capacity = 100,
            squareMeters = 1000,
            roomVertices = Coordinates(Point(0, 0), Point(0, 0), Point(0, 0), Point(0, 0)),
            antennaPosition = Point(0, 0)
        )
    ): _ *)
    
    (0 to 10).foreach(_ => {
        val u =
            (0 to 5).map(id => Room(
                roomsInfo(id),
                Cell(CellInfo("uri" + id, "0.0.0.0", 0), sensors = List(SensorInfo(0, Random.nextDouble() * Random.nextInt(30)), SensorInfo(1, 2.0), SensorInfo(2, 1.55))),
                neighbors = List(),
                passages = List(Passage(Random.nextInt(1000), Point(1, 1), Point(2, 1))),
                currentPeople = Random.nextInt(95),
                practicability = 0.0,
            ))
        
        Thread.sleep(100)
        streamer ! u
    })
}
