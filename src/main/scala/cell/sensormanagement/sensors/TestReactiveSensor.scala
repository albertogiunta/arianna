package cell.sensormanagement.sensors

/**
  * Created by Matteo Gabellini on 05/07/2017.
  */
object TestReactiveSensor extends App {
    var tSensor = new BasicTemperatureSensor(0, -40.0, 100.0, 100.0 - -40.0)
    var simulatedTempSensor = new SimulatedMonotonicTemperatureSensor(tSensor, 1000, 0.15)
    var oTSensor: ObservableTemperatureSensor = new ObservableTemperatureSensor(simulatedTempSensor)

    var gasSensor = new BasicGasSensor(0, 0, 50.0, 50.0, "CO2")
    var simulatedGasSensor = new SimulatedMonotonicGasSensor(gasSensor, 1000, 0.2)
    var oGSensor = new ObservableGasSensor(simulatedGasSensor)


    println("Test ReactiveTemperatureSensor")
    println("Current Temp:" + simulatedTempSensor.currentValue)
    Thread.sleep(2000)
    println("Current Temp:" + simulatedTempSensor.currentValue)
    Thread.sleep(2000)
    println("Current Temp:" + simulatedTempSensor.currentValue)


    oTSensor.createObservable(1000).subscribe(X => println("Temperature: " + X))

    Thread.sleep(10000)
    simulatedTempSensor.stopGeneration()

    println("Test ReactiveGasSensor")
    println("Current Gas Level:" + simulatedGasSensor.currentValue)
    Thread.sleep(2000)
    println("Current Gas Level:" + simulatedGasSensor.currentValue)
    Thread.sleep(2000)
    println("Current Gas Level:" + simulatedGasSensor.currentValue)


    oGSensor.createObservable(1000).subscribe(X => println("Gas Level: " + X))

    Thread.sleep(10000)
    simulatedGasSensor.stopGeneration()
    println("Bye Bye!")
}