package ontologies

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

    object Temperature extends BasicSensorCategory("temperature sensor", 1)

    object Smoke extends BasicSensorCategory("smoke sensor", 2)

    object Humidity extends BasicSensorCategory("humidity sensor", 3)

    object Oxygen extends BasicSensorCategory("oxygen sensor", 4)

    object CO2 extends BasicSensorCategory("CO2 Sensor", 5)

}