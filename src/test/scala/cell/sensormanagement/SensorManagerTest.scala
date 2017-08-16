package cell.sensormanagement

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestKit, TestProbe}
import cell.sensormanagement.sensors.SensorsFactory
import com.actors.CustomActor
import com.typesafe.config.ConfigFactory
import ontologies.messages.AriannaJsonProtocol._
import ontologies.messages.Location._
import ontologies.messages.MessageType._
import ontologies.messages._
import ontologies.sensor.SensorCategories
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import spray.json._

import scala.concurrent.duration._

/**
  * Created by Matteo Gabellini on 05/08/2017.
  */
class SensorManagerTest extends TestKit(ActorSystem("SensorManagerTest",
    ConfigFactory.parseString("""akka.loggers = ["akka.testkit.TestEventListener"]""")))
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll {

    val actorName = "SensorManager"

    val tempSensConfig = SensorInfoFromConfig(
        SensorCategories.Temperature.id,
        -10,
        10,
        DoubleThresholdInfo(-1, 1))

    val smokeSensConfig = SensorInfoFromConfig(
        SensorCategories.Smoke.id,
        0,
        50,
        SingleThresholdInfo(30))

    var loadedConfig: CellConfig = CellConfig(
        CellInfo("uriTest", 8080),
        List(tempSensConfig, smokeSensConfig))

    val sensorsNumber = loadedConfig.sensors.size

    val testSensorInfoMsg: SensorInfo = new SensorInfo(1, 0.5)

    val sensorManagerInitMsg = AriadneMessage(Init,
        Init.Subtype.Greetings,
        Location.Self >> Location.Self,
        Greetings(List(loadedConfig.sensors.toJson.toString())))

    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }

    "A Sensor Manager" should {
        "initialize the same number of sensors of the configuration" in {
            val sManager = system.actorOf(Props[SensorManager], actorName)

            EventFilter.info(message = "[" + actorName + "] " + sensorsNumber + " sensor/s initialized", occurrences = 1) intercept {
                sManager ! sensorManagerInitMsg
            }

            system.stop(sManager)
        }

        "sends sensor updates to its parent after the initialization" in {
            val proxy = TestProbe()
            val parent = system.actorOf(Props(new TestParent(proxy.ref, actorName)), "TestParent")

            proxy.send(parent, sensorManagerInitMsg)

            proxy.expectMsg("Update Received")
            system.stop(proxy.ref)
            system.stop(parent)
        }

        "sends an alarm when a sensor exceeds the threshold" in {
            val proxy = TestProbe()
            val parent = system.actorOf(Props(new TestParent(proxy.ref, actorName)), "TestParent")

            proxy.send(parent, sensorManagerInitMsg)
            val valuesBeforeAlarm = tempSensConfig.threshold.asInstanceOf[DoubleThresholdInfo].highThreshold /
                SensorsFactory.DefaultValues.ChangeStep.temperature
            val timeForAlarm = SensorsFactory.DefaultValues.simulationRefreshRate * valuesBeforeAlarm

            proxy.fishForMessage((timeForAlarm + 1) seconds, "") {
                case "Alarm Received" => true
                case "Update Received" => false
            }

            system.stop(proxy.ref)
            system.stop(parent)
        }


    }
}

class TestParent(proxy: ActorRef, sonName: String) extends CustomActor {

    private val self2Self: MessageDirection = Location.Self >> Location.Self

    val child = context.actorOf(Props[SensorManager], sonName)

    override def receive: Receive = {
        case msg@AriadneMessage(Update, Update.Subtype.Sensors, this.self2Self, cnt: SensorsInfoUpdate)
            if sender == child => proxy ! "Update Received"
        case msg@AriadneMessage(Alarm, _, _, cnt: SensorInfo) if sender == child =>
            proxy ! "Alarm Received"
        case x => child forward x
    }
}

