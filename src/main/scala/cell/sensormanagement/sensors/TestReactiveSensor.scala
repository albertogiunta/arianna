package cell.sensormanagement.sensors

/**
  * Created by Matteo Gabellini on 05/07/2017.
  */
object TestReactiveSensor extends App {
    var tSensor = new BasicTemperatureSensor("tSensor", 0, -40.0, 100.0, 100.0 - -40.0)
    var simulatedTempSensor = new SimulatedMonotonicTemperatureSensor(tSensor, 1000, 0.15)
    var oTSensor: ObservableTemperatureSensor = new ObservableTemperatureSensor(simulatedTempSensor)


    println("Test Simulated Generation" + oTSensor.name)
    println("Current Temp:" + simulatedTempSensor.currentValue)
    Thread.sleep(2000)
    println("Current Temp:" + simulatedTempSensor.currentValue)
    Thread.sleep(2000)
    println("Current Temp:" + simulatedTempSensor.currentValue)


    println("...attach a reactive observable to " + oTSensor.name)
    oTSensor.createObservable(1000).subscribe(X => println("Temperature: " + X))

    Thread.sleep(10000)
    simulatedTempSensor.stopGeneration()


    var smokeSensor = new SmokeSensor("SmokeSensor", 0, 0, 50.0, 50.0)
    var simulatedSmokeSensor = new SimulatedMonotonicGasSensor(smokeSensor, 1000, 0.2)
    var oSSensor = new ObservableGasSensor(simulatedSmokeSensor)
    monotonicGasSensorTest(smokeSensor, simulatedSmokeSensor, oSSensor)


    var co2Sensor = new CO2Sensor("CO2Sensor", 0, 0, 100.0, 100.0)
    var simulatedCO2Sensor = new SimulatedMonotonicGasSensor(co2Sensor, 1000, 0.5)
    var oCSensor = new ObservableGasSensor(simulatedCO2Sensor)
    monotonicGasSensorTest(co2Sensor, simulatedCO2Sensor, oCSensor)

    var oxygenSensor = new OxygenSensor("OxygenSensor", 0, 0, 70.0, 70.0)
    var simulatedOxygenSensor = new SimulatedMonotonicGasSensor(oxygenSensor, 1000, 0.38)
    var oOSensor = new ObservableGasSensor(simulatedOxygenSensor)
    monotonicGasSensorTest(oxygenSensor, simulatedOxygenSensor, oOSensor)


    var hSensor = new BasicHumiditySensor("humiditySensor", 0, 0, 100, 100)
    var simulatedHumiditySensor = new SimulatedMonotonicHumiditySensor(hSensor, 1000, 1)
    var oHSensor: ObservableHumiditySensor = new ObservableHumiditySensor(simulatedHumiditySensor)

    println("Test Simulated Generation" + oHSensor.name)
    println("Current Humidity:" + simulatedHumiditySensor.currentValue + "%")
    Thread.sleep(2000)
    println("Current Humidity:" + simulatedHumiditySensor.currentValue + "%")
    Thread.sleep(2000)
    println("Current Humidity:" + simulatedHumiditySensor.currentValue + "%")


    println("...attach a reactive observable to " + oTSensor.name)
    oHSensor.createObservable(1000).subscribe(X => println("Humidity: " + X + "%"))

    Thread.sleep(10000)
    simulatedHumiditySensor.stopGeneration()


    Thread.sleep(2000)
    println("Bye Bye!")


    private def monotonicGasSensorTest(sensor: Sensor,
                                       simulatedGasSensor: SimulatedMonotonicGasSensor,
                                       oSensor: ObservableGasSensor) {
        println("Test Simulated Generation for " + oSensor.name)
        println("Current " + oSensor.gasMeasured + " Level:" + simulatedGasSensor.currentValue)
        Thread.sleep(2000)
        println("Current " + oSensor.gasMeasured + " Level:" + simulatedGasSensor.currentValue)
        Thread.sleep(2000)
        println("Current " + oSensor.gasMeasured + " Level:" + simulatedGasSensor.currentValue)

        println("...attach a reactive observable to " + oSensor.name)
        oSensor.createObservable(1000).subscribe(X => println(oSensor.gasMeasured + " Level: " + X))

        Thread.sleep(10000)
        simulatedGasSensor.stopGeneration()
    }
}
