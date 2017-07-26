package master

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import master.core.{AdminManager, DataStreamer}
import ontologies.messages._

import scala.util.Random

/**
  * Created by Xander_C on 09/07/2017.
  */
object TestDataStreamer extends App {
    
    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/res/conf/akka/application.conf"
    val config = ConfigFactory.parseFile(new File(path2Config))
    val system = ActorSystem.create("serverSystem", config.getConfig("adminManager"))
    
    val adminManager = system.actorOf(Props[AdminManager], "AdminManager")
    
    Thread.sleep(5000)
    
    val streamer = system.actorOf(Props[DataStreamer], "DataStreamer")
    
    (0 to 10).foreach(_ => {
        val u =
            (0 to 5).map(id => ontologies.messages.Cell(
                InfoCell(id, "uri" + id, "name" + id,
                    Coordinates(Point(1, 1), Point(-1, -1), Point(-1, 1), Point(1, -1)),
                    Point(0, 0)
                ),
                neighbors = List(InfoCell(Random.nextInt(5), "uri" + Random.nextInt(5), "name" + Random.nextInt(5),
                    Coordinates(Point(1, 1), Point(-1, -1), Point(-1, 1), Point(1, -1)),
                    Point(0, 0)
                ), InfoCell(Random.nextInt(5), "uri" + Random.nextInt(5), "name" + Random.nextInt(5),
                    Coordinates(Point(1, 1), Point(-1, -1), Point(-1, 1), Point(1, -1)),
                    Point(0, 0)
                )),
                isExitPoint = true,
                isEntryPoint = true,
                capacity = 100,
                currentPeople = Random.nextInt(95),
                practicability = 0.0,
                squareMeters = 1000,
                passages = List(Passage(Random.nextInt(1000), Point(1, 1), Point(2, 1))),
                sensors = List(Sensor(0, Random.nextDouble() * Random.nextInt(30), 0.0, 0.0), Sensor(1, 2.0, 0.0, 0.0), Sensor(2, 1.55, 0.0, 0.0))
            ))
        
        Thread.sleep(100)
        streamer ! u
    })
}
