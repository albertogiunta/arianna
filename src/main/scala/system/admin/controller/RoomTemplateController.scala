package system.admin.controller

import javafx.fxml.{FXML, FXMLLoader}
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.layout._
import javafx.scene.paint.Color
import javafx.scene.text.Text

import akka.actor.ActorRef
import com.utils.{GraphicResources, InterfaceText}
import system.ontologies.messages.Location._
import system.ontologies.messages.MessageType.Interface
import system.ontologies.messages._

import scala.collection.mutable

/**
  * This class represent the controller for each Room template inside the interface
  */
class RoomTemplateController extends ViewController {

    var adminManager: ActorRef = _
    private var roomInfo: RoomInfo = _
    private val sensorsController: mutable.Map[Int, SensorTemplateController] = new mutable.HashMap[Int, SensorTemplateController]
    @FXML
    private var roomName: Text = _
    @FXML
    private var currentPeopleValue: Text = _
    @FXML
    private var sensorsContainer: VBox = _
    @FXML
    private var maxCapacityValue: Text = _
    @FXML
    private var sqrMetersValue: Text = _
    @FXML
    private var entranceValue: Text = _
    @FXML
    private var exitValue: Text = _
    @FXML
    private var header: Pane = _
    @FXML
    private var chartsButton: Button = _

    /**
      * This method fills the interface with static information about the Cell; it's called only one time when the map is loaded
      *
      * @param room : Room object containing data
      *
      **/
    def setStaticInformation(room: Room): Unit = {
        roomInfo = room.info
        setRoomInfo(roomInfo)
    }

    /**
      * This method update the interface with dynamic information about the Room; it's called everytime the Application receive
      * an update from the System
      *
      * @param update : RoomDataUpdate object containing only dynamic data
      * */
    def setDynamicInformation(update: RoomDataUpdate): Unit = {
        currentPeopleValue setText update.currentPeople.toString + "/" + maxCapacityValue.getText
        update.cell.sensors.foreach(sensor => {
            if (sensorsController.contains(sensor.categoryId)) {
                sensorsController(sensor.categoryId) updateSensor sensor
            }
        })
    }

    /**
      * This method fills the interface with data about sensors of the Room, once the Application has received it from the System.
      *
      * @param sensorsInfo : SensorsInfoUpdate object containing data
      *
      * */
    def addSensors(sensorsInfo: SensorsInfoUpdate): Unit = {
        chartsButton setDisable false
        sensorsInfo.sensors.foreach(sensor => loadSensor(sensor))

    }

    /**
      * This method modifies the interface in order to show to the administrator that an Alarm arrived.
      *
      * */
    def handleAlarm(): Unit = {
        header setBackground new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY))
    }

    /**
      * This method modifies the interface in order to show to the administrator that an Alarm ended.
      *
      **/
    def endAlarm(): Unit = {
        header setBackground new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY))
    }

    /**
      * This method is called when the administrator clicks on the Chart button, in order to open the secondary
      * window with charts.
      **/
    def openCharts(): Unit = {
        adminManager ! AriadneMessage(Interface, Interface.Subtype.OpenChart, Location.Admin >> Location.Self, CellForChart(roomInfo, sensorsController.keys.toList))
        chartsButton setDisable true
    }

    /**
      * This method is called when the Chart button is enabled back, after closing the secondary window
      **/
    def enableChartButton(): Unit = {
        chartsButton setDisable false
    }
    
    private def loadSensor(sensor: SensorInfo): Unit = {
        var loader = new FXMLLoader(getClass.getResource(GraphicResources.Sensor))
        var sensorTemplate = loader.load[HBox]
        val sensorController = loader.getController[SensorTemplateController]
        sensorController createSensor sensor
        sensorsController += ((sensor.categoryId, sensorController))
        sensorsContainer.getChildren add sensorTemplate
    }
    
    private def setRoomInfo(roomInfo: RoomInfo): Unit = {
        roomName setText roomInfo.id.name
        maxCapacityValue setText roomInfo.capacity.toString
        sqrMetersValue setText roomInfo.squareMeters.toString
        if (roomInfo.isEntryPoint) entranceValue setText InterfaceText.One else entranceValue setText InterfaceText.Zero
        if (roomInfo.isExitPoint) exitValue setText InterfaceText.One else exitValue setText InterfaceText.Zero
    }
}
