package system.cell.cluster

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.actors.{ClusterMembersListener, CustomActor}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import system.names.NamingSystem
import system.ontologies.Topic
import system.ontologies.messages.Location._
import system.ontologies.messages.MessageType._
import system.ontologies.messages.{AriadneMessage, _}


/**
  * Created by Matteo Gabellini on 08/08/2017.
  */
class CellSubscriberTest extends TestKit(ActorSystem("CellSubscriberTest", CellSubscriberTest.config))
    with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

    private val actorName = NamingSystem.Subscriber

    val initMsg = AriadneMessage(
        Init,
        Init.Subtype.Greetings,
        Location.Cell >> Location.Self,
        Greetings(List(ClusterMembersListener.greetings)))

    val handshakeAckMsg = AriadneMessage(
        Handshake,
        Handshake.Subtype.Acknowledgement,
        Location.Master >> Location.Cell,
        Empty())

    val topologyMsg = AriadneMessage(
        Topology,
        Topology.Subtype.ViewedFromACell,
        Location.Master >> Location.Cell,
        AreaViewedFromACell(0, List.empty[RoomViewedFromACell]))

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

    val alarmEndMsg = AriadneMessage(
        Alarm,
        Alarm.Subtype.End,
        Location.Master >> Location.Cell,
        Empty()
    )

    val shutdownMsg = AriadneMessage(
        Init,
        Init.Subtype.Goodbyes,
        Location.Master >> Location.Cell,
        Empty()
    )

    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }

    "A Subscriber of a Cell" should {

        val proxy = TestProbe()
        val parent = system.actorOf(Props(new TestParentForSubscriber(proxy.ref)), "TestParent")

        "initially ignore all messages that aren't Init messages" in {

            proxy.send(parent, topologyMsg)
            proxy.send(parent, updateMsg)
            proxy.send(parent, alarmMsg)
            proxy.send(parent, alarmEndMsg)
            proxy.send(parent, shutdownMsg)
            proxy.expectNoMsg()
        }

        "after received the init message, it registers itself on message topics" in {
            proxy.send(parent, initMsg)
            val topicsSubscribedByACell = Set(
                //        Topic.HandShakes,
                Topic.Alarms,
                Topic.Topologies,
                Topic.Practicabilities,
                Topic.ShutDown
            )
            topicsSubscribedByACell foreach (X => proxy.expectMsg(CellSubscriberTest.subscriptionResponse))
        }

        "after topic subscription change its behaviour to \"subscribed\" " +
            "and stashes messages that are Alarm" in {
            proxy.send(parent, alarmMsg)
            proxy.expectNoMsg()
        }

        "after topic subscription change its behaviour to \"subscribed\" " +
            "and stashes messages that are Update" in {
            proxy.send(parent, updateMsg)
            proxy.expectNoMsg()
        }

        "after topic subscription change its behaviour to \"subscribed\" " +
            "and stashes messages that notify alarm end" in {
            proxy.send(parent, alarmEndMsg)
            proxy.expectNoMsg()
        }

        "in the \"subscribed\" behaviour, if receives Shutdown Message, forward the message to the parent" in {
            proxy.send(parent, shutdownMsg)
            proxy.expectMsg(shutdownMsg)
        }

        "in the \"subscribed\" behaviour, if receives Handshake Acknowledge, sends a message to the Publisher" in {
            proxy.send(parent, handshakeAckMsg)
            proxy.expectMsg(CellSubscriberTest.handshakeAckResponse)
        }

        "in the \"subscribed\" behaviour, if receives a topology forwards it to the parent" +
            "become \"cultured\" and unstash previous stashed message" in {
            proxy.send(parent, topologyMsg)
            proxy.expectMsg(topologyMsg)
        }

        "in the \"cultured\" state, process the previous stashed messages (Alarm, Update and Alarm End) " +
            "and forward them to the parent actor" in {
            proxy.expectMsg(alarmMsg)
            proxy.expectMsg(updateMsg)
            proxy.expectMsg(alarmEndMsg)
        }

        "in the \"cultured\" state, if receives a shutdown message, forward it to the parent" in {
            proxy.send(parent, shutdownMsg)
            proxy.expectMsg(shutdownMsg)
        }
    }


}

object CellSubscriberTest {
    val path2Project: String = Paths.get("").toFile.getAbsolutePath
    val configPath: String = path2Project + "/res/conf/test/testCellSubscriber.conf"

    val config: Config = ConfigFactory.parseFile(new File(configPath)).withFallback(ConfigFactory.load()).resolve()

    val subscriptionResponse = "Subscribed to a topic"

    val handshakeAckResponse = "Handshake ack received"
}


class TestParentForSubscriber(proxy: ActorRef) extends CustomActor {

    val fakeMediator = context.actorOf(Props(new TestMediatorForSubscriber(proxy)), "Mediator")
    val child = context.actorOf(Props(new CellSubscriber(fakeMediator)), NamingSystem.Subscriber)
    val fakePublisher = context.actorOf(Props(new TestPublisher(proxy)), NamingSystem.Publisher)
    override def receive: Receive = {
        case msg if sender == child => proxy forward msg
        case x => child forward x
    }
}

class TestPublisher(proxy: ActorRef) extends CustomActor {

    override def receive: Receive = {
        case msg@AriadneMessage(Handshake, Handshake.Subtype.Acknowledgement, _, cnt) =>
            proxy forward CellSubscriberTest.handshakeAckResponse
    }
}

class TestMediatorForSubscriber(proxy: ActorRef) extends CustomActor {

    override def receive: Receive = {
        case msg: Subscribe => {
            sender ! SubscribeAck(Subscribe(msg.topic, Option.empty, sender()))
            proxy forward CellSubscriberTest.subscriptionResponse
        }
    }
}


