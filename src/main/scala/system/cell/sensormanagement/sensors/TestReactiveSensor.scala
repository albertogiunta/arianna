package system.cell.sensormanagement.sensors

import io.reactivex.Flowable

/**
  * Created by Matteo Gabellini on 05/07/2017.
  */
object TestReactiveSensor extends App {
    var tSensor = new BasicTemperatureSensor("tSensor", 0, -40.0, 100.0, new TemperatureThreshold(-10, 50))
    var simulatedTempSensor = new SimulatedLinearTemperatureSensor(tSensor, 1000, 0.15)
    var oTSensor: ObservableTemperatureSensor = new ObservableTemperatureSensor(simulatedTempSensor)


    println("Test Simulated Generation" + oTSensor.name)
    println("Current Temp:" + simulatedTempSensor.currentValue)
    Thread.sleep(2000)
    println("Current Temp:" + simulatedTempSensor.currentValue)
    Thread.sleep(2000)
    println("Current Temp:" + simulatedTempSensor.currentValue)


    println("...attach a reactive observable to " + oTSensor.name)
    oTSensor.createObservable(1000).subscribe((X: Double) => println("Temperature: " + X))

    Thread.sleep(10000)
    simulatedTempSensor.stopGeneration()
    //oTSensor.stopObservation()


    var smokeSensor = new SmokeSensor("SmokeSensor", 0, 0, 50.0, new SmokeThreshold(30))
    var simulatedSmokeSensor = new SimulatedLinearGasSensor(smokeSensor, 1000, 0.2)
    var oSSensor = new ObservableGasSensor(simulatedSmokeSensor)
    linearGasSensorTest(smokeSensor, simulatedSmokeSensor, oSSensor)


    var co2Sensor = new CO2Sensor("CO2Sensor", 0, 0, 100.0, new CO2Threshold(50))
    var simulatedCO2Sensor = new SimulatedLinearGasSensor(co2Sensor, 1000, 0.5)
    var oCSensor = new ObservableGasSensor(simulatedCO2Sensor)
    linearGasSensorTest(co2Sensor, simulatedCO2Sensor, oCSensor)

    var oxygenSensor = new OxygenSensor("OxygenSensor", 0, 0, 70.0, new OxygenThreshold(25))
    var simulatedOxygenSensor = new SimulatedLinearGasSensor(oxygenSensor, 1000, 0.38)
    var oOSensor = new ObservableGasSensor(simulatedOxygenSensor)
    linearGasSensorTest(oxygenSensor, simulatedOxygenSensor, oOSensor)


    var hSensor = new BasicHumiditySensor("humiditySensor", 0, 0, 100, new HumidityThreshold(20, 80))
    var simulatedHumiditySensor = new SimulatedLinearHumiditySensor(hSensor, 1000, 1)
    var oHSensor: ObservableHumiditySensor = new ObservableHumiditySensor(simulatedHumiditySensor)

    println("Test Simulated Generation" + oHSensor.name)
    println("Current Humidity:" + simulatedHumiditySensor.currentValue + "%")
    Thread.sleep(2000)
    println("Current Humidity:" + simulatedHumiditySensor.currentValue + "%")
    Thread.sleep(2000)
    println("Current Humidity:" + simulatedHumiditySensor.currentValue + "%")


    println("...attach a reactive observable to " + oHSensor.name)
    val flowVal: Flowable[Int] = oHSensor.createObservable(1000)
    //Try also a threshold checking mode
    flowVal.filter(X => X > 8).subscribe(X => println("THRESHOLD EXCEEDED"))
    flowVal.subscribe(X => println("Humidity: " + X + "%"))


    Thread.sleep(10000)
    simulatedHumiditySensor.stopGeneration()


    Thread.sleep(2000)
    println("Bye Bye!")


    private def linearGasSensorTest(sensor: Sensor,
                                    simulatedGasSensor: SimulatedLinearGasSensor,
                                    oSensor: ObservableGasSensor) {
        println("Test Simulated Generation for " + oSensor.name)
        println("Current " + oSensor.gasMeasured + " Level:" + simulatedGasSensor.currentValue)
        Thread.sleep(2000)
        println("Current " + oSensor.gasMeasured + " Level:" + simulatedGasSensor.currentValue)
        Thread.sleep(2000)
        println("Current " + oSensor.gasMeasured + " Level:" + simulatedGasSensor.currentValue)

        println("...attach a reactive observable to " + oSensor.name)
        oSensor.createObservable(1000).subscribe((X: Double) => println(oSensor.gasMeasured + " Level: " + X))

        Thread.sleep(10000)
        simulatedGasSensor.stopGeneration()
    }
}
