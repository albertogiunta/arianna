package cell.sensormanagement.sensors

import ontologies.sensor.{DoubleThreshold, SensorCategories, SensorCategory, Threshold}

/**
  * A basic trait for a temperature sensor
  * Created by Matteo Gabellini on 05/07/2017.
  */
trait TemperatureSensor extends NumericSensor[Double] with SensorWithThreshold[Double] {
    def measureUnit: String

    override def category: SensorCategory = SensorCategories.Temperature

    override def range: Double = maxValue - minValue
}

/**
  * A Basic implementation of a temperature sensor
  **/
protected class BasicTemperatureSensor(override val name: String,
                                       override val currentValue: Double,
                                       override val minValue: Double,
                                       override val maxValue: Double,
                                       override val threshold: TemperatureThreshold,
                                       override val measureUnit: String = TemperatureMeasureUnit.Celsius
                                 ) extends TemperatureSensor
/**
  * An object that contains the string representation
  * of the basic temperature measure unit
  */
object TemperatureMeasureUnit {
    val Celsius = "Celsius"
    val Fahrenheit = "Fahrenheit"
    val Kelvin = "Kelvin"
}

/**
  * This is a decoration for a temperature sensor. This class provide
  * a simulated monotonic behaviour to the decorated sensor
  **/
protected class SimulatedMonotonicTemperatureSensor(override val sensor: TemperatureSensor,
                                                    override val millisRefreshRate: Long,
                                                    val changeStep: Double)
    extends SimulatedNumericSensor[Double](sensor,
        millisRefreshRate,
        SimulationStrategies.MonotonicDoubleSimulation(changeStep)) with TemperatureSensor {
    override def measureUnit: String = sensor.measureUnit

    override def threshold: Threshold[Double] = sensor.threshold
}

/**
  * This class implements the Reactivex method to create
  * a Flowable attached to the specified temperature sensor
  *
  * @param sensor : TemperatureSensor
  *               in order to become reactive to the sensor value changes
  **/
class ObservableTemperatureSensor(private val sensor: TemperatureSensor)
    extends ObservableNumericSensor[Double](sensor) with TemperatureSensor {
    override def measureUnit: String = sensor.measureUnit

    override def threshold: Threshold[Double] = sensor.threshold
}

/**
  * A Temperature threshold, this class models also the logic to decide
  * when the temperature is under or upper the specified threshold values
  **/
case class TemperatureThreshold(var low: Double, var high: Double) extends DoubleThreshold[Double] {

    override def hasBeenExceeded(currentSensorValue: Double): Boolean =
        currentSensorValue < low || currentSensorValue > high
}