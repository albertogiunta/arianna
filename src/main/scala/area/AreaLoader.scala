package area

class AreaLoader {
}

import akka.actor.ActorRef
import spray.json.{DefaultJsonProtocol, _}

import scala.collection.mutable.ListBuffer
import scala.io.Source

final case class Point(x: Int, y: Int)

final case class Coordinates(northWest: Point,
                             northEast: Point,
                             southWest: Point,
                             southEast: Point)

final case class InfoCell(id: Int,
                          uri: String,
                          name: String,
                          roomVertices: Coordinates,
                          antennaPosition: Point)

final case class InfoNeighbor(id: Int, uri: String)

final case class Passage(neighborId: Int,
                         startCoordinates: Point,
                         endCoordinates: Point)

final case class Sensor(category: Int, value: Double)

final case class Cell(infoCell: InfoCell,
                      sensors: List[Sensor],
                      neighbors: List[InfoCell],
                      passages: List[Passage],
                      isEntryPoint: Boolean,
                      isExitPoint: Boolean,
                      capacity: Int,
                      squareMeters: Double,
                      currentPeople: Int,
                      practicabilityLevel: Double)

final case class Area(cells: ListBuffer[Cell])

final case class CellForUser(cell: Cell, cellActorRef: ActorRef) {

    val actorRef: ActorRef = cellActorRef
    val infoCell: InfoCell = cell.infoCell
    val neighbors: List[InfoCell] = cell.neighbors
    val passages: List[Passage] = cell.passages

}

final case class CellForCell(cell: Cell) {

    val infoCell: InfoCell = cell.infoCell
    val neighbors: List[InfoCell] = cell.neighbors
    val passages: List[Passage] = cell.passages
    val isEntryPoint: Boolean = cell.isEntryPoint
    val isExitPoint: Boolean = cell.isExitPoint
    val practicabilityLevel: Double = cell.practicabilityLevel

}

final case class AreaForCell(area: Area) {

    val cells: ListBuffer[CellForCell] = area.cells.map(c => CellForCell(c))

}

final case class CellUpdate(cell: Cell){
    val infoCell = cell.infoCell
    val currentPeople = cell.currentPeople
    val sensors = cell.sensors
}
final case class UpdateForAdmin(list: ListBuffer[CellUpdate])

final case class SampleUpdate(people : Int, temperature : Double)

object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val northWestFormat = jsonFormat2(Point)
    implicit val northEasatFormat = jsonFormat2(Point)
    implicit val southWestFormat = jsonFormat2(Point)
    implicit val southEastFormat = jsonFormat2(Point)
    implicit val startFormat = jsonFormat2(Point)
    implicit val endFormat = jsonFormat2(Point)
    implicit val coordinatesFormat = jsonFormat4(Coordinates)
    implicit val infoCellFormat = jsonFormat5(InfoCell)
    implicit val infoNeighborFormat = jsonFormat2(InfoNeighbor)
    implicit val passageFormat = jsonFormat3(Passage)
    implicit val sensorFormat = jsonFormat2(Sensor)
    implicit val cellFormat = jsonFormat10(Cell)
    //    implicit val areaFormat = jsonFormat1(Area)
    //    implicit val cellForUserFormat = jsonFormat2(CellForUser)
}

import area.MyJsonProtocol._

object AreaLoader {

    var area: Area = null

    private def readJson(filename: String): JsValue = {
        val source = Source.fromFile(filename).getLines.mkString
        source.parseJson
    }

    def loadCell(cellName: String): Cell = {
        val cell = readJson(s"res/json/cell/$cellName.json").convertTo[Cell]
        cell
    }

    def loadArea(a : Area): Unit = {
        //        area = readJson("res/json/map.json").convertTo[Area]
        //area = Area(new ListBuffer[Cell])
        area = a
    }

    def areaForCell: AreaForCell = {
        AreaForCell(area)
    }

}