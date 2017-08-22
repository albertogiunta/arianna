package system.cell.sensormanagement.sensors

import org.scalatest.{FlatSpec, Matchers}
import system.ontologies.sensor.SensorCategories

/**
  * Created by Matteo Gabellini on 31/07/2017.
  */
class CO2SensorTest extends FlatSpec with Matchers {
    val sensorName = "co2Sensor"
    val minValue = 0.0
    val maxValue = 50.0
    val thresholdValue = 50
    val cThreshold = new CO2Threshold(thresholdValue)
    var cSensor = new CO2Sensor(sensorName, 0, minValue, maxValue, cThreshold)


    "A CO2 Sensor" should "have the same name assigned during the creation" in {
        cSensor.name should be(sensorName)
    }

    "A CO2 Sensor" should "have the category co2" in {
        cSensor.category should be(SensorCategories.CO2)
    }


    "A CO2 Sensor" should "measure the carbon dioxide value" in {
        cSensor.gasMeasured should be(Gas.carbonDioxide)
    }

    "A CO2 Sensor" should "have the same minimum value assigned in the creation" in {
        cSensor.minValue should be(minValue)
    }

    "A CO2Sensor" should "have the same maximum value assigned in the creation" in {
        cSensor.maxValue should be(maxValue)
    }

    "A CO2 Threshold checking method" should "return true if the co2 value is over the threshold" in {
        cThreshold.hasBeenExceeded(thresholdValue + 1) should be(true)
    }

    "A CO2 Sensor" should "have the same threshold assigned in the creation" in {
        cSensor.threshold should be(cThreshold)
    }


    val refreshRate = 1000
    val changeStep = 0.15
    var simulatedCO2Sensor = new SimulatedGasSensor(cSensor, refreshRate, SimulationStrategies.LinearDoubleSimulation(changeStep))
    var oGSensor: ObservableGasSensor = new ObservableGasSensor(simulatedCO2Sensor)


    "A Simulated CO2 Sensor" should "have a same gas measured of the decorated co2 sensor" in {
        simulatedCO2Sensor.gasMeasured should be(cSensor.gasMeasured)
    }


    "A Simulated CO2 Sensor" should "have a same threshold of the decorated co2 sensor" in {
        simulatedCO2Sensor.threshold should be(cSensor.threshold)
    }


    "A Observable CO2 Sensor" should "have a same gas measured of the decorated co2 sensor" in {
        oGSensor.gasMeasured should be(simulatedCO2Sensor.gasMeasured)
    }


    "A Observable CO2 Sensor" should "have a same threshold of the decorated co2 sensor" in {
        oGSensor.threshold should be(simulatedCO2Sensor.threshold)
    }
}
