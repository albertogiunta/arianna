package area

import scala.collection.mutable.ListBuffer

final case class Point(var x: Int, var y: Int)

final case class Coordinates(northWest: Point,
                             northEast: Point,
                             southWest: Point,
                             southEast: Point)

final case class InfoCell(id: Int,
                          uri: String,
                          name: String,
                          roomVertices: Coordinates,
                          antennaPosition: Point)

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

final case class Area(cells: List[Cell])

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

    val cells: List[CellForCell] = area.cells.map(c => CellForCell(c))

}

final case class CellUpdate(cell: Cell){
    val infoCell = cell.infoCell
    val currentPeople = cell.currentPeople
    val sensors = cell.sensors
}
final case class UpdateForAdmin(list: ListBuffer[CellUpdate])

final case class SampleUpdate(people : Int, temperature : Double)