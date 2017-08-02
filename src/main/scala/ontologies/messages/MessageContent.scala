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
  * This class is a static representation of
  * the basic configuration of a cell when it starts
  **/
final case class CellConfig(uri: String, sensors: List[SensorInfoFromConfig]) extends MessageContent


final case class Area(id: Int, rooms: List[Room]) extends MessageContent

/**
  * This class is a Dynamic representation on a Room
  *
  * @param info           The static representation of the room
  * @param cell           The Cell associated to this room
  * @param neighbors      The other rooms near this room
  * @param passages       The connections between this room the its neighbor
  * @param currentPeople  The current number of people in this room
  * @param practicability How much likely you should walk through this Room
  */
final case class Room(info: RoomInfo,
                      cell: Cell,
                      neighbors: List[RoomInfo],
                      passages: List[Passage],
                      currentPeople: Int,
                      practicability: Double) extends MessageContent

/**
  * This class is a Static representation of a Room
  *
  * @param id              The Identifier of the Room where the Cell is Located
  * @param roomVertices    The spatial Vertices of the Room
  * @param antennaPosition The Position of the WiFi antenna inside the Room
  * @param isEntryPoint    If this Cell is an Exit
  * @param isExitPoint     If this Cell is an Entrance
  * @param capacity        Tha Max number of person the Room can hold at one time
  * @param squareMeters    The area covered by the Room
  */
final case class RoomInfo(id: RoomID,
                          roomVertices: Coordinates,
                          antennaPosition: Point,
                          isEntryPoint: Boolean,
                          isExitPoint: Boolean,
                          capacity: Int,
                          squareMeters: Double) extends MessageContent

/**
  * This class rapresent a unique Identifier for a Room
  *
  * @param serial The Identifier of the Room where the Cell is Located
  * @param name   The name of the Room
  */
final case class RoomID(serial: Int, name: String)

object RoomID {
    def empty: RoomID = RoomID(Int.MinValue, "")
}

/**
  * This is a Complete representation of a Cell, whit both Dynamic and Static properties
  *
  * @param info    Static Info of a Cell
  * @param sensors The sensors that are placed in the Room and attached to the Cell
  */
final case class Cell(info: CellInfo, sensors: List[SensorInfo]) extends MessageContent

/**
  * This Class is a Static representation of a Cell
  *
  * @param uri  The URI that point to this Cell
  * @param port The port on which the cell is listening for user connections
  */
final case class CellInfo(uri: String, port: Int) extends MessageContent

object CellInfo {
    def empty: CellInfo = CellInfo("", Int.MinValue)
}

/**
  * This class wrap toeghter identification info for both a Room and a Cell
  *
  * @param room The room associated to the Cell
  * @param cell The cell associated to the room
  */
final case class RCInfo(room: RoomID, cell: CellInfo) extends MessageContent

object RCInfo {
    
    def empty: RCInfo = RCInfo(RoomID.empty, CellInfo.empty)
    
    def apply(room: Room): RCInfo = new RCInfo(room.info.id, room.cell.info)
    
    def apply(room: RoomViewedFromACell): RCInfo = new RCInfo(room.info.id, room.cell)
    
    def apply(room: RoomViewedFromAUser): RCInfo = new RCInfo(room.info.id, room.cell)
}

/**
  * This case class represent a user request of a Route from Cell X to Cell Y
  *
  * @param userID   The ID of the User requesting the Route {-1 for Cells, 0 for Admin, n > 0 others}
  * @param fromCell The Cell where the user is located
  * @param toCell   The Cell where the user have to arrive, will an empty String for Escape Routes
  */
final case class RouteRequest(userID: String, fromCell: RoomID, toCell: RoomID, isEscape: Boolean) extends MessageContent

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
final case class RouteResponse(request: RouteRequest, route: List[RoomID]) extends MessageContent

/**
  * This case class represent a response to a route request that only contains the route (no additional metadata).
  *
  * This is sent to the UserManager Actor by the Vertx instance.
  *
  * @param route The calculated optimal route from Cell X to Cell Y
  */

final case class RouteResponseShort(route: List[RoomID]) extends MessageContent


final case class RouteRequestShort(userID: String, fromCellId: Int, toCellId: Int, isEscape: Boolean) extends MessageContent

/**
  * View of the Topology from the Cell perspective
  *
  * @param id    Random ID value identifying the Topology
  * @param rooms List of the Cells of the Topology
  */
final case class AreaViewedFromACell(id: Int, rooms: List[RoomViewedFromACell]) extends MessageContent

object AreaViewedFromACell {
    def apply(area: Area): AreaViewedFromACell = new AreaViewedFromACell(area.id, area.rooms.map(c => RoomViewedFromACell(c)))
}

/**
  * View of another Cell from the perspective of a Cell
  *
  * @param cell           Static Info of a Cell
  * @param neighbors      Cells near this cell
  * @param passages       Openings that lead to neighbor Cells
  * @param practicability How much likely you should walk through this Room
  */
final case class RoomViewedFromACell(info: RoomInfo,
                                     cell: CellInfo,
                                     neighbors: List[RoomInfo],
                                     passages: List[Passage],
                                     practicability: Double) extends MessageContent

object RoomViewedFromACell {
    def apply(room: Room): RoomViewedFromACell =
        new RoomViewedFromACell(room.info, room.cell.info, room.neighbors, room.passages, room.practicability)
}

/**
  * View of a Cell from the Perspective of a User
  *
  * @param actorPath The URL pointing the Cell that the User have to connect to
  * @param info      Static Info of a Cell
  * @param neighbors Cells near this cell
  * @param passages  Openings that lead to neighbor Cells
  */
final case class RoomViewedFromAUser(actorPath: String,
                                     info: RoomInfo,
                                     cell: CellInfo,
                                     neighbors: List[RoomInfo],
                                     passages: List[Passage]) extends MessageContent // To be renamed CellViewedFromAUser

object RoomViewedFromAUser {
    def apply(room: Room, actorPath: String): RoomViewedFromAUser =
        new RoomViewedFromAUser(actorPath, room.info, room.cell.info, room.neighbors, room.passages)
}

/**
  * This case class is meant to be used as Update of the number of people present in the various rooms
  * for the Master node or for the Cell itself.
  *
  * Could've also been used the LightCell View but to the master practicability isn't necessary.
  *
  * @param cell          The identification info of the cell sending the data.
  * @param currentPeople The actual number of people inside the Room where the cell is located into.
  */
final case class CurrentPeopleUpdate(cell: CellInfo, currentPeople: Int) extends MessageContent

object CurrentPeopleUpdate {
    def apply(room: Room): CurrentPeopleUpdate = new CurrentPeopleUpdate(room.cell.info, room.currentPeople)
}

/**
  * This case class represent a sensor.
  *
  * @param categoryId The Category of the Sensor
  * @param value      The actual Value percieved from the sensor
  */
final case class SensorInfo(categoryId: Int, value: Double) extends MessageContent

/**
  * This is the information that a cell load during the start up
  * configuration relative to a sensor that must be initialized
  *
  * @param categoryId the sensor category
  * @param minValue   the minimum level reachable by the sensor
  * @param maxValue   the minimum level reachable by the sensor
  * @param threshold  the threshold of the sensor
  **/
final case class SensorInfoFromConfig(categoryId: Int, minValue: Double, maxValue: Double, threshold: ThresholdInfo) extends MessageContent

/**
  * An abstract representation of information relative
  * to a threshold of a generic sensor
  **/
abstract class ThresholdInfo extends MessageContent

/**
  * The concrete representation of a threshold of a sensor
  * that has a single threshold level
  *
  * @param value the threshold level
  **/
final case class SingleThresholdInfo(value: Double) extends ThresholdInfo


/**
  * The concrete representation of a threshold of a sensor
  * that has a two threshold level
  *
  * @param lowThreshold  the lowest level that the program's logic consider as normal value for the sensor
  * @param highThreshold the highest level that the program's logic consider as normal value for the sensor
  **/
final case class DoubleThresholdInfo(lowThreshold: Double, highThreshold: Double) extends ThresholdInfo

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
  * @param cell    The identification info of the Cells that is sending the Updates
  * @param sensors A list of sensors data
  */
final case class SensorsInfoUpdate(cell: CellInfo, sensors: List[SensorInfo]) extends MessageContent

object SensorsInfoUpdate {
    def apply(room: Room): SensorsInfoUpdate = new SensorsInfoUpdate(room.cell.info, room.cell.sensors)
}

/**
  * This case Class is only meant to be used under the hood of a List into LightArea representations.
  *
  * This class give a simplified view of a Cell for other cells, contaning the new number of people
  * and the calculated practicability level for the cell to be updated
  *
  * @param cell           The Identification Info of the Cell to be updated
  * @param practicability The actual practicability level of the Room the Cell is located into
  */
case class PracticabilityUpdate(cell: CellInfo, practicability: Double) extends MessageContent

object PracticabilityUpdate {
    def apply(room: Room): PracticabilityUpdate =
        new PracticabilityUpdate(room.cell.info, room.practicability)
}

/**
  * This case Class is meant to be used only under the hood of a List into UpdateForAdmin.
  *
  * This Class grant a simplified view of a Cell, only containing dynamic and identification Info of it
  *
  * @param cell          Identification Info of the cell and the room
  * @param currentPeople Actual number of people inside the Room
  *
  */
final case class RoomDataUpdate(cell: Cell,
                                currentPeople: Int) extends MessageContent // To be renamed CellData

object RoomDataUpdate {
    def apply(room: Room): RoomDataUpdate =
        new RoomDataUpdate(room.cell, room.currentPeople)
}

/**
  * This Case Class is meant to be used as a Content for Messages sent to the Admin Application,
  * in order to updates its information
  *
  * @param list A List of CellUpdates, containing a simplified view of a Cell
  */
final case class AdminUpdate(list: List[RoomDataUpdate]) extends MessageContent // to be renamed AdminUpdate

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
    def apply(area: Area): AreaPracticability = new AreaPracticability(area.id, area.rooms.map(c => PracticabilityUpdate(c)))
}

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
  * @param info The info of the cell associated to the room that had the alarm triggered
  * @param room The room where the Alarm has originated
  */
final case class AlarmContent(info: CellInfo, room: RoomInfo) extends MessageContent

object AlarmContent {
    def apply(room: Room): AlarmContent = new AlarmContent(room.cell.info, room.info)
}

/**
  * An Empty message content
  *
  */
final case class Empty() extends MessageContent

/**
  * This Case Class is meant to be used as a Content for Messages inside the Admin System in order to update the view
  * with new data from a single cell
  *
  * @param id            : Int representing cell ID
  * @param name          : String containing cell name
  * @param currentPeople : Int values how the actual number of people inside the room
  * @param sensors       : list of Sensor object containing updated values coming from them
  **/
final case class CellForView(id: Int, name: String, currentPeople: Int, sensors: List[SensorInfo]) extends MessageContent

/**
  * This Case Class is meant to be used as a Content for Messages inside the Admin Systemi in order to initialize
  * the charts with information about a single cell
  *
  * @param cell      : RCInfo object containing all information about the cell
  * @param sensorsId : list of Int representing the sensor ID inside the room
  *
  **/
final case class CellForChart(cell: CellInfo, sensorsId: List[Int]) extends MessageContent