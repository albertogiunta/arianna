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

final case class Cell(info: InfoCell,
                      sensors: List[Sensor],
                      neighbors: List[InfoCell],
                      passages: List[Passage],
                      isEntryPoint: Boolean,
                      isExitPoint: Boolean,
                      capacity: Int,
                      squareMeters: Double,
                      currentPeople: Int,
                      practicability: Double) extends MessageContent

final case class Passage(neighborId: Int,
                         startCoordinates: Point,
                         endCoordinates: Point) extends MessageContent

/* Route content data structures */
final case class RouteRequestLight(userID: String, fromCellId: Int, toCellId: Int) extends MessageContent

final case class RouteRequest(userID: String, fromCell: InfoCell, toCell: InfoCell) extends MessageContent

final case class RouteInfo(req: RouteRequest, topology: AreaForCell) extends MessageContent

final case class RouteResponse(request: RouteRequest, route: List[InfoCell]) extends MessageContent

final case class RouteResponseShort(route: List[InfoCell]) extends MessageContent

final case class EscapeRequest(info: InfoCell, topology: AreaForCell) extends MessageContent

final case class EscapeResponse(info: InfoCell, route: List[InfoCell]) extends MessageContent

/**
  * View of the Topology from the Cell perspective
  *
  * @param id    Random ID value identifying the Topology
  * @param cells List of the Cells of the Topology
  */
final case class AreaForCell(id: Int, cells: List[CellForCell]) extends MessageContent

object AreaForCell {
    def apply(area: Area): AreaForCell = new AreaForCell(area.id, area.cells.map(c => CellForCell(c)))
}

/**
  * View of another Cell from the perspective of a Cell
  *
  * @param info
  * @param neighbors
  * @param passages
  * @param isEntryPoint
  * @param isExitPoint
  * @param practicability
  */
final case class CellForCell(info: InfoCell,
                             neighbors: List[InfoCell],
                             passages: List[Passage],
                             isEntryPoint: Boolean,
                             isExitPoint: Boolean,
                             practicability: Double) extends MessageContent

object CellForCell {
    def apply(cell: Cell): CellForCell =
        new CellForCell(cell.info, cell.neighbors, cell.passages, cell.isEntryPoint,
            cell.isExitPoint, cell.practicability)
}

/**
  * View of a Cell from the Perspective of a User
  *
  * @param actorPath
  * @param info
  * @param neighbors
  * @param passages
  */
final case class CellForUser(actorPath: String,
                             info: InfoCell,
                             neighbors: List[InfoCell],
                             passages: List[Passage]) extends MessageContent

object CellForUser {
    def apply(cell: Cell, actorPath: String): CellForUser =
        new CellForUser(actorPath, cell.info, cell.neighbors, cell.passages)
}

/* Updates for the server from the Cells */
final case class ActualLoadUpdate(info: InfoCell, actualLoad: Int) extends MessageContent

object ActualLoadUpdate {
    def apply(cell: Cell): ActualLoadUpdate = new ActualLoadUpdate(cell.info, cell.currentPeople)
}

final case class Sensor(category: Int, value: Double) extends MessageContent

final case class SensorList(info: InfoCell, sensors: List[Sensor]) extends MessageContent

object SensorList {
    def apply(cell: Cell): SensorList = new SensorList(cell.info, cell.sensors)
}

/* Light weight Topology updates for Cells */
case class LightCell(info: InfoCell, actualLoad: Int, practicabilityLevel: Double) extends MessageContent

object LightCell {
    def apply(cell: Cell): LightCell = new LightCell(cell.info, cell.currentPeople, cell.practicability)
}

case class LightArea(id: Int, cells: List[LightCell]) extends MessageContent

object LightArea {
    def apply(area: Area): LightArea = new LightArea(area.id, area.cells.map(c => LightCell(c)))
}

/* Updates for the Admin Dashboard */
final case class CellUpdate(info: InfoCell,
                            currentPeople: Int,
                            sensors: List[Sensor]) extends MessageContent

object CellUpdate {
    def apply(cell: Cell): CellUpdate = new CellUpdate(cell.info, cell.currentPeople, cell.sensors)
}

final case class UpdateForAdmin(list: List[CellUpdate]) extends MessageContent

/* General Content */
final case class Greetings(args: List[String]) extends MessageContent

final case class AlarmContent(info: InfoCell, isExitPoint: Boolean, isEntryPoint: Boolean) extends MessageContent

object AlarmContent {
    def apply(cell: Cell): AlarmContent = new AlarmContent(cell.info, cell.isExitPoint, cell.isEntryPoint)
}

/* BOH */
final case class SampleUpdate(people: Int, temperature: Double) extends MessageContent

final case class UserAndAntennaPositionUpdate(userPosition: Point, antennaPosition: Point) extends MessageContent

final case class AntennaPositions(userPosition: Point, antennaPositions: List[InfoCell]) extends MessageContent

final case class Empty() extends MessageContent

final case class CellForSwitcher(info: InfoCell,
                                 neighbors: List[InfoCell]) extends MessageContent

object CellForSwitcher {
    def apply(cell: CellForUser): CellForSwitcher =
        new CellForSwitcher(cell.info, cell.neighbors)
}