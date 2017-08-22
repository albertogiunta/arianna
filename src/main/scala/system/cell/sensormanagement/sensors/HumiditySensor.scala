package system.cell.sensormanagement.sensors

import system.ontologies.sensor.{DoubleThreshold, SensorCategories, SensorCategory, Threshold}

/**
  * A trait for a humidity sensor that measure the relative humidity
  * Created by Matteo Gabellini on 22/07/2017.
  */
trait HumiditySensor extends NumericSensor[Double] with SensorWithThreshold[Double] {
    override def category: SensorCategory = SensorCategories.Humidity

    override def range: Double = maxValue - minValue
}

/**
  * A Basic implementation of a humidity sensor
  **/
protected class BasicHumiditySensor(override val name: String,
                                    override val currentValue: Double,
                                    override val minValue: Double,
                                    override val maxValue: Double,
                                    override val threshold: HumidityThreshold) extends HumiditySensor

/**
  * This is a decoration for a humidity sensor. This class provide
  * a simulated linear behaviour to the decorated sensor
  **/
protected class SimulatedLinearHumiditySensor(override val sensor: HumiditySensor,
                                              override val millisRefreshRate: Long,
                                              val changeStep: Double)
    extends SimulatedNumericSensor[Double](sensor,
        millisRefreshRate,
        SimulationStrategies.LinearDoubleSimulation(changeStep)) with HumiditySensor {
    override def threshold: Threshold[Double] = sensor.threshold
}

/**
  * This class implements the Reactivex method to create
  * a Flowable attached to the specified Humidity Sensor sensor
  *
  * @param sensor : HumiditySensor
  *               in order to become reactive to the sensor value changes
  **/
class ObservableHumiditySensor(private val sensor: HumiditySensor)
    extends ObservableNumericSensor[Double](sensor) with HumiditySensor {
    override def threshold: Threshold[Double] = sensor.threshold
}

/**
  * A Humidity threshold, this class models also the logic to decide
  * when the humidity is under or upper the specified threshold values
  **/
case class HumidityThreshold(var low: Double, var high: Double) extends DoubleThreshold[Double] {

    override def hasBeenExceeded(currentSensorValue: Double): Boolean =
        currentSensorValue < low || currentSensorValue > high
}