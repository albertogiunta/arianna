package cell.sensormanagement.sensors

/**
  * A trait for a basic smoke sensor
  * Created by Matteo Gabellini on 05/07/2017.
  */
trait GasSensor extends NumericSensor[Double] {
    def gasMeasured: String
}

case class BasicGasSensor(override val currentValue: Double,
                          override val minValue: Double,
                          override val maxValue: Double,
                          override val range: Double,
                          override val gasMeasured: String) extends GasSensor

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