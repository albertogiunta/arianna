package system.cell.sensormanagement.sensors

import org.scalatest.{FlatSpec, Matchers}
import system.ontologies.sensor.SensorCategories

/**
  * Created by Matteo Gabellini on 27/07/2017.
  */
class TemperatureSensorTest extends FlatSpec with Matchers {
    val sensorName = "tSensor"
    val minValue = -40.0
    val maxValue = 100.0
    val lowThreshold = -10
    val highThreshold = 50
    val tThreshold = new TemperatureThreshold(lowThreshold, highThreshold)
    var tSensor = new BasicTemperatureSensor(sensorName, 0, minValue, maxValue, tThreshold)


    var sensor: Sensor[Double] = tSensor

    "A Temperature Sensor" should "have the same name assigned during the creation" in {
        sensor.name should be(sensorName)
    }

    "A Temperature Sensor" should "have the category temperature" in {
        sensor.category should be(SensorCategories.Temperature)
    }

    "A Temperature Sensor" should "have the same minimum value assigned in the creation" in {
        tSensor.minValue should be(minValue)
    }


    "A Temperature Sensor" should "have the same maximum value assigned in the creation" in {
        tSensor.maxValue should be(maxValue)
    }

    "A Temperature Threshold checking method" should "return true if the temperature value is over the high threshold" in {
        tThreshold.hasBeenExceeded(highThreshold + 1) should be(true)
    }

    "A Temperature Threshold checking method" should "return true if the temperature value is under the low threshold" in {
        tThreshold.hasBeenExceeded(lowThreshold - 1) should be(true)
    }

    "A Temperature Threshold checking method" should "return false if the sensor temperature is between the two threshold vlues" in {
        tThreshold.hasBeenExceeded(highThreshold - ((highThreshold - lowThreshold) / 2)) should be(false)
    }

    "A Temperature Sensor" should "have the same threshold assigned in the creation" in {
        tSensor.threshold should be(tThreshold)
    }

    val refreshRate = 1000
    val changeStep = 0.15
    var simulatedTempSensor = new SimulatedLinearTemperatureSensor(tSensor, refreshRate, changeStep)
    var oTSensor: ObservableTemperatureSensor = new ObservableTemperatureSensor(simulatedTempSensor)


    "A Simulated Temperature Sensor" should "have a same measure unit of the decorated temperature sensor" in {
        simulatedTempSensor.measureUnit should be(tSensor.measureUnit)
    }


    "A Simulated Temperature Sensor" should "have a same threshold of the decorated temperature sensor" in {
        simulatedTempSensor.threshold should be(tSensor.threshold)
    }


    "A Observable Temperature Sensor" should "have a same measure unit of the decorated temperature sensor" in {
        oTSensor.measureUnit should be(simulatedTempSensor.measureUnit)
    }


    "A Observable Temperature Sensor" should "have a same threshold of the decorated temperature sensor" in {
        oTSensor.threshold should be(simulatedTempSensor.threshold)
    }

}
