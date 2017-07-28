package admin

import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.{FXML, Initializable}
import javafx.scene.chart.{LineChart, XYChart}

import scalafx.application.Platform

class SensorChartController extends Initializable {

    @FXML
    private var chart: LineChart[Int, Double] = _

    private val data: XYChart.Series[Int, Double] = new XYChart.Series[Int, Double]

    private var time: Int = 0


    override def initialize(location: URL, resources: ResourceBundle): Unit = {
        Platform.runLater {
            chart.getData add data
            //chart.getXAxis setAutoRanging true
            //chart.getYAxis setAutoRanging true
        }
    }

    def addValue(value: Double): Unit = {
        if (data.getData.size().equals(25)) {
            data.getData.remove(0)
        }
        data.getData add new XYChart.Data(time, value)
        time = time + 5
    }
}
