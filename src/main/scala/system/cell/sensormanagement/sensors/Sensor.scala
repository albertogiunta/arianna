package system.cell.sensormanagement.sensors

import java.util.concurrent.{Executors, TimeUnit}

import io.reactivex.{BackpressureStrategy, Flowable}
import system.ontologies.sensor.{SensorCategory, Threshold}


/**
  * A trait for a sensor, it's implementation should have only a public getter method of the current value,
  * because the real management of it should be encapsulated internally
  *
  * @tparam A the type of data managed by the sensor in order to
  *           represents the physical quantity measured
  *           Created by Matteo Gabellini on 05/07/2017.
  */
trait Sensor[A] {
    /**
      * Get the current sensor value of type
      **/
    def currentValue: A

    /**
      * Get the sensor name
      **/
    def name: String

    /**
      * Get the sensor category
      **/
    def category: SensorCategory
}

/**
  * A trait for a sensor that works with a scale of value
  * that has a min and a max value
  *
  * @tparam B the type of data managed by the sensor
  **/
trait ScaledSensor[B] extends Sensor[B] {

    /**
      * Get the minimum value that the sensor can reach
      **/
    def minValue: B

    /**
      * Get the maximum value that the sensor can reach
      **/
    def maxValue: B
}

/**
  * A trait for a Numeric Sensor that works in a specific range of
  * numeric value
  *
  * @tparam C the type of data managed by the sensor
  */
trait NumericSensor[C] extends ScaledSensor[C] {
    /**
      * Get the range of value that the sensor can assume
      **/
    def range: C
}

/**
  * A trait for a sensor that have a threshold where is defined
  * the threshold value and the logic that define when it is exceeded
  *
  * @tparam H the type of data managed by the sensor
  */
trait SensorWithThreshold[H] extends Sensor[H] {
    /**
      * Get the sensor threshold
      **/
    def threshold: Threshold[H]
}

/**
  * This trait define the basic method of a simulation change strategy
  *
  * @tparam D the type of data managed by the sensor.
  *           corresponds to the T type of the sensor (see the trait Sensor)
  * @tparam S an implementation of a Sensor
  */
trait SimulationStrategy[D, S <: Sensor[D]] {
    /**
      * Get the next value for the simulated sensor
      * depending on the status of the sensor and
      * strategy algorithm
      **/
    def nextValue(sensor: S): D
}

/**
  * This object contains the simulation strategies that a SimulatedSensor can use
  *
  **/
object SimulationStrategies {

    /**
      * A trait that define the basic method that a linear simulations must have
      */
    trait LinearSimulation[D, S <: Sensor[D]] extends SimulationStrategy[D, S] {
        def changeStep: D
    }

    /**
      * A simulation strategy implementation for sensors that manage value Double.
      * This strategy models a behaviour for a sensor that change its value according to its scale
      * in a linear way. The sensor increase its value by the specified "changeStep" parameter value until
      * it reaches the max value. When the sensor reaches the max value, starts to decrease its value
      * (always by the "changeStep") until it reaches the min value. After this, its restart to increase
      * and repeats the previous operations
      *
      * @param changeStep the value added/subtracted to/from the sensor value at each step
      *
      **/
    case class LinearDoubleSimulation(val changeStep: Double)
        extends LinearSimulation[Double, NumericSensor[Double]] {
        private var increasePhase: Boolean = true

        private def shouldIncrease(sensor: NumericSensor[Double]): Boolean =
            if (increasePhase) {
                increasePhase = sensor.currentValue < sensor.maxValue
                increasePhase
            } else {
                increasePhase = !(sensor.currentValue > sensor.minValue)
                increasePhase
            }

        private def increasedValueOf(sensor: NumericSensor[Double]): Double =
            if (sensor.currentValue + changeStep <= sensor.maxValue) sensor.currentValue + changeStep else sensor.maxValue

        private def decreasedValueOf(sensor: NumericSensor[Double]): Double =
            if (sensor.currentValue - changeStep >= sensor.minValue) sensor.currentValue - changeStep else sensor.minValue

        override def nextValue(sensor: NumericSensor[Double]): Double = if (this shouldIncrease sensor)
            this increasedValueOf sensor else this decreasedValueOf sensor
    }

    /**
      * A simulation strategy implementation for sensors that manage value Int.
      * This strategy models a behaviour for a sensor that change its value according to its scale
      * in a linear way. The sensor increase its value by the specified "changeStep" parameter value until
      * it reaches the max value. When the sensor reaches the max value, starts to decrease its value
      * (always by the "changeStep") until it reaches the min value. After this, its restart to increase
      * and repeats the previous operations
      *
      * @param changeStep the value added/subtracted to/from the sensor value at each step
      *
      **/
    case class LinearIntSimulation(val changeStep: Int)
        extends LinearSimulation[Int, NumericSensor[Int]] {
        private var increasePhase: Boolean = true

        private def shouldIncrease(sensor: NumericSensor[Int]): Boolean =
            if (increasePhase) {
                increasePhase = sensor.currentValue < sensor.maxValue
                increasePhase
            } else {
                increasePhase = !(sensor.currentValue > sensor.minValue)
                increasePhase
            }

        private def increasedValueOf(sensor: NumericSensor[Int]): Int =
            if (sensor.currentValue + changeStep <= sensor.maxValue) sensor.currentValue + changeStep else sensor.maxValue

        private def decreasedValueOf(sensor: NumericSensor[Int]): Int =
            if (sensor.currentValue - changeStep >= sensor.minValue) sensor.currentValue - changeStep else sensor.minValue

        override def nextValue(sensor: NumericSensor[Int]): Int = if (this shouldIncrease sensor)
            this increasedValueOf sensor else this decreasedValueOf sensor
    }

}

/**
  * An abstract decoration of a sensor in order to simulate a real sensor
  *
  * @param sensor            a generic sensor implementation to decorate
  * @param millisRefreshRate rate at the sensor change its value
  * @param changeStrategy    the strategy that specifies the sensor behaviour
  */
abstract class SimulatedSensor[E](val sensor: Sensor[E],
                                  val millisRefreshRate: Long,
                                  var changeStrategy: SimulationStrategy[E, Sensor[E]])
    extends Sensor[E] {
    @volatile var value: E = sensor.currentValue

    //thread safe read-access to the current value
    override def currentValue = sensor.synchronized {
        value
    }

    //thread safe write-access to the current value
    private def currentValue_=(i: E): Unit = sensor.synchronized {
        value = i
    }

    private val executor = Executors.newScheduledThreadPool(1)

    private def changeLogic(): Runnable = () => this.currentValue = changeStrategy.nextValue(this)

    this.executor.scheduleAtFixedRate(changeLogic, 0L, millisRefreshRate, TimeUnit.MILLISECONDS)

    def stopGeneration(): Unit = {
        this.executor.shutdown()
    }

    override def name: String = sensor.name

    override def category: SensorCategory = sensor.category
}

/**
  * An decoration of a numeric sensor in order to simulate a real sensor
  *
  * @param sensor                a generic sensor implementation to decorate
  * @param millisRefreshRate     rate at the sensor change its value
  * @param numericChangeStrategy the strategy that specifies the sensor behaviour
  */
class SimulatedNumericSensor[F](override val sensor: NumericSensor[F],
                                override val millisRefreshRate: Long,
                                var numericChangeStrategy: SimulationStrategy[F, NumericSensor[F]])
    extends SimulatedSensor[F](sensor,
        millisRefreshRate,
        numericChangeStrategy.asInstanceOf[SimulationStrategy[F, Sensor[F]]])
        with NumericSensor[F] {

    override def minValue: F = sensor.minValue

    override def maxValue: F = sensor.maxValue

    override def range: F = sensor.range
}


/**
  * A trait for an object that can be observed with the
  * Scala ReactiveX API
  **/
trait ObservableSensor[G] extends Sensor[G] {
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
