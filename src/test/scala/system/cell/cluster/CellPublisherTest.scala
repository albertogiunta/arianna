package system.cell.cluster

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.actors.{ClusterMembersListener, CustomActor}
import com.typesafe.config.{Config, ConfigFactory}
import com.utils.WatchDog
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import system.names.NamingSystem
import system.ontologies.messages.Location._
import system.ontologies.messages.MessageType.Handshake.Subtype.Acknowledgement
import system.ontologies.messages.MessageType.{Alarm, Handshake, Info, Init, Topology, Update}
import system.ontologies.messages._

import scala.concurrent.duration._

/**
  * Created by Matteo Gabellini on 12/08/2017.
  */
class CellPublisherTest extends TestKit(ActorSystem("CellPublisherTest", CellPublisherTest.config))
    with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

    val initMsg = AriadneMessage(
        Init,
        Init.Subtype.Greetings,
        Location.Cell >> Location.Self,
        Greetings(List(ClusterMembersListener.greetings)))


    val infoRequestMsg = AriadneMessage(
        Info,
        Info.Subtype.Request,
        Location.Self >> Location.Self,
        SensorsInfoUpdate.empty
    )

    val simulatedResponse = AriadneMessage(
        Info,
        Info.Subtype.Response,
        Location.Self >> Location.Self,
        SensorsInfoUpdate.empty)

    val handshakeAck = AriadneMessage(
        Handshake,
        Acknowledgement,
        Location.Master >> Location.Cell,
        Empty())

    val sensorUpdate = AriadneMessage(Update,
        Update.Subtype.Sensors,
        Location.Self >> Location.Self,
        SensorsInfoUpdate.empty)

    val practicabilityMsg = AriadneMessage(
        Update,
        Update.Subtype.Practicability,
        Location.Cell >> Location.Cell,
        PracticabilityUpdate(RoomID.empty, 10)
    )

    val alarmMsg = AriadneMessage(
        Alarm,
        Alarm.Subtype.FromCell,
        Location.Cell >> Location.Cluster,
        AlarmContent(
            CellInfo.empty,
            RoomInfo(
                RoomID.empty,
                Coordinates(
                    Point(0, 0),
                    Point(1, 1),
                    Point(2, 2),
                    Point(3, 3)),
                Point(0, 0),
                true,
                false,
                10,
                10)
        )
    )


    val topologyAck = AriadneMessage(
        Topology,
        Topology.Subtype.Acknowledgement,
        Location.Self >> Location.Self,
        CellInfo.empty
    )

    val currentPeopleMsg = AriadneMessage(
        Update,
        Update.Subtype.CurrentPeople,
        Location.User >> Location.Cell,
        CurrentPeopleUpdate(RoomID.empty, 0))


    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }

    "A Cell Publisher" should {
        val proxy = TestProbe()
        val parent = TestActorRef(Props(new TestParentForPublisher(proxy.ref)), "TestParent") //system.actorOf(Props(new TestParentForPublisher(proxy.ref)), "TestParent")

        "after receiving the init message, send a info request to its parent" in {
            proxy.send(parent, initMsg)
            proxy.expectMsg(CellPublisherTest.infoRequestReceivedResponse)
        }
    
        "after the parent response relative to the previous info request, sends a handshake message to the system.master" in {
            proxy.send(parent, simulatedResponse)
            proxy.expectMsg(CellPublisherTest.handshakeResponse)
        }

        "if the acknowledge isn't received between the first sending and the watchdog notification," +
            "request another time the info to the father and resend the handshake message" in {
            proxy.expectMsg((WatchDog.waitTime + 1000) millisecond, "Info Request Received")
            proxy.send(parent, simulatedResponse)
            proxy.expectMsg(CellPublisherTest.handshakeResponse)
        }

        "after receiving the handshake ack, not resend another handshake message" in {
            proxy.send(parent, handshakeAck)
            proxy.expectNoMsg((WatchDog.waitTime + 1000) millisecond)
        }

        "when has \"cultured\", forward messages to the mediator of type Alarm" in {
            proxy.send(parent, alarmMsg)
            proxy.expectMsg(CellPublisherTest.alarmResponse)

        }

        "when has \"cultured\", forward messages to the mediator of type Sensor Update" in {
            proxy.send(parent, sensorUpdate)
            proxy.expectMsg(CellPublisherTest.sensorUpdateResponse)
        }

        "when has \"cultured\", forward messages to the mediator of type Practicability" in {
            proxy.send(parent, practicabilityMsg)
            proxy.expectMsg(CellPublisherTest.practicabilityResponse)
        }

        "when has \"cultured\", forward messages to the mediator of type Current People" in {
            proxy.send(parent, currentPeopleMsg)
            proxy.expectMsg(CellPublisherTest.currentPeopleResponse)
        }

        "when has \"cultured\", forward messages to the mediator of type topology ack" in {
            proxy.send(parent, topologyAck)
            proxy.expectMsg(CellPublisherTest.topologyAckResponse)
        }
    }

}

object CellPublisherTest {
    val path2Project: String = Paths.get("").toFile.getAbsolutePath
    val configPath: String = path2Project + "/res/conf/akka/testCellPublisher.conf"

    val config: Config = ConfigFactory.parseFile(new File(configPath)).withFallback(ConfigFactory.load()).resolve()

    val infoRequestReceivedResponse = "Info Request Received"
    val alarmResponse = "Alarm sent on the Cluster"
    val handshakeResponse = "Handshake Received"
    val sensorUpdateResponse = "Sensor update sent on the Cluster"
    val practicabilityResponse = "Practicability update sent on the Cluster"
    val currentPeopleResponse = "Current people number sent on the Cluster"
    val topologyAckResponse = "Topology Ack sent on the Cluster"
}


class TestParentForPublisher(proxy: ActorRef) extends CustomActor {

    val selfToSelf: MessageDirection = Location.Self >> Location.Self

    val fakeMediator = context.actorOf(Props(new TestMediatorForPublisher(proxy)), "Mediator")
    val child = context.actorOf(Props(new CellPublisher(fakeMediator)), NamingSystem.Publisher)

    override def receive: Receive = {
        case msg@AriadneMessage(Info, Info.Subtype.Request, this.selfToSelf, _) =>
            proxy ! CellPublisherTest.infoRequestReceivedResponse
        case msg if sender == child => proxy forward msg
        case x => child forward x
    }
}

class TestMediatorForPublisher(proxy: ActorRef) extends CustomActor {

    private val selfToSelf: MessageDirection = Location.Self >> Location.Self
    private val cellToCluster: MessageDirection = Location.Cell >> Location.Cluster
    private val cellToCell: MessageDirection = Location.Cell >> Location.Cell
    private val cellToMaster: MessageDirection = Location.Cell >> Location.Master

    override def receive: Receive = {
        case m@Publish(_, cnt, _) => cnt match {
            case msg@AriadneMessage(Handshake, Handshake.Subtype.CellToMaster, this.cellToMaster, sensorsInfoUpdate) =>
                proxy ! CellPublisherTest.handshakeResponse
            case msg@AriadneMessage(Alarm, _, this.cellToCluster, _) =>
                proxy ! CellPublisherTest.alarmResponse
            case msg@AriadneMessage(Update, Update.Subtype.Sensors, this.cellToMaster, _) =>
                proxy ! CellPublisherTest.sensorUpdateResponse
            case msg@AriadneMessage(Update, Update.Subtype.Practicability, this.cellToCell, _) =>
                proxy ! CellPublisherTest.practicabilityResponse
            case msg@AriadneMessage(Update, Update.Subtype.CurrentPeople, this.cellToMaster, _) =>
                proxy ! CellPublisherTest.currentPeopleResponse
            case msg@AriadneMessage(Topology, Topology.Subtype.Acknowledgement, this.cellToMaster, _) =>
                proxy ! CellPublisherTest.topologyAckResponse
        }
    }
}
