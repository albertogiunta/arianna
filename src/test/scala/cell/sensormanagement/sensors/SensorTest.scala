package cell.sensormanagement.sensors

import org.scalatest.Matchers

/**
  * Created by Matteo Gabellini on 26/07/2017.
  */
class SensorTest extends org.scalatest.FlatSpec with Matchers {
    val sensorName = "tSensor"
    val minValue = -40.0
    val maxValue = 100.0
    val temperatureTreshold = new TemperatureThreshold(-10, 50)
    var tSensor = new BasicTemperatureSensor(sensorName, 0, minValue, maxValue, temperatureTreshold)


    var sensor: Sensor = tSensor

    var orderedScaleSensor: OrderedScaleSensor[Double] = tSensor

    "A Sensor with a ordered scale" should "have a minimum value assigned during the creation" in {
        orderedScaleSensor.minValue should be(minValue)
    }


    "A Sensor with a ordered scale" should "have a maximum value assigned during the creation" in {
        orderedScaleSensor.maxValue should be(maxValue)
    }

    var numericSensor: NumericSensor[Double] = tSensor

    "A Numeric sensor" should "have a numeric range that is interval between the minimum value and the maximum value" in {
        numericSensor.range should be(maxValue - minValue)
    }

    var refreshRate = 1000
    var changeStep = 0.1
    var simulatedSensor: SimulatedNumericSensor[Double] = new SimulatedNumericSensor[Double](tSensor,
        refreshRate,
        SimulationStrategies.LinearDoubleSimulation(changeStep))

    "A simulated Linear Numeric Sensor" should "change its current value after the refresh time" in {
        val currentValue = simulatedSensor.currentValue
        Thread.sleep(refreshRate)
        simulatedSensor.currentValue should be(currentValue + changeStep)
    }

    "A Simulated sensor" should "stop the simulation after stopGeneration method invocation" in {
        simulatedSensor.stopGeneration()
        var oldValue = simulatedSensor.currentValue
        Thread.sleep(refreshRate)
        oldValue should be(simulatedSensor.currentValue)
    }

    var oTSensor: ObservableNumericSensor[Double] = new ObservableNumericSensor[Double](simulatedSensor)

    "A Observable Numeric Sensor" should "notify the new current value of the sensor " +
        "if it is changed between two refresh loop" in {
        @volatile var waitChange = true
        @volatile var currentSimValue = 10.0
        @volatile var currentObsValue = 0.0

        val watchDog = new Thread(() => {
            Thread.sleep(refreshRate)
            waitChange = false
        })

        oTSensor.createObservable(refreshRate * 10).subscribe((X: Double) => {
            currentSimValue = simulatedSensor.currentValue
            currentObsValue = X
            waitChange = false
            oTSensor.stopObservation()
        })
        watchDog.start()

        while (waitChange) {}
        currentObsValue should be(currentSimValue)
    }

}
