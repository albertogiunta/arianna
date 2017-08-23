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
    
    var usersWaitingForDisconnection: mutable.Map[String, ServerWebSocket] = new mutable.HashMap[String, ServerWebSocket]
    var usersWaitingForConnectionAck: mutable.Map[String, ServerWebSocket] = new scala.collection.mutable.HashMap[String, ServerWebSocket]
    var usersWaitingForArea: mutable.Map[String, ServerWebSocket] = new scala.collection.mutable.HashMap[String, ServerWebSocket]
    var usersReadyForAlarm: mutable.Map[String, ServerWebSocket] = new scala.collection.mutable.HashMap[String, ServerWebSocket]
    var usersWaitingForRoute: mutable.Map[String, ConcurrentHashSet[Pair[String, ServerWebSocket]]] = new scala.collection.mutable.HashMap[String, ConcurrentHashSet[Pair[String, ServerWebSocket]]]
    
    @throws[Exception]
    override def start(): Unit = {
        val options = new HttpServerOptions().setTcpKeepAlive(true).setIdleTimeout(0)
        vertx.createHttpServer(options).websocketHandler((ws: ServerWebSocket) => {
            Log.info("[SERVER " + baseUrl + "] PATH " + ws.path + " " + ws.uri + " " + ws.query)
            ws.path.split(baseUrl)(1) match {
                case "/connect" =>
                    ws.handler((data) => {
                        data.toString() match {
                            case MSGTAkkaVertx.FIRST_CONNECTION => this.usersWaitingForArea.put(ws.textHandlerID, ws)
                            case MSGTAkkaVertx.NORMAL_CONNECTION => this.usersWaitingForConnectionAck.put(ws.textHandlerID, ws)
                            case MSGTAkkaVertx.DISCONNECT => this.usersWaitingForDisconnection.put(ws.textHandlerID, ws)
                        }
                        tell(data.toString())
                    })
                    ws.closeHandler((_) => {
                        this.usersWaitingForDisconnection.put(ws.textHandlerID, ws)
                        tell(MSGTAkkaVertx.DISCONNECT)
                    })
                case "/route" =>
                    ws.handler((data) => {
                        val uriStart = data.toString().split("-")(0)
                        val uriEnd = data.toString().split("-")(1)
                        val uri = buildRouteId(uriStart, uriEnd)
                        val value = new Pair[String, ServerWebSocket](ws.textHandlerID, ws)
                        if (usersWaitingForRoute.get(uri).isEmpty) {
                            usersWaitingForRoute.put(uri, new ConcurrentHashSet[Pair[String, ServerWebSocket]]())
                        }
                        usersWaitingForRoute(uri).add(value)
                        Log.info("asked route " + uri + " " + usersWaitingForRoute.size)
                        tell(RouteRequestFromClient(ws.textHandlerID, uriStart, uriEnd, isEscape = false))
                    })
                case "/alarm" => this.usersReadyForAlarm.put(ws.textHandlerID, ws)
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
        Log.info("Waiting for ACK " + usersWaitingForConnectionAck.size)
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
        Log.info("Waiting for AREA " + usersWaitingForArea.size)
        usersWaitingForArea.values.foreach((ws: ServerWebSocket) => ws.writeTextMessage(area))
        usersWaitingForArea.clear()
    }
    
    /**
      * Called when a user disconnects from a cell because he wants to connect to the next one
      */
    def disconnectUsers(): Unit = {
        Log.info("Waiting for DISCONNECTION " + usersWaitingForDisconnection.size)
        this.usersWaitingForDisconnection.keySet.foreach((id: String) => {
            usersWaitingForConnectionAck.remove(id)
            usersWaitingForArea.remove(id)
            usersReadyForAlarm.remove(id)
        })
        this.usersWaitingForDisconnection.clear()

    }
    
    /**
      * Called when an alarm is shut down because the emergency's done
      */
    def sendSystemShutDownToUsers(): Unit = {
        usersReadyForAlarm.foreach(p => p._2.writeTextMessage(MSGTAkkaVertx.SYS_SHUTDOWN))
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
        usersReadyForAlarm.foreach(p => p._2.writeTextMessage(MSGTAkkaVertx.END_ALARM))
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
    
    def sendRouteToUsers(initialRouteId: Int, finalRouteId: Int, routeAsJson: String): Unit = {
        Log.info(usersWaitingForRoute.toString)
        val routeId = buildRouteId(initialRouteId, finalRouteId)
        if (this.usersWaitingForRoute.get(routeId).isDefined) {
            this.usersWaitingForRoute(routeId).forEach(p => p.snd.writeTextMessage(routeAsJson))
        }
        this.usersWaitingForRoute.remove(routeId)
    }

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
