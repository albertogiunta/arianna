package cell.sensormanagement.sensors

import cell.sensormanagement.{DoubleThreshold, Threshold}
import ontologies.{SensorCategories, SensorCategory}

/**
  * A trait for a humidity sensor that measure the relative humidity
  * Created by Matteo Gabellini on 22/07/2017.
  */
trait HumiditySensor extends NumericSensor[Int] with SensorWithThreshold[Int] {
    override def category: SensorCategory = SensorCategories.Humidity
}

case class BasicHumiditySensor(override val name: String,
                               override val currentValue: Int,
                               override val minValue: Int,
                               override val maxValue: Int,
                               override val range: Int,
                               override val threshold: HumidityThreshold) extends HumiditySensor


case class SimulatedMonotonicHumiditySensor(override val sensor: HumiditySensor,
                                            override val millisRefreshRate: Long,
                                            val changeStep: Int)
    extends SimulatedNumericSensor[Int](sensor,
        millisRefreshRate,
        SimulationStrategies.MonotonicIntSimulation(changeStep)) with HumiditySensor {
    override def threshold: Threshold[Int] = sensor.threshold
}

/**
  * This class implements the Reactivex method to create
  * a Flowable attached to the specified Humidity Sensor sensor
  *
  * @param sensor : HumiditySensor
  *               in order to become reactive to the sensor value changes
  **/
class ObservableHumiditySensor(private val sensor: HumiditySensor)
    extends ObservableNumericSensor[Int](sensor) with HumiditySensor {
    override def threshold: Threshold[Int] = sensor.threshold
}


case class HumidityThreshold(var minValue: Int, var maxValue: Int) extends DoubleThreshold[Int] {

    override def hasBeenExceeded(currentSensorValue: Int): Boolean =
        currentSensorValue < minValue || currentSensorValue > maxValue
}