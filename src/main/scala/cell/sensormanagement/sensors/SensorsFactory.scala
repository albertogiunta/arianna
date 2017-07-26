package cell.sensormanagement.sensors

import ontologies.messages.{DoubleThresholdInfo, SensorInfoFromConfig, SingleThresholdInfo}
import ontologies.sensor.SensorCategories
import ontologies.sensor.SensorCategories._

/**
  * This object contains different method to facilitate the correct sensor creation
  * Created by Matteo Gabellini on 24/07/2017.
  */
object SensorsFactory {
    val DEFAULT_REFRESH_RATE = 1000

    /**
      * A set of method can be used to create simulated sensors
      **/
    object Simulated {
        def createTempSensor(minValue: Double,
                             maxValue: Double,
                             minThreshold: Double,
                             maxThreshold: Double,
                             refreshRate: Long): TemperatureSensor = {
            val sensor = new BasicTemperatureSensor(
                "Simulated Temp Sensor",
                0,
                minValue,
                maxValue,
                new TemperatureThreshold(minThreshold, maxThreshold))
            new SimulatedMonotonicTemperatureSensor(sensor, refreshRate, 0.1)
        }

        def createSmokeSensor(minValue: Double,
                              maxValue: Double,
                              threshold: Double,
                              refreshRate: Long): GasSensor = {
            val sensor = new SmokeSensor("SimulatedSmokeSensor", 0, minValue, maxValue, new SmokeThreshold(30))
            new SimulatedMonotonicGasSensor(sensor, refreshRate, 0.1)
        }

        def createCO2Sensor(minValue: Double,
                            maxValue: Double,
                            threshold: Double,
                            refreshRate: Long): GasSensor = {
            val sensor = new CO2Sensor("SimulatedCO2Sensor", 0, minValue, maxValue, new CO2Threshold(30))
            new SimulatedMonotonicGasSensor(sensor, refreshRate, 0.1)
        }

        def createOxygenSensor(minValue: Double,
                               maxValue: Double,
                               threshold: Double,
                               refreshRate: Long): GasSensor = {
            val sensor = new OxygenSensor("SimulatedOxygenSensor", 0, minValue, maxValue, new OxygenThreshold(30))
            new SimulatedMonotonicGasSensor(sensor, refreshRate, 0.1)
        }

        def createHumiditySensor(minValue: Double,
                                 maxValue: Double,
                                 minThreshold: Double,
                                 maxThreshold: Double,
                                 refreshRate: Long): HumiditySensor = {
            val sensor = new BasicHumiditySensor("SimulatedHumiditySensor", 0, minValue, maxValue, new HumidityThreshold(minThreshold, maxThreshold))
            new SimulatedMonotonicHumiditySensor(sensor, refreshRate, 1)
        }
    }

    /**
      * A method that create the correct observable sensor version
      * relative to the sensor type passed like parameter
      *
      * @param sensor The sensor of which you want
      *               to create the observable decoration
      **/
    def createTheObservableVersion(sensor: Sensor): ObservableSensor[_ <: Any] = sensor.category match {
        case Temperature => new ObservableTemperatureSensor(sensor.asInstanceOf[TemperatureSensor])
        case Smoke | Oxygen | CO2 => new ObservableGasSensor(sensor.asInstanceOf[GasSensor])
        case Humidity => new ObservableHumiditySensor(sensor.asInstanceOf[HumiditySensor])
    }

    /**
      * A method that automatic create the correct sensor instance from a SensorInfoFromConfig
      * loaded from the json file of the cell configuration
      *
      * @param sensorInfo the sensor info loaded from the config json file
      * */
    def createASensorFromConfig(sensorInfo: SensorInfoFromConfig): Sensor = SensorCategories.categoryWithId(sensorInfo.categoryId) match {
        case Temperature =>
            val threshold = sensorInfo.threshold.asInstanceOf[DoubleThresholdInfo]
            Simulated.createTempSensor(
                sensorInfo.minValue,
                sensorInfo.maxValue,
                threshold.lowThreshold,
                threshold.highThreshold, DEFAULT_REFRESH_RATE)
        case Smoke =>
            val threshold = sensorInfo.threshold.asInstanceOf[SingleThresholdInfo]
            Simulated.createSmokeSensor(
                sensorInfo.minValue,
                sensorInfo.maxValue,
                threshold.value, DEFAULT_REFRESH_RATE)
        case Oxygen =>
            val threshold = sensorInfo.threshold.asInstanceOf[SingleThresholdInfo]
            Simulated.createOxygenSensor(
                sensorInfo.minValue,
                sensorInfo.maxValue,
                threshold.value, DEFAULT_REFRESH_RATE)
        case CO2 =>
            val threshold = sensorInfo.threshold.asInstanceOf[SingleThresholdInfo]
            Simulated.createCO2Sensor(
                sensorInfo.minValue,
                sensorInfo.maxValue,
                threshold.value, DEFAULT_REFRESH_RATE)
        case Humidity =>
            val threshold = sensorInfo.threshold.asInstanceOf[DoubleThresholdInfo]
            Simulated.createHumiditySensor(
                sensorInfo.minValue,
                sensorInfo.maxValue,
                threshold.lowThreshold,
                threshold.highThreshold, DEFAULT_REFRESH_RATE)
    }

}
