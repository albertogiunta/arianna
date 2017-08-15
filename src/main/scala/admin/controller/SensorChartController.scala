package admin.controller

import java.net.URL
import java.util.ResourceBundle
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.chart.{LineChart, XYChart}

/**
  * This is the Controller for the sensor chart, that initialized it and keep it updated.
  **/
class SensorChartController extends ViewController {

    @FXML
    private var chart: LineChart[Int, Double] = _

    private val data: XYChart.Series[Int, Double] = new XYChart.Series[Int, Double]

    private var time = (0 to Int.MaxValue - 1).iterator


    override def initialize(location: URL, resources: ResourceBundle): Unit = {
        Platform.runLater(() => chart.getData add data)
    }

    /**
      * This method adds a new value in the chart, and provides to keep only the last 20 values.
      *
      * @param value : new Double to add in the chart
      **/
    def addValue(value: Double): Unit = {
        if (data.getData.size.equals(20)) {
            data.getData.remove(0)
        }
        data.getData add new XYChart.Data(time.next, value)
    }
}
