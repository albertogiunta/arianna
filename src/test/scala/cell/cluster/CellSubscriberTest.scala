package cell.cluster

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.actors.{ClusterMembersListener, CustomActor}
import com.typesafe.config.{Config, ConfigFactory}
import ontologies.messages.Location._
import ontologies.messages.MessageType._
import ontologies.messages.{AriadneMessage, _}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import system.names.NamingSystem

/**
  * Created by Matteo Gabellini on 08/08/2017.
  */
class CellSubscriberTest extends TestKit(ActorSystem("CellSubscriberTest", CellSubscriberTest.config))
    with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

    val middleware = DistributedPubSub(system).mediator
    private val actorName = NamingSystem.Subscriber

    val initMsg = AriadneMessage(Init, Init.Subtype.Greetings,
        Location.Cell >> Location.Self, Greetings(List(ClusterMembersListener.greetings)))

    val handshakeMsg = AriadneMessage(
        Handshake,
        Handshake.Subtype.Acknowledgement,
        Location.Master >> Location.Cell,
        _)

    val topologyMsg = AriadneMessage(
        Topology,
        Topology.Subtype.ViewedFromACell,
        Location.Master >> Location.Cell,
        AreaViewedFromACell(0, List.empty[RoomViewedFromACell]))

    //    val routeMsg = AriadneMessage(Route, _, _, _)

    val updateMsg = AriadneMessage(
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

    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }

    "A Subscriber of a Cell" should {
        "initially ignore all messages that aren't Init messages" in {
            val proxy = TestProbe()
            val parent = system.actorOf(Props(new TestParent(proxy.ref)), "TestParent")

            proxy.send(parent, topologyMsg)
            //            proxy.send(parent, routeMsg)
            proxy.send(parent, updateMsg)
            proxy.send(parent, alarmMsg)
            proxy.expectNoMsg()

            system.stop(proxy.ref)
            system.stop(parent)
        }
    }
}

object CellSubscriberTest {
    val path2Project: String = Paths.get("").toFile.getAbsolutePath
    val configPath: String = path2Project + "/res/config/testCell.conf"

    val config: Config = ConfigFactory.parseFile(new File(configPath)).withFallback(ConfigFactory.load()).resolve()
}


class TestParent(proxy: ActorRef) extends CustomActor {

    val child = context.actorOf(Props[CellSubscriber], NamingSystem.Subscriber)

    override def receive: Receive = {
        case msg if sender == child => proxy forward msg
        case x => child forward x
    }
}

