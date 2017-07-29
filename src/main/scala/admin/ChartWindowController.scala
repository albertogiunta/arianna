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

/**
  * This is the Controller class for the external charts window.
  *
  **/
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

    override def initialize(location: URL, resources: ResourceBundle): Unit = {
        Platform.runLater {
            peopleChart.getData add data
        }
    }

    /**
      * This method initializes the title of the window with the name of the room
      *
      * @param name : String containing the name of the room
      **/
    def initializeTitle(name: String): Unit = {
        cellName setText name
    }

    /**
      * This method initializes the charts based on the list of sensor id and add it to the
      * GridPane, after loading the template from .fxml file
      *
      * @param sensorsId : List of the sensors ID which are inside the room
      *
      * */
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

    /**
      * This method is called everytime the charts are updated with new values
      *
      * @param update : CellForView object containing information about the room and all
      *               the new values coming from the sensors.
      *
      * */
    def updateCharts(update: CellForView): Unit = {
        Platform.runLater {
            update.sensors.foreach(sensor => {
                var controller = sensorChartControllers.get(sensor.category).get
                controller.addValue(sensor.value)
            })
            if (data.getData.size().equals(20)) {
                data.getData.remove(0)
            }
            data.getData add new XYChart.Data(time, update.currentPeople)
            time = time + 5
        }

    }

}
