import spray.json.DefaultJsonProtocol
import spray.json._
import scala.io.Source

final case class InfoArea(id: Int, name: String, uri: String)
final case class Sensor(id: Int, name: String, value: Double)
final case class Area(infoArea: InfoArea,
                      sensors: List[Sensor],
                      neighbors: List[InfoArea],
                      isEntryPoint: Boolean,
                      isExitPoint: Boolean,
                      capacity: Int,
                      squareMeters: Double,
                      currentPeople: Int,
                      practicabilityLevel: Double)
final case class Maps(id: Int, areas: List[Area])

object MyJsonProtocol extends DefaultJsonProtocol {
     implicit val infoAreaFormat = jsonFormat3(InfoArea)
     implicit val sensorFormat = jsonFormat3(Sensor)
     implicit val areaFormat = jsonFormat9(Area)
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