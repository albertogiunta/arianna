package admin

import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.{FXML, Initializable}
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color

import ontologies.messages.{Cell, Coordinates}

sealed case class Room(roomVertices: Coordinates) {
    val x: Double = roomVertices.southWest.x
    val y: Double = roomVertices.southWest.y
    val width: Double = roomVertices.northEast.x - roomVertices.northWest.x
    val height: Double = roomVertices.northWest.y - roomVertices.southWest.y
}

class CanvasController extends Initializable {

    @FXML
    var mapCanvas: Canvas = _

    override def initialize(location: URL, resources: ResourceBundle): Unit = {}

    def drawOnMap(cell: Cell): Unit = {
        val room: Room = Room(cell.info.roomVertices)
        val gc = mapCanvas.getGraphicsContext2D
        gc setStroke Color.BLACK
        gc setLineWidth 2.0
        gc strokeRect(room.x, room.y, room.width, room.height)
        gc setStroke Color.WHITE
        cell.passages.foreach(passage => {
            gc strokeLine(passage.startCoordinates.x, passage.startCoordinates.y, passage.endCoordinates.x, passage.endCoordinates.y)
        })
    }

}
