package cell.sensormanagement.sensors

import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by Matteo Gabellini on 27/07/2017.
  */
class HumiditySensorTest extends FlatSpec with Matchers {
    val sensorName = "hSensor"
    val minValue = 0.0
    val maxValue = 100.0
    val lowThreshold = 20
    val highThreshold = 80
    val hThreshold = new HumidityThreshold(lowThreshold, highThreshold)
    var hSensor = new BasicHumiditySensor(sensorName, 0, minValue, maxValue, hThreshold)


    var sensor: Sensor = hSensor

    "A Humidity Sensor" should "have the same name assigned during the creation" in {
        sensor.name should be(sensorName)
    }

    "A Humidity Sensor" should "have the same minimum value assigned in the creation" in {
        hSensor.minValue should be(minValue)
    }


    "A Humidity Sensor" should "have the same maximum value assigned in the creation" in {
        hSensor.maxValue should be(maxValue)
    }

    "A Humidity Threshold checking method" should "return true if the humidity value is over the high threshold" in {
        hThreshold.hasBeenExceeded(highThreshold + 1) should be(true)
    }

    "A Humidity Threshold checking method" should "return true if the humidity value is under the low threshold" in {
        hThreshold.hasBeenExceeded(lowThreshold - 1) should be(true)
    }

    "A Humidity Threshold checking method" should "return false if the humidity value is between the two threshold values" in {
        hThreshold.hasBeenExceeded(highThreshold - ((highThreshold - lowThreshold) / 2)) should be(false)
    }

    "A Humidity Sensor" should "have the same threshold assigned in the creation" in {
        hSensor.threshold should be(hThreshold)
    }

    val refreshRate = 1000
    val changeStep = 0.15
    var simulatedHumiditySensor = new SimulatedMonotonicHumiditySensor(hSensor, refreshRate, changeStep)
    var oHSensor: ObservableHumiditySensor = new ObservableHumiditySensor(simulatedHumiditySensor)


    "A Simulated Humidity Sensor" should "have a same threshold of the decorated humidity sensor" in {
        simulatedHumiditySensor.threshold should be(hSensor.threshold)
    }


    "A Observable humidity Sensor" should "have a same threshold of the decorated humidity sensor" in {
        oHSensor.threshold should be(simulatedHumiditySensor.threshold)
    }
}
