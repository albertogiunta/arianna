package cell.sensormanagement.sensors

import ontologies.messages.{DoubleThresholdInfo, SensorInfoFromConfig, SingleThresholdInfo}
import ontologies.sensor.SensorCategories
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by Matteo Gabellini on 01/08/2017.
  */
@RunWith(classOf[JUnitRunner])
class SensorsFactoryTest extends FlatSpec with Matchers {

    //Temperature sensor 
    val tSensorMinValue = 0
    val tSensorMaxValue = 100
    val tSensorLowThreshold = 20
    val tSensorHighThreshold = 80
    val tSensConfig: SensorInfoFromConfig = new SensorInfoFromConfig(
        SensorCategories.Temperature.id,
        tSensorMinValue,
        tSensorMaxValue,
        new DoubleThresholdInfo(tSensorLowThreshold, tSensorHighThreshold)
    )

    val tSensor = SensorsFactory.createASensorFromConfig(tSensConfig)

    "The sensor factory" should "create a temperature sensor from a temperature sensor config info" in {
        tSensor shouldBe a[TemperatureSensor]
    }

    "The sensor factory" should "create a temperature sensor with the same max value specified in the config" in {
        tSensor.asInstanceOf[TemperatureSensor].maxValue should be(tSensorMaxValue)
    }

    "The sensor factory" should "create a temperature sensor with the same min value specified in the config" in {
        tSensor.asInstanceOf[TemperatureSensor].minValue should be(tSensorMinValue)
    }

    "The sensor factory" should "create a temperature sensor with the same high threshold value specified in the config" in {
        tSensor.asInstanceOf[TemperatureSensor]
            .threshold
            .asInstanceOf[TemperatureThreshold]
            .high should be(tSensorHighThreshold)
    }

    "The sensor factory" should "create a temperature sensor with the same low threshold value specified in the config" in {
        tSensor.asInstanceOf[TemperatureSensor]
            .threshold
            .asInstanceOf[TemperatureThreshold]
            .low should be(tSensorLowThreshold)
    }

    "The sensor factory" should "create a Observable Version of the temperature sensor" in {
        SensorsFactory.createTheObservableVersion(tSensor) shouldBe a[ObservableTemperatureSensor]
    }

    //Humidity sensor
    val hSensorMinValue = 0
    val hSensorMaxValue = 100
    val hSensorLowThreshold = 20
    val hSensorHighThreshold = 80
    val hSensConfig: SensorInfoFromConfig = new SensorInfoFromConfig(
        SensorCategories.Humidity.id,
        hSensorMinValue,
        hSensorMaxValue,
        new DoubleThresholdInfo(hSensorLowThreshold, hSensorHighThreshold)
    )

    val hSensor = SensorsFactory.createASensorFromConfig(hSensConfig)

    "The sensor factory" should "create a humidity sensor from a humidity sensor config info" in {
        hSensor shouldBe a[HumiditySensor]
    }

    "The sensor factory" should "create a humidity sensor with the same max value specified in the config" in {
        hSensor.asInstanceOf[HumiditySensor].maxValue should be(hSensorMaxValue)
    }

    "The sensor factory" should "create a humidity sensor with the same min value specified in the config" in {
        hSensor.asInstanceOf[HumiditySensor].minValue should be(hSensorMinValue)
    }

    "The sensor factory" should "create a humidity sensor with the same high threshold value specified in the config" in {
        hSensor.asInstanceOf[HumiditySensor]
            .threshold
            .asInstanceOf[HumidityThreshold]
            .high should be(hSensorHighThreshold)
    }

    "The sensor factory" should "create a humidity sensor with the same low threshold value specified in the config" in {
        hSensor.asInstanceOf[HumiditySensor]
            .threshold
            .asInstanceOf[HumidityThreshold]
            .low should be(hSensorLowThreshold)
    }

    "The sensor factory" should "create a Observable Version of the humidity sensor" in {
        SensorsFactory.createTheObservableVersion(hSensor) shouldBe a[ObservableHumiditySensor]
    }

    //Smoke Sensor
    val sSensorMinValue = 0
    val sSensorMaxValue = 100
    val sSensorThreshold = 70
    val sSensConfig: SensorInfoFromConfig = new SensorInfoFromConfig(
        SensorCategories.Smoke.id,
        sSensorMinValue,
        sSensorMaxValue,
        new SingleThresholdInfo(sSensorThreshold)
    )

    val sSensor = SensorsFactory.createASensorFromConfig(sSensConfig)

    "The sensor factory" should "create a gas sensor from a smoke sensor config info" in {
        sSensor shouldBe a[GasSensor]
    }

    "The sensor factory" should "create a gas sensor from a smoke sensor config " +
        "that have the smoke category id" in {
        sSensor.category.id should be(SensorCategories.Smoke.id)
    }

    "The sensor factory" should "create a gas sensor from a smoke sensor config " +
        "that have the smoke threshold" in {
        sSensor.asInstanceOf[GasSensor].threshold shouldBe a[SmokeThreshold]
    }

    "The sensor factory" should "create a gas sensor from a smoke sensor config " +
        " with the same max value specified in the config" in {
        sSensor.asInstanceOf[GasSensor].maxValue should be(sSensorMaxValue)
    }

    "The sensor factory" should "create a gas sensor from a smoke sensor config " +
        "with the same min value specified in the config" in {
        sSensor.asInstanceOf[GasSensor].minValue should be(sSensorMinValue)
    }

    "The sensor factory" should "create a gas sensor from a smoke sensor config" +
        " with the same threshold value specified in the config" in {
        sSensor.asInstanceOf[GasSensor]
            .threshold
            .asInstanceOf[SmokeThreshold]
            .value should be(sSensorThreshold)
    }

    "The sensor factory" should "create a observable gas sensor from the smoke sensor" in {
        SensorsFactory.createTheObservableVersion(sSensor) shouldBe a[ObservableGasSensor]
    }

    //CO2 Sensor
    val cSensorMinValue = 0
    val cSensorMaxValue = 100
    val cSensorThreshold = 70
    val cSensConfig: SensorInfoFromConfig = new SensorInfoFromConfig(
        SensorCategories.CO2.id,
        cSensorMinValue,
        cSensorMaxValue,
        new SingleThresholdInfo(cSensorThreshold)
    )

    val cSensor = SensorsFactory.createASensorFromConfig(cSensConfig)

    "The sensor factory" should "create a gas sensor from a co2 sensor config info" in {
        cSensor shouldBe a[GasSensor]
    }

    "The sensor factory" should "create a gas sensor from a co2 sensor config " +
        "that have the smoke category id" in {
        cSensor.category.id should be(SensorCategories.CO2.id)
    }

    "The sensor factory" should "create a gas sensor from a co2 sensor config " +
        "that have the co2 threshold" in {
        cSensor.asInstanceOf[GasSensor].threshold shouldBe a[CO2Threshold]
    }

    "The sensor factory" should "create a gas sensor from a co2 sensor config " +
        "with the same max value specified in the config" in {
        cSensor.asInstanceOf[GasSensor].maxValue should be(cSensorMaxValue)
    }

    "The sensor factory" should "create a gas sensor from a co2 sensor config " +
        "with the same min value specified in the config" in {
        cSensor.asInstanceOf[GasSensor].minValue should be(cSensorMinValue)
    }

    "The sensor factory" should "create a gas sensor from a co2 sensor config " +
        "with the same threshold value specified in the config" in {
        cSensor.asInstanceOf[GasSensor]
            .threshold
            .asInstanceOf[CO2Threshold]
            .value should be(cSensorThreshold)
    }

    "The sensor factory" should "create a observable gas sensor from a co2 sensor config" in {
        SensorsFactory.createTheObservableVersion(cSensor) shouldBe a[ObservableGasSensor]
    }

    //Oxygen sensor
    val oSensorMinValue = 0
    val oSensorMaxValue = 100
    val oSensorThreshold = 70
    val oSensConfig: SensorInfoFromConfig = new SensorInfoFromConfig(
        SensorCategories.Oxygen.id,
        oSensorMinValue,
        oSensorMaxValue,
        new SingleThresholdInfo(oSensorThreshold)
    )

    val oSensor = SensorsFactory.createASensorFromConfig(oSensConfig)

    "The sensor factory" should "create a gas sensor from a oxygen sensor config info" in {
        oSensor shouldBe a[GasSensor]
    }

    "The sensor factory" should "create a gas sensor from a oxygen sensor config " +
        "that have the smoke category id" in {
        oSensor.category.id should be(SensorCategories.Oxygen.id)
    }

    "The sensor factory" should "create a gas sensor from a oxygen sensor config " +
        "that have the oxygen threshold" in {
        oSensor.asInstanceOf[GasSensor].threshold shouldBe a[OxygenThreshold]
    }

    "The sensor factory" should "create a gas sensor from a oxygen sensor config " +
        "with the same max value specified in the config" in {
        oSensor.asInstanceOf[GasSensor].maxValue should be(oSensorMaxValue)
    }

    "The sensor factory" should "create a gas sensor from a oxygen sensor config " +
        "with the same min value specified in the config" in {
        oSensor.asInstanceOf[GasSensor].minValue should be(oSensorMinValue)
    }

    "The sensor factory" should "create a gas sensor from a oxygen sensor config " +
        "with the same threshold value specified in the config" in {
        oSensor.asInstanceOf[GasSensor]
            .threshold
            .asInstanceOf[OxygenThreshold]
            .value should be(oSensorThreshold)
    }

    "The sensor factory" should "create a observable gas sensor from a oxygen sensor config" in {
        SensorsFactory.createTheObservableVersion(oSensor) shouldBe a[ObservableGasSensor]
    }
}
