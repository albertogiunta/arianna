package processor.route.actors

import ontologies.messages._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Random

/**
  * Created by Alessandro on 14/07/2017.
  */
@RunWith(classOf[JUnitRunner])
class RouteProcessingTest extends FunSuite with BeforeAndAfter {
    
    val infoCells: Map[Int, (CellInfo, RoomInfo)] = (1 to 9).map(i => i ->
        (
            CellInfo(uri = "http://Arianna/Cell" + i + "@127.0.0.1:" + Random.nextInt(65535) + "/", port = 0),
            RoomInfo(
                id = RoomID(i, "Cell" + i),
                isEntryPoint = if (i == 2 || i == 8) true else false,
                isExitPoint = if (i == 2 || i == 8) true else false,
                capacity = Random.nextInt(20),
                roomVertices = Coordinates(Point(0, 0), Point(0, 6), Point(6, 0), Point(6, 6)),
                antennaPosition = Point(3, 3),
                squareMeters = Random.nextInt(100)
            )
        )
    ).toMap
    
    val routeForAlarmSolution = List(infoCells(9), infoCells(6), infoCells(4), infoCells(5), infoCells(1), infoCells(2))
    
    val areaForAlarm = AreaViewedFromACell(
        Random.nextInt(Int.MaxValue),
        List(
            RoomViewedFromACell(
                infoCells(1)._2,
                infoCells(1)._1,
                neighbors = List(infoCells(2)._2.id, infoCells(5)._2.id),
                passages = List(
                    Passage(infoCells(2)._2.id.serial, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(5)._2.id.serial, Point(6, 3), Point(6, 3))
                ),
                practicability = 0.0
            ),
            RoomViewedFromACell(
                infoCells(2)._2,
                infoCells(2)._1,
                neighbors = List(infoCells(1)._2.id, infoCells(3)._2.id, infoCells(4)._2.id),
                passages = List(
                    Passage(infoCells(1)._2.id.serial, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(3)._2.id.serial, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(4)._2.id.serial, Point(6, 3), Point(6, 3))
                ),
                practicability = 100
            ),
            RoomViewedFromACell(
                infoCells(3)._2,
                infoCells(3)._1,
                neighbors = List(infoCells(2)._2.id, infoCells(6)._2.id),
                passages = List(
                    Passage(infoCells(2)._2.id.serial, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(6)._2.id.serial, Point(6, 3), Point(6, 3))
                ),
                practicability = 150
            ),
            RoomViewedFromACell(
                infoCells(4)._2,
                infoCells(4)._1,
                neighbors = List(infoCells(2)._2.id, infoCells(5)._2.id, infoCells(6)._2.id, infoCells(8)._2.id),
                passages = List(
                    Passage(infoCells(2)._2.id.serial, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(5)._2.id.serial, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(6)._2.id.serial, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(8)._2.id.serial, Point(6, 3), Point(6, 3))
                ),
                practicability = 0.0
            ),
            RoomViewedFromACell(
                infoCells(5) _2,
                infoCells(5) _1,
                neighbors = List(infoCells(1)._2.id, infoCells(4)._2.id, infoCells(7)._2.id),
                passages = List(
                    Passage(infoCells(1)._2.id.serial, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(4)._2.id.serial, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(7)._2.id.serial, Point(6, 3), Point(6, 3))
                ),
                practicability = 0.0
            ),
            RoomViewedFromACell(
                infoCells(6) _2,
                infoCells(6) _1,
                neighbors = List(infoCells(3)._2.id, infoCells(4)._2.id, infoCells(9)._2.id),
                passages = List(
                    Passage(infoCells(4)._2.id.serial, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(3)._2.id.serial, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(9)._2.id.serial, Point(3, 6), Point(3, 6))
                ),
                practicability = 0.0
            ),
            RoomViewedFromACell(
                infoCells(7) _2,
                infoCells(7) _1,
                neighbors = List(infoCells(8)._2.id, infoCells(5)._2.id),
                passages = List(
                    Passage(infoCells(4)._2.id.serial, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(8)._2.id.serial, Point(3, 6), Point(3, 6))
                ),
                practicability = 100
            ),
            RoomViewedFromACell(
                infoCells(8) _2,
                infoCells(8) _1,
                neighbors = List(infoCells(4)._2.id, infoCells(7)._2.id, infoCells(9)._2.id),
                passages = List(
                    Passage(infoCells(4)._2.id.serial, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(7)._2.id.serial, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(9)._2.id.serial, Point(3, 6), Point(3, 6))
                ),
                practicability = Double.PositiveInfinity //10 * Random.nextDouble()
            ),
            RoomViewedFromACell(
                infoCells(9) _2,
                infoCells(9) _1,
                neighbors = List(infoCells(6)._2.id, infoCells(8)._2.id),
                passages = List(
                    Passage(infoCells(6)._2.id.serial, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(8)._2.id.serial, Point(3, 0), Point(3, 0))
                ),
                practicability = 10
            )
        ))
    
    val routeForAreaSolution = List(infoCells(1), infoCells(5), infoCells(4), infoCells(6), infoCells(9))
    
    val areaForRoute = AreaViewedFromACell(
        Random.nextInt(Int.MaxValue),
        List(
            RoomViewedFromACell(
                infoCells(1)._2,
                infoCells(1)._1,
                neighbors = List(infoCells(2)._2.id, infoCells(5)._2.id),
                passages = List(
                    Passage(infoCells(2)._2.id.serial, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(5)._2.id.serial, Point(6, 3), Point(6, 3))
                ),
                practicability = 0.0
            ),
            RoomViewedFromACell(
                infoCells(2)._2,
                infoCells(2)._1,
                neighbors = List(infoCells(1)._2.id, infoCells(3)._2.id, infoCells(4)._2.id),
                passages = List(
                    Passage(infoCells(1)._2.id.serial, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(3)._2.id.serial, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(4)._2.id.serial, Point(6, 3), Point(6, 3))
                ),
                practicability = 100
            ),
            RoomViewedFromACell(
                infoCells(3)._2,
                infoCells(3)._1,
                neighbors = List(infoCells(2)._2.id, infoCells(6)._2.id),
                passages = List(
                    Passage(infoCells(2)._2.id.serial, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(6)._2.id.serial, Point(6, 3), Point(6, 3))
                ),
                practicability = 150
            ),
            RoomViewedFromACell(
                infoCells(4)._2,
                infoCells(4)._1,
                neighbors = List(infoCells(2)._2.id, infoCells(5)._2.id, infoCells(6)._2.id, infoCells(8)._2.id),
                passages = List(
                    Passage(infoCells(2)._2.id.serial, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(5)._2.id.serial, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(6)._2.id.serial, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(8)._2.id.serial, Point(6, 3), Point(6, 3))
                ),
                practicability = 0.0
            ),
            RoomViewedFromACell(
                infoCells(5) _2,
                infoCells(5) _1,
                neighbors = List(infoCells(1)._2.id, infoCells(4)._2.id, infoCells(7)._2.id),
                passages = List(
                    Passage(infoCells(1)._2.id.serial, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(4)._2.id.serial, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(7)._2.id.serial, Point(6, 3), Point(6, 3))
                ),
                practicability = 0.0
            ),
            RoomViewedFromACell(
                infoCells(6) _2,
                infoCells(6) _1,
                neighbors = List(infoCells(3)._2.id, infoCells(4)._2.id, infoCells(9)._2.id),
                passages = List(
                    Passage(infoCells(4)._2.id.serial, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(3)._2.id.serial, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(9)._2.id.serial, Point(3, 6), Point(3, 6))
                ),
                practicability = 0.0
            ),
            RoomViewedFromACell(
                infoCells(7) _2,
                infoCells(7) _1,
                neighbors = List(infoCells(8)._2.id, infoCells(5)._2.id),
                passages = List(
                    Passage(infoCells(4)._2.id.serial, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(8)._2.id.serial, Point(3, 6), Point(3, 6))
                ),
                practicability = 100
            ),
            RoomViewedFromACell(
                infoCells(8) _2,
                infoCells(8) _1,
                neighbors = List(infoCells(4)._2.id, infoCells(7)._2.id, infoCells(9)._2.id),
                passages = List(
                    Passage(infoCells(4)._2.id.serial, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(7)._2.id.serial, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(9)._2.id.serial, Point(3, 6), Point(3, 6))
                ),
                practicability = 100
            ),
            RoomViewedFromACell(
                infoCells(9) _2,
                infoCells(9) _1,
                neighbors = List(infoCells(6)._2.id, infoCells(8)._2.id),
                passages = List(
                    Passage(infoCells(6)._2.id.serial, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(8)._2.id.serial, Point(3, 0), Point(3, 0))
                ),
                practicability = 10
            )
        )
    )
    
    test("From Area to Graph and then Compute Route") {
        
        val fromCell = "Cell1"
        val toCell = "Cell9"
        
        val future = RouteProcessor.computeRoute(fromCell, toCell, areaForRoute.rooms)
        
        val shp: List[RoomID] = Await.result(future, Duration.Inf)._1
        
        assert(shp == routeForAreaSolution.map(p => p._2.id))
    }
    
    test("From Area to Graph and then Compute Escape") {
        
        val actualCell = infoCells(9)
        
        val futures = areaForAlarm.rooms.filter(r => r.info.isExitPoint)
            .map(exit => RouteProcessor.computeRoute(actualCell._2.id.name, exit.info.id.name, areaForAlarm.rooms))
        
        val res = Future {
            futures.map(f => Await.result(f, Duration.Inf)).minBy(res => res._2)._1
        }
        
        val shp: List[RoomID] = Await.result(res, Duration.Inf)
        
        assert(shp == routeForAlarmSolution.map(p => p._2.id))
    }
}
