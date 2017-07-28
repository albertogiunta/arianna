package admin

import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.{FXML, FXMLLoader, Initializable}
import javafx.scene.chart.LineChart
import javafx.scene.control.TitledPane
import javafx.scene.layout.GridPane

import ontologies.messages.Sensor

import scala.collection.mutable
import scalafx.application.Platform


class ChartWindowController extends Initializable {

    @FXML
    private var mainPane: GridPane = _

    @FXML
    private var peopleChart: LineChart[Int, Int] = _

    private val sensorChartControllers: mutable.Map[Int, SensorChartController] = new mutable.HashMap[Int, SensorChartController]()

    override def initialize(location: URL, resources: ResourceBundle): Unit = {}

    def initializeCharts(sensorsId: List[Int]): Unit = {
        val positions = List((1, 0), (0, 1), (1, 1), (0, 2), (1, 2)).iterator
        sensorsId.foreach(sensorId => {
            Platform.runLater {
                val loader = new FXMLLoader(getClass().getResource("/chartTemplate.fxml"));
                val template = loader.load[TitledPane]
                template setText Sensor.categoryName(sensorId)
                sensorChartControllers += ((sensorId, loader.getController[SensorChartController]))
                var position: (Int, Int) = positions.next()
                mainPane.add(template, position._1, position._2)
            }
        })
    }

    def updateCharts(sensors: List[Sensor]): Unit = {
        sensors.foreach(sensor => sensorChartControllers.get(sensor.category).get.addValue(sensor.value))
    }

}
