package admin

import java.io.File
import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.{FXML, FXMLLoader, Initializable}
import javafx.scene.canvas.Canvas
import javafx.scene.control.{Button, SplitPane}
import javafx.scene.layout.{Pane, VBox}
import javafx.scene.text.Text

import akka.actor.ActorRef
import ontologies.messages.Location._
import ontologies.messages.MessageType.Topology
import ontologies.messages._

import scala.collection.mutable
import scala.io.Source
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

/**
  * This class contains only the piece of Cell information that the interface needs in order to update the View.
  *
  **/
sealed case class CellForView(id: Int, name: String, currentOccupation: Int, sensors: List[Sensor])

/**
  * This is the main controller for the interface of the Application
  *
  **/
class InterfaceController extends Initializable {
    var actorRef: ActorRef = _
    var interfaceView: InterfaceView = _
    private val cellControllers: mutable.Map[Int, CellTemplateController] = new mutable.HashMap[Int, CellTemplateController]
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

    /**
      * This methods updates the interface with the information about all the cells
      *
      * @param update : this is a List of CellForView containing the updated data
      **/
    def updateView(update: List[CellForView]): Unit = {
        Platform.runLater {
            update.foreach(cell => {
                var cellController = cellControllers.get(cell.id).get
                cellController setDynamicInformation cell
            })
        }
    }

    /**
      * This method is called to upload the map file from the interface (accepting only Json files)
      *
      * */
    @FXML
    def handleFileLoad(): Unit = {
        val fc = new FileChooser()
        fc.title = "Get JSON"
        fc.extensionFilters += (new ExtensionFilter("JSON Files", "*.json"))
        val json: File = fc.showOpenDialog(null)
        parseFile(json)
    }

    /**
      * This method add all the information about sensors of a single Cell after getting it from the System
      *
      * @param sensorsInfo : SensorUpdate object containing sensors data of a cell
      *
      **/
    def initializeSensors(sensorsInfo: SensorsUpdate): Unit = {
        var cellController = cellControllers.get(sensorsInfo.info.id).get
        cellController addSensors sensorsInfo
    }

    /**
      * This method is called when an Alarm comes from the System and provides to show it to the interface
      *
      * @param alarmContent : AlarmContent object that contains information about the Cell from which the alarm comes
      **/
    def triggerAlarm(alarmContent: AlarmContent): Unit = {
        actorRef ! new AriadneMessage(MessageType.Alarm, MessageType.Alarm.Subtype.FromInterface, Location.Admin >> Location.Self, Empty())
        println("Allarme ricevuto dal controller")
        cellControllers.get(alarmContent.info.id).get.handleAlarm
        canvasController handleAlarm alarmContent.info.id

        //Fai qualcosa all'interfaccia
    }

    /**
      * This method is called when the administrator press the Alarm button on the interface.
      *
      **/
    def triggerAlarm(): Unit = {
        //Allarme lanciato dall'interfaccia
    }

    private def parseFile(file: File): Unit = {
        val source = Source.fromFile(file).getLines.mkString
        val area = Topology.Subtype.Planimetrics.unmarshal(source)
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
        mapContainer.getChildren += canvas
    }

    private def createCells(initialConfiguration: List[Cell]) = {
        nRooms.text = initialConfiguration.size.toString
        initialConfiguration.foreach(cell => {
            Platform.runLater {
                var node = createCellTemplate(cell)
                vBoxPane.getChildren += node
                canvasController drawOnMap cell
            }
        })
    }

    private def createCellTemplate(cell: Cell): SplitPane = {
        var loader = new FXMLLoader(getClass.getResource("/cellTemplate2.fxml"))
        var node = loader.load[SplitPane]
        var controller = loader.getController[CellTemplateController]
        cellControllers += ((cell.info.id, controller))
        Platform.runLater {
            controller setStaticInformation cell
        }
        node
    }


}
