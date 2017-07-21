package cell.sensormanagement.sensors

import java.util.concurrent.{Executors, TimeUnit}

import io.reactivex.{BackpressureStrategy, Flowable}

/**
  * a trait for a sensor
  **/
trait Sensor {
    def name: String

    def category: String
}

object SensorCategories {
    val temperatureSensor = "temperature sensor" //1
    val smokeSensor = "smoke sensor" //2
    val humiditySensor = "humidity sensor" //3
    val oxygenSensor = "oxygen sensor" //4
    val CO2Sensor = "CO2 Sensor" //5
}
/**
  * A trait for a generic sensor
  *
  * @tparam T type of data managed by the sensor
  * Created by Matteo Gabellini on 05/07/2017.
  */
trait GenericSensor[T] extends Sensor {
    def name: String
    def currentValue: T
}

/**
  * A trait for a sensor that works with an ordered scale of value
  * with a min and a max value
  **/
trait OrderedScaleSensor[T] extends GenericSensor[T] {

    def minValue: T

    def maxValue: T
}

/**
  * A trait for a Numeric Sensor that works in a specific range of
  * numeric value
  */
trait NumericSensor[T] extends OrderedScaleSensor[T] {
    def range: T
}


/**
  * This trait define the basic method of a simulation change strategy
  *
  * @tparam T type of data managed by the sensor.
  *           corresponds to the T type of the sensor see the trait Sensor
  * @tparam S an implementation of a Sensor
  */
trait SimulationStrategy[T, S <: GenericSensor[T]] {
    def execute(sensor: S): T
}

/**
  * This object contains the simulation strategies that a SimulatedSensor can use
  *
  **/
object SimulationStrategies {

    case class MonotonicNumericSimulation(val changeStep: Double)
        extends SimulationStrategy[Double, NumericSensor[Double]] {
        private var increasePhase: Boolean = true

        private def shouldIncrease(sensor: NumericSensor[Double]): Boolean =
            if (increasePhase) {
                increasePhase = sensor.currentValue < sensor.maxValue
                increasePhase
            } else {
                increasePhase = !(sensor.currentValue > sensor.minValue)
                increasePhase
            }

        private def increase(sensor: NumericSensor[Double]): Double = sensor.currentValue + changeStep

        private def decrease(sensor: NumericSensor[Double]): Double = sensor.currentValue - changeStep

        override def execute(sensor: NumericSensor[Double]): Double = if (this shouldIncrease sensor)
            this increase sensor else this decrease sensor
    }

}

/**
  * An abstract decoration of a generic sensor in order to simulate a real sensor
  *
  * @param sensor            a generic sensor implementation to decorate
  * @param millisRefreshRate rate at the sensor change its value
  * @param changeStrategy    the strategy that specifies the sensor behaviour
  */
abstract class SimulatedSensor[T](val sensor: GenericSensor[T],
                                  val millisRefreshRate: Long,
                                  var changeStrategy: SimulationStrategy[T, GenericSensor[T]]) extends GenericSensor[T] {
    var value: T = sensor.currentValue

    //thread safe read-access to the current value
    override def currentValue = sensor.synchronized {
        value
    }

    //thread safe write-access to the current value
    def currentValue_=(i: T): Unit = sensor.synchronized {
        value = i
    }

    private val executor = Executors.newScheduledThreadPool(1)

    def changeLogic(): Runnable = () => this.currentValue = changeStrategy.execute(this)

    this.executor.scheduleAtFixedRate(changeLogic, 0L, millisRefreshRate, TimeUnit.MILLISECONDS)

    def stopGeneration(): Unit = {
        this.executor.shutdown()
    }
}


class SimulatedNumericSensor[T](override val sensor: NumericSensor[T],
                                override val millisRefreshRate: Long,
                                var numericChangeStrategy: SimulationStrategy[T, NumericSensor[T]])
    extends SimulatedSensor[T](sensor, millisRefreshRate, numericChangeStrategy.asInstanceOf[SimulationStrategy[T, GenericSensor[T]]]) with NumericSensor[T] {

    override def minValue: T = sensor.minValue

    override def maxValue: T = sensor.maxValue

    override def range: T = sensor.range

    override def name: String = sensor.name

    override def category: String = sensor.category
}


/**
  * A trait for an object that can be observed with the
  * Scala ReactiveX API
  **/
trait ObservableSensor[T] extends GenericSensor[T] {
    /**
      * Create a Flowable for the sensor values
      *
      * @param refreshPeriod milliseconds between each refresh
      * @return
      */
    def createObservable(refreshPeriod: Long): Flowable[T]
}


/**
  * This class is a decoration of a generic numeric sensor and provides
  * the ReactiveX method to create a Flowable attached to the sensor
  *
  * @param sensor : Sensor[Double]
  *
  **/
class ObservableNumericSensor[T](private val sensor: NumericSensor[T])
    extends ObservableSensor[T] with NumericSensor[T] {

    override def currentValue: T = sensor.currentValue

    override def createObservable(refreshPeriod: Long): Flowable[T] = Flowable.create(emitter => {
        new Thread(() => {
            var currentValue: T = sensor.currentValue
            var tmpPrev = sensor.minValue
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

    override def minValue: T = sensor.minValue

    override def maxValue: T = sensor.maxValue

    override def range: T = sensor.range

    override def name: String = sensor.name

    override def category: String = sensor.category
}