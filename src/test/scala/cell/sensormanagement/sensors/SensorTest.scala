package cell.sensormanagement.sensors

import ontologies.sensor.SensorCategories
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

    "A Sensor" should "have the same name assigned during the creation" in {
        sensor.name should be(sensorName)
    }

    "A Temperature Sensor" should "have the category temperauture" in {
        sensor.category should be(SensorCategories.Temperature)
    }

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
    var simulatedSensor: SimulatedNumericSensor[Double] = new SimulatedNumericSensor[Double](tSensor,
        refreshRate,
        SimulationStrategies.MonotonicDoubleSimulation(0.1))

    "A Simulated sensor" should "stop the simulation after stopGeneration method call" in {
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
