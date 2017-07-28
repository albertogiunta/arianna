package admin

import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.{FXML, FXMLLoader, Initializable}
import javafx.scene.chart.{LineChart, XYChart}
import javafx.scene.control.{Label, TitledPane}
import javafx.scene.layout.GridPane

import ontologies.messages.{CellForView, Sensor}

import scala.collection.mutable
import scalafx.application.Platform


class ChartWindowController extends Initializable {

    @FXML
    private var mainPane: GridPane = _

    @FXML
    private var peopleChart: LineChart[Int, Int] = _

    @FXML
    private var cellName: Label = _

    private val sensorChartControllers: mutable.Map[Int, SensorChartController] = new mutable.HashMap[Int, SensorChartController]()

    private val data: XYChart.Series[Int, Int] = new XYChart.Series[Int, Int]

    private var time: Int = 0

    override def initialize(location: URL, resources: ResourceBundle): Unit = {}

    def initializeTitle(name: String): Unit = {
        cellName setText name
        peopleChart.getData add data
    }
    def initializeCharts(sensorsId: List[Int]): Unit = {
        val positions = List((1, 0), (0, 1), (1, 1), (0, 2), (1, 2)).iterator
        sensorsId.foreach(sensorId => {
            val loader = new FXMLLoader(getClass().getResource("/chartTemplate.fxml"));
            val template = loader.load[TitledPane]
            template setText Sensor.categoryName(sensorId)
            sensorChartControllers += ((sensorId, loader.getController[SensorChartController]))
            Platform.runLater {
                var position: (Int, Int) = positions.next()
                mainPane.add(template, position._1, position._2)
            }
        })
    }

    def updateCharts(update: CellForView): Unit = {
        Platform.runLater {
            if (data.getData.size().equals(25)) {
                data.getData.remove(0)
            }
            data.getData add new XYChart.Data(time, update.currentOccupation)
            time = time + 5
            update.sensors.foreach(sensor => {
                var controller = sensorChartControllers.get(sensor.category).get
                controller.addValue(sensor.value)
            })
        }

    }

}
