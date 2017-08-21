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
  * A basic implementation of a gas sensor
  **/
abstract class BasicGasSensor(override val name: String,
                              override val currentValue: Double,
                              override val minValue: Double,
                              override val maxValue: Double,
                              override val gasMeasured: String) extends GasSensor

/**
  * A smoke sensor implementation, this senspr measure the CO gas level
  **/
protected class SmokeSensor(override val name: String,
                            override val currentValue: Double,
                            override val minValue: Double,
                            override val maxValue: Double,
                            override val threshold: SmokeThreshold)
    extends BasicGasSensor(name, currentValue, minValue, maxValue, Gas.carbonMonoxide) {
    override def category: SensorCategory = SensorCategories.Smoke
}

/**
  * A smoke threshold, this class models also the logic to decide
  * if the threshold is exceeded
  **/
case class SmokeThreshold(var value: Double) extends SingleThreshold[Double] {
    override def hasBeenExceeded(currentSensorValue: Double): Boolean = currentSensorValue > value
}

/**
  * A CO2 sensor implementation
  **/
protected class CO2Sensor(override val name: String,
                          override val currentValue: Double,
                          override val minValue: Double,
                          override val maxValue: Double,
                          override val threshold: CO2Threshold)
    extends BasicGasSensor(name, currentValue, minValue, maxValue, Gas.carbonDioxide) {
    override def category: SensorCategory = SensorCategories.CO2
}


/**
  * A CO2 threshold, this class models also the logic to decide
  * if the threshold is exceeded
  **/
case class CO2Threshold(var value: Double) extends SingleThreshold[Double] {
    override def hasBeenExceeded(currentSensorValue: Double): Boolean = currentSensorValue > value
}

/**
  * A Oxygen sensor implementation
  **/
protected class OxygenSensor(override val name: String,
                             override val currentValue: Double,
                             override val minValue: Double,
                             override val maxValue: Double,
                             override val threshold: OxygenThreshold)
    extends BasicGasSensor(name, currentValue, minValue, maxValue, Gas.oxygen) {
    override def category: SensorCategory = SensorCategories.Oxygen
}

/**
  * A Oxygen threshold, this class models also the logic to decide
  * when the oxygen is under the specified threshold value
  **/
case class OxygenThreshold(var value: Double) extends SingleThreshold[Double] {
    override def hasBeenExceeded(currentSensorValue: Double): Boolean = currentSensorValue < value
}

/**
  * This is a decoration for a gas sensor. This class provide
  * a simulated linear behaviour to the decorated sensor
  **/
protected class SimulatedLinearGasSensor(override val sensor: GasSensor,
                                         override val millisRefreshRate: Long,
                                         val changeStep: Double)
    extends SimulatedNumericSensor[Double](
        sensor,
        millisRefreshRate,
        SimulationStrategies.LinearDoubleSimulation(changeStep)) with GasSensor {
    override def gasMeasured: String = sensor.gasMeasured

    override def threshold: Threshold[Double] = sensor.threshold
}

/**
  * This a decoration of a Gas sensor that provide the methods to manage a Reactivex Flowable object
  * attached to the decorated sensor
  **/
class ObservableGasSensor(private val sensor: GasSensor) extends ObservableNumericSensor[Double](sensor) with GasSensor {
    override def gasMeasured: String = sensor.gasMeasured

    override def threshold: Threshold[Double] = sensor.threshold
}
