package com.utils

import javafx.scene.chart.XYChart

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