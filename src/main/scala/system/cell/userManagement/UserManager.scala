package system.cell.userManagement

import _root_.io.vertx.core.Vertx
import akka.actor.ActorLogging
import com.actors.TemplateActor
import io.vertx.scala.core.http.ServerWebSocket
import spray.json._
import system.exceptions.IncorrectInitMessageException
import system.ontologies.messages.AriannaJsonProtocol._
import system.ontologies.messages.Location._
import system.ontologies.messages.MessageType.Topology.Subtype._
import system.ontologies.messages.MessageType.{Alarm, Topology, Update}
import system.ontologies.messages._

object MSGTAkkaVertx {
    val NORMAL_CONNECTION: String = "normalConnection"
    val FIRST_CONNECTION: String = "firstConnection"
    val NORMAL_CONNECTION_RESPONSE = "ack"
    val DISCONNECT: String = "disconnect"
    val ALARM_SETUP: String = "okToReceiveAlarms"
    val END_ALARM: String = "endAlarm"
    val SYS_SHUTDOWN: String = "sysShutdown"
}

class UserManager extends TemplateActor with ActorLogging {

    var uri: String = _
    var serial: Int = _
    var vertx: Vertx = _
    var wsServer: WSServer = _
    var c: WSClient = _
    //    var usrNumber = 0
    var areaForCell: AreaViewedFromACell = _
    var areaForUser: AreaViewedFromAUser = _
    var alarmMessage: String = _
    var isAlarmed: Boolean = false


    override protected def init(args: List[String]): Unit = {
        if (args.size != 2) throw IncorrectInitMessageException(this.name, args)

        uri = args.head
        serial = uri.split("uri")(1).toInt
        vertx = Vertx.vertx()
        wsServer = new WSServer(vertx, self, "/" + args.head, args(1).toInt)
        vertx.deployVerticle(wsServer)
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
                case RouteRequest(_, _, _, false) => wsServer.sendRouteToUsers(response, RouteResponseShort(route).toJson.toString())
                case RouteRequest(_, _, _, true) =>
                    val routeAsString = RouteResponseShort(route).toJson.toString()
                    wsServer.sendAlarmToUsers(routeAsString)
                    isAlarmed = true
                    alarmMessage = routeAsString
                    log.info(s"alarmMessage is ${RouteResponseShort(route).route.size}")
            }
        case AriadneMessage(Alarm, Alarm.Subtype.End, _, _) =>
            wsServer.sendAlarmEndToUsers()
            isAlarmed = false
    }

    protected def operationalForMobile: Receive = {
        case MSGTAkkaVertx.FIRST_CONNECTION =>
            wsServer.sendAreaToNewUser(areaForUser.toJson.toString())
            sendCurrentPeopleUpdate()
        case MSGTAkkaVertx.NORMAL_CONNECTION =>
            wsServer.sendAckToNewUser(MSGTAkkaVertx.NORMAL_CONNECTION_RESPONSE)
            sendCurrentPeopleUpdate()
        case MSGTAkkaVertx.DISCONNECT =>
            sendCurrentPeopleUpdate()
        case MSGTAkkaVertx.ALARM_SETUP =>
            log.info(s"is alarmed? $isAlarmed")
            if (isAlarmed) wsServer.sendAlarmToUsers(alarmMessage)
        case msg: RouteRequestFromClient =>
            parent ! AriadneMessage(MessageType.Route, MessageType.Route.Subtype.Request, Location.User >> Location.Cell, RouteRequest(msg.userID, getCellWithUri(msg.fromCellUri), getCellWithUri(msg.toCellUri), isEscape = false))
        case _ => desist _
    }

    override def postStop(): Unit = {
        wsServer.sendSystemShutDownToUsers()
        super.postStop()
    }

    //    private def incrementUserNumber(): Unit = {
    //        usrNumber = usrNumber + 1
    //        sendCurrentPeopleUpdate(wsServer.getUserNumber())
    //    }


    //    private def decrementUserNumber(): Unit = {
    //        usrNumber = if (usrNumber > 0) usrNumber - 1 else usrNumber
    //        sendCurrentPeopleUpdate(wsServer.getUserNumber())
    //    }


    private def sendCurrentPeopleUpdate(): Unit = {
        parent ! AriadneMessage(Update, Update.Subtype.CurrentPeople, Location.User >> Location.Cell, CurrentPeopleUpdate(RoomID(serial, uri), wsServer.getUserNumber()))
    }
    
    private def getCellWithUri(uri: String): RoomID = {
        areaForCell.rooms.filter(p => p.cell.uri == uri).map(f => f.info.id).head
    }

}

case class WSRouteInfo(connectWSId: String, routeWSId: String, serverWebSocket: ServerWebSocket)