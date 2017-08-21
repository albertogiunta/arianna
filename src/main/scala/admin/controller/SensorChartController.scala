package admin.controller

import java.net.URL
import java.util.ResourceBundle
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.chart.{LineChart, XYChart}

import com.utils.ChartUtils

/**
  * This is the Controller for the sensor chart, that initializes it and keep it updated.
  **/
class SensorChartController extends ViewController {

    @FXML
    private var chart: LineChart[Double, Double] = _
    
    private var data: XYChart.Series[Double, Double] = new XYChart.Series[Double, Double]

    private var time = (0 to Int.MaxValue - 1).iterator


    override def initialize(location: URL, resources: ResourceBundle): Unit = {
        Platform.runLater(() => chart.getData add data)
    }

    /**
      * This method adds a new value in the chart.
      *
      * @param value : new Double to add in the chart
      **/
    def addValue(value: Double): Unit = {
        data = ChartUtils.resizeIfNeeded(data)
        data.getData add new XYChart.Data(time.next, value)
    }
}
