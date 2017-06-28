import spray.json.{DefaultJsonProtocol, _}

import scala.io.Source

final case class InfoCell(id: Int, name: String, uri: String)
final case class Sensor(id: Int, name: String, value: Double)

final case class Cell(infoCell: InfoCell,
                      sensors: List[Sensor],
                      neighbors: List[InfoCell],
                      isEntryPoint: Boolean,
                      isExitPoint: Boolean,
                      capacity: Int,
                      squareMeters: Double,
                      currentPeople: Int,
                      practicabilityLevel: Double)

final case class Maps(id: Int, areas: List[Cell])

object MyJsonProtocol extends DefaultJsonProtocol {
     implicit val infoCellFormat = jsonFormat3(InfoCell)
     implicit val sensorFormat = jsonFormat3(Sensor)
     implicit val areaFormat = jsonFormat9(Cell)
     implicit val mapsFormat = jsonFormat2(Maps)
}

import MyJsonProtocol._

object MainScala extends App {
     val filename = "res/map.json"
     val source = Source.fromFile(filename).getLines.mkString
     val json = source.parseJson
     val area = json.convertTo[Maps]
     println(area, json)
}