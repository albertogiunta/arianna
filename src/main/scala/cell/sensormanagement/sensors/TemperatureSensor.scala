package cell.sensormanagement.sensors

import java.util.concurrent.{Executors, TimeUnit}

import io.reactivex.{BackpressureStrategy, Flowable}

/**
  * A basic trait for a temperature sensor
  * Created by Matteo Gabellini on 05/07/2017.
  */
trait TemperatureSensor extends Sensor[Double] {
    def measureUnit: String
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

/**
  * An abstract implementation of a simulated temperature sensor,
  * extends this class to provide the value changing strategy
  *
  * @param startValue        initial value of the sensor
  * @param minValue          minimun value measurable by the sensor
  * @param maxValue          maximun value measurable by the sensor
  * @param range             of mesurement
  * @param measureUnit
  * @param millisRefreshRate rate at the sensor change its value
  */
abstract class SimulatedTemperatureSensor(val startValue: Double,
                                          override val minValue: Double,
                                          override val maxValue: Double,
                                          override val range: Double,
                                          val measureUnit: String = TemperatureUnitMeasure.Celsius,
                                          val millisRefreshRate: Long) extends TemperatureSensor {
    var value: Double = startValue

    //thread safe read-access to the current value
    override def currentValue = value.synchronized {
        value
    }

    //thread safe write-access to the current value
    def currentValue_=(i: Double): Unit = value.synchronized {
        value = i}


    private val executor = Executors.newScheduledThreadPool(1)

    def strategy: Runnable

    this.executor.scheduleAtFixedRate(strategy, 0L, millisRefreshRate, TimeUnit.MILLISECONDS)

    def stopGeneration(): Unit = {
        this.executor.shutdown()
    }
}

/**
  * An extension of the simulated temperature sensor that implements
  * a monotonic behaviour of the sensor value change
  *
  * @param startValue        initial value of the sensor
  * @param minValue          minimun value measurable by the sensor
  * @param maxValue          maximun value measurable by the sensor
  * @param millisRefreshRate rate at the sensor change its value
  * @param changeStep
  */
class SimulatedMonotonicTemperatureSensor(override val startValue: Double,
                                          override val minValue: Double,
                                          override val maxValue: Double,
                                          override val millisRefreshRate: Long,
                                          val changeStep: Double)
    extends SimulatedTemperatureSensor(startValue,
        minValue,
        maxValue,
        maxValue - minValue,
        TemperatureUnitMeasure.Celsius, millisRefreshRate) {

    override def strategy: Runnable = () => this.currentValue = if (this.currentValue < this.maxValue)
        this.currentValue + changeStep else this.currentValue - changeStep
}


/**
  * This class implements the Reactivex method to create
  * a Flowable attached to the specified temperature sensor
  *
  * @param sensor : TemperatureSensor
  *               in order to become reactive to the sensor value changes
  **/
class ObservableTemperatureSensor(private val sensor: TemperatureSensor)
    extends ObservableSensor[Double] {

    override def currentValue: Double = sensor.currentValue

    override def createObservable(refreshPeriod: Long): Flowable[Double] = Flowable.create(emitter => {
        new Thread(() => {
            var currentValue: Double = 0
            var tmpPrev = Double.MinValue
            while (true) {
                try {
                    currentValue = sensor.currentValue
                    if (tmpPrev != currentValue) {
                        emitter.onNext(currentValue);
                        tmpPrev = currentValue;
                        Thread.sleep(refreshPeriod)
                    }
                } catch {
                    case ex: Exception => //ignore
                }
            }
        }).start()
    }, BackpressureStrategy.BUFFER)

    override def minValue: Double = sensor.minValue

    override def maxValue: Double = sensor.maxValue

    override def range: Double = sensor.range
}