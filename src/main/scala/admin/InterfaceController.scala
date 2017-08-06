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
import ontologies.messages.MessageType.{Init, Topology}
import ontologies.messages._

import scala.collection.mutable
import scala.io.Source
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

/**
  * This is the main controller for the interface of the Application
  *
  **/
class InterfaceController extends Initializable {
    var interfaceActor: ActorRef = _
    var interfaceView: InterfaceView = _
    private val cellControllers: mutable.Map[RoomID, CellTemplateController] = new mutable.HashMap[RoomID, CellTemplateController]
    private var canvasController: CanvasController = _

    @FXML
    private var fileName: Text = _
    @FXML
    private var loadButton: Button = _
    @FXML
    private var alarmButton: Button = _
    @FXML
    private var closeButton: Button = _
    @FXML
    private var vBoxPane: VBox = _
    @FXML
    private var mapContainer: Pane = _

    override def initialize(location: URL, resources: ResourceBundle): Unit = {
        println("Controller initialized")
    }

    /**
      * This method updates the interface with the information about all the cells
      *
      * @param update : this is a List of CellForView containing the updated data
      **/
    def updateView(update: List[RoomDataUpdate]): Unit = {
        Platform.runLater {
            if (alarmButton.isDisabled) {
                alarmButton setDisable false
            }
            update.foreach(update => {
                println(update.room.toString)
                var cellController = cellControllers.get(update.room).get
                cellController setDynamicInformation update
            })
        }
    }

    /**
      * This method is called to upload the map file from the interface (accepting only Json files)
      *
      * */
    @FXML
    def handleFileLoad(): Unit = {
        loadButton.disable = true
        val fc = new FileChooser()
        fc.title = "Get JSON"
        fc.extensionFilters += (new ExtensionFilter("JSON Files", "*.json"))
        val json: File = fc.showOpenDialog(null)
        parseFile(json)
    }

    @FXML
    def handleClosing(): Unit = {
        interfaceActor ! AriadneMessage(Init, Init.Subtype.Goodbyes, Location.Admin >> Location.Self, new Empty)
    }

    /**
      * This method add all the information about sensors of a single Cell after getting it from the System
      *
      * @param sensorsInfo : SensorsInfoUpdate object containing sensors data of a cell
      *
      **/
    def initializeSensors(sensorsInfo: SensorsInfoUpdate, roomID: RoomID): Unit = {
        var cellController = cellControllers.get(roomID).get
        Platform.runLater {
            cellController addSensors sensorsInfo
        }
    }

    /**
      * This method is called when an Alarm comes from the System and provides to show it to the interface
      *
      * @param alarmContent : AlarmContent object that contains information about the Cell from which the alarm comes
      **/
    def triggerAlarm(alarmContent: AlarmContent): Unit = {
        interfaceActor ! new AriadneMessage(MessageType.Alarm, MessageType.Alarm.Subtype.FromInterface, Location.Admin >> Location.Self, Empty())
        cellControllers.get(alarmContent.room.id).get.handleAlarm
        canvasController handleAlarm alarmContent.info.uri
    }

    /**
      * This method is called when the administrator press the Alarm button on the interface.
      *
      **/
    def triggerAlarm(): Unit = {
        //Allarme lanciato dall'interfaccia
    }

    /**
      * This method enables the Chart button when the secondary window is closed
      *
      **/
    def enableButton(cellId: RoomID): Unit = {
        cellControllers.get(cellId).get.enableChartButton
    }

    private def parseFile(file: File): Unit = {
        val source = Source.fromFile(file).getLines.mkString
        val area = Topology.Subtype.Planimetrics.unmarshal(source)
        interfaceActor ! AriadneMessage(MessageType.Topology, MessageType.Topology.Subtype.Planimetrics, Location.Admin >> Location.Self, area)
        loadCanvas()
        createCells(area.rooms)
        fileName.text = file.getName
    }

    private def loadCanvas(): Unit = {
        var loader = new FXMLLoader(getClass.getResource("/canvasTemplate.fxml"))
        var canvas = loader.load[Canvas]
        canvasController = loader.getController[CanvasController]
        mapContainer.getChildren += canvas
    }

    private def createCells(initialConfiguration: List[Room]) = {
        initialConfiguration.foreach(cell => {
            Platform.runLater {
                var node = createCellTemplate(cell)
                vBoxPane.getChildren += node
                canvasController drawOnMap cell
            }
        })
    }

    private def createCellTemplate(cell: Room): SplitPane = {
        var loader = new FXMLLoader(getClass.getResource("/cellTemplate2.fxml"))
        var node = loader.load[SplitPane]
        var controller = loader.getController[CellTemplateController]
        controller.adminActor = interfaceActor
        cellControllers += ((cell.info.id, controller))
        Platform.runLater {
            controller setStaticInformation cell
        }
        node
    }


}
