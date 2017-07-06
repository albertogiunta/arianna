package cell.sensormanagement.sensors

import org.scalatest.FunSuite

/**
  * This class implements a ScalaTest for a basic TemperatureSensor
  * and its observable decoration
  * Created by Matteo Gabellini on 06/07/2017.
  */
class ObservableTemperatureSensorTest extends FunSuite {

    var tSensor: TemperatureSensor = new SimulatedMonotonicTemperatureSensor(0, -40.0, 100.0, 1000, 0.15)
    var oTSensor: ObservableTemperatureSensor = new ObservableTemperatureSensor(tSensor)
    var sensor: Sensor[Double] = tSensor

    test("Te min value of the sensor must be the same if I access to the object through different interface") {
        assert(tSensor.minValue == oTSensor.minValue == sensor.minValue)
    }
}
