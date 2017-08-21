package cell.sensormanagement.sensors

import ontologies.sensor.SensorCategories
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by Matteo Gabellini on 31/07/2017.
  */
class OxygenSensorTest extends FlatSpec with Matchers {
    val sensorName = "oxygenSensor"
    val minValue = 0.0
    val maxValue = 50.0
    val thresholdValue = 50
    val oThreshold = new OxygenThreshold(thresholdValue)
    var oSensor = new OxygenSensor(sensorName, 0, minValue, maxValue, oThreshold)


    "A Oxygen Sensor" should "have the same name assigned during the creation" in {
        oSensor.name should be(sensorName)
    }

    "A Oxygen Sensor" should "have the category oxygen" in {
        oSensor.category should be(SensorCategories.Oxygen)
    }

    "A Oxygen Sensor" should "measure the carbon dioxide value" in {
        oSensor.gasMeasured should be(Gas.oxygen)
    }

    "A Oxygen Sensor" should "have the same minimum value assigned in the creation" in {
        oSensor.minValue should be(minValue)
    }

    "A OxygenSensor" should "have the same maximum value assigned in the creation" in {
        oSensor.maxValue should be(maxValue)
    }

    "A Oxygen Threshold checking method" should "return true if the oxygen value is under the threshold" in {
        oThreshold.hasBeenExceeded(thresholdValue - 1) should be(true)
    }

    "A Oxygen Sensor" should "have the same threshold assigned in the creation" in {
        oSensor.threshold should be(oThreshold)
    }

    val refreshRate = 1000
    val changeStep = 0.15
    var simulatedOxygenSensor = new SimulatedLinearGasSensor(oSensor, refreshRate, changeStep)
    var oGSensor: ObservableGasSensor = new ObservableGasSensor(simulatedOxygenSensor)


    "A Simulated Oxygen Sensor" should "have a same gas measured of the decorated oxygen sensor" in {
        simulatedOxygenSensor.gasMeasured should be(oSensor.gasMeasured)
    }


    "A Simulated Oxygen Sensor" should "have a same threshold of the decorated oxygen sensor" in {
        simulatedOxygenSensor.threshold should be(oSensor.threshold)
    }


    "A Observable Oxygen Sensor" should "have a same gas measured of the decorated oxygen sensor" in {
        oGSensor.gasMeasured should be(simulatedOxygenSensor.gasMeasured)
    }


    "A Observable Oxygen Sensor" should "have a same threshold of the decorated oxygen sensor" in {
        oGSensor.threshold should be(simulatedOxygenSensor.threshold)
    }
}
