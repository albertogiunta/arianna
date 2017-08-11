package cell.core

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import com.actors.CustomActor
import com.typesafe.config.{Config, ConfigFactory}
import ontologies.messages.Location._
import ontologies.messages.MessageType.Topology.Subtype.{Planimetrics, ViewedFromACell}
import ontologies.messages.MessageType.{Init, Topology, Update}
import ontologies.messages._
import org.junit.runner.RunWith
import org.scalatest.WordSpecLike
import org.scalatest.junit.JUnitRunner
import system.names.NamingSystem

import scala.io.Source

@RunWith(classOf[JUnitRunner])
class UserManagerTest extends TestKit(ActorSystem("UserManagerTest")) with WordSpecLike {

    val path2Project: String = Paths.get("").toFile.getAbsolutePath
    val path2map: String = path2Project + "/res/json/map15_room.json"
    val area: Area = Planimetrics.unmarshal(Source.fromFile(new File(path2map)).getLines.mkString)

    val topology = AriadneMessage(Topology, ViewedFromACell, Location.Cell >> Location.Cell, AreaViewedFromACell(area))
    val routeRequest = RouteRequestShort("uri1", "uri1", "uri1", isEscape = false)
    val routeForward = AriadneMessage(MessageType.Route, MessageType.Route.Subtype.Request, Location.User >> Location.Cell, RouteRequest("uri1",
        area.rooms.head.info.id, area.rooms.head.info.id, isEscape = false))
    val currentPeopleUpdate0 = AriadneMessage(Update, Update.Subtype.CurrentPeople, Location.User >> Location.Cell, CurrentPeopleUpdate(RoomID(1, "uri1"), 0))
    val currentPeopleUpdate1 = AriadneMessage(Update, Update.Subtype.CurrentPeople, Location.User >> Location.Cell, CurrentPeopleUpdate(RoomID(1, "uri1"), 1))
    val currentPeopleUpdate2 = AriadneMessage(Update, Update.Subtype.CurrentPeople, Location.User >> Location.Cell, CurrentPeopleUpdate(RoomID(1, "uri1"), 2))
    val currentPeopleUpdate3 = AriadneMessage(Update, Update.Subtype.CurrentPeople, Location.User >> Location.Cell, CurrentPeopleUpdate(RoomID(1, "uri1"), 3))

    "A UserManager" must {
        val probe = TestProbe()
        val tester: TestActorRef[Tester] = TestActorRef(Props(new Tester(probe.ref)), "Tester")

        "In the beginning only wants to receive the map and nothing else" in {
            tester ! MSGTAkkaVertx.FIRST_CONNECTION
            tester ! MSGTAkkaVertx.NORMAL_CONNECTION
            tester ! MSGTAkkaVertx.DISCONNECT
            probe.expectNoMsg()
            tester ! topology
            probe.expectNoMsg()
        }

        "After the map is sent it's ready to accept messages from users and the users number increases" in {
            tester ! MSGTAkkaVertx.FIRST_CONNECTION
            probe.expectMsg(currentPeopleUpdate1)
        }

        "If a user disconnects the total number of users decreases and vice versa" in {
            tester ! MSGTAkkaVertx.DISCONNECT
            probe.expectMsg(currentPeopleUpdate0)
            tester ! MSGTAkkaVertx.FIRST_CONNECTION
            probe.expectMsg(currentPeopleUpdate1)
            tester ! MSGTAkkaVertx.FIRST_CONNECTION
            probe.expectMsg(currentPeopleUpdate2)
            tester ! MSGTAkkaVertx.NORMAL_CONNECTION
            probe.expectMsg(currentPeopleUpdate3)
            tester ! MSGTAkkaVertx.DISCONNECT
            probe.expectMsg(currentPeopleUpdate2)
            tester ! MSGTAkkaVertx.NORMAL_CONNECTION
            probe.expectMsg(currentPeopleUpdate3)
        }

        "If a route is asked by a user" in {
            tester ! routeRequest
            probe.expectMsg(routeForward)
        }
    }

    private class Tester(probe: ActorRef) extends CustomActor {
        val cellCoreActor = TestActorRef(Props(new CustomActor {
            val userManager: ActorRef = context.actorOf(Props[UserManager], NamingSystem.UserManager)
            userManager ! AriadneMessage(Init, Init.Subtype.Greetings, Location.Admin >> Location.Self, Greetings(List("uri1", "8081")))

            override def receive: Receive = {
                case msg if sender == userManager =>
                    probe ! msg
                case msg =>
                    userManager forward msg
            }
        }), self, "cellCoreActor")

        override def receive: Receive = {
            case msg if sender == cellCoreActor =>
                probe forward msg
            case msg =>
                cellCoreActor forward msg
        }
    }
}

object UserManagerTest {
    val path2Project: String = Paths.get("").toFile.getAbsolutePath
    val path2Config: String = path2Project + "/res/conf/akka/application.conf"
    val config: Config = ConfigFactory.parseFile(new File(path2Config)).withFallback(ConfigFactory.load()).resolve()
    val system: ActorSystem = ActorSystem.create("userSystem", config.getConfig("user"))
}