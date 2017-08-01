package ontologies.messages

/**
  * A trait that represent a Content for Ariadne Messages
  *
  */
sealed trait MessageContent

final case class Point(var x: Int, var y: Int) extends MessageContent

final case class Coordinates(northWest: Point,
                             northEast: Point,
                             southWest: Point,
                             southEast: Point)

final case class Passage(neighborId: Int,
                         startCoordinates: Point,
                         endCoordinates: Point) extends MessageContent

/**
  * This Class is a Static representation of a Cell
  *
  * @param id              The Identifier of the Room where the Cell is Located
  * @param uri             The URI that point to this Cell
  * @param name            The name of this Cell
  * @param roomVertices    The spatial Vertices of the Room where the Cell is Located
  * @param antennaPosition The Position of the WiFi antenna inside the Room where the Cell is Located
  */
final case class InfoCell(id: Int,
                          uri: String,
                          name: String,
                          roomVertices: Coordinates,
                          antennaPosition: Point) extends MessageContent

object InfoCell {
    def empty: InfoCell =
        InfoCell(
            Int.MinValue,
            "", "",
            Coordinates(Point(0, 0), Point(0, 0), Point(0, 0), Point(0, 0)),
            Point(0, 0)
        )
}

/**
  * This class is a Static representation of a Room
  *
  * @param id              The Identifier of the Room where the Cell is Located
  * @param capacity        Tha Max number of person the Room can hold at one time
  * @param squareMeters    The area covered by the Room
  * @param roomVertices    The spatial Vertices of the Room
  * @param neighbors       Rooms near this Room
  * @param passages        Openings that lead to neighbor Rooms
  * @param isEntryPoint    If this Cell is an Exit
  * @param isExitPoint     If this Cell is an Entrance
  * @param antennaPosition The Position of the WiFi antenna inside the Room
  */
final case class Room(id: Int,
                      capacity: Int,
                      squareMeters: Double,
                      roomVertices: Coordinates,
                      neighbors: List[InfoCell],
                      passages: List[Passage],
                      isEntryPoint: Boolean,
                      isExitPoint: Boolean,
                      antennaPosition: Point) extends MessageContent

final case class Area(id: Int,
                      cells: List[Cell]) extends MessageContent

/**
  * This is a Complete representation of a Cell, whit both Dynamic and Static properties
  *
  * @param info           Static Info of a Cell
  * @param sensors        The sensors that are placed in the Room and attached to the Cell
  * @param neighbors      Cells near this cell
  * @param passages       Openings that lead to neighbor Cells
  * @param isEntryPoint   If this Cell is an Exit
  * @param isExitPoint    If this Cell is an Entrance
  * @param capacity       Tha Max number of person the Room where the Cell is located can hold at one time
  * @param squareMeters   The area covered by the Room
  * @param currentPeople  The current number of people occupying this Room
  * @param practicability How much likely you should walk through this Room
  */
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

/**
  * This case class represent a user request of a Route from Cell X to Cell Y
  *
  * @param userID   The ID of the User requesting the Route {-1 for Cells, 0 for Admin, n > 0 others}
  * @param fromCell The Cell where the user is located
  * @param toCell   The Cell where the user have to arrive, will an empty String for Escape Routes
  */
final case class RouteRequest(userID: String, fromCell: InfoCell, toCell: InfoCell, isEscape: Boolean) extends MessageContent

/**
  * This case class represent info sent to the RouteManager Actor from the CellCore Actor.
  *
  * This wrap a RouteRequest and add the topology on which calculating the route
  *
  * @param request  The RouteRequest sent by a User
  * @param topology The View of the Topology help by the CoreCell Actor
  */
final case class RouteInfo(request: RouteRequest, topology: AreaViewedFromACell) extends MessageContent

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
  * This case class represent a response to a route request that only contains the route (no additional metadata).
  *
  * This is sent to the UserManager Actor by the Vertx instance.
  *
  * @param route The calculated optimal route from Cell X to Cell Y
  */

final case class RouteResponseShort(route: List[InfoCell]) extends MessageContent


final case class RouteRequestShort(userID: String, fromCellId: Int, toCellId: Int, isEscape: Boolean) extends MessageContent

/**
  * View of the Topology from the Cell perspective
  *
  * @param id    Random ID value identifying the Topology
  * @param cells List of the Cells of the Topology
  */
final case class AreaViewedFromACell(id: Int, cells: List[CellViewedFromACell]) extends MessageContent

object AreaViewedFromACell {
    def apply(area: Area): AreaViewedFromACell = new AreaViewedFromACell(area.id, area.cells.map(c => CellViewedFromACell(c)))
}

/**
  * View of another Cell from the perspective of a Cell
  *
  * @param info           Static Info of a Cell
  * @param neighbors      Cells near this cell
  * @param passages       Openings that lead to neighbor Cells
  * @param isEntryPoint   If this Cell is an Exit
  * @param isExitPoint    If this Cell is an Entrance
  * @param practicability How much likely you should walk through this Room
  */
final case class CellViewedFromACell(info: InfoCell,
                                     neighbors: List[InfoCell],
                                     passages: List[Passage],
                                     isEntryPoint: Boolean,
                                     isExitPoint: Boolean,
                                     capacity: Int,
                                     practicability: Double) extends MessageContent

object CellViewedFromACell {
    def apply(cell: Cell): CellViewedFromACell =
        new CellViewedFromACell(cell.info, cell.neighbors, cell.passages, cell.isEntryPoint,
            cell.isExitPoint, cell.capacity, cell.practicability)
}

/**
  * View of a Cell from the Perspective of a User
  *
  * @param actorPath The URL pointing the Cell that the User have to connect to
  * @param info      Static Info of a Cell
  * @param neighbors Cells near this cell
  * @param passages  Openings that lead to neighbor Cells
  */
final case class CellForUser(actorPath: String,
                             info: InfoCell,
                             neighbors: List[InfoCell],
                             passages: List[Passage]) extends MessageContent // To be renamed CellViewedFromAUser

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
  * @param info          The identification info of the cell sending the data.
  * @param currentPeople The actual number of people inside the Room where the cell is located into.
  */
final case class CurrentPeopleUpdate(info: InfoCell, currentPeople: Int) extends MessageContent

object CurrentPeopleUpdate {
    def apply(cell: Cell): CurrentPeopleUpdate = new CurrentPeopleUpdate(cell.info, cell.currentPeople)
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

object Sensor {
    def categoryName(category: Int): String = category match {
        case 1 => "Temperature"
        case 2 => "Smoke"
        case 3 => "Humidity"
        case 4 => "Other"
        case _ => ""
    }
}

/**
  * This case Class is meant to be used as an Update of all the Sensors data
  * from a single Cell to the Master.
  *
  * @param info    The identification info of the Cells that is sending the Updates
  * @param sensors A list of sensors data
  */
final case class SensorsUpdate(info: InfoCell, sensors: List[Sensor]) extends MessageContent

object SensorsUpdate {
    def apply(cell: Cell): SensorsUpdate = new SensorsUpdate(cell.info, cell.sensors)
}

/**
  * This case Class is only meant to be used under the hood of a List into LightArea representations.
  *
  * This class give a simplified view of a Cell for other cells, contaning the new number of people
  * and the calculated practicability level for the cell to be updated
  *
  * @param info           The Identification Info of the Cell to be updated
  * @param practicability The actual practicability level of the Room the Cell is located into
  */
case class PracticabilityUpdate(info: InfoCell, practicability: Double) extends MessageContent

object PracticabilityUpdate {
    def apply(cell: Cell): PracticabilityUpdate = new PracticabilityUpdate(cell.info, cell.practicability)
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
case class AreaPracticability(id: Int, cells: List[PracticabilityUpdate]) extends MessageContent // Actually not Used

object AreaPracticability {
    def apply(area: Area): AreaPracticability = new AreaPracticability(area.id, area.cells.map(c => PracticabilityUpdate(c)))
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
final case class CellDataUpdate(info: InfoCell,
                                currentPeople: Int,
                                sensors: List[Sensor]) extends MessageContent // To be renamed CellData

object CellDataUpdate {
    def apply(cell: Cell): CellDataUpdate = new CellDataUpdate(cell.info, cell.currentPeople, cell.sensors)
}

/**
  * This Case Class is meant to be used as a Content for Messages sent to the Admin Application,
  * in order to updates its information
  *
  * @param list A List of CellUpdates, containing a simplified view of a Cell
  */
final case class UpdateForAdmin(list: List[CellDataUpdate]) extends MessageContent // to be renamed AdminUpdate

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

/**
  * An Empty message content
  *
  */
final case class Empty() extends MessageContent

final case class UserAndAntennaPositionUpdate(userPosition: Point, antennaPosition: Point) extends MessageContent

final case class AntennaPositions(userPosition: Point, antennaPositions: List[InfoCell]) extends MessageContent

final case class CellForSwitcher(info: InfoCell,
                                 neighbors: List[InfoCell]) extends MessageContent

object CellForSwitcher {
    def apply(cell: CellForUser): CellForSwitcher =
        new CellForSwitcher(cell.info, cell.neighbors)
}

/**
  * This Case Class is meant to be used as a Content for Messages inside the Admin System in order to update the view
  * with new data from a single cell
  *
  * @param id            : Int representing cell ID
  * @param name          : String containing cell name
  * @param currentPeople : Int values how the actual number of people inside the room
  * @param sensors       : list of Sensor object containing updated values coming from them
  **/
final case class CellForView(id: Int, name: String, currentPeople: Int, sensors: List[Sensor]) extends MessageContent

/**
  * This Case Class is meant to be used as a Content for Messages inside the Admin Systemi in order to initialize
  * the charts with information about a single cell
  *
  * @param info      : InfoCell object containing all information about the cell
  * @param sensorsId : list of Int representing the sensor ID inside the room
  *
  **/
final case class CellForChart(info: InfoCell, sensorsId: List[Int]) extends MessageContent
