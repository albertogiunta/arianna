package admin

import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.Label
import javafx.scene.text.Text

import ontologies.messages.Sensor

/**
  * This class represents the controller for the Sensor element inside the interface
  *
  **/
class SensorTemplateController extends Initializable {

    @FXML
    private var sensorName: Label = _

    @FXML
    private var sensorValue: Text = _

    private var sensorCategory: Int = _

    override def initialize(location: URL, resources: ResourceBundle): Unit = {}

    /**
      * This method initializes the sensor template
      *
      * @param sensor : Sensor object containing data
      **/
    def createSensor(sensor: Sensor): Unit = {
        sensorCategory = sensor.category
        sensorName.setText(assignSensorName());
    }

    /**
      * This method updates the current value of the sensor
      *
      * @param sensor : Sensor object containing the current value
      * */
    def updateSensor(sensor: Sensor): Unit = {
        sensorValue.setText(sensor.value.toString)
    }

    private def assignSensorName(): String = sensorCategory match {
        case 1 => "Temperature"
        case 2 => "Smoke"
        case 3 => "Humidity"
        case 4 => "Other"
        case _ => ""
    }

}
