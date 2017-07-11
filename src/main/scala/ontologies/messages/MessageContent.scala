package ontologies.messages

sealed trait MessageContent

/* Complete Topology for the server */
final case class InfoCell(id: Int,
                          uri: String,
                          name: String,
                          roomVertices: Coordinates,
                          antennaPosition: Point) extends MessageContent


final case class Point(var x: Int, var y: Int) extends MessageContent

final case class Coordinates(northWest: Point,
                             northEast: Point,
                             southWest: Point,
                             southEast: Point)

final case class Area(id: Int,
                      cells: List[Cell]) extends MessageContent

final case class Cell(infoCell: InfoCell,
                      sensors: List[Sensor],
                      neighbors: List[InfoCell],
                      passages: List[Passage],
                      isEntryPoint: Boolean,
                      isExitPoint: Boolean,
                      capacity: Int,
                      squareMeters: Double,
                      currentPeople: Int,
                      practicabilityLevel: Double) extends MessageContent

final case class Passage(neighborId: Int,
                         startCoordinates: Point,
                         endCoordinates: Point) extends MessageContent


/* Semi complete topology for the Cells */
final case class CellForSwitcher(infoCell: InfoCell,
                                 neighbors: List[InfoCell]) extends MessageContent

object CellForSwitcher {
    def apply(cell: CellForUser): CellForSwitcher =
        new CellForSwitcher(cell.infoCell, cell.neighbors)
}

final case class CellForCell(infoCell: InfoCell,
                             neighbors: List[InfoCell],
                             passages: List[Passage],
                             isEntryPoint: Boolean,
                             isExitPoint: Boolean,
                             practicabilityLevel: Double) extends MessageContent

object CellForCell {
    def apply(cell: Cell): CellForCell =
        new CellForCell(cell.infoCell, cell.neighbors, cell.passages, cell.isEntryPoint,
            cell.isExitPoint, cell.practicabilityLevel)
}

final case class AreaForCell(id: Int, cells: List[CellForCell]) extends MessageContent

object AreaForCell {
    def apply(area: Area): AreaForCell = new AreaForCell(area.id, area.cells.map(c => CellForCell(c)))
}

/* User point of view of a Cell*/
final case class CellForUser(actorPath: String,
                             infoCell: InfoCell,
                             neighbors: List[InfoCell],
                             passages: List[Passage]) extends MessageContent

object CellForUser {
    def apply(cell: Cell, actorPath: String): CellForUser =
        new CellForUser(actorPath, cell.infoCell, cell.neighbors, cell.passages)
}

/* Updates for the server from the Cells */
final case class ActualLoadUpdate(info: InfoCell, actualLoad: Int) extends MessageContent

object ActualLoadUpdate {
    def apply(cell: Cell): ActualLoadUpdate = new ActualLoadUpdate(cell.infoCell, cell.currentPeople)
}

final case class Sensor(category: Int, value: Double) extends MessageContent

final case class SensorList(info: InfoCell, sensors: List[Sensor]) extends MessageContent

object SensorList {
    
    def apply(cell: Cell): SensorList = new SensorList(cell.infoCell, cell.sensors)
}

/* Light weight Topology updates for Cells */
case class LightCell(info: InfoCell, actualLoad: Int, practicabilityLevel: Double) extends MessageContent

object LightCell {
    def apply(cell: Cell): LightCell = new LightCell(cell.infoCell, cell.currentPeople, cell.practicabilityLevel)
}

case class LightArea(id: Int, cells: List[LightCell]) extends MessageContent

object LightArea {
    def apply(area: Area): LightArea = new LightArea(area.id, area.cells.map(c => LightCell(c)))
}

/* Updates for the Admin Dashboard */
final case class CellUpdate(infoCell: InfoCell,
                            currentPeople: Int,
                            sensors: List[Sensor]) extends MessageContent

final case class UserAndAntennaPositionUpdate(userPosition: Point, antennaPosition: Point)

final case class AntennaPositions(userPosition: Point, antennaPositions: List[InfoCell])

final case class Empty()

object CellUpdate {
    def apply(cell: Cell): CellUpdate = new CellUpdate(cell.infoCell, cell.currentPeople, cell.sensors)
}

final case class UpdateForAdmin(list: List[CellUpdate]) extends MessageContent

//object UpdateForAdmin {
//    def apply(area: Area): UpdateForAdmin = new UpdateForAdmin(area.cells.map(c => CellUpdate(c)))
//}

/* General Content */
final case class Greetings(args: List[String]) extends MessageContent

final case class AlarmContent(info: InfoCell, isExitPoint: Boolean, isEntryPoint: Boolean) extends MessageContent

object AlarmContent {
    def apply(cell: Cell): AlarmContent = new AlarmContent(cell.infoCell, cell.isExitPoint, cell.isEntryPoint)
}

/* BOH */

final case class SampleUpdate(people: Int, temperature: Double) extends MessageContent