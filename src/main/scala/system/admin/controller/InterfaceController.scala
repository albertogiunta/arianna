package system.admin.controller

import java.io.File
import javafx.application.Platform
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.canvas.Canvas
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.{Alert, Button, SplitPane}
import javafx.scene.layout.{Pane, VBox}
import javafx.scene.paint.{Color, Paint}
import javafx.scene.text.Text
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter

import akka.actor.ActorRef
import com.utils.{GraphicResources, InterfaceText}
import system.admin.view.InterfaceView
import system.ontologies.messages.Location._
import system.ontologies.messages.MessageType.{Alarm, Init, Topology}
import system.ontologies.messages._

import scala.collection.mutable
import scala.io.Source

/**
  * This is the main controller for the interface of the Application
  *
  **/
class InterfaceController extends ViewController {
    var interfaceActor: ActorRef = _
    var interfaceView: InterfaceView = _
    private val cellControllers: mutable.Map[RoomID, RoomTemplateController] = new mutable.HashMap[RoomID, RoomTemplateController]
    private var canvasController: CanvasController = _

    @FXML
    private var fileName: Text = _
    @FXML
    private var loadButton: Button = _
    @FXML
    private var alarmButton: Button = _
    @FXML
    private var vBoxPane: VBox = _
    @FXML
    private var mapContainer: Pane = _
    @FXML
    private var status: Text = _

    /**
      * This method updates the interface with the information about all the cells
      *
      * @param update : this is a List of CellForView containing the updated data
      **/
    def updateView(update: List[RoomDataUpdate]): Unit = {
        Platform.runLater(() => {
            checkStatus()
            update.foreach(update => {
                if (cellControllers.contains(update.room)) {
                    cellControllers(update.room) setDynamicInformation update
                }
            })
        })
    }

    /**
      * This method is called to upload the map file from the interface (accepting only Json files)
      *
      * */
    @FXML
    def handleFileLoad(): Unit = {
        loadButton setDisable true
        val fc = new FileChooser
        fc setTitle InterfaceText.FileSelectionText
        fc setSelectedExtensionFilter new ExtensionFilter(InterfaceText.Extension, "*.json")
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
        checkStatus()
        Platform.runLater(() => cellControllers(roomID) addSensors sensorsInfo)
    }

    /**
      * This method is called when an Alarm comes from the System and provides to show it to the interface
      *
      * @param alarmContent : AlarmContent object that contains information about the Cell from which the alarm comes
      **/
    def triggerAlarm(alarmContent: AlarmContent): Unit = {
        Platform.runLater(() => {
            cellControllers(alarmContent.room.id).handleAlarm()
            canvasController handleAlarm alarmContent.info.uri
            alarmButton setText InterfaceText.EndAlarm
            alarmButton setOnAction ((e) => endAlarm())
        })

    }

    /**
      * This method is called when the administrator press the Alarm button on the interface.
      *
      **/
    @FXML
    def triggerAlarm(): Unit = {
        interfaceActor ! AriadneMessage(MessageType.Alarm, MessageType.Alarm.Subtype.FromInterface, Location.Admin >> Location.Self, Empty())
        Platform.runLater(() => {
            cellControllers.values.foreach(cellController => cellController.handleAlarm())
            canvasController.handleAlarm()
        })
        alarmButton setText InterfaceText.EndAlarm
        alarmButton setOnAction ((e) => endAlarm())
    }

    /**
      * This method enables the Chart button when the secondary window is closed
      *
      **/
    def enableButton(cellId: RoomID): Unit = {
        Platform.runLater(() => cellControllers(cellId).enableChartButton())
    }

    /**
      * This method shows an alert dialog when the System already has a map and
      * another map is loaded from the GUI.
      *
      **/
    def showErrorDialog(): Unit = {
        Platform.runLater(() => {
            cleanInterface()
            openAlert()
            loadButton setDisable false

        })
    }

    /**
      * This method set the Status text value, showing if the Master node is connected to the application
      **/
    def connected(isConnected: Boolean): Unit = {
        Platform.runLater(() => {
            if (isConnected) {
                setStatus(InterfaceText.Connected, Color.GREEN)
            } else {
                setStatus(InterfaceText.Disconnected, Color.RED)
            }
        })

    }

    private def parseFile(file: File): Unit = {
        val source = Source.fromFile(file).getLines.mkString
        val area = Topology.Subtype.Planimetrics unmarshal source
        Platform.runLater(() => {
            loadCanvas()
            createCells(area.rooms)
            fileName setText file.getName
            interfaceActor ! AriadneMessage(MessageType.Topology, MessageType.Topology.Subtype.Planimetrics, Location.Admin >> Location.Self, area)
        })
    }

    private def loadCanvas(): Unit = {
        val loader = new FXMLLoader(getClass.getResource(GraphicResources.Canvas))
        val canvas = loader.load[Canvas]
        canvasController = loader.getController[CanvasController]
        mapContainer.getChildren add canvas
    }

    private def createCells(initialConfiguration: List[Room]): Unit = {
        initialConfiguration.foreach(cell => {
            val node = createCellTemplate(cell)
                vBoxPane.getChildren add node
                canvasController drawOnMap cell
            })
    }

    private def createCellTemplate(cell: Room): SplitPane = {
        val loader = new FXMLLoader(getClass.getResource(GraphicResources.Cell))
        val node = loader.load[SplitPane]
        val controller = loader.getController[RoomTemplateController]
        controller.adminManager = interfaceActor
        cellControllers += ((cell.info.id, controller))
        controller setStaticInformation cell
        node
    }

    private def cleanInterface(): Unit = {
        cellControllers.clear
        vBoxPane.getChildren.clear()
        canvasController.cleanCanvas()
        fileName setText InterfaceText.None
    }

    private def endAlarm(): Unit = {
        Platform.runLater(() => {
            interfaceActor ! AriadneMessage(Alarm, Alarm.Subtype.End, Location.Admin >> Location.Self, Empty())
            alarmButton setText InterfaceText.SendAlarm
            alarmButton setOnAction ((e) => triggerAlarm())
            canvasController.redrawMap()
            cellControllers.values.foreach(cellController => cellController.endAlarm())
        })
    }

    private def openAlert(): Unit = {
        val alert = new Alert(AlertType.ERROR)
        alert setTitle InterfaceText.ErrorTitle
        alert setHeaderText InterfaceText.ErrorHeader
        alert setContentText InterfaceText.ErrorText
        alert.showAndWait
    }

    private def checkStatus(): Unit = {
        if (status.getText.equals(InterfaceText.Disconnected)) {
            connected(true)
        }
        if (alarmButton.isDisabled) {
            alarmButton setDisable false
        }
    }

    private def setStatus(text: String, color: Paint): Unit = {
        status setFill color
        status setText text
    }


}
