package admin

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
import scalafx.application.Platform

/**
  * This class represent the controller for each Cell template inside the interface
  */
class CellTemplateController extends Initializable {

    var adminActor: ActorRef = _
    private var cellInfo: CellInfo = _
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
      * @param cell : Cell object containing data
      *
      **/
    def setStaticInformation(cell: Room): Unit = {
        cellInfo = cell.info
        roomName setText cell.info.name
        maxCapacityValue setText cell.capacity.toString
        sqrMetersValue setText cell.squareMeters.toString
        if (cell.isEntryPoint) entranceValue setText "1" else entranceValue setText "0"
        if (cell.isExitPoint) exitValue setText "1" else exitValue setText "0"
    }

    /**
      * This method update the interface with dynamic information about the Cell; it's called everytime the Application receive
      * an update from the System
      *
      * @param cell : CellForView object containing only dynamic data
      * */
    def setDynamicInformation(cell: CellForView): Unit = {
        if (chartsButton.isDisabled) {
            chartsButton setDisable false
        }
        currentPeopleValue setText cell.currentPeople.toString
        cell.sensors.foreach(sensor => {
            println("Cerco il controller " + sensor.categoryId)
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
        sensorsInfo.sensors.foreach(sensor => {
            var loader = new FXMLLoader(getClass.getResource("/sensorTemplate.fxml"))
            var sensorTemplate = loader.load[HBox]
            val sensorController = loader.getController[SensorTemplateController]
            sensorController createSensor sensor
            println("Aggiungo il controller del sensore " + sensor.categoryId)
            sensorsController += ((sensor.categoryId, sensorController))
            sensorsContainer.getChildren add sensorTemplate
        })

    }

    /**
      * This method modifies the interface in order to show to the administrator that an Alarm arrived.
      *
      * */
    def handleAlarm(): Unit = {
        Platform.runLater {
            header setBackground new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY))
        }
    }

    /**
      * This method is called when the administrator clicks on the Chart button, in order to open the secondary
      * window with charts.
      **/
    def openCharts(): Unit = {
        adminActor ! AriadneMessage(Interface, Interface.Subtype.OpenChart, Location.Admin >> Location.Self, CellForChart(cellInfo, sensorsController.keys.toList))
        chartsButton setDisable true
    }

    /**
      * This method is called when the Chart button is enabled back, after closing the secondary window
      **/
    def enableChartButton(): Unit = {
        chartsButton setDisable false
    }
}
