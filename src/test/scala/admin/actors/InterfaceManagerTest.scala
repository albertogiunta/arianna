package scala.admin.actors

import java.io.File
import java.nio.file.Paths
import javafx.embed.swing.JFXPanel

import admin.actors.InterfaceManager
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import com.actors.CustomActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.Topology.Subtype.Planimetrics
import ontologies.messages.MessageType.{Alarm, Init, Topology, Update}
import ontologies.messages._
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
        Planimetrics.unmarshal(plan)
    )

    val roomDataUpdate: RoomDataUpdate = RoomDataUpdate(RoomID(0, "room1"), ontologies.messages.Cell(CellInfo("uri", 8080), List.empty[SensorInfo]), 50)
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

    "A InterfaceManager" must {
        val probe = TestProbe()
        val tester: TestActorRef[Tester] = TestActorRef(Props(new Tester(probe.ref)), "Tester")

        "In the beginning receives the map and send it to the parent" in {
            probe.expectMsg(planimetric)
        }

        "When receiving a Goodbye, forward to parent" in {
            tester ! goodbye
            probe.expectMsg(goodbye)
        }

        "When receiving an Alarm, forward to parent" in {
            tester ! alarm
            probe.expectMsg(alarm)
        }
    }

    private class Tester(probe: ActorRef) extends CustomActor {

        val adminActor: TestActorRef[CustomActor] =
            TestActorRef(Props(new CustomActor {
                new JFXPanel
                val interfaceManager = context.actorOf(Props[InterfaceManager], NamingSystem.InterfaceManager)
                interfaceManager ! AriadneMessage(Init, Init.Subtype.Greetings, Location.Admin >> Location.Self, Greetings(List.empty))

                override def receive: Receive = {
                    case "Start" => {
                        interfaceManager ! planimetric
                    }
                    case msg if sender == interfaceManager => {
                        probe ! msg
                    }
                    case msg => interfaceManager forward msg
                }
            }), self, "adminActor")

        override def preStart {
            adminActor ! "Start"
        }

        override def receive: Receive = {
            //case msg if sender == adminActor => probe forward msg
            case msg => adminActor forward msg
        }
    }

}