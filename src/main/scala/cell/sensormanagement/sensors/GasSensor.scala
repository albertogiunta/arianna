package cell.sensormanagement.sensors

import ontologies.sensor.{SensorCategories, SensorCategory, SingleThreshold, Threshold}

/**
  * A trait for a generic gas sensor that measure the gas level
  * with a Double type scale
  * Created by Matteo Gabellini on 05/07/2017.
  */
trait GasSensor extends NumericSensor[Double] with SensorWithThreshold[Double] {
    def gasMeasured: String

    override def range: Double = maxValue - minValue
}

/**
  * Different type of Gas
  **/
object Gas {
    val carbonMonoxide = "CO"
    val carbonDioxide = "CO2"
    val oxygen = "O"
}

/**
  * A basic implementation of a sensor
  **/
abstract class BasicGasSensor(override val name: String,
                              override val currentValue: Double,
                          override val minValue: Double,
                          override val maxValue: Double,
                          override val gasMeasured: String) extends GasSensor

case class SmokeSensor(override val name: String,
                       override val currentValue: Double,
                       override val minValue: Double,
                       override val maxValue: Double,
                       override val threshold: SmokeThreshold)
    extends BasicGasSensor(name, currentValue, minValue, maxValue, Gas.carbonMonoxide) {
    override def category: SensorCategory = SensorCategories.Smoke
}


case class SmokeThreshold(var value: Double) extends SingleThreshold[Double] {
    override def hasBeenExceeded(currentSensorValue: Double): Boolean = currentSensorValue > value
}

case class CO2Sensor(override val name: String,
                     override val currentValue: Double,
                     override val minValue: Double,
                     override val maxValue: Double,
                     override val threshold: CO2Threshold)
    extends BasicGasSensor(name, currentValue, minValue, maxValue, Gas.carbonMonoxide) {
    override def category: SensorCategory = SensorCategories.CO2
}


case class CO2Threshold(var value: Double) extends SingleThreshold[Double] {
    override def hasBeenExceeded(currentSensorValue: Double): Boolean = currentSensorValue > value
}

case class OxygenSensor(override val name: String,
                        override val currentValue: Double,
                        override val minValue: Double,
                        override val maxValue: Double,
                        override val threshold: OxygenThreshold)
    extends BasicGasSensor(name, currentValue, minValue, maxValue, Gas.oxygen) {
    override def category: SensorCategory = SensorCategories.Oxygen
}

case class OxygenThreshold(var value: Double) extends SingleThreshold[Double] {
    override def hasBeenExceeded(currentSensorValue: Double): Boolean = currentSensorValue < value
}

case class SimulatedMonotonicGasSensor(override val sensor: GasSensor,
                                       override val millisRefreshRate: Long,
                                       val changeStep: Double)
    extends SimulatedNumericSensor[Double](
        sensor,
        millisRefreshRate,
        SimulationStrategies.MonotonicDoubleSimulation(changeStep)) with GasSensor {
    override def gasMeasured: String = sensor.gasMeasured

    override def threshold: Threshold[Double] = sensor.threshold
}

class ObservableGasSensor(private val sensor: GasSensor) extends ObservableNumericSensor[Double](sensor) with GasSensor {
    override def gasMeasured: String = sensor.gasMeasured

    override def threshold: Threshold[Double] = sensor.threshold
}
