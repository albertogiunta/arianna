package master.tests

import akka.actor.{ActorSystem, Props}
import master.core.DataStreamer
import ontologies.messages._

import scala.util.Random

/**
  * Created by Xander_C on 09/07/2017.
  */
object TestDataStreamer extends App {
    
    implicit val system = ActorSystem("Test")
    
    val streamer = system.actorOf(Props[DataStreamer], "DataStreamer")
    
    (0 to Int.MaxValue).foreach(_ => {
        val u =
            (1 to 10).map(_ => ontologies.messages.Cell(
                InfoCell(Random.nextInt(1000), "uri" + Random.nextInt(100), "name" + Random.nextInt(100),
                    Coordinates(Point(1, 1), Point(-1, -1), Point(-1, 1), Point(1, -1)),
                    Point(0, 0)
                ),
                neighbors = List(InfoCell(Random.nextInt(1000), "uri" + Random.nextInt(100), "name" + Random.nextInt(100),
                    Coordinates(Point(1, 1), Point(-1, -1), Point(-1, 1), Point(1, -1)),
                    Point(0, 0)
                ), InfoCell(Random.nextInt(1000), "uri" + Random.nextInt(100), "name" + Random.nextInt(100),
                    Coordinates(Point(1, 1), Point(-1, -1), Point(-1, 1), Point(1, -1)),
                    Point(0, 0)
                )),
                isExitPoint = true,
                isEntryPoint = true,
                capacity = Random.nextInt(100),
                currentPeople = Random.nextInt(90),
                practicabilityLevel = 0.0,
                squareMeters = 1000,
                passages = List(Passage(Random.nextInt(1000), Point(1, 1), Point(2, 1))),
                sensors = List(Sensor(1, 2.0), Sensor(2, 1.55))
            ))
        
        Thread.sleep(100)
        streamer ! u
    })
}
