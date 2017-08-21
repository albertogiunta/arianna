package system.cell.processor

import java.io.File
import java.nio.file.Paths

import spray.json._
import system.cell.processor.route.actors.RouteProcessor
import system.ontologies.messages.AriannaJsonProtocol._
import system.ontologies.messages.{Area, AreaViewedFromACell}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.Source

object TryRoute extends App {
    
    val path2Project: String = Paths.get("").toFile.getAbsolutePath
    val path2map: String = path2Project + "/res/json/map15_room.json"
    
    val plan: String = Source.fromFile(new File(path2map)).getLines.mkString
    println(plan)
    val map: Area = plan.parseJson.convertTo[Area]
    
    map.rooms.foreach(room => room.neighbors.foreach(n => println(room.info.id.name -> n.name)))
    
    val mapViewedFromACell = AreaViewedFromACell(map)
    
    val fut = RouteProcessor.computeRoute("Room A", "Room E", mapViewedFromACell.rooms)
    val res = Await.result(fut, Duration.Inf)
    
    println(res._1.map(c => c.name) + " " + res._2)
}