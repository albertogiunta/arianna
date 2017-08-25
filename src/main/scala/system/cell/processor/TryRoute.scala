package system.cell.processor

import system.cell.processor.route.actors.RouteProcessor
import system.ontologies.messages._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Random

object TryRoute extends App {
    
    //    val path2Project: String = Paths.get("").toFile.getAbsolutePath
    //    val path2map: String = path2Project + "/res/json/map15_room.json"
    //
    //    val plan: String = Source.fromFile(new File(path2map)).getLines.mkString
    //    println(plan)
    //    val map: Area = plan.parseJson.convertTo[Area]
    //
    //    map.rooms.foreach(room => room.neighbors.foreach(n => println(room.info.id.name -> n.name)))
    //
    //    val mapViewedFromACell = AreaViewedFromACell(map)
    
    val infoCells: Map[Int, (CellInfo, RoomInfo)] = (1 to 9).map(i => i ->
        (
            CellInfo(uri = "uri/Cell1", "127.0.0.1", port = 25521),
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
                practicability = Double.PositiveInfinity
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
                practicability = 34
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
    
    val fut1 = RouteProcessor.computeRoute("Cell1", "Cell2", areaForAlarm.rooms)
    val res1 = Await.result(fut1, Duration.Inf)
    
    val fut2 = RouteProcessor.computeRoute("Cell1", "Cell8", areaForAlarm.rooms)
    val res2 = Await.result(fut2, Duration.Inf)
    
    println(res1._1.map(c => c.name) + " " + res1._2)
    println(res2._1.map(c => c.name) + " " + res2._2)
}