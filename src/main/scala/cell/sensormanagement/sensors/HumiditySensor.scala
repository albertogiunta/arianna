package cell.sensormanagement.sensors

import ontologies.sensor.{DoubleThreshold, SensorCategories, SensorCategory, Threshold}

/**
  * A trait for a humidity sensor that measure the relative humidity
  * Created by Matteo Gabellini on 22/07/2017.
  */
trait HumiditySensor extends NumericSensor[Double] with SensorWithThreshold[Double] {
    override def category: SensorCategory = SensorCategories.Humidity

    override def range: Double = maxValue - minValue
}

case class BasicHumiditySensor(override val name: String,
                               override val currentValue: Double,
                               override val minValue: Double,
                               override val maxValue: Double,
                               override val threshold: HumidityThreshold) extends HumiditySensor


case class SimulatedMonotonicHumiditySensor(override val sensor: HumiditySensor,
                                            override val millisRefreshRate: Long,
                                            val changeStep: Double)
    extends SimulatedNumericSensor[Double](sensor,
        millisRefreshRate,
        SimulationStrategies.MonotonicDoubleSimulation(changeStep)) with HumiditySensor {
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


case class HumidityThreshold(var minValue: Double, var maxValue: Double) extends DoubleThreshold[Double] {

    override def hasBeenExceeded(currentSensorValue: Double): Boolean =
        currentSensorValue < minValue || currentSensorValue > maxValue
}