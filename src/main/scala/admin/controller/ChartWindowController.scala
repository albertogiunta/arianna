package admin.controller

import java.net.URL
import java.util.ResourceBundle
import javafx.application.Platform
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.chart.{LineChart, XYChart}
import javafx.scene.control.{Label, TitledPane}
import javafx.scene.layout.GridPane

import akka.actor.ActorRef
import ontologies.messages.Location._
import ontologies.messages.MessageType.Interface
import ontologies.messages._
import ontologies.sensor.SensorCategories

import scala.collection.mutable

/**
  * This is the Controller class for the external charts window.
  *
  **/

object ChartUtils {
    private val HEAD = 0
    private val MAX_DATA_ON_GRAPH = 20
    
    def resizeIfNeeded(data: XYChart.Series[Double, Double]): XYChart.Series[Double, Double] = {
        if (data.getData.size.equals(MAX_DATA_ON_GRAPH)) {
            data.getData remove HEAD
        }
        data
    }
    
}

class ChartWindowController extends ViewController {
    
    var chartActor: ActorRef = _
    @FXML
    private var mainPane: GridPane = _

    @FXML
    private var peopleChart: LineChart[Double, Double] = _

    @FXML
    private var cellName: Label = _

    private var roomInfo: RoomInfo = _

    private val sensorChartControllers: mutable.Map[Int, SensorChartController] = new mutable.HashMap[Int, SensorChartController]()
    
    private var data: XYChart.Series[Double, Double] = new XYChart.Series[Double, Double]

    private var time = (0 to Int.MaxValue - 1).iterator

    override def initialize(location: URL, resources: ResourceBundle): Unit = {
        Platform.runLater(() => peopleChart.getData add data)
    }

    /**
      * This method initializes the title of the window with the name of the room
      *
      * @param info : InfoCell containing the room data
      **/
    def initializeWindow(info: RoomInfo): Unit = {
        cellName setText info.id.name
        roomInfo = info
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
            val loader = new FXMLLoader(getClass.getResource(GraphicResources.chart));
            val template = loader.load[TitledPane]
            template setText SensorCategories.categoryWithId(sensorId).name
            sensorChartControllers += ((sensorId, loader.getController[SensorChartController]))
            var position: (Int, Int) = positions.next
            mainPane.add(template, position._1, position._2)

        })
    }

    /**
      * This method is called everytime the charts are updated with new values
      *
      * @param update : CellForView object containing information about the room and all
      *               the new values coming from the sensors.
      *
      * */
    def updateCharts(update: RoomDataUpdate): Unit = {
        update.cell.sensors.foreach(sensor => sensorChartControllers.get(sensor.categoryId).get.addValue(sensor.value))
        data = ChartUtils.resizeIfNeeded(data)
        data.getData add new XYChart.Data(time.next.toDouble, update.currentPeople.toDouble)

    }

    /**
      * This method is called when the secondary window is closed.
      **/
    def closeWindow(): Unit = {
        chartActor ! AriadneMessage(Interface, Interface.Subtype.CloseChart, Location.Admin >> Location.Self, roomInfo)
    }

}
