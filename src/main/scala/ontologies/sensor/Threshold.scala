package ontologies.sensor

/**
  * This trait define the basic method to implements a threshold
  * for a sensor
  * Created by Matteo Gabellini on 21/07/2017.
  */
trait Threshold[T] {
    /**
      * Check if the parameter value exceeded the threshold
      *
      * @param currentSensorValue The value of the sensor of which
      *                           you want to know if it has exceeded
      *                           the threshold
      **/
    def hasBeenExceeded(currentSensorValue: T): Boolean
}

/**
  * This trait define a Threshold with a single value
  **/
trait SingleThreshold[T] extends Threshold[T] {
    def value: T
}

trait DoubleThreshold[T] extends Threshold[T] {
    def minValue: T

    def maxValue: T
}