package cell.sensormanagement.sensors

/**
  * Created by Matteo Gabellini on 05/07/2017.
  */
object TestReactiveSensor extends App {
    var tSensor = new BasicTemperatureSensor(0, -40.0, 100.0, 100.0 - -40.0)
    var simulatedTempSensor: SimulatedMonotonicTemperatureSensor = new SimulatedMonotonicTemperatureSensor(tSensor, 1000, 0.15)
    var oTSensor: ObservableTemperatureSensor = new ObservableTemperatureSensor(simulatedTempSensor)
    var sensor: Sensor[Double] = tSensor

    println("Test ReactiveTemperatureSensor")
    println("Current Temp:" + simulatedTempSensor.currentValue)
    Thread.sleep(2000)
    println("Current Temp:" + simulatedTempSensor.currentValue)
    Thread.sleep(2000)
    println("Current Temp:" + simulatedTempSensor.currentValue)


    oTSensor.createObservable(1000).subscribe(X => println("Temperature: " + X))

    Thread.sleep(10000)
    simulatedTempSensor.stopGeneration()
    println("Bye Bye!")
}