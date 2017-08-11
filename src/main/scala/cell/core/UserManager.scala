package cell.core

import _root_.io.vertx.core.Vertx
import akka.actor.ActorLogging
import cell.WSClient
import com.actors.BasicActor
import ontologies.messages.AriannaJsonProtocol._
import ontologies.messages.Location._
import ontologies.messages.MessageType.Topology.Subtype._
import ontologies.messages.MessageType.{Topology, Update}
import ontologies.messages._
import spray.json._
import system.exceptions.IncorrectInitMessageException

object MSGTAkkaVertx {
    val NORMAL_CONNECTION: String = "normalConnection"
    val FIRST_CONNECTION: String = "firstConnection"
    val NORMAL_CONNECTION_RESPONSE = "ack"
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

    var uri: String = _
    var serial: Int = _
    var vertx: Vertx = _
    var s: WSServer = _
    var c: WSClient = _
    var usrNumber = 0
    var areaForCell: AreaViewedFromACell = _
    var areaForUser: AreaViewedFromAUser = _

    override protected def init(args: List[Any]): Unit = {
        if (args.size != 2) throw IncorrectInitMessageException(this.name, args)

        uri = args.head.asInstanceOf[String]
        serial = uri.split("uri")(1).toInt
        vertx = Vertx.vertx()
        s = new WSServer(vertx, self, "/" + args.head.asInstanceOf[String], Port.getPort)
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
        case AriadneMessage(Topology, ViewedFromACell, _, area: AreaViewedFromACell) =>
            areaForCell = area
            areaForUser = AreaViewedFromAUser(area)
            context.become(receptiveForMobile)
    }

    protected def receptiveForMobile: Receive = {
        case MSGTAkkaVertx.FIRST_CONNECTION =>
            log.info("GOT NEW FIRST USER")
            s.sendAreaToNewUser(areaForUser.toJson.toString())
            incrementUsrNumber()
            sendCurrentPeopleUpdate()
        case MSGTAkkaVertx.NORMAL_CONNECTION =>
            log.info("GOT NEW NORMAL USER")
            s.sendOkToNewUser(MSGTAkkaVertx.NORMAL_CONNECTION_RESPONSE)
            incrementUsrNumber()
            sendCurrentPeopleUpdate()
        case MSGTAkkaVertx.DISCONNECT =>
            log.info("USER DISCONNECTING")
            s.disconnectUsers()
            decrementUsrNumber()
            sendCurrentPeopleUpdate()
        case msg: RouteRequestShort =>
            // use for test
            //            Thread.sleep(1500)
            //            self ! AriadneMessage(MessageType.Route, MessageType.Route.Subtype.Response, Location.User >> Location.Cell, RouteResponse(RouteRequest("", areaForCell.rooms.head.info.id, areaForCell.rooms.tail.head.info.id, isEscape = true), areaForCell.rooms.map(c => c.info.id)))
            //            Thread.sleep(1500)
            //            self ! AriadneMessage(MessageType.Route, MessageType.Route.Subtype.Response, Location.User >> Location.Cell, RouteResponse(RouteRequest("", getCellWithUri(msg.fromCellUri), getCellWithUri(msg.toCellUri), isEscape = false), areaForCell.rooms.map(c => c.info.id)))
            // use in production
            parent ! AriadneMessage(MessageType.Route, MessageType.Route.Subtype.Request, Location.User >> Location.Cell, RouteRequest(msg.userID, getCellWithUri(msg.fromCellUri), getCellWithUri(msg.toCellUri), isEscape = false))
        case AriadneMessage(MessageType.Route, MessageType.Route.Subtype.Response, _, response@RouteResponse(request, route)) =>
            request match {
                case RouteRequest(_, _, _, false) => s.sendRouteToUsers(response, RouteResponseShort(route).toJson.toString())
                case RouteRequest(_, _, _, true) => s.sendAlarmToUsers(RouteResponseShort(route).toJson.toString())
            }
    }

    private def incrementUsrNumber() = usrNumber += 1

    private def decrementUsrNumber() = usrNumber -= 1

    private def sendCurrentPeopleUpdate(): Unit = {
        parent ! AriadneMessage(Update, Update.Subtype.CurrentPeople, Location.User >> Location.Cell, CurrentPeopleUpdate(RoomID(serial, uri), usrNumber))
    }

    def getCellWithUri(uri: String): RoomID = {
        areaForCell.rooms.filter(p => p.cell.uri == uri).map(f => f.info.id).head
    }

}