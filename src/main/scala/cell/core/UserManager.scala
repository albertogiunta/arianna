package cell.core

import java.io.File
import java.nio.file.Paths

import _root_.io.vertx.core.Vertx
import akka.actor.{ActorLogging, ActorSystem, Props}
import cell.WSClient
import com.typesafe.config.ConfigFactory
import common.BasicActor
import ontologies.messages.AriannaJsonProtocol._
import ontologies.messages.Location._
import ontologies.messages.MessageType.Topology
import ontologies.messages.MessageType.Topology.Subtype.Topology4Cell
import ontologies.messages._
import spray.json._

import scala.io.Source

object MSGToAkka {
    val NORMAL_CONNECTION: String = "connect"
    val FIRST_CONNECTION: String = "firstconnection"
    val DISCONNECT: String = "disconnect"
}

class UserManager extends BasicActor with ActorLogging {

    var vertx: Vertx = _
    var s: WSServer = _
    var c: WSClient = _
    var usrNumber = 0
    var area: AreaForCell = _

    override protected def init(args: List[Any]): Unit = {
        vertx = Vertx.vertx()
        s = new WSServer(vertx, self, args.head.asInstanceOf[String], args.take(2).last.asInstanceOf[String].toInt)
        c = new WSClient(vertx)
        log.info("Started actor")
        vertx.deployVerticle(s)
        //        initWSClient()
    }

    def initWSClient(): Unit = {
        vertx.deployVerticle(c)
        Thread.sleep(1000)
        c.sendMessageConnect()
    }

    override protected def receptive: Receive = {
        case msg@AriadneMessage(Topology, Topology4Cell, _, area: AreaForCell) =>
            this.area = area
            context.become(receptiveForMobile)
    }

    protected def receptiveForMobile: Receive = {
        case MSGToAkka.NORMAL_CONNECTION =>
            println("[ACTOR] GOT NEW USER")
            s.sendOkToNewUser()
            usrNumber += 1
        // todo tell parent
        case MSGToAkka.FIRST_CONNECTION =>
            println("[ACTOR] GOT NEW FIRST USER")
            println(s"Area received from the Cell Core")
            s.sendAreaToNewUser(area.toJson.toString())
            usrNumber += 1
        // todo tell parent
        case MSGToAkka.DISCONNECT =>
            println("[ACTOR] USER DISCONNECTING")
            s.disconnectUsers()
            usrNumber -= 1
        // todo tell parent
        case msg: RouteRequestLight =>
            // use for test
            self ! AriadneMessage(MessageType.Route, MessageType.Route.Subtype.Escape.Response, Location.User >> Location.Cell, EscapeResponse(area.cells.head.info, area.cells.map(c => c.info)))
            Thread.sleep(500)
            self ! AriadneMessage(MessageType.Route, MessageType.Route.Subtype.Response, Location.User >> Location.Cell, RouteResponse(RouteRequest(msg.userID, getCellWithId(msg.fromCellId), getCellWithId(msg.toCellId)), area.cells.map(c => c.info)))
        // use in production
        //            parent ! AriadneMessage(MessageType.Route, MessageType.Route.Subtype.Request, Location.User >> Location.Cell, RouteRequest(msg.userID, getCellWithId(msg.fromCellId), getCellWithId(msg.toCellId)))
        case msg@AriadneMessage(MessageType.Route, MessageType.Route.Subtype.Response, _, response: RouteResponse) =>
            s.sendRouteToUsers(response, RouteResponseShort(response.route).toJson.toString())
        case msg@AriadneMessage(MessageType.Route, MessageType.Route.Subtype.Escape.Response, _, response: EscapeResponse) =>
            s.sendAlarmToUsers(RouteResponseShort(response.route).toJson.toString())
        case _ => ""
    }

    def getCellWithId(id: Int): InfoCell = {
        area.cells.filter(c => c.info.id == id).map(c => c.info).head
    }
}

object UserRun {

    private def readJson(filename: String): JsValue = {
        val source: String = Source.fromFile(filename).getLines.mkString
        source.parseJson
    }

    def loadArea(): Area = {
        val area = readJson(s"res/json/map.json").convertTo[Area]
        area
    }

    def areaForCell: AreaForCell = {
        AreaForCell(area)
    }

    var area: Area = loadArea()

    def main(args: Array[String]): Unit = {
        val path2Project = Paths.get("").toFile.getAbsolutePath
        val path2Config = path2Project + "/res/conf/akka/application.conf"
        val config = ConfigFactory.parseFile(new File(path2Config))
        val system = ActorSystem.create("userSystem", config.getConfig("user"))
        val userActor = system.actorOf(Props.create(classOf[UserManager]), "user1")
        userActor ! AriadneMessage(MessageType.Init, MessageType.Init.Subtype.Greetings, Location.User >> Location.Self, Greetings(List("/uri1", "8080")))
        Thread.sleep(500)
        val userActor2 = system.actorOf(Props.create(classOf[UserManager]), "user2")
        userActor2 ! AriadneMessage(MessageType.Init, MessageType.Init.Subtype.Greetings, Location.User >> Location.Self, Greetings(List("/uri2", "8081")))
        Thread.sleep(500)
        val userActor3 = system.actorOf(Props.create(classOf[UserManager]), "user3")
        userActor3 ! AriadneMessage(MessageType.Init, MessageType.Init.Subtype.Greetings, Location.User >> Location.Self, Greetings(List("/uri3", "8082")))
        Thread.sleep(500)
        userActor ! AriadneMessage(Topology, Topology4Cell, Location.User >> Location.Self, areaForCell)
        Thread.sleep(500)
        userActor2 ! AriadneMessage(Topology, Topology4Cell, Location.User >> Location.Self, areaForCell)
        Thread.sleep(500)
        userActor3 ! AriadneMessage(Topology, Topology4Cell, Location.User >> Location.Self, areaForCell)
    }
}