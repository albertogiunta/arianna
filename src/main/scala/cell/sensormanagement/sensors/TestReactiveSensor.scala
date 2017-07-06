package cell.sensormanagement.sensors

/**
  * Created by Matteo Gabellini on 05/07/2017.
  */
object TestReactiveSensor extends App {
    val tempSensorA1 = new ObservableTemperatureSensor(new SimulatedMonotonicTemperatureSensor(0, -40.0, 100.0, 1000, 0.15))


}