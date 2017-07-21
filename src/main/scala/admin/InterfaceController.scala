package admin

import java.io.File
import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.{FXML, FXMLLoader, Initializable}
import javafx.scene.canvas.Canvas
import javafx.scene.control.{Button, SplitPane}
import javafx.scene.layout.{HBox, Pane, VBox}
import javafx.scene.text.Text

import akka.actor.ActorRef
import ontologies.messages.Location._
import ontologies.messages._

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

final case class CellForView(id: Int, name: String, currentOccupation: Int, sensors: List[Sensor])

class InterfaceController extends Initializable {
    var actorRef: ActorRef = _
    var interfaceView: InterfaceView = _
    private val cellControllers: ListBuffer[CellTemplateController] = new ListBuffer[CellTemplateController]
    private var canvasController: CanvasController = _

    @FXML
    var nRooms: Text = _
    @FXML
    var fileName: Text = _
    @FXML
    var loadButton: Button = _
    @FXML
    var alarmButton: Button = _
    @FXML
    var vBoxPane: VBox = _
    @FXML
    var mapContainer: Pane = _

    override def initialize(location: URL, resources: ResourceBundle): Unit = {
        println("Controller initialized")
    }

    def updateView(update: List[CellForView]): Unit = {
        Platform.runLater {
            update.foreach(c => {
                var cellController = cellControllers.filter(controller => controller.cellId.equals(c.id)).head
                cellController.setDynamicInformation(c)
            })
        }
    }

    @FXML
    def handleFileLoad(): Unit = {
        val fc = new FileChooser()
        fc.setTitle("Get JSON")
        fc.getExtensionFilters.add(new ExtensionFilter("JSON Files", "*.json"))
        val json: File = fc.showOpenDialog(null)
        parseFile(json)
    }

    private def parseFile(file: File): Unit = {
        val source = Source.fromFile(file).getLines.mkString
        val area = MessageType.Topology.Subtype.Planimetrics.unmarshal(source)
        actorRef ! AriadneMessage(MessageType.Topology, MessageType.Topology.Subtype.Planimetrics, Location.Admin >> Location.Self, area)
        loadCanvas()
        createCells(area.cells)
        fileName.text = file.getName
        loadButton.disable = true
    }

    private def loadCanvas(): Unit = {
        var loader = new FXMLLoader(getClass.getResource("/canvasTemplate.fxml"))
        var canvas = loader.load[Canvas]
        canvasController = loader.getController[CanvasController]
        mapContainer.getChildren.add(canvas)
    }

    private def createCells(initialConfiguration: List[Cell]) = {
        nRooms.text = initialConfiguration.size.toString
        initialConfiguration.foreach(c => {
            Platform.runLater {
                var node = createCellTemplate(c)
                vBoxPane.getChildren.add(node)
                canvasController.drawOnMap(c)
            }
        })
    }

    def initializeSensors(sensorsInfo: SensorList): Unit = {
        var cellController = cellControllers.filter(c => c.cellId.equals(sensorsInfo.info.id)).head
        Platform.runLater {
            sensorsInfo.sensors.foreach(s => {
                var loader = new FXMLLoader(getClass.getResource("/sensorTemplate.fxml"))
                var sensor = loader.load[HBox]
                val sensorController = loader.getController[SensorTemplateController]
                sensorController.sensorCategory = s.category
                cellController.sensorsController += sensorController
                cellController.addSensorTemplate(sensor, s)
            })
        }
    }

    private def createCellTemplate(c: Cell): SplitPane = {
        var loader = new FXMLLoader(getClass.getResource("/cellTemplate2.fxml"))
        var node = loader.load[SplitPane]
        var controller = loader.getController[CellTemplateController]
        cellControllers += controller
        Platform.runLater {
            controller.setStaticInformation(c)
        }
        node
    }

    def triggerAlarm(): Unit = {
        actorRef ! new AriadneMessage(MessageType.Alarm, MessageType.Alarm.Subtype.FromInterface, Location.Admin >> Location.Self, Empty())
        println("Allarme ricevuto dal controller")
        //Fai qualcosa all'interfaccia
    }

}
