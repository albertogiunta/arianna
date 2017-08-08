package cell.core

import java.io.File
import java.nio.file.Paths

import _root_.io.vertx.core.Vertx
import akka.actor.{ActorLogging, ActorRef, ActorSystem, Props}
import cell.WSClient
import com.actors.BasicActor
import com.typesafe.config.ConfigFactory
import ontologies.messages.AriannaJsonProtocol._
import ontologies.messages.Location._
import ontologies.messages.MessageType.Topology
import ontologies.messages.MessageType.Topology.Subtype._
import ontologies.messages._
import spray.json._

import scala.collection.mutable
import scala.io.Source

object MSGToAkka {
    val NORMAL_CONNECTION: String = "connect"
    val FIRST_CONNECTION: String = "firstconnection"
    val DISCONNECT: String = "disconnect"
}

object Port {
    private var port = 8080

    def getPort: Int = {
        port += 1
        port
    }
}

class UserManager extends BasicActor with ActorLogging {

    var vertx: Vertx = _
    var s: WSServer = _
    var c: WSClient = _
    var usrNumber = 0
    var areaForCell: AreaViewedFromACell = _
    var areaForUser: AreaViewedFromAUser = _

    override protected def init(args: List[Any]): Unit = {
        if (args.size != 2) throw new Exception()
        vertx = Vertx.vertx()
        s = new WSServer(vertx, self, "/" + args.head.asInstanceOf[String], Port.getPort)
        //        s = new WSServer(vertx, self, "/" + args.head.asInstanceOf[String], args(1).asInstanceOf[String].toInt)
        vertx.deployVerticle(s)
        log.info("Started User Manager")
        //        initWSClient()
    }

    def initWSClient(): Unit = {
        c = new WSClient(vertx)
        vertx.deployVerticle(c)
        Thread.sleep(1000)
        c.sendMessageConnect()
    }

    override protected def receptive: Receive = {
        case msg@AriadneMessage(Topology, ViewedFromACell, _, area: AreaViewedFromACell) =>
            areaForCell = area
            areaForUser = AreaViewedFromAUser(area)
            context.become(receptiveForMobile)
    }

    protected def receptiveForMobile: Receive = {
        case MSGToAkka.NORMAL_CONNECTION =>
            println("[ACTOR] GOT NEW USER")
            s.sendOkToNewUser()
            usrNumber += 1
        // todo tell CellCoreActore
        case MSGToAkka.FIRST_CONNECTION =>
            println("[ACTOR] GOT NEW FIRST USER")
            println(s"Area received from the Cell Core")
            s.sendAreaToNewUser(areaForUser.toJson.toString())
            usrNumber += 1
        // todo tell CellCoreActore
        case MSGToAkka.DISCONNECT =>
            println("[ACTOR] USER DISCONNECTING")
            s.disconnectUsers()
            usrNumber -= 1
        // todo tell CellCoreActore
        case msg: RouteRequestShort =>
            // use for test
            Thread.sleep(1500)
            self ! AriadneMessage(MessageType.Route, MessageType.Route.Subtype.Response, Location.User >> Location.Cell, RouteResponse(RouteRequest("", areaForCell.rooms.head.info.id, areaForCell.rooms.tail.head.info.id, isEscape = true), areaForCell.rooms.map(c => c.info.id)))
            Thread.sleep(1500)
            self ! AriadneMessage(MessageType.Route, MessageType.Route.Subtype.Response, Location.User >> Location.Cell, RouteResponse(RouteRequest("", getCellWithId(msg.fromCellUri), getCellWithId(msg.toCellUri), isEscape = false), areaForCell.rooms.map(c => c.info.id)))
        // use in production
        //            parent ! AriadneMessage(MessageType.Route, MessageType.Route.Subtype.Request, Location.User >> Location.Cell, RouteRequest(msg.userID, getCellWithId(msg.fromCellId), getCellWithId(msg.toCellId), isEscape = false))
        //            parent ! AriadneMessage(MessageType.Route, MessageType.Route.Subtype.Request, Location.User >> Location.Cell, RouteRequest(msg.userID, getCellWithId(msg.fromCellId), getCellWithId(msg.toCellId), isEscape = true))
        case msg@AriadneMessage(MessageType.Route, MessageType.Route.Subtype.Response, _, response@RouteResponse(request, route)) =>
            request match {
                case RouteRequest(_, _, _, false) => s.sendRouteToUsers(response, RouteResponseShort(route).toJson.toString())
                case RouteRequest(_, _, _, true) => s.sendAlarmToUsers(RouteResponseShort(route).toJson.toString())
            }
    }

    def getCellWithId(uri: String): RoomID = {
        areaForCell.rooms.filter(p => p.cell.uri == uri).map(f => f.info.id).head
    }
}

object RunUser {

    private def readJson(filename: String): JsValue = {
        val source: String = Source.fromFile(filename).getLines.mkString
        source.parseJson
    }

    def loadArea(): Area = {
        val area = readJson(s"res/json/map15_room.json").convertTo[Area]
        area
    }

    def areaForCell: AreaViewedFromACell = {
        AreaViewedFromACell(area)
    }

    var area: Area = loadArea()

    def main(args: Array[String]): Unit = {

        val path2Project = Paths.get("").toFile.getAbsolutePath
        val path2Config = path2Project + "/res/conf/akka/application.conf"
        val config = ConfigFactory.parseFile(new File(path2Config))
        val system = ActorSystem.create("userSystem", config.getConfig("user"))

        val map: mutable.LinkedHashMap[String, Greetings] = mutable.LinkedHashMap()
        val actors: mutable.MutableList[ActorRef] = mutable.MutableList()

        for (i <- 1 to 15) {
            map.put(s"user$i", Greetings(List(s"uri$i", (8080+i).toString)))
        }

        map.foreach(u => {
            val userActor = system.actorOf(Props.create(classOf[UserManager]), u._1)
            userActor ! AriadneMessage(MessageType.Init, MessageType.Init.Subtype.Greetings, Location.User >> Location.Self, u._2)
            Thread.sleep(1000)
            userActor ! AriadneMessage(Topology, ViewedFromACell, Location.User >> Location.Self, areaForCell)
            Thread.sleep(500)
            actors += userActor
        })
    }
}