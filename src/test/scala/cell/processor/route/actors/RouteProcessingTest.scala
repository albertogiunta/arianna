package cell.processor.route.actors

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
    
    val infoCells: Map[Int, InfoCell] = (1 to 9).map(i => i ->
        InfoCell(id = i, uri = "http://Arianna/Cell" + i + "@127.0.0.1:" + Random.nextInt(65535) + "/", 0, name = "Cell" + i, roomVertices = Coordinates(Point(0, 0), Point(0, 6), Point(6, 0), Point(6, 6)), antennaPosition = Point(3, 3))
    ).toMap
    
    val routeForAlarmSolution = List(infoCells(9), infoCells(6), infoCells(4), infoCells(5), infoCells(1), infoCells(2))
    
    val areaForAlarm = AreaViewedFromACell(
        Random.nextInt(Int.MaxValue),
        List(
            CellViewedFromACell(
                infoCells(1),
                neighbors = List(infoCells(2), infoCells(5)),
                passages = List(
                    Passage(infoCells(2).id, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(5).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                Random.nextInt(20),
                0.0
            ),
            CellViewedFromACell(
                infoCells(2),
                neighbors = List(infoCells(1), infoCells(3), infoCells(4)),
                passages = List(
                    Passage(infoCells(1).id, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(3).id, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(4).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = true,
                isExitPoint = true,
                Random.nextInt(20),
                100
            ),
            CellViewedFromACell(
                infoCells(3),
                neighbors = List(infoCells(2), infoCells(6)),
                passages = List(
                    Passage(infoCells(2).id, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(6).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                Random.nextInt(20),
                150
            ),
            CellViewedFromACell(
                infoCells(4),
                neighbors = List(infoCells(2), infoCells(5), infoCells(6), infoCells(8)),
                passages = List(
                    Passage(infoCells(2).id, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(5).id, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(6).id, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(8).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                Random.nextInt(20),
                0.0
            ),
            CellViewedFromACell(
                infoCells(5),
                neighbors = List(infoCells(1), infoCells(4), infoCells(7)),
                passages = List(
                    Passage(infoCells(1).id, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(4).id, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(7).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                Random.nextInt(20),
                0.0
            ),
            CellViewedFromACell(
                infoCells(6),
                neighbors = List(infoCells(3), infoCells(4), infoCells(9)),
                passages = List(
                    Passage(infoCells(4).id, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(3).id, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(9).id, Point(3, 6), Point(3, 6))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                Random.nextInt(20),
                0.0
            ),
            CellViewedFromACell(
                infoCells(7),
                neighbors = List(infoCells(8), infoCells(5)),
                passages = List(
                    Passage(infoCells(4).id, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(8).id, Point(3, 6), Point(3, 6))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                Random.nextInt(20),
                100
            ),
            CellViewedFromACell(
                infoCells(8),
                neighbors = List(infoCells(4), infoCells(7), infoCells(9)),
                passages = List(
                    Passage(infoCells(4).id, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(7).id, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(9).id, Point(3, 6), Point(3, 6))
                ),
                isEntryPoint = true,
                isExitPoint = true,
                Random.nextInt(20),
                Double.PositiveInfinity //10 * Random.nextDouble()
            ),
            CellViewedFromACell(
                infoCells(9),
                neighbors = List(infoCells(6), infoCells(8)),
                passages = List(
                    Passage(infoCells(6).id, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(8).id, Point(3, 0), Point(3, 0))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                Random.nextInt(20),
                10
            )
        ))
    
    val routeForAreaSolution = List(infoCells(1), infoCells(5), infoCells(4), infoCells(6), infoCells(9))
    
    val areaForRoute = AreaViewedFromACell(
        Random.nextInt(Int.MaxValue),
        List(
            CellViewedFromACell(
                infoCells(1),
                neighbors = List(infoCells(2), infoCells(5)),
                passages = List(
                    Passage(infoCells(2).id, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(5).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                capacity = Random.nextInt(20),
                practicability = 0.0
            ),
            CellViewedFromACell(
                infoCells(2),
                neighbors = List(infoCells(1), infoCells(3), infoCells(4)),
                passages = List(
                    Passage(infoCells(1).id, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(3).id, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(4).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = true,
                isExitPoint = true,
                capacity = Random.nextInt(20),
                practicability = 100
            ),
            CellViewedFromACell(
                infoCells(3),
                neighbors = List(infoCells(2), infoCells(6)),
                passages = List(
                    Passage(infoCells(2).id, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(6).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                capacity = Random.nextInt(20),
                practicability = 100
            ),
            CellViewedFromACell(
                infoCells(4),
                neighbors = List(infoCells(2), infoCells(5), infoCells(6), infoCells(8)),
                passages = List(
                    Passage(infoCells(2).id, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(5).id, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(6).id, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(8).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                capacity = Random.nextInt(20),
                practicability = 0.0
            ),
            CellViewedFromACell(
                infoCells(5),
                neighbors = List(infoCells(1), infoCells(4), infoCells(7)),
                passages = List(
                    Passage(infoCells(1).id, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(4).id, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(7).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = false,
                isExitPoint = true,
                capacity = Random.nextInt(20),
                practicability = 0.0
            ),
            CellViewedFromACell(
                infoCells(6),
                neighbors = List(infoCells(3), infoCells(4), infoCells(9)),
                passages = List(
                    Passage(infoCells(4).id, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(3).id, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(9).id, Point(3, 6), Point(3, 6))
                ),
                isEntryPoint = false,
                isExitPoint = true,
                capacity = Random.nextInt(20),
                practicability = 0.0 //Double.PositiveInfinity//
            ),
            CellViewedFromACell(
                infoCells(7),
                neighbors = List(infoCells(8), infoCells(5)),
                passages = List(
                    Passage(infoCells(4).id, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(8).id, Point(3, 6), Point(3, 6))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                capacity = Random.nextInt(20),
                practicability = 100
            ),
            CellViewedFromACell(
                infoCells(8),
                neighbors = List(infoCells(4), infoCells(7), infoCells(9)),
                passages = List(
                    Passage(infoCells(4).id, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(7).id, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(9).id, Point(3, 6), Point(3, 6))
                ),
                isEntryPoint = true,
                isExitPoint = true,
                capacity = Random.nextInt(20),
                practicability = 100
            ),
            CellViewedFromACell(
                infoCells(9),
                neighbors = List(infoCells(6), infoCells(8)),
                passages = List(
                    Passage(infoCells(6).id, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(8).id, Point(3, 0), Point(3, 0))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                capacity = Random.nextInt(20),
                practicability = 10
            )
        ))
    
    test("From Area to Graph and then Compute Route") {
        
        val fromCell = "Cell1"
        val toCell = "Cell9"
        
        val future = RouteProcessor.computeRoute(fromCell, toCell, areaForRoute.cells)
        
        val shp: List[InfoCell] = Await.result(future, Duration.Inf)._1
        
        assert(shp == routeForAreaSolution)
    }
    
    test("From Area to Graph and then Compute Escape") {
        
        val actualCell = infoCells(9)
        
        val futures = areaForAlarm.cells.filter(c => c.isExitPoint)
            .map(exit => RouteProcessor.computeRoute(actualCell.name, exit.info.name, areaForAlarm.cells))
        
        val res = Future {
            futures.map(f => Await.result(f, Duration.Inf)).minBy(res => res._2)._1
        }
        
        val shp: List[InfoCell] = Await.result(res, Duration.Inf)
        
        assert(shp == routeForAlarmSolution)
    }
}
