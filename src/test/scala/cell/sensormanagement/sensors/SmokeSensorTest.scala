package cell.sensormanagement.sensors

import ontologies.sensor.SensorCategories
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by Matteo Gabellini on 31/07/2017.
  */
class SmokeSensorTest extends FlatSpec with Matchers {
    val sensorName = "smokeSensor"
    val minValue = 0.0
    val maxValue = 50.0
    val thresholdValue = 50
    val sThreshold = new SmokeThreshold(thresholdValue)
    var sSensor = new SmokeSensor(sensorName, 0, minValue, maxValue, sThreshold)


    "A Smoke Sensor" should "have the same name assigned during the creation" in {
        sSensor.name should be(sensorName)
    }

    "A Smoke Sensor" should "have the category smoke" in {
        sSensor.category should be(SensorCategories.Smoke)
    }

    "A Smoke Sensor" should "measure the carbon monoxide value" in {
        sSensor.gasMeasured should be(Gas.carbonMonoxide)
    }

    "A Smoke Sensor" should "have the same minimum value assigned in the creation" in {
        sSensor.minValue should be(minValue)
    }

    "A SmokeSensor" should "have the same maximum value assigned in the creation" in {
        sSensor.maxValue should be(maxValue)
    }

    "A Smoke Threshold checking method" should "return true if the smoke value is over the threshold" in {
        sThreshold.hasBeenExceeded(thresholdValue + 1) should be(true)
    }

    "A Smoke Sensor" should "have the same threshold assigned in the creation" in {
        sSensor.threshold should be(sThreshold)
    }

    val refreshRate = 1000
    val changeStep = 0.15
    var simulatedSmokeSensor = new SimulatedMonotonicGasSensor(sSensor, refreshRate, changeStep)
    var oGSensor: ObservableGasSensor = new ObservableGasSensor(simulatedSmokeSensor)


    "A Simulated Smoke Sensor" should "have a same gas measured of the decorated smoke sensor" in {
        simulatedSmokeSensor.gasMeasured should be(sSensor.gasMeasured)
    }


    "A Simulated Smoke Sensor" should "have a same threshold of the decorated smoke sensor" in {
        simulatedSmokeSensor.threshold should be(sSensor.threshold)
    }


    "A Observable Smoke Sensor" should "have a same gas measured of the decorated smoke sensor" in {
        oGSensor.gasMeasured should be(simulatedSmokeSensor.gasMeasured)
    }


    "A Observable Smoke Sensor" should "have a same threshold of the decorated smoke sensor" in {
        oGSensor.threshold should be(simulatedSmokeSensor.threshold)
    }
}
