package admin.controller

import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.{FXML, Initializable}
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color

import ontologies.messages.{Coordinates, Point, Room}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Class representing the room starting from a Coordinates object,
  * with fields needed by JavaFX to draw a rectangle shape inside
  * the Canvas.
  *
  * @param roomVertices : Coordinates object containing Point vertices of the room
  *
  **/
private sealed case class RoomData(roomVertices: Coordinates, name: String) {
    val roomName: String = name
    val x: Int = roomVertices.northWest.x
    val y: Int = roomVertices.northWest.y
    val width: Int = roomVertices.northEast.x - roomVertices.northWest.x
    val height: Int = roomVertices.southWest.y - roomVertices.northWest.y
    var passages: ListBuffer[PassageLine] = new ListBuffer[PassageLine]
}

/**
  *
  * Class representing the line to draw for a Passage between rooms
  *
  * @param startPoint : starting point of the line
  * @param endPoint   : ending point of the line
  *
  **/

sealed case class PassageLine(startPoint: Point, endPoint: Point)

/**
  * This class is the Controller for the Canvas node inside the interface.
  *
  * */
class CanvasController extends Initializable {

    @FXML
    private var mapCanvas: Canvas = _

    private val rooms: mutable.Map[String, RoomData] = new mutable.HashMap[String, RoomData]

    override def initialize(location: URL, resources: ResourceBundle): Unit = {}

    /**
      * This method draws on the Canvas a rectangle representing the Cell passed as parameter
      *
      * @param room : cell to be drawn
      *
      * */
    def drawOnMap(room: Room): Unit = {
        val roomData: RoomData = RoomData(room.info.roomVertices, room.info.id.name)
        val passages: ListBuffer[PassageLine] = new ListBuffer[PassageLine]
        room.passages.foreach(passage => passages += PassageLine(passage.startCoordinates, passage.endCoordinates))
        roomData.passages = passages
        rooms += ((room.cell.info.uri, roomData))
        drawRoom(roomData, Color.WHITE)
        drawPassages(passages)
        drawAntenna(room.info.antennaPosition)
        drawName(roomData.name, new Point(roomData.x, roomData.y), Color.BLACK)
    }

    /**
      * This method handles the alarm, redrawing and marking on the interface the room from which the alarm comes
      *
      * @param id : id of the cell where the alarm comes from
      *
      * */
    def handleAlarm(id: String): Unit = {
        val roomData = rooms.get(id).get
        drawRoom(roomData, Color.RED)
        drawPassages(roomData.passages)
        drawName(roomData.name, new Point(roomData.x, roomData.y), Color.WHITE)
    }

    /**
      * This method handles the alarm, when it's launched by the administrator, redrawing all the map with
      * all rooms colored in red.
      *
      **/
    def handleAlarm(): Unit = {
        rooms.values.foreach(roomData => {
            drawRoom(roomData, Color.RED)
            drawPassages(roomData.passages)
        })
    }

    private def drawRoom(room: RoomData, color: Color): Unit = {
        val gc = mapCanvas.getGraphicsContext2D
        gc setStroke Color.BLACK
        gc setFill color
        gc setLineWidth 2.0
        gc strokeRect(room.x, room.y, room.width, room.height)
        gc fillRect(room.x, room.y, room.width, room.height)
    }

    private def drawPassages(lines: ListBuffer[PassageLine]): Unit = {
        val gc = mapCanvas.getGraphicsContext2D
        gc setStroke Color.LIGHTGRAY
        lines.foreach(line => gc strokeLine(line.startPoint.x, line.startPoint.y, line.endPoint.x, line.endPoint.y))
    }

    private def drawAntenna(point: Point): Unit = {
        val gc = mapCanvas.getGraphicsContext2D
        gc setStroke Color.GREEN
        gc strokeOval(point.x, point.y, 2.0, 2.0)
    }

    private def drawName(name: String, initialPoint: Point, color: Color): Unit = {
        val gc = mapCanvas.getGraphicsContext2D
        gc setFill color
        gc.fillText(name, initialPoint.x + 4, initialPoint.y + 15)
    }

}
