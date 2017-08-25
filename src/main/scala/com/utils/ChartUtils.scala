package com.utils

import javafx.scene.chart.XYChart

/**
  * This is a utility class for all the methods related to chart management.
  *
  **/
object ChartUtils {
    private val Head = 0
    private val MaxNumberOfData = 20

    val positionIterator: Iterator[(Int, Int)] = List((1, 0), (0, 1), (1, 1), (0, 2), (1, 2)).iterator

    /** *
      * This method resizes the Series in input in order to keep it of the wanted size.
      *
      * @param data : XYChart.Series to be resized
      *
      */
    def resizeIfNeeded(data: XYChart.Series[Double, Double]): XYChart.Series[Double, Double] = {
        if (data.getData.size.equals(MaxNumberOfData)) {
            data.getData remove Head
        }
        data
    }

    def timeIterator(): Iterator[Int] = {
        (0 until Int.MaxValue).iterator
    }

}