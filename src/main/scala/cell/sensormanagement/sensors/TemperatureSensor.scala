package cell.sensormanagement.sensors

import cell.sensormanagement.{DoubleThreshold, Threshold}
import ontologies.{SensorCategories, SensorCategory}

/**
  * A basic trait for a temperature sensor
  * Created by Matteo Gabellini on 05/07/2017.
  */
trait TemperatureSensor extends NumericSensor[Double] with SensorWithThreshold[Double] {
    def measureUnit: String

    override def category: SensorCategory = SensorCategories.Temperature
}

case class BasicTemperatureSensor(override val name: String,
                                  override val currentValue: Double,
                                  override val minValue: Double,
                                  override val maxValue: Double,
                                  override val range: Double,
                                  override val threshold: TemperatureThreshold,
                                  override val measureUnit: String = TemperatureUnitMeasure.Celsius
                                 ) extends TemperatureSensor
/**
  * An object that contains the string representation
  * of the basic temperature measure unit
  */
object TemperatureUnitMeasure {
    val Celsius = "Celsius"
    val Fahrenheit = "Fahrenheit"
    val Kelvin = "Kelvin"
}

case class SimulatedMonotonicTemperatureSensor(override val sensor: TemperatureSensor,
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
    override def measureUnit: String = ???

    override def threshold: Threshold[Double] = sensor.threshold
}


case class TemperatureThreshold(var minValue: Double, var maxValue: Double) extends DoubleThreshold[Double] {

    override def hasBeenExceeded(currentSensorValue: Double): Boolean =
        currentSensorValue < minValue || currentSensorValue > maxValue
}