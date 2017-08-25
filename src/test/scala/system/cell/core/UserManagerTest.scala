package system.cell.core

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import com.actors.CustomActor
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.WordSpecLike
import system.cell.userManagement.{MSGTAkkaVertx, UserManager}
import system.names.NamingSystem
import system.ontologies.messages.Location._
import system.ontologies.messages.MessageType.Topology.Subtype.{Planimetrics, ViewedFromACell}
import system.ontologies.messages.MessageType.{Init, Topology}
import system.ontologies.messages._

import scala.io.Source

class UserManagerTest extends TestKit(ActorSystem("UserManagerTest")) with WordSpecLike {

    val path2Project: String = Paths.get("").toFile.getAbsolutePath
    val path2map: String = path2Project + "/res/json/map15_room.json"
    val area: Area = Planimetrics.unmarshal(Source.fromFile(new File(path2map)).getLines.mkString)

    val topology = AriadneMessage(Topology, ViewedFromACell, Location.Cell >> Location.Cell, AreaViewedFromACell(area))
    val routeRequest = RouteRequestFromClient("uri1", "uri1", "uri1", isEscape = false)
    val routeForward = AriadneMessage(MessageType.Route, MessageType.Route.Subtype.Request, Location.User >> Location.Cell, RouteRequest("uri1",
        area.rooms.head.info.id, area.rooms.head.info.id, isEscape = false))

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