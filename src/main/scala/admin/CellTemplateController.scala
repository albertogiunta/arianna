package admin

import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.{FXML, Initializable}
import javafx.scene.text.Text

import ontologies.messages.Cell

/**
  * Created by lisamazzini on 14/07/17.
  */
class CellTemplateController extends Initializable {

    var cellId: Int = _
    @FXML
    var roomName: Text = _
    @FXML
    var currentPeopleValue: Text = _
    @FXML
    var temperatureValue: Text = _
    @FXML
    var humidityValue: Text = _
    @FXML
    var smokeValue: Text = _
    @FXML
    var sensorYValue: Text = _
    @FXML
    var sensorXValue: Text = _
    @FXML
    var maxCapacityValue: Text = _
    @FXML
    var sqrMetersValue: Text = _
    @FXML
    var entranceValue: Text = _
    @FXML
    var exitValue: Text = _

    override def initialize(location: URL, resources: ResourceBundle): Unit = {}

    def setStaticInformation(cell: Cell): Unit = {
        cellId = cell.infoCell.id
        roomName.setText(cell.infoCell.name)
        maxCapacityValue.setText(cell.capacity.toString)
        sqrMetersValue.setText(cell.squareMeters.toString)
        if (cell.isEntryPoint) entranceValue.setText("1") else entranceValue.setText("0")
        if (cell.isExitPoint) exitValue.setText("1") else exitValue.setText("0")
    }

    def setDynamicInformation(cell: CellForView): Unit = {
        currentPeopleValue.setText(cell.currentOccupation.toString)
        temperatureValue.setText(cell.sensors.filter(s => s.categoryId == 0).head.value.toString)
        humidityValue.setText(cell.sensors.filter(s => s.categoryId == 1).head.value.toString)
        smokeValue.setText(cell.sensors.filter(s => s.categoryId == 2).head.value.toString)
    }
}
