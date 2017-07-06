package cell.sensormanagement.sensors

import java.util.concurrent.{Executors, TimeUnit}

import io.reactivex.{BackpressureStrategy, Flowable}

/**
  * A basic trait for a temperature sensor
  * Created by Matteo Gabellini on 05/07/2017.
  */
trait TemperatureSensor extends Sensor[Double] {
    def measureUnit
}

/**
  * An object that contains the string representation
  * of the basic temperature measure unit
  */
object TemperatureUnitMeasure {
    val Celsius = "Celsius"
    val Fahrenheit = "Fahrenheit"
    val Kelvin = "Kelvin"
}

abstract class SimulatedTemperatureSensor(override var currentValue: Double,
                                          override val minValue: Double,
                                          override val maxValue: Double,
                                          override val range: Double,
                                          override val measureUnit: String = TemperatureUnitMeasure.Celsius,
                                          val millisRefreshRate: Long) extends TemperatureSensor {
    private val executor = Executors.newScheduledThreadPool(1)

    def strategy: Runnable

    this.executor.scheduleAtFixedRate(strategy, 0L, millisRefreshRate, TimeUnit.MILLISECONDS)

    def stopGeneration(): Unit = {
        this.executor.shutdown()
    }
}

class SimulatedMonotonicTemperatureSensor(var startvalue: Double,
                                          override val minValue: Double,
                                          override val maxValue: Double,
                                          override val millisRefreshRate: Long,
                                          val changeStep: Double)
    extends SimulatedTemperatureSensor(startvalue,
        minValue,
        maxValue,
        maxValue - minValue,
        TemperatureUnitMeasure.Celsius, millisRefreshRate) {

    override def strategy: Runnable = () => this.currentValue = if (this.currentValue < this.maxValue)
        this.currentValue + changeStep else this.currentValue - changeStep
}


/**
  * This class implements the Reactivex method to create a Flowable attached to the specified
  *
  * @param sensor : TemperatureSensor
  *               in order to become reactive to the sensor value changes
  **/
class ObservableTemperatureSensor(private val sensor: TemperatureSensor)
    extends ObservableSensor[Double] {

    override def currentValue: Double = sensor.currentValue

    override def createObservable: Flowable[Double] = Flowable.create(emitter => {
        new Thread(() => {
            var currentValue: Double = 0
            var tmpPrev = Double.MinValue
            while (true) {
                try {
                    currentValue = sensor.currentValue
                } catch {
                    case ex: Exception => //ignore
                }
            }
        })
    }, BackpressureStrategy.BUFFER)

    override def minValue: Double = sensor.minValue

    override def maxValue: Double = sensor.maxValue

    override def range: Double = sensor.range
}