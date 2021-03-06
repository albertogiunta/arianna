package system.ontologies.sensor

/**
  * A trait for a category of sensor
  * Created by Matteo Gabellini on 22/07/2017.
  */
trait SensorCategory {
    def name: String

    def id: Int
}

abstract class BasicSensorCategory(val name: String, val id: Int) extends SensorCategory

object SensorCategories {

    object Temperature extends BasicSensorCategory("Temperature", 1)

    object Smoke extends BasicSensorCategory("Smoke", 2)

    object Humidity extends BasicSensorCategory("Humidity", 3)

    object Oxygen extends BasicSensorCategory("Oxygen", 4)

    object CO2 extends BasicSensorCategory("CO2", 5)


    def categoryWithId(id: Int): SensorCategory = id match {
        case 1 => Temperature
        case 2 => Smoke
        case 3 => Humidity
        case 4 => Oxygen
        case 5 => CO2
    }

    def thresholdNumberValue(category: SensorCategory): Int = category match {
        case Temperature => 2
        case Smoke => 1
        case Humidity => 2
        case Oxygen => 1
        case CO2 => 1
    }
}

