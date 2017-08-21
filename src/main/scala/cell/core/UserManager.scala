package cell.core

import _root_.io.vertx.core.Vertx
import akka.actor.ActorLogging
import cell.WSClient
import com.actors.TemplateActor
import io.vertx.scala.core.http.ServerWebSocket
import ontologies.messages.AriannaJsonProtocol._
import ontologies.messages.Location._
import ontologies.messages.MessageType.Topology.Subtype._
import ontologies.messages.MessageType.{Alarm, Topology, Update}
import ontologies.messages._
import spray.json._
import system.exceptions.IncorrectInitMessageException

object MSGTAkkaVertx {
    val NORMAL_CONNECTION: String = "normalConnection"
    val FIRST_CONNECTION: String = "firstConnection"
    val NORMAL_CONNECTION_RESPONSE = "ack"
    val DISCONNECT: String = "disconnect"
    val END_ALARM: String = "endAlarm"
    val SYS_SHUTDOWN: String = "sysShutdown"
}

class UserManager extends TemplateActor with ActorLogging {

    var uri: String = _
    var serial: Int = _
    var vertx: Vertx = _
    var s: WSServer = _
    var c: WSClient = _
    var usrNumber = 0
    var areaForCell: AreaViewedFromACell = _
    var areaForUser: AreaViewedFromAUser = _

    override protected def init(args: List[String]): Unit = {
        if (args.size != 2) throw IncorrectInitMessageException(this.name, args)

        uri = args.head
        serial = uri.split("uri")(1).toInt
        vertx = Vertx.vertx()
        s = new WSServer(vertx, self, "/" + args.head, args(1).toInt)
        vertx.deployVerticle(s)
        log.info("Started User Manager")
        //        initWSClient()
    }

    def initWSClient(): Unit = {
        c = new WSClient(vertx)
        vertx.deployVerticle(c)
        Thread.sleep(1000)
        c.sendMessageFirstConnection()
    }

    override protected def receptive: Receive = {
        case AriadneMessage(Topology, ViewedFromACell, _, area: AreaViewedFromACell) =>
            areaForCell = area
            areaForUser = AreaViewedFromAUser(area)
            context.become(operational)
        case _ => desist _
    }

    protected def operational: Receive = operationalForCell orElse operationalForMobile

    protected def operationalForCell: Receive = {
        case AriadneMessage(MessageType.Route, MessageType.Route.Subtype.Response, _, response@RouteResponse(request, route)) =>
            request match {
                case RouteRequest(_, _, _, false) => s.sendRouteToUsers(response, RouteResponseShort(route).toJson.toString())
                case RouteRequest(_, _, _, true) => s.sendAlarmToUsers(RouteResponseShort(route).toJson.toString())
            }
        case AriadneMessage(Alarm, Alarm.Subtype.End, _, _) =>
            s.sendAlarmEndToUsers()
    }

    protected def operationalForMobile: Receive = {
        case MSGTAkkaVertx.FIRST_CONNECTION =>
            s.sendAreaToNewUser(areaForUser.toJson.toString())
            incrementUserNumber()
            println(usrNumber)
            sendCurrentPeopleUpdate()
        case MSGTAkkaVertx.NORMAL_CONNECTION =>
            s.sendAckToNewUser(MSGTAkkaVertx.NORMAL_CONNECTION_RESPONSE)
            incrementUserNumber()
            sendCurrentPeopleUpdate()
        case MSGTAkkaVertx.DISCONNECT =>
            s.disconnectUsers()
            decrementUserNumber()
            sendCurrentPeopleUpdate()
        case msg: RouteRequestFromClient =>
            parent ! AriadneMessage(MessageType.Route, MessageType.Route.Subtype.Request, Location.User >> Location.Cell, RouteRequest(msg.userID, getCellWithUri(msg.fromCellUri), getCellWithUri(msg.toCellUri), isEscape = false))
        case _ => desist _
    }

    override def postStop(): Unit = {
        s.sendSystemShutDownToUsers()
        super.postStop()
    }

    private def incrementUserNumber(): Unit = usrNumber = usrNumber + 1

    private def decrementUserNumber(): Unit = usrNumber = usrNumber - 1

    private def sendCurrentPeopleUpdate(): Unit = {
        parent ! AriadneMessage(Update, Update.Subtype.CurrentPeople, Location.User >> Location.Cell, CurrentPeopleUpdate(RoomID(serial, uri), usrNumber))
    }

    private def getCellWithUri(uri: String): RoomID = {
        areaForCell.rooms.filter(p => p.cell.uri == uri).map(f => f.info.id).head
    }

}

case class WSRouteInfo(connectWSId: String, routeWSId: String, serverWebSocket: ServerWebSocket)