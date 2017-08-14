package cell.core;

import akka.actor.ActorRef;
import com.utils.Pair;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import ontologies.messages.RouteRequestShort;
import ontologies.messages.RouteResponse;
import scala.tools.jline_embedded.internal.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Vertx implementation of several websockets needed to communicate with the end user and keep a
 * reliable and
 * stable connection active.
 */
public class WSServer extends AbstractVerticle {

    private final Vertx vertx;
    private final ActorRef userActor;

    private Map<String, ServerWebSocket> usersWaitingForDisconnection;
    private Map<String, ServerWebSocket> usersWaitingForConnectionAck;
    private Map<String, ServerWebSocket> usersWaitingForArea;
    private Map<String, ServerWebSocket> usersReadyForAlarm;
    private Map<Pair<String, String>, List<Pair<String, ServerWebSocket>>> usersWaitingForRoute;

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
        vertx.createHttpServer().websocketHandler(ws -> {
            System.out.println("[SERVER " + baseUrl + "] PATH " + ws.path() + " " + ws.uri() + " " + ws.query());
            if (ws.path().equals(baseUrl + "/connect")) {
                ws.handler(data -> {
                    Log.info("[SERVER " + baseUrl + "] GOT NEW USER | " + data.toString());
                    if (data.toString().equalsIgnoreCase(MSGTAkkaVertx.NORMAL_CONNECTION())) {
                        System.out.println("sending normal to actor");
                        this.usersWaitingForConnectionAck.put(ws.textHandlerID(), ws);
                    } else if (data.toString().equalsIgnoreCase(MSGTAkkaVertx.FIRST_CONNECTION())) {
                        System.out.println("sending first to actor");
                        this.usersWaitingForArea.put(ws.textHandlerID(), ws);
                    } else if (data.toString().equalsIgnoreCase(MSGTAkkaVertx.DISCONNECT())) {
                        System.out.println("sending disconnect to actor");
                        this.usersWaitingForDisconnection.put(ws.textHandlerID(), ws);
                    }
                    userActor.tell(data.toString(), ActorRef.noSender());
                });
            } else if (ws.path().equals(baseUrl + "/route")) {
                ws.handler(data -> {
                    Log.info("asked route " + data.toString());
                    String uriStart = data.toString().split("-")[0];
                    String uriEnd = data.toString().split("-")[1];
                    Pair<String, String> p = new Pair<>(uriStart, uriEnd);
                    usersWaitingForRoute.computeIfAbsent(p, k -> new LinkedList<>()).add(new Pair<>(ws.textHandlerID(), ws));
                    userActor.tell(new RouteRequestShort(ws.textHandlerID(), uriStart, uriEnd, false), ActorRef.noSender());
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
    public void sendOkToNewUser(String ack) {
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
            // remove from usersWaitingForRoute
        });
        this.usersWaitingForDisconnection.clear();
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
     * Called when a route is requested from one or more users and it's finally calculated and sent
     *
     * @param route       the RouteResponse object
     * @param routeAsJson the marshaled version of the route
     */
    public void sendRouteToUsers(RouteResponse route, String routeAsJson) {
        Pair<String, String> p = new Pair<>("uri" + route.request().fromCell().serial(), "uri" + route.request().toCell().serial());
        this.usersWaitingForRoute.getOrDefault(p, new LinkedList<>()).forEach(u -> u.snd().writeTextMessage(routeAsJson));
        this.usersWaitingForRoute.remove(p);
    }
}
