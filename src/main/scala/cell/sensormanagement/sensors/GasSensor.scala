package cell.sensormanagement.sensors

/**
  * A trait for a basic smoke sensor
  * Created by Matteo Gabellini on 05/07/2017.
  */
trait GasSensor extends NumericSensor[Double] {
    def gasMeasured: String
}

object Gas {
    val carbonMonoxide = "CO"
    val carbonDioxide = "CO2"
    val oxygen = "O"
}

abstract class BasicGasSensor(override val name: String,
                              override val currentValue: Double,
                          override val minValue: Double,
                          override val maxValue: Double,
                          override val range: Double,
                          override val gasMeasured: String) extends GasSensor

case class SmokeSensor(override val name: String,
                       override val currentValue: Double,
                       override val minValue: Double,
                       override val maxValue: Double,
                       override val range: Double)
    extends BasicGasSensor(name, currentValue, minValue, maxValue, range, Gas.carbonMonoxide) {
    override def category: String = SensorCategories.smokeSensor
}

case class CO2Sensor(override val name: String,
                     override val currentValue: Double,
                     override val minValue: Double,
                     override val maxValue: Double,
                     override val range: Double,
                     override val gasMeasured: String)
    extends BasicGasSensor(name, currentValue, minValue, maxValue, range, Gas.carbonMonoxide) {
    override def category: String = SensorCategories.smokeSensor
}

case class OxygenSensor(override val name: String,
                        override val currentValue: Double,
                        override val minValue: Double,
                        override val maxValue: Double,
                        override val range: Double)
    extends BasicGasSensor(name, currentValue, minValue, maxValue, range, Gas.oxygen) {
    override def category: String = SensorCategories.oxygenSensor
}


case class SimulatedMonotonicGasSensor(override val sensor: GasSensor,
                                       override val millisRefreshRate: Long,
                                       val changeStep: Double)
    extends SimulatedNumericSensor[Double](
        sensor,
        millisRefreshRate,
        SimulationStrategies.MonotonicNumericSimulation(changeStep)) with GasSensor {
    override def gasMeasured: String = sensor.gasMeasured
}

class ObservableGasSensor(private val sensor: GasSensor) extends ObservableNumericSensor[Double](sensor)
