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
                      practicabilityLevel: Double) extends MessageContent

final case class Passage(neighborId: Int,
                         startCoordinates: Point,
                         endCoordinates: Point) extends MessageContent

/**
  * This case class represent a user request of a Route from Cell X to Cell Y
  *
  * @param userID   The ID of the User requesting the Route
  * @param fromCell The Cell where the user is located
  * @param toCell   The Cell where the user have to arrive
  */
final case class RouteRequest(userID: String, fromCell: InfoCell, toCell: InfoCell) extends MessageContent

/**
  * This case class represent info sent to the RouteManager Actor from the CellCore Actor.
  *
  * This wrap a RouteRequest and add the topology on which calculating the route
  *
  * @param req      The RouteRequest sent by a User
  * @param topology The View of the Topology help by the CoreCell Actor
  */
final case class RouteInfo(req: RouteRequest, topology: AreaForCell) extends MessageContent

/**
  * This case class represent a response to a route request.
  *
  * This is sent to the CoreCell Actor by the RouteProcessor.
  *
  * @param request The wrapped RouteRequest
  * @param route   The calculated optimal route from Cell X to Cell Y
  */
final case class RouteResponse(request: RouteRequest, route: List[InfoCell]) extends MessageContent

/**
  * This case class is a RouteRequest but cause from the trigger of an alarm.
  *
  * As such there are no chosen starting point.
  *
  * Being triggered from inside the cluster or from the Admin, the Topology is already attached to
  * this Request
  *
  * @param info     The identification data of the cell from which calculatin the route (that is the starting/source point)
  * @param topology The actual view of the topology held by the CoreCell Actor
  */
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
  * @param practicabilityLevel
  */
final case class CellForCell(info: InfoCell,
                             neighbors: List[InfoCell],
                             passages: List[Passage],
                             isEntryPoint: Boolean,
                             isExitPoint: Boolean,
                             capacity: Int,
                             practicabilityLevel: Double) extends MessageContent

object CellForCell {
    def apply(cell: Cell): CellForCell =
        new CellForCell(cell.info, cell.neighbors, cell.passages, cell.isEntryPoint,
            cell.isExitPoint, cell.capacity, cell.practicabilityLevel)
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

/**
  * This case class is meant to be used as Update of the number of people present in the various rooms
  * for the Master node or for the Cell itself.
  *
  * Could've also been used the LightCell View but to the master practicability isn't necessary.
  *
  * @param info       The identification info of the cell sending the data.
  * @param actualLoad The actual number of people inside the Room where the cell is located into.
  */
final case class ActualLoadUpdate(info: InfoCell, actualLoad: Int) extends MessageContent

object ActualLoadUpdate {
    def apply(cell: Cell): ActualLoadUpdate = new ActualLoadUpdate(cell.info, cell.currentPeople)
}

/**
  * This case class represent a sensor.
  *
  * @param category The Category of the Sensor
  * @param value    The actual Value percieved from the sensor
  * @param min      The Minimum value for which the held value is not dangerous
  * @param max      The Maximum value for which the held value is not dangerous
  */
final case class Sensor(category: Int, value: Double, min: Double, max: Double) extends MessageContent

/**
  * This case Class is meant to be used as an Update of all the Sensors data
  * from a single Cell to the Master.
  *
  * @param info    The identification info of the Cells that is sending the Updates
  * @param sensors A list of sensors data
  */
final case class SensorList(info: InfoCell, sensors: List[Sensor]) extends MessageContent

object SensorList {
    def apply(cell: Cell): SensorList = new SensorList(cell.info, cell.sensors)
}

/**
  * This case Class is only meant to be used under the hood of a List into LightArea representations.
  *
  * This class give a simplified view of a Cell for other cells, contaning the new number of people
  * and the calculated practicability level for the cell to be updated
  *
  * @param info                The Identification Info of the Cell to be updated
  * @param practicabilityLevel The actual practicability level of the Room the Cell is located into
  */
case class LightCell(info: InfoCell, practicabilityLevel: Double) extends MessageContent

object LightCell {
    def apply(cell: Cell): LightCell = new LightCell(cell.info, cell.practicabilityLevel)
}

/**
  * This case class is meant to be used as Content to update Cells
  * about the updated Topology of the place from their PoV.
  *
  * It's a simplified view of the area.
  *
  * @param id    Identification number for this Topology
  * @param cells List of LightCells composing the Area
  */
case class LightArea(id: Int, cells: List[LightCell]) extends MessageContent // Actually not Used

object LightArea {
    def apply(area: Area): LightArea = new LightArea(area.id, area.cells.map(c => LightCell(c)))
}

/**
  * This case Class is meant to be used only under the hood of a List into UpdateForAdmin.
  *
  * This Class grant a simplified view of a Cell, only containing dynamic and identification Info of it
  *
  * @param info          Identification Info of the cell
  * @param currentPeople Actual number of people inside the Room
  * @param sensors       A List of Sensors containing the new values sensed by them
  */
final case class CellUpdate(info: InfoCell,
                            currentPeople: Int,
                            sensors: List[Sensor]) extends MessageContent

object CellUpdate {
    def apply(cell: Cell): CellUpdate = new CellUpdate(cell.info, cell.currentPeople, cell.sensors)
}

/**
  * This Case Class is meant to be used as a Content for Messages sent to the Admin Application,
  * in order to updates its information
  *
  * @param list A List of CellUpdates, containing a simplified view of a Cell
  */
final case class UpdateForAdmin(list: List[CellUpdate]) extends MessageContent

/**
  * This case class is meant to be used as a Content for Initialization Messages,
  * where the various arguments for an Actor are submitted as Strings
  *
  * This obviously doesn't permit to send complex objects inside of it,
  * but can always use .json.
  *
  * @param args Initialization Arguments as List of Strings
  */
final case class Greetings(args: List[String]) extends MessageContent

/**
  * This case class in meant to be sent as Content for Alarm sent by Cells to other Cells and the Master
  *
  * @param info         The Info of the Cell tha gave origin to the Alarm
  * @param isExitPoint  Is the Cell located in a Room with Exits?
  * @param isEntryPoint Is the Cell Located in a Room with Entries?
  */
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