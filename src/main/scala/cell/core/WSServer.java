package cell.core;

import akka.actor.ActorRef;
import com.utils.Pair;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.impl.ConcurrentHashSet;
import ontologies.messages.RouteRequestFromClient;
import ontologies.messages.RouteResponse;
import scala.tools.jline_embedded.internal.Log;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Vertx implementation of several websockets needed to communicate with the end user and keep a
 * reliable and
 * stable connection active.
 */
public class WSServer extends AbstractVerticle {

    private final Vertx vertx;
    private final ActorRef userActor;

    Map<String, ServerWebSocket> usersWaitingForDisconnection;
    Map<String, ServerWebSocket> usersWaitingForConnectionAck;
    Map<String, ServerWebSocket> usersWaitingForArea;
    Map<String, ServerWebSocket> usersReadyForAlarm;
    Map<String, Set<Pair<String, ServerWebSocket>>> usersWaitingForRoute;

    private String baseUrl = "";
    private Integer basePort = 0;

    public WSServer(Vertx vertx, ActorRef userActor, String baseUrl, Integer port) {
        this.vertx = vertx;
        this.userActor = userActor;
        this.baseUrl = baseUrl;
        this.basePort = port;

        this.usersWaitingForDisconnection = new ConcurrentHashMap<>();
        this.usersWaitingForConnectionAck = new ConcurrentHashMap<>();
        this.usersWaitingForArea = new ConcurrentHashMap<>();
        this.usersReadyForAlarm = new ConcurrentHashMap<>();
        this.usersWaitingForRoute = new ConcurrentHashMap<>();
    }

    @Override
    public void start() throws Exception {
        HttpServerOptions options = new HttpServerOptions().setTcpKeepAlive(true);
        vertx.createHttpServer(options).websocketHandler(ws -> {
            System.out.println("[SERVER " + baseUrl + "] PATH " + ws.path() + " " + ws.uri() + " " + ws.query());
            if (ws.path().equals(baseUrl + "/connect")) {
                ws.handler(data -> {
                    Log.info("[SERVER " + baseUrl + "] GOT NEW USER | " + data.toString());
                    if (data.toString().equalsIgnoreCase(MSGTAkkaVertx.FIRST_CONNECTION())) {
                        this.usersWaitingForArea.put(ws.textHandlerID(), ws);
                        System.out.println("sending first to actor" + usersWaitingForArea.size());
                    } else if (data.toString().equalsIgnoreCase(MSGTAkkaVertx.NORMAL_CONNECTION())) {
                        System.out.println("sending normal to actor");
                        this.usersWaitingForConnectionAck.put(ws.textHandlerID(), ws);
                    } else if (data.toString().equalsIgnoreCase(MSGTAkkaVertx.DISCONNECT())) {
                        System.out.println("sending disconnect to actor");
                        this.usersWaitingForDisconnection.put(ws.textHandlerID(), ws);
                    }
                    if (userActor != null) userActor.tell(data.toString(), ActorRef.noSender());
                });
            } else if (ws.path().equals(baseUrl + "/route")) {
                ws.handler(data -> {
                    String uriStart = data.toString().split("-")[0];
                    String uriEnd = data.toString().split("-")[1];
                    usersWaitingForRoute.computeIfAbsent(data.toString(), k -> new ConcurrentHashSet<>()).add(new Pair<>(ws.textHandlerID(), ws));
                    Log.info("asked route " + data.toString() + " " + usersWaitingForRoute.size());
                    if (userActor != null)
                        userActor.tell(new RouteRequestFromClient(ws.textHandlerID(), uriStart, uriEnd, false), ActorRef.noSender());
                });
            } else if (ws.path().equals(baseUrl + "/alarm")) {
                this.usersReadyForAlarm.put(ws.textHandlerID(), ws);
                ws.handler(data -> {
//                    this.usersReadyForAlarm.put(ws.textHandlerID(), ws);
                });
            } else if (ws.path().equals(baseUrl + "/position-update")) {
                ws.handler(data -> {
                });
            } else {
                ws.reject();
            }
        }).listen(basePort);
    }

    /**
     * Called after a user asks to connect to a specific cell but was already present in the system,
     * hence he
     * doesn't need no further information about it
     *
     * @param ack the ack message
     */
    public void sendAckToNewUser(String ack) {
        System.out.println("Waiting for ACK " + usersWaitingForConnectionAck.size());
        usersWaitingForConnectionAck.values().forEach(ws -> ws.writeTextMessage(ack));
        usersWaitingForConnectionAck.clear();
    }

    /**
     * Called when the user first connects to a cell, he should receive the area, so that he can
     * complete his work
     * on his end
     *
     * @param area the marshaled version of the area
     */
    public void sendAreaToNewUser(String area) {
        System.out.println("Waiting for AREA " + usersWaitingForArea.size());
        usersWaitingForArea.values().forEach(ws -> ws.writeTextMessage(area));
        usersWaitingForArea.clear();
    }

    /**
     * Called when a user disconnects from a cell because he wants to connect to the next one
     */
    public void disconnectUsers() {
        System.out.println("Waiting for DISCONNECTION " + usersWaitingForDisconnection.size());
        this.usersWaitingForDisconnection.keySet().forEach(id -> {
            usersWaitingForConnectionAck.remove(id);
            usersWaitingForArea.remove(id);
            usersReadyForAlarm.remove(id);
        });
        this.usersWaitingForDisconnection.clear();
    }

    /**
     * Called when an alarm is shut down because the emergency's done
     */
    public void sendSystemShutDownToUsers() {
        usersReadyForAlarm.forEach((s, ws) -> ws.writeTextMessage(MSGTAkkaVertx.SYS_SHUTDOWN()));
    }

    /**
     * Called when an alarm is the detected in the system and should be propagated to all the end
     * users
     *
     * @param routeAsJson the marshaled version of the route from this cell to the exit
     */
    public void sendAlarmToUsers(String routeAsJson) {
        usersReadyForAlarm.forEach((s, ws) -> ws.writeTextMessage(routeAsJson));
    }

    /**
     * Called when an alarm is shut down because the emergency's done
     */
    public void sendAlarmEndToUsers() {
        usersReadyForAlarm.forEach((s, ws) -> ws.writeTextMessage(MSGTAkkaVertx.END_ALARM()));
    }

    /**
     * Called when a route is requested from one or more users and it's finally calculated and sent
     *
     * @param route       the RouteResponse object
     * @param routeAsJson the marshaled version of the route
     */
    public void sendRouteToUsers(RouteResponse route, String routeAsJson) {
        int departureCellId = route.request().fromCell().serial();
        int arrivalCellId = route.request().toCell().serial();
        sendRouteToUsers(departureCellId, arrivalCellId, routeAsJson);
    }

    public void sendRouteToUsers(int initialRouteId, int finalRouteId, String routeAsJson) {
        System.out.println(usersWaitingForRoute.toString());
        String routeId = buildRouteId(initialRouteId, finalRouteId);
        this.usersWaitingForRoute.getOrDefault(routeId, new ConcurrentHashSet<>()).forEach(u -> u.snd().writeTextMessage(routeAsJson));
        this.usersWaitingForRoute.remove(routeId);
    }

    private String buildRouteId(int departureCell, int arrivalCell) {
        String uri = "uri";
        return uri + departureCell + "-" + uri + arrivalCell;
    }
}
