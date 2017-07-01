import spray.json.{DefaultJsonProtocol, _}

import scala.io.Source

final case class NorthWest(x: Int, y: Int)

final case class NorthEast(x: Int, y: Int)

final case class SouthWest(x: Int, y: Int)

final case class SouthEast(x: Int, y: Int)

final case class Start(x: Int, y: Int)

final case class End(x: Int, y: Int)

final case class Coordinates(northWest: NorthWest,
                             northEast: NorthEast,
                             southWest: SouthWest,
                             southEast: SouthEast)

final case class InfoCell(id: Int,
                          uri: String,
                          name: String,
                          coordinates: Coordinates)

final case class InfoNeighbor(id: Int, uri: String)

final case class Passage(neighborId: Int,
                         startCoordinates: Start,
                         endCoordinates: End)

final case class Sensor(category: Int, value: Double)

final case class Cell(infoCell: InfoCell,
                      sensors: List[Sensor],
                      neighbors: List[InfoNeighbor],
                      passages: List[Passage],
                      isEntryPoint: Boolean,
                      isExitPoint: Boolean,
                      capacity: Int,
                      squareMeters: Double,
                      currentPeople: Int,
                      practicabilityLevel: Double)

final case class Area(cells: List[Cell])

object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val northWestFormat = jsonFormat2(NorthWest)
    implicit val northEasatFormat = jsonFormat2(NorthEast)
    implicit val southWestFormat = jsonFormat2(SouthWest)
    implicit val southEastFormat = jsonFormat2(SouthEast)
    implicit val startFormat = jsonFormat2(Start)
    implicit val endFormat = jsonFormat2(End)
    implicit val coordinatesFormat = jsonFormat4(Coordinates)
    implicit val infoCellFormat = jsonFormat4(InfoCell)
    implicit val infoNeighborFormat = jsonFormat2(InfoNeighbor)
    implicit val passageFormat = jsonFormat3(Passage)
    implicit val sensorFormat = jsonFormat2(Sensor)
    implicit val cellFormat = jsonFormat10(Cell)
    implicit val areaFormat = jsonFormat1(Area)
}

import MyJsonProtocol._

object MainScala extends App {
    private val filename = "res/map.json"
    private val source = Source.fromFile(filename).getLines.mkString
    private val json = source.parseJson
    val area = json.convertTo[Area]
    println(area)
}