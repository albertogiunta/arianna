package ontologies.sensor

/**
  * This trait define the basic method to implements a threshold
  * for an entity
  * Created by Matteo Gabellini on 21/07/2017.
  */
trait Threshold[T] {
    def hasBeenExceeded(currentSensorValue: T): Boolean
}

trait SingleThreshold[T] extends Threshold[T] {
    def value: T
}

trait DoubleThreshold[T] extends Threshold[T] {
    def minValue: T

    def maxValue: T
}