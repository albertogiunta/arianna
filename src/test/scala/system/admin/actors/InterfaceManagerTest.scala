package system.admin.actors

import java.io.File
import java.nio.file.Paths
import javafx.embed.swing.JFXPanel

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import com.actors.CustomActor
import system.ontologies.messages.Location._
import system.ontologies.messages.MessageType.Topology.Subtype.Planimetrics
import system.ontologies.messages.MessageType.{Alarm, Handshake, Init, Interface, Topology, Update}
import system.ontologies.messages._
import system.ontologies.messages.Cell
import org.junit.runner.RunWith
import org.scalatest.WordSpecLike
import org.scalatest.junit.JUnitRunner
import system.names.NamingSystem

import scala.io.Source


@RunWith(classOf[JUnitRunner])
class InterfaceManagerTest extends TestKit(ActorSystem("InterfaceManagerTest")) with WordSpecLike {

    val path2Project: String = Paths.get("").toFile.getAbsolutePath
    val path2map: String = path2Project + "/res/json/map4test.json"
    val plan: String = Source.fromFile(new File(path2map)).getLines.mkString

    val planimetric = AriadneMessage(
        Topology,
        Topology.Subtype.Planimetrics,
        Location.Admin >> Location.Master,
        Planimetrics unmarshal plan
    )

    val roomDataUpdate: RoomDataUpdate = RoomDataUpdate(RoomID(0, "room1"),
        Cell(CellInfo("uri", 8080), List.empty[SensorInfo]), 50)
    val list: List[RoomDataUpdate] = List(roomDataUpdate)

    val update = AriadneMessage(
        Update, Update.Subtype.Admin,
        Location.Master >> Location.Admin, new AdminUpdate(1, list)
    )

    val alarm = AriadneMessage(
        Alarm, Alarm.Subtype.FromInterface,
        Location.Admin >> Location.Self, Empty()
    )

    val goodbye = AriadneMessage(
        Init, Init.Subtype.Goodbyes,
        Location.Admin >> Location.Self, Empty()
    )

    val closeChart = AriadneMessage(
        Interface, Interface.Subtype.CloseChart,
        Location.Admin >> Location.Self, RoomInfo.empty
    )

    val handshake = AriadneMessage(
        Handshake,
        Handshake.Subtype.CellToMaster,
        Location.Cell >> Location.Master,
        SensorsInfoUpdate(CellInfo(uri = "PancoPillo", port = 8080)
            , List(SensorInfo(1, 10.0)))
    )


    "A InterfaceManager" must {
        val probe = TestProbe()
        val tester: TestActorRef[Tester] = TestActorRef(Props(new Tester(probe.ref)), "Tester")

        "In the beginning receive the map and send it to the parent" in {
            // Giving time to load the map from GUI once started
            Thread.sleep(5000)
            probe expectMsg planimetric
            tester ! handshake
        }

        "When receiving an Alarm, forward to parent" in {
            tester ! alarm
            probe.expectMsg(alarm)
        }

        "When receiving an Goodbye message, forward to parent" in {
            tester ! goodbye
            probe.expectMsg(goodbye)
        }

        "When receiving a CloseChart message, kills the sender" in {
            probe watch tester
            tester ! closeChart
            probe.expectTerminated(tester)
        }
    }

    private class Tester(probe: ActorRef) extends CustomActor {

        private var interfaceManager: ActorRef = _

        override def preStart {
            new JFXPanel
            interfaceManager = context.actorOf(Props[InterfaceManager], NamingSystem.InterfaceManager)
            interfaceManager ! AriadneMessage(Init, Init.Subtype.Greetings, Location.Admin >> Location.Self, Greetings(List.empty))
        }

        override def receive: Receive = {
            case msg if sender == interfaceManager => probe ! msg
            case msg => interfaceManager ! msg
        }
    }

}