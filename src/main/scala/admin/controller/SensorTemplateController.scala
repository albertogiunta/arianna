package admin.controller

import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.Label
import javafx.scene.text.Text

import ontologies.messages.SensorInfo
import ontologies.sensor.SensorCategories

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
    def createSensor(sensor: SensorInfo): Unit = {
        sensorCategory = sensor.categoryId
        sensorName setText SensorCategories.categoryWithId(sensorCategory).name
    }

    /**
      * This method updates the current value of the sensor
      *
      * @param sensor : Sensor object containing the current value
      * */
    def updateSensor(sensor: SensorInfo): Unit = {
        sensorValue setText sensor.value.toString
    }


}
