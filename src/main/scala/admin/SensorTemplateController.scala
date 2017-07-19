package admin

import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.Label
import javafx.scene.text.Text

import ontologies.messages.Sensor

class SensorTemplateController extends Initializable {

    @FXML
    private var sensorName: Label = _

    @FXML
    private var sensorValue: Text = _

    var sensorId: Int = _

    def createSensor(sensor: Sensor): Unit = {
        sensorName.setText(assignSensorName(sensor.category));
    }

    def updateSensor(sensor: Sensor): Unit = {
        sensorValue.setText(sensor.value.toString)
    }

    private def assignSensorName(i: Int): String = i match {
        case 1 => "Temperature"
        case 2 => "Smoke"
        case 3 => "Humidity"
        case _ => ""
    }

    override def initialize(location: URL, resources: ResourceBundle): Unit = {}


}
