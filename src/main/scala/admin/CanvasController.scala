package admin

import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.{FXML, Initializable}
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color

import ontologies.messages.{Cell, Coordinates, Point}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

sealed case class Room(roomVertices: Coordinates) {
    val x: Double = roomVertices.southWest.x
    val y: Double = roomVertices.southWest.y
    val width: Double = roomVertices.northEast.x - roomVertices.northWest.x
    val height: Double = roomVertices.northWest.y - roomVertices.southWest.y
}

sealed case class PassageLine(startPoint: Point, endPoint: Point)

class CanvasController extends Initializable {

    @FXML
    private var mapCanvas: Canvas = _

    private val rooms: mutable.Map[Int, (Room, ListBuffer[PassageLine])] = new mutable.HashMap[Int, (Room, ListBuffer[PassageLine])]

    override def initialize(location: URL, resources: ResourceBundle): Unit = {}

    def drawOnMap(cell: Cell): Unit = {
        val room: Room = Room(cell.info.roomVertices)
        val passages: ListBuffer[PassageLine] = new ListBuffer[PassageLine]
        cell.passages.foreach(passage => {
            passages += PassageLine(passage.startCoordinates, passage.endCoordinates)
            //gc strokeLine(passage.startCoordinates.x, passage.startCoordinates.y, passage.endCoordinates.x, passage.endCoordinates.y)
        })

        rooms += ((cell.info.id, (room, passages)))
        drawRoom(room, Color.WHITE)
        drawPassages(passages)
    }

    def handleAlarm(id: Int): Unit = {
        val roomData = rooms.get(id).get
        drawRoom(roomData._1, Color.RED)
        drawPassages(roomData._2)
    }

    private def drawRoom(room: Room, color: Color): Unit = {
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

}
