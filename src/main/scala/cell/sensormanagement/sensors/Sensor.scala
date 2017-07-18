package cell.sensormanagement.sensors

import io.reactivex.Flowable

/**
  * A trait for a basic sensor
  * Created by Matteo Gabellini on 05/07/2017.
  */
trait Sensor[T] {
    def currentValue: T

    def minValue: T

    def maxValue: T

    def range: T
}

trait ObservableSensor[T] extends Sensor[T] {
    /**
      * Create a Flowable for the sensor values
      *
      * @param refreshPeriod milliseconds between each refresh
      * @return
      */
    def createObservable(refreshPeriod: Long): Flowable[T]
}

