package cell.sensormanagement.sensors

/**
  * Created by Matteo Gabellini on 05/07/2017.
  */
object TestReactiveSensor extends App {
    var tSensor: SimulatedTemperatureSensor = new SimulatedMonotonicTemperatureSensor(0, -40.0, 100.0, 1000, 0.15)
    var oTSensor: ObservableTemperatureSensor = new ObservableTemperatureSensor(tSensor)
    var sensor: Sensor[Double] = tSensor

    println("Test ReactiveTemperatureSensor")
    println("Current Temp:" + tSensor.currentValue)
    Thread.sleep(2000)
    println("Current Temp:" + tSensor.currentValue)
    Thread.sleep(2000)
    println("Current Temp:" + tSensor.currentValue)


    oTSensor.createObservable(1000).subscribe(X => println("Temperature: " + X))

    Thread.sleep(10000)
    tSensor.stopGeneration()
    println("Bye Bye!")
}