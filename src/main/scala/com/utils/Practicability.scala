package com.utils

import java.io.File
import java.nio.file.Paths

import spray.json._
import system.ontologies.messages.AriannaJsonProtocol._
import system.ontologies.messages._

import scala.collection.mutable
import scala.io.Source

object Practicability {
    
    val log_b: (Double, Double) => Double = (b, n) => Math.log(n) / Math.log(b)
    
    def apply(capacity: Double, load: Double, flows: Double): Double = {
        if (flows == 0) Double.PositiveInfinity
        else load / capacity * (
            if (flows == 1) log_b(3.0, 4.5)
            else if (flows == 2) log_b(3.0, 3.0)
            else if (flows == 3.0) log_b(3.0, 2.65)
            else log_b(3.0, 2.15)
            )
    }
    
    def toWeight(from: Double, to: Double): Double = to
    
    def round(value: Double): Double = BigDecimal(value).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
}

object TryPracticability extends App {
    
    println(Practicability(100.0, 1.0, 2.0))
    
    val path2Project: String = Paths.get("").toFile.getAbsolutePath
    val path2map: String = path2Project + "/res/json/map15_room.json"
    
    val plan: String = Source.fromFile(new File(path2map)).getLines.mkString
    val map: Area = plan.parseJson.convertTo[Area]
    
    val mapViewedFromACell: mutable.Map[String, RoomViewedFromACell] =
        mutable.HashMap(AreaViewedFromACell(map).rooms.map(r => r.info.id.name -> r): _*)
    
    mapViewedFromACell += "Room A" -> mapViewedFromACell("Room A").copy(practicability = Practicability(100.0, 1.0, 2.0))
    
    val x = mutable.HashMap.empty[String, Double]
    
    x += "A" -> 666
    
    println(mapViewedFromACell("Room A").practicability)
    println(x)
}