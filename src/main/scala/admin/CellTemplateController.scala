package admin

import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.{FXML, FXMLLoader, Initializable}
import javafx.scene.layout.{HBox, VBox}
import javafx.scene.text.Text

import ontologies.messages.{Cell, SensorsUpdate}

import scala.collection.mutable
import scalafx.application.Platform

/**
  * Created by lisamazzini on 14/07/17.
  */
class CellTemplateController extends Initializable {

    var sensorsController: mutable.Map[Int, SensorTemplateController] = new mutable.HashMap[Int, SensorTemplateController]
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


    override def initialize(location: URL, resources: ResourceBundle): Unit = {}

    def setStaticInformation(cell: Cell): Unit = {
        roomName setText cell.info.name
        maxCapacityValue setText cell.capacity.toString
        sqrMetersValue setText cell.squareMeters.toString
        if (cell.isEntryPoint) entranceValue setText "1" else entranceValue setText "0"
        if (cell.isExitPoint) exitValue setText "1" else exitValue setText "0"
    }

    def setDynamicInformation(cell: CellForView): Unit = {
        currentPeopleValue setText cell.currentOccupation.toString
        cell.sensors.foreach(sensor => {
            val controller = sensorsController.get(sensor.category).get
            controller updateSensor sensor
        })
    }

    def addSensors(sensorsInfo: SensorsUpdate): Unit = {
        Platform.runLater {
            sensorsInfo.sensors.foreach(sensor => {
                var loader = new FXMLLoader(getClass.getResource("/sensorTemplate.fxml"))
                var sensorTemplate = loader.load[HBox]
                val sensorController = loader.getController[SensorTemplateController]
                sensorController createSensor sensor
                sensorsContainer.getChildren.add(sensorTemplate)
            })
        }
        //TODO : ordina gli elementi sull'id
        //sensorsContainer.getChildren.sort()
    }
}
