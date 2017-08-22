package system.cell.sensormanagement.sensors

import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}
import system.ontologies.sensor.{SensorCategories, SensorCategory}

/**
  * Created by Matteo Gabellini on 31/07/2017.
  */
class SimulationStrategiesTest extends FlatSpec with Matchers with BeforeAndAfterEach {


    val refreshTime = 1000
    val sleepTime = refreshTime + 500
    val doubleChangeStep = 1.0
    val minDoubleValue = 0.0
    val maxDoubleValue = 5.0
    var doubleSensor = new NumericSensor[Double] {
        /**
          * Get the current sensor value of type
          **/
        override def currentValue: Double = 0

        /**
          * Get the sensor name
          **/
        override def name: String = "testDoubleSensor"

        /**
          * Get the sensor category
          **/
        override def category: SensorCategory = SensorCategories.Temperature

        /**
          * Get the range of value that the sensor can assume
          **/
        override def range: Double = maxValue - minValue

        /**
          * Get the minimum value that the sensor can reach
          **/
        override def minValue: Double = minDoubleValue

        /**
          * Get the maximum value that the sensor can reach
          **/
        override def maxValue: Double = maxDoubleValue
    }

    var simulatedDoubleSensor: SimulatedNumericSensor[Double] = _
    var simulatedIntSensor: SimulatedNumericSensor[Int] = _

    override def beforeEach(): Unit = {
        simulatedDoubleSensor = new SimulatedNumericSensor[Double](doubleSensor,
            refreshTime,
            SimulationStrategies.LinearDoubleSimulation(doubleChangeStep))
        simulatedIntSensor = new SimulatedNumericSensor[Int](intSensor,
            refreshTime,
            SimulationStrategies.LinearIntSimulation(intChangeStep))
    }

    "A linear double strategies" should "change the sensor value after the defined refresh time" in {
        @volatile var oldValue = simulatedDoubleSensor.currentValue
        Thread.sleep(sleepTime)
        oldValue should not be (simulatedDoubleSensor.currentValue)
    }

    "A linear double strategies" should "increase the sensor value after the defined refresh time " +
        "if the current value hasn't reached the max value" in {
        @volatile var oldValue = simulatedDoubleSensor.currentValue
        Thread.sleep(sleepTime)
        oldValue should be < (simulatedDoubleSensor.currentValue)
    }

    "A linear double strategies" should "decrease the sensor value after the defined refresh time " +
        "if the current value has reached the max value" in {
        Thread.sleep(((refreshTime * simulatedDoubleSensor.maxValue) / doubleChangeStep).toLong)
        @volatile var oldValue = simulatedDoubleSensor.currentValue
        Thread.sleep(sleepTime)
        oldValue should be > (simulatedDoubleSensor.currentValue)
    }

    "A linear double strategies" should "increase the sensor value after the defined refresh time " +
        "if the current value has reached the min value during the decreasing phase" in {
        Thread.sleep(((refreshTime * simulatedDoubleSensor.maxValue) / doubleChangeStep).toLong)
        @volatile var oldValue = simulatedDoubleSensor.currentValue
        var waitTime: Long = (simulatedDoubleSensor.minValue + oldValue).toLong
        Thread.sleep((refreshTime * waitTime) / doubleChangeStep.toInt)
        oldValue = simulatedDoubleSensor.currentValue
        Thread.sleep(sleepTime)
        oldValue should be < (simulatedDoubleSensor.currentValue)
    }


    val intChangeStep = 1
    val minIntValue = 0
    val maxIntValue = 5
    var intSensor = new NumericSensor[Int] {
        /**
          * Get the current sensor value of type
          **/
        override def currentValue: Int = 0

        /**
          * Get the sensor name
          **/
        override def name: String = "testDoubleSensor"

        /**
          * Get the sensor category
          **/
        override def category: SensorCategory = SensorCategories.Temperature

        /**
          * Get the range of value that the sensor can assume
          **/
        override def range: Int = maxValue - minValue

        /**
          * Get the minimum value that the sensor can reach
          **/
        override def minValue: Int = minIntValue

        /**
          * Get the maximum value that the sensor can reach
          **/
        override def maxValue: Int = maxIntValue
    }


    "A linear integer strategies" should "change the sensor value after the defined refresh time" in {
        @volatile var oldValue = simulatedIntSensor.currentValue
        Thread.sleep(sleepTime)
        oldValue should not be (simulatedIntSensor.currentValue)
    }

    "A linear integer strategies" should "increase the sensor value after the defined refresh time, " +
        "if the current value hasn't reached the max value" in {
        @volatile var oldValue = simulatedIntSensor.currentValue
        Thread.sleep(sleepTime)
        oldValue should be < (simulatedIntSensor.currentValue)
    }

    "A linear integer strategies" should "decrease the sensor value after the defined refresh time, " +
        "if the current value has reached the max value" in {
        Thread.sleep((refreshTime * simulatedIntSensor.maxValue) / intChangeStep)
        @volatile var oldValue = simulatedIntSensor.currentValue
        Thread.sleep(sleepTime)
        oldValue should be > (simulatedIntSensor.currentValue)
    }

    "A linear integer strategies" should "increase the sensor value after the defined refresh time, " +
        "if the current value has reached the min value during the decreasing phase" in {
        Thread.sleep((refreshTime * simulatedIntSensor.maxValue) / intChangeStep)
        @volatile var oldValue = simulatedIntSensor.currentValue
        var waitTime: Long = (simulatedIntSensor.minValue + oldValue).toLong
        Thread.sleep((refreshTime * waitTime) / intChangeStep)
        oldValue = simulatedIntSensor.currentValue
        Thread.sleep(sleepTime)
        oldValue should be < (simulatedIntSensor.currentValue)
    }


}
