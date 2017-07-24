package admin

import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.{FXML, Initializable}
import javafx.scene.layout.{HBox, VBox}
import javafx.scene.text.Text

import ontologies.messages.{Cell, SensorInfo}

import scala.collection.mutable.ListBuffer

/**
  * Created by lisamazzini on 14/07/17.
  */
class CellTemplateController extends Initializable {

    var cellId: Int = _

    var sensorsController: ListBuffer[SensorTemplateController] = new ListBuffer[SensorTemplateController]
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
        cellId = cell.info.id
        roomName.setText(cell.info.name)
        maxCapacityValue.setText(cell.capacity.toString)
        sqrMetersValue.setText(cell.squareMeters.toString)
        if (cell.isEntryPoint) entranceValue.setText("1") else entranceValue.setText("0")
        if (cell.isExitPoint) exitValue.setText("1") else exitValue.setText("0")
    }

    def setDynamicInformation(cell: CellForView): Unit = {
        currentPeopleValue.setText(cell.currentOccupation.toString)
        cell.sensors.foreach(s => {
            val controller = sensorsController.filter(c => c.sensorCategory.equals(s.categoryId)).head
            controller.updateSensor(s)
        })
    }

    def addSensorTemplate(sensorTemplate: HBox, sensor: SensorInfo): Unit = {
        val controller = sensorsController.filter(c => c.sensorCategory.equals(sensor.categoryId)).head
        controller.createSensor(sensor)
        sensorsContainer.getChildren.add(sensorTemplate)
        //TODO : ordina gli elementi sull'id
        //sensorsContainer.getChildren.sort()
    }
}
