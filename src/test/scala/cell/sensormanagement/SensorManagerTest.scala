package cell.sensormanagement

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestKit, TestProbe}
import com.actors.CustomActor
import com.typesafe.config.ConfigFactory
import ontologies.messages.AriannaJsonProtocol._
import ontologies.messages.Location._
import ontologies.messages.MessageType._
import ontologies.messages._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import spray.json._

import scala.concurrent.duration._
import scala.io.Source

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
    val configPath: String = "res/json/cell/cell4Test.json"
    val config = Source.fromFile(configPath).getLines.mkString
    var loadedConfig: CellConfig = config.parseJson.convertTo[CellConfig]

    val testSensorInfoMsg: SensorInfo = new SensorInfo(1, 0.5)

    val sensorManagerInitMsg = AriadneMessage(Init,
        Init.Subtype.Greetings,
        Location.Self >> Location.Self,
        Greetings(List(loadedConfig.sensors.toJson.toString())))

    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }

    "A Sensor Manager" should {
        "initialize the same number of sensor of the configuration" in {
            val sManager = system.actorOf(Props[SensorManager], actorName)

            EventFilter.info(message = "[" + actorName + "] 3 sensor/s initialized", occurrences = 1) intercept {
                sManager ! sensorManagerInitMsg
            }

            system.stop(sManager)
        }
    }

    "A Sensor Manager" should {
        "sends sensor updates to its parent after the initialization" in {
            val proxy = TestProbe()
            val parent = system.actorOf(Props(new TestParent(proxy.ref, actorName)), "TestParent")

            proxy.send(parent, sensorManagerInitMsg)

            proxy.expectMsg("Update Received")
            system.stop(proxy.ref)
            system.stop(parent)
        }
    }

    "A Sensor Manager" should {
        "sends an alarm when a sensor exceeds the threshold" in {
            val proxy = TestProbe()
            val parent = system.actorOf(Props(new TestParent(proxy.ref, actorName)), "TestParent")
            //Time related to the threshold specified in the configuration file for the test
            //The monotonic humidity sensor must exceed the threshold after 10 seconds
            proxy.send(parent, sensorManagerInitMsg)
            proxy.ignoreMsg {
                case msg: String => msg != "Alarm Received"
            }
            proxy.expectMsg(15 seconds, "Alarm Received")

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

