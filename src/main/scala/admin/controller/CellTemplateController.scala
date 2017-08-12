package admin.controller

import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.{FXML, FXMLLoader, Initializable}
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.layout._
import javafx.scene.paint.Color
import javafx.scene.text.Text

import akka.actor.ActorRef
import ontologies.messages.Location._
import ontologies.messages.MessageType.Interface
import ontologies.messages._

import scala.collection.mutable

/**
  * This class represent the controller for each Cell template inside the interface
  */
class CellTemplateController extends Initializable {

    var adminActor: ActorRef = _
    private val ONE: String = "1"
    private val ZERO: String = "0"
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

    override def initialize(location: URL, resources: ResourceBundle): Unit = {}

    /**
      * This method fills the interface with static information about the Cell; it's called only one time when the map is loaded
      *
      * @param room : Cell object containing data
      *
      **/
    def setStaticInformation(room: Room): Unit = {
        roomInfo = room.info
        roomName setText room.info.id.name
        maxCapacityValue setText room.info.capacity.toString
        sqrMetersValue setText room.info.squareMeters.toString
        if (room.info.isEntryPoint) entranceValue setText ONE else entranceValue setText ZERO
        if (room.info.isExitPoint) exitValue setText ONE else exitValue setText ZERO
    }

    /**
      * This method update the interface with dynamic information about the Cell; it's called everytime the Application receive
      * an update from the System
      *
      * @param update : CellForView object containing only dynamic data
      * */
    def setDynamicInformation(update: RoomDataUpdate): Unit = {
        currentPeopleValue setText update.currentPeople.toString
        update.cell.sensors.foreach(sensor => {
            sensorsController.get(sensor.categoryId).get updateSensor sensor
        })
    }

    /**
      * This method fills the interface with data about sensors of the Cell, once the Application has received it from the System.
      *
      * @param sensorsInfo : SensorsInfoUpdate object containing data
      *
      * */
    def addSensors(sensorsInfo: SensorsInfoUpdate): Unit = {
        chartsButton setDisable false
        sensorsInfo.sensors.foreach(sensor => {
            var loader = new FXMLLoader(getClass.getResource(GraphicResources.sensor))
            var sensorTemplate = loader.load[HBox]
            val sensorController = loader.getController[SensorTemplateController]
            sensorController createSensor sensor
            sensorsController += ((sensor.categoryId, sensorController))
            sensorsContainer.getChildren add sensorTemplate
        })

    }

    /**
      * This method modifies the interface in order to show to the administrator that an Alarm arrived.
      *
      * */
    def handleAlarm(): Unit = {
        header setBackground new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY))
    }

    /**
      * This method is called when the administrator clicks on the Chart button, in order to open the secondary
      * window with charts.
      **/
    def openCharts(): Unit = {
        adminActor ! AriadneMessage(Interface, Interface.Subtype.OpenChart, Location.Admin >> Location.Self, CellForChart(roomInfo, sensorsController.keys.toList))
        chartsButton setDisable true
    }

    /**
      * This method is called when the Chart button is enabled back, after closing the secondary window
      **/
    def enableChartButton(): Unit = {
        chartsButton setDisable false
    }
}
