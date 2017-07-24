package cell.sensormanagement.sensors

import java.util.concurrent.{Executors, TimeUnit}

import cell.sensormanagement.Threshold
import io.reactivex.{BackpressureStrategy, Flowable}
import ontologies.SensorCategory

/**
  * a trait for a sensor
  **/
trait Sensor {
    def name: String

    def category: SensorCategory
}

/**
  * A trait for a generic sensor
  *
  * @tparam A the type of data managed by the sensor
  *           Created by Matteo Gabellini on 05/07/2017.
  */
trait GenericSensor[A] extends Sensor {
    def name: String

    def currentValue: A
}

/**
  * A trait for a sensor that works with an ordered scale of value
  * with a min and a max value
  **/
trait OrderedScaleSensor[B] extends GenericSensor[B] {

    def minValue: B

    def maxValue: B
}

/**
  * A trait for a Numeric Sensor that works in a specific range of
  * numeric value
  */
trait NumericSensor[C] extends OrderedScaleSensor[C] {
    def range: C
}

/**
  * A trait for a sensor that have a threshold where is defined
  * the threshold value and the logic that define when it is exceeded
  */
trait SensorWithThreshold[H] extends GenericSensor[H] {
    def threshold: Threshold[H]
}

/**
  * This trait define the basic method of a simulation change strategy
  *
  * @tparam D the type of data managed by the sensor.
  *           corresponds to the T type of the sensor see the trait Sensor
  * @tparam S an implementation of a Sensor
  */
trait SimulationStrategy[D, S <: GenericSensor[D]] {
    def execute(sensor: S): D
}

/**
  * This object contains the simulation strategies that a SimulatedSensor can use
  *
  **/
object SimulationStrategies {

    case class MonotonicDoubleSimulation(val changeStep: Double)
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

    case class MonotonicIntSimulation(val changeStep: Int)
        extends SimulationStrategy[Int, NumericSensor[Int]] {
        private var increasePhase: Boolean = true

        private def shouldIncrease(sensor: NumericSensor[Int]): Boolean =
            if (increasePhase) {
                increasePhase = sensor.currentValue < sensor.maxValue
                increasePhase
            } else {
                increasePhase = !(sensor.currentValue > sensor.minValue)
                increasePhase
            }

        private def increase(sensor: NumericSensor[Int]): Int = sensor.currentValue + changeStep

        private def decrease(sensor: NumericSensor[Int]): Int = sensor.currentValue - changeStep

        override def execute(sensor: NumericSensor[Int]): Int = if (this shouldIncrease sensor)
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
abstract class SimulatedSensor[E](val sensor: GenericSensor[E],
                                  val millisRefreshRate: Long,
                                  var changeStrategy: SimulationStrategy[E, GenericSensor[E]]) extends GenericSensor[E] {
    var value: E = sensor.currentValue

    //thread safe read-access to the current value
    override def currentValue = sensor.synchronized {
        value
    }

    //thread safe write-access to the current value
    private def currentValue_=(i: E): Unit = sensor.synchronized {
        value = i
    }

    private val executor = Executors.newScheduledThreadPool(1)

    def changeLogic(): Runnable = () => this.currentValue = changeStrategy.execute(this)

    this.executor.scheduleAtFixedRate(changeLogic, 0L, millisRefreshRate, TimeUnit.MILLISECONDS)

    def stopGeneration(): Unit = {
        this.executor.shutdown()
    }
}


class SimulatedNumericSensor[F](override val sensor: NumericSensor[F],
                                override val millisRefreshRate: Long,
                                var numericChangeStrategy: SimulationStrategy[F, NumericSensor[F]])
    extends SimulatedSensor[F](sensor, millisRefreshRate, numericChangeStrategy.asInstanceOf[SimulationStrategy[F, GenericSensor[F]]]) with NumericSensor[F] {

    override def minValue: F = sensor.minValue

    override def maxValue: F = sensor.maxValue

    override def range: F = sensor.range

    override def name: String = sensor.name

    override def category: SensorCategory = sensor.category
}


/**
  * A trait for an object that can be observed with the
  * Scala ReactiveX API
  **/
trait ObservableSensor[G] extends GenericSensor[G] {
    /**
      * Create a Flowable for the sensor values
      *
      * @param refreshPeriod milliseconds between each refresh
      * @return
      */
    def createObservable[G](refreshPeriod: Long): Flowable[G]

    /**
      * Stop the observation on the decorated Sensor
      **/
    def stopObservation(): Unit
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

    private var continueObservation = true;

    override def currentValue: T = sensor.currentValue

    override def createObservable[T](refreshPeriod: Long): Flowable[T] = Flowable.create[T](emitter => {
        continueObservation = true
        new Thread(() => {
            var currentValue: T = sensor.currentValue.asInstanceOf[T]
            var tmpPrev = sensor.minValue.asInstanceOf[T]
            while (continueObservation) {
                try {
                    currentValue = sensor.currentValue.asInstanceOf[T]
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

    override def category: SensorCategory = sensor.category

    override def stopObservation(): Unit = continueObservation = false
}
