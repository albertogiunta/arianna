package cell.processor.tests

import akka.actor.{ActorSystem, Props}
import cell.processor.route.RouteManager
import ontologies.messages.Location._
import ontologies.messages.MessageType.{Init, Route}
import ontologies.messages._

import scala.collection.immutable.HashMap
import scala.util.Random

/**
  * Created by Alessandro on 14/07/2017.
  */
object TestRouteProcessor extends App {
    
    val infos = (1 to 9).map(i => i ->
        InfoCell(
            id = Random.nextInt(65535),
            uri = "http://Cell" + i + "@127.0.0.1:" + Random.nextInt(65535) + "/",
            name = "Cell" + i,
            roomVertices = Coordinates(Point(0, 0), Point(0, 6), Point(6, 0), Point(6, 6)),
            antennaPosition = Point(3, 3)
        )
    ).toMap

    val areaForRoute = AreaViewedFromACell(
        Random.nextInt(Int.MaxValue),
        List(
            CellViewedFromACell(
                infos(1),
                neighbors = List(infos(2), infos(5)),
                passages = List(
                    Passage(infos(2).id, Point(3, 6), Point(3, 6)),
                    Passage(infos(5).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                Random.nextInt(20),
                10 * Random.nextDouble()
            ),
            CellViewedFromACell(
                infos(2),
                neighbors = List(infos(1), infos(3), infos(4)),
                passages = List(
                    Passage(infos(1).id, Point(3, 0), Point(3, 0)),
                    Passage(infos(3).id, Point(3, 6), Point(3, 6)),
                    Passage(infos(4).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = true,
                isExitPoint = true,
                Random.nextInt(20),
                10 * Random.nextDouble()
            ),
            CellViewedFromACell(
                infos(3),
                neighbors = List(infos(2), infos(6)),
                passages = List(
                    Passage(infos(2).id, Point(3, 0), Point(3, 0)),
                    Passage(infos(6).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                Random.nextInt(20),
                10 * Random.nextDouble() //Double.PositiveInfinity
            ),
            CellViewedFromACell(
                infos(4),
                neighbors = List(infos(2), infos(5), infos(6), infos(8)),
                passages = List(
                    Passage(infos(2).id, Point(0, 3), Point(0, 3)),
                    Passage(infos(5).id, Point(3, 0), Point(3, 0)),
                    Passage(infos(6).id, Point(3, 6), Point(3, 6)),
                    Passage(infos(8).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                Random.nextInt(20),
                10 * Random.nextDouble()
            ),
            CellViewedFromACell(
                infos(5),
                neighbors = List(infos(1), infos(4), infos(7)),
                passages = List(
                    Passage(infos(1).id, Point(0, 3), Point(0, 3)),
                    Passage(infos(4).id, Point(3, 6), Point(3, 6)),
                    Passage(infos(7).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = false,
                isExitPoint = true,
                Random.nextInt(20),
                10 * Random.nextDouble()
            ),
            CellViewedFromACell(
                infos(6),
                neighbors = List(infos(3), infos(4), infos(9)),
                passages = List(
                    Passage(infos(4).id, Point(0, 3), Point(0, 3)),
                    Passage(infos(3).id, Point(3, 0), Point(3, 0)),
                    Passage(infos(9).id, Point(3, 6), Point(3, 6))
                ),
                isEntryPoint = false,
                isExitPoint = true,
                Random.nextInt(20),
                10 * Random.nextDouble() //Double.PositiveInfinity//
            ),
            CellViewedFromACell(
                infos(7),
                neighbors = List(infos(8), infos(5)),
                passages = List(
                    Passage(infos(4).id, Point(0, 3), Point(0, 3)),
                    Passage(infos(8).id, Point(3, 6), Point(3, 6))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                Random.nextInt(20),
                10 * Random.nextDouble()
            ),
            CellViewedFromACell(
                infos(8),
                neighbors = List(infos(4), infos(7), infos(9)),
                passages = List(
                    Passage(infos(4).id, Point(0, 3), Point(0, 3)),
                    Passage(infos(7).id, Point(3, 0), Point(3, 0)),
                    Passage(infos(9).id, Point(3, 6), Point(3, 6))
                ),
                isEntryPoint = true,
                isExitPoint = true,
                Random.nextInt(20),
                10 * Random.nextDouble()
            ),
            CellViewedFromACell(
                infos(9),
                neighbors = List(infos(6), infos(8)),
                passages = List(
                    Passage(infos(6).id, Point(0, 3), Point(0, 3)),
                    Passage(infos(8).id, Point(3, 0), Point(3, 0))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                Random.nextInt(20),
                10 * Random.nextDouble()
            )
        ))
    val cells = areaForRoute.cells

    val asMap: Map[String, CellViewedFromACell] = HashMap(cells.map(c => c.info.name -> c): _*)
    
    val fromCell = "Cell1"
    val toCell = "Cell9"

    //    import cell.processor.route.algorithms.AStarSearch
    //    import cell.processor.route.algorithms.AStarSearch.Graph
    //
    //    areaForRoute.cells.foreach(c => println(c.infoCell.name -> c.practicabilityLevel))
    //
    //    val graph: Graph[String] =
    //        HashMap(
    //            cells.map(cell =>
    //                cell.infoCell.name ->
    //                    HashMap(cell.neighbors
    //                        // remove the source cell from it's respective neighbours
    //                        .filter(c => c.name != fromCell).map(c =>
    //                        c.name -> Math.max(0.0, 100.0 + asMap(c.name).practicabilityLevel - cell.practicabilityLevel)
    //                    ): _*)
    //            ): _*)
    //
    //    println(graph)
    //
    //    val (shp, cost) = AStarSearch.A_*(graph)(fromCell, toCell)(AStarSearch.Extractors.toList)
    //
    //    println(shp)
    
    implicit val system = ActorSystem("Arianna-Cluster")
    
    val routeManager = system.actorOf(Props[RouteManager], "RouteManager")
    
    routeManager ! AriadneMessage(
        Init,
        Init.Subtype.Greetings,
        Location.Cell >> Location.Self,
        Greetings(List.empty)
    )
    
    Thread.sleep(1000L)
    
    println("Sending request to manager...")
    
    routeManager ! AriadneMessage(
        Route,
        Route.Subtype.Info,
        Location.User >> Location.Cell,
        RouteInfo(
            RouteRequest(Random.nextInt().toString, infos(1), infos(9), isEscape = false),
            areaForRoute
        )
    )
    
    Thread.sleep(1000L)

    val areaForAlarm = AreaViewedFromACell(
        Random.nextInt(Int.MaxValue),
        List(
            CellViewedFromACell(
                infos(1),
                neighbors = List(infos(2), infos(5)),
                passages = List(
                    Passage(infos(2).id, Point(3, 6), Point(3, 6)),
                    Passage(infos(5).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                Random.nextInt(20),
                10 * Random.nextDouble()
            ),
            CellViewedFromACell(
                infos(2),
                neighbors = List(infos(1), infos(3), infos(4)),
                passages = List(
                    Passage(infos(1).id, Point(3, 0), Point(3, 0)),
                    Passage(infos(3).id, Point(3, 6), Point(3, 6)),
                    Passage(infos(4).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = true,
                isExitPoint = true,
                Random.nextInt(20),
                10 * Random.nextDouble()
            ),
            CellViewedFromACell(
                infos(3),
                neighbors = List(infos(2), infos(6)),
                passages = List(
                    Passage(infos(2).id, Point(3, 0), Point(3, 0)),
                    Passage(infos(6).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                Random.nextInt(20),
                10 * Random.nextDouble() //Double.PositiveInfinity
            ),
            CellViewedFromACell(
                infos(4),
                neighbors = List(infos(2), infos(5), infos(6), infos(8)),
                passages = List(
                    Passage(infos(2).id, Point(0, 3), Point(0, 3)),
                    Passage(infos(5).id, Point(3, 0), Point(3, 0)),
                    Passage(infos(6).id, Point(3, 6), Point(3, 6)),
                    Passage(infos(8).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                Random.nextInt(20),
                10 * Random.nextDouble()
            ),
            CellViewedFromACell(
                infos(5),
                neighbors = List(infos(1), infos(4), infos(7)),
                passages = List(
                    Passage(infos(1).id, Point(0, 3), Point(0, 3)),
                    Passage(infos(4).id, Point(3, 6), Point(3, 6)),
                    Passage(infos(7).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = false,
                isExitPoint = true,
                Random.nextInt(20),
                10 * Random.nextDouble()
            ),
            CellViewedFromACell(
                infos(6),
                neighbors = List(infos(3), infos(4), infos(9)),
                passages = List(
                    Passage(infos(4).id, Point(0, 3), Point(0, 3)),
                    Passage(infos(3).id, Point(3, 0), Point(3, 0)),
                    Passage(infos(9).id, Point(3, 6), Point(3, 6))
                ),
                isEntryPoint = false,
                isExitPoint = true,
                Random.nextInt(20),
                Double.PositiveInfinity //10*Random.nextDouble()//
            ),
            CellViewedFromACell(
                infos(7),
                neighbors = List(infos(8), infos(5)),
                passages = List(
                    Passage(infos(4).id, Point(0, 3), Point(0, 3)),
                    Passage(infos(8).id, Point(3, 6), Point(3, 6))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                Random.nextInt(20),
                10 * Random.nextDouble()
            ),
            CellViewedFromACell(
                infos(8),
                neighbors = List(infos(4), infos(7), infos(9)),
                passages = List(
                    Passage(infos(4).id, Point(0, 3), Point(0, 3)),
                    Passage(infos(7).id, Point(3, 0), Point(3, 0)),
                    Passage(infos(9).id, Point(3, 6), Point(3, 6))
                ),
                isEntryPoint = true,
                isExitPoint = true,
                Random.nextInt(20),
                10 * Random.nextDouble()
            ),
            CellViewedFromACell(
                infos(9),
                neighbors = List(infos(6), infos(8)),
                passages = List(
                    Passage(infos(6).id, Point(0, 3), Point(0, 3)),
                    Passage(infos(8).id, Point(3, 0), Point(3, 0))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                Random.nextInt(20),
                10 * Random.nextDouble()
            )
        ))
    
    println("Sending alarm to manager...")
    
    routeManager ! AriadneMessage(
        Route,
        Route.Subtype.Info,
        Location.User >> Location.Cell,
        RouteInfo(
            RouteRequest(Random.nextInt().toString, infos(9), InfoCell.empty, isEscape = true),
            areaForAlarm
        )
    )
}