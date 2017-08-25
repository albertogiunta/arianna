package system.cell.userManagement

import akka.actor.ActorRef
import com.utils.Pair
import io.vertx.core.http.{HttpServerOptions, ServerWebSocket}
import io.vertx.core.impl.ConcurrentHashSet
import io.vertx.core.{AbstractVerticle, Vertx}
import system.ontologies.messages.{RouteRequestFromClient, RouteResponse}

import scala.collection.mutable
import scala.tools.jline_embedded.internal.Log

class WSServer(vertx: Vertx, userActor: ActorRef, val baseUrl: String, port: Integer) extends AbstractVerticle {

    var usersOnChannelConnect: mutable.Map[String, ServerWebSocket] = new mutable.HashMap[String, ServerWebSocket]
    var usersWaitingForDisconnection: mutable.Map[String, ServerWebSocket] = new mutable.HashMap[String, ServerWebSocket]
    var usersWaitingForConnectionAck: mutable.Map[String, ServerWebSocket] = new scala.collection.mutable.HashMap[String, ServerWebSocket]
    var usersWaitingForArea: mutable.Map[String, ServerWebSocket] = new scala.collection.mutable.HashMap[String, ServerWebSocket]
    var usersReadyForAlarm: mutable.Map[String, ServerWebSocket] = new scala.collection.mutable.HashMap[String, ServerWebSocket]
    var usersWaitingForRoute: mutable.Map[String, ConcurrentHashSet[Pair[String, ServerWebSocket]]] = new scala.collection.mutable.HashMap[String, ConcurrentHashSet[Pair[String, ServerWebSocket]]]

    @throws[Exception]
    override def start(): Unit = {
        val options = new HttpServerOptions().setTcpKeepAlive(true).setIdleTimeout(0)
        vertx.createHttpServer(options).websocketHandler((ws: ServerWebSocket) => {
            Log.info(s"[SERVER $baseUrl] PATH ${ws.path}  ${ws.uri}")
            ws.path.split(baseUrl)(1) match {
                case "/connect" =>
                    ws.handler((data) => {
                        data.toString() match {
                            case MSGTAkkaVertx.FirstConnection => this.usersWaitingForArea += ws.textHandlerID -> ws
                            case MSGTAkkaVertx.NormalConnection => this.usersWaitingForConnectionAck += ws.textHandlerID -> ws
                            case _ => Log.info("Unknown message received in /connect: " + data.toString())
                        }
                        data.toString() match {
                            case str if MSGTAkkaVertx.FirstConnection == str ||
                                    MSGTAkkaVertx.NormalConnection == str =>
                                tell(data.toString())
                                usersOnChannelConnect += ws.textHandlerID() -> ws
                                Log.info(s"OPENED a /connect, (${usersOnChannelConnect.size} currently active on this channel)")
                            case _ =>
                        }
                    })
                    ws.closeHandler((_) => {
                        this.usersOnChannelConnect.remove(ws.textHandlerID())
                        this.usersWaitingForArea.remove(ws.textHandlerID())
                        this.usersWaitingForConnectionAck.remove(ws.textHandlerID())
                        this.usersWaitingForDisconnection.remove(ws.textHandlerID())
                        tell(MSGTAkkaVertx.Disconnect)
                        Log.info(s"CLOSED a /connect, (${usersOnChannelConnect.size} currently active on this channel)")
                    })
                case "/route" =>
                    ws.handler((data) => {
                        data.toString() match {
                            case s if s.matches("uri[1-9]+-uri[1-9]+") =>
                                val uriStart = data.toString().split("-")(0)
                                val uriEnd = data.toString().split("-")(1)
                                val uri = buildRouteId(uriStart, uriEnd)
                                val value = new Pair[String, ServerWebSocket](ws.textHandlerID, ws)
                                if (usersWaitingForRoute.get(uri).isEmpty) usersWaitingForRoute += uri -> new ConcurrentHashSet[Pair[String, ServerWebSocket]]()
                                usersWaitingForRoute(uri).add(value)
                                tell(RouteRequestFromClient(ws.textHandlerID, uriStart, uriEnd, isEscape = false))
                            case _ => Log.info("Unknown message received in /route: " + data.toString())
                        }

                    })
                    ws.closeHandler((_) => {
                        val value = new Pair[String, ServerWebSocket](ws.textHandlerID, ws)
                        usersWaitingForRoute.foreach(r => r._2.remove(value))
                        Log.info(s"CLOSED a /route, (${usersWaitingForRoute.size} currently active on this channel)")
                    })
                case "/alarm" =>
                    ws.handler((data) => {
                        data.toString() match {
                            case MSGTAkkaVertx.AlarmSetup =>
                                this.usersReadyForAlarm += ws.textHandlerID -> ws
                                tell(MSGTAkkaVertx.AlarmSetup)
                            case _ => Log.info("Unknown message received in /alarm: " + data.toString())
                        }
                    })
                    ws.closeHandler((_) => {
                        this.usersReadyForAlarm.remove(ws.textHandlerID())
                        Log.info(s"CLOSED a /alarm (${usersReadyForAlarm.size} currently active on this channel)")
                    })
                case "/position-update" => throw new UnsupportedOperationException
                case _ => ws.reject()
            }
        }).listen(port)
    }

    /**
      * Called after a user asks to connect to a specific cell but was already present in the system,
      * hence he
      * doesn't need no further information about it
      *
      * @param ack the ack message
      */
    def sendAckToNewUser(ack: String): Unit = {
        usersWaitingForConnectionAck.values.foreach((ws: ServerWebSocket) => ws.writeTextMessage(ack))
        usersWaitingForConnectionAck.clear()
    }

    /**
      * Called when the user first connects to a cell, he should receive the area, so that he can
      * complete his work
      * on his end
      *
      * @param area the marshaled version of the area
      */
    def sendAreaToNewUser(area: String): Unit = {
        usersWaitingForArea.values.foreach((ws: ServerWebSocket) => ws.writeTextMessage(area))
        usersWaitingForArea.clear()
    }

    /**
      * Called when an alarm is shut down because the emergency's done
      */
    def sendSystemShutDownToUsers(): Unit = {
        usersReadyForAlarm.foreach(p => p._2.writeTextMessage(MSGTAkkaVertx.SysShutdown))
    }

    /**
      * Called when an alarm is the detected in the system and should be propagated to all the end
      * users
      *
      * @param routeAsJson the marshaled version of the route from this cell to the exit
      */
    def sendAlarmToUsers(routeAsJson: String): Unit = {
        usersReadyForAlarm.foreach(p => p._2.writeTextMessage(routeAsJson))
    }

    /**
      * Called when an alarm is shut down because the emergency's done
      */
    def sendAlarmEndToUsers(): Unit = {
        usersReadyForAlarm.foreach(p => p._2.writeTextMessage(MSGTAkkaVertx.EndAlarm))
    }

    /**
      * Called when a route is requested from one or more users and it's finally calculated and sent
      *
      * @param route       the RouteResponse object
      * @param routeAsJson the marshaled version of the route
      */
    def sendRouteToUsers(route: RouteResponse, routeAsJson: String): Unit = {
        val departureCellId = route.request.fromCell.serial
        val arrivalCellId = route.request.toCell.serial
        sendRouteToUsers(departureCellId, arrivalCellId, routeAsJson)
    }

    /**
      * Called when a route is requested from one or more users and it's finally calculated and sent
      *
      * @param initialRouteId The ID of the first cell
      * @param finalRouteId   The ID of the final cell
      * @param routeAsJson    The route as a JSON String
      */
    def sendRouteToUsers(initialRouteId: Int, finalRouteId: Int, routeAsJson: String): Unit = {
        val routeId = buildRouteId(initialRouteId, finalRouteId)
        if (this.usersWaitingForRoute.get(routeId).isDefined) {
            this.usersWaitingForRoute(routeId).forEach(p => p.snd.writeTextMessage(routeAsJson))
        }
        this.usersWaitingForRoute.remove(routeId)
    }

    /**
      * Get the user number as the number of active connections on /connect
      *
      * @return the current user number
      */
    def getUserNumber: Int = usersOnChannelConnect.size

    private def tell(obj: Any): Unit = {
        if (userActor != null) {
            userActor.tell(obj, ActorRef.noSender)
        }
    }

    private def buildRouteId(departureCell: Int, arrivalCell: Int) = {
        val uri = "uri"
        s"$uri$departureCell-$uri$arrivalCell"
    }

    private def buildRouteId(departureCell: String, arrivalCell: String) = {
        s"$departureCell-$arrivalCell"
    }
}
