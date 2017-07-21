package cell.core;

import com.sun.tools.javac.util.Pair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import akka.actor.ActorRef;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import ontologies.messages.RouteRequestLight;
import ontologies.messages.RouteResponse;

public class WSServer extends AbstractVerticle {

    private final Vertx    vertx;
    private final ActorRef userActor;

    private Map<String, ServerWebSocket>                                     usersWaitingForDisconnection;
    private Map<String, ServerWebSocket>                                     usersWaitingForConnectionOk;
    private Map<String, ServerWebSocket>                                     usersWaitingForArea;
    private Map<String, ServerWebSocket>                                     usersReadyForAlarm;
    private Map<Pair<Integer, Integer>, List<Pair<String, ServerWebSocket>>> usersWaitingForRoute;

    private String  baseUrl  = "";
    private Integer basePort = 0;

    public WSServer(Vertx vertx, ActorRef userActor, String baseUrl, Integer port) {
        this.vertx = vertx;
        this.userActor = userActor;
        this.baseUrl = baseUrl;
        this.basePort = port;

        this.usersWaitingForDisconnection = new HashMap();
        this.usersWaitingForConnectionOk = new HashMap();
        this.usersWaitingForArea = new HashMap();
        this.usersReadyForAlarm = new HashMap();
        this.usersWaitingForRoute = new HashMap();
    }

    @Override
    public void start() throws Exception {
        vertx.createHttpServer().websocketHandler(ws -> {
            System.out.println("[SERVER " + baseUrl + "] PATH " + ws.path() + " " + ws.uri() + " " + ws.query());
            if (ws.path().equals(baseUrl + "/connect")) {
                ws.handler(data -> {
                    System.out.println("[SERVER " + baseUrl + "] GOT NEW USER | " + data.toString());
                    if (data.toString().equalsIgnoreCase(MSGToAkka.NORMAL_CONNECTION())) {
                        this.usersWaitingForConnectionOk.put(ws.textHandlerID(), ws);
                    } else if (data.toString().equalsIgnoreCase(MSGToAkka.FIRST_CONNECTION())) {
                        this.usersWaitingForArea.put(ws.textHandlerID(), ws);
                    } else if (data.toString().equalsIgnoreCase(MSGToAkka.DISCONNECT())) {
                        this.usersWaitingForDisconnection.put(ws.textHandlerID(), ws);
                    }
                    userActor.tell(data.toString(), ActorRef.noSender());
                });
            } else if (ws.path().equals(baseUrl + "/route")) {
                ws.handler(data -> {
                    System.out.println("asked route " + data.toString());
                    Integer idStart = Integer.parseInt(data.toString().split("-")[0]);
                    Integer idEnd   = Integer.parseInt(data.toString().split("-")[1]);
                    Pair    p       = new Pair<>(idStart, idEnd);
                    usersWaitingForRoute.computeIfAbsent(p, k -> new LinkedList<>()).add(new Pair<>(ws.textHandlerID(), ws));
                    userActor.tell(new RouteRequestLight(ws.textHandlerID(), idStart, idEnd), ActorRef.noSender());
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

    public void sendOkToNewUser() {
        System.out.println("[N USERS OK] " + usersWaitingForConnectionOk.size());
        usersWaitingForConnectionOk.values().forEach(ws -> ws.writeTextMessage("ack"));
        usersWaitingForConnectionOk.clear();
        System.out.println("[N USERS OK] " + usersWaitingForConnectionOk.size());
    }

    public void sendAreaToNewUser(String area) {
        System.out.println("[N USERS AREA] " + usersWaitingForArea.size());
        usersWaitingForArea.values().forEach(ws -> ws.writeTextMessage(area));
        usersWaitingForArea.clear();
        System.out.println("[N USERS AREA] " + usersWaitingForArea.size());
    }

    public void disconnectUsers() {
        this.usersWaitingForDisconnection.keySet().forEach(id -> {
            usersWaitingForConnectionOk.remove(id);
            usersWaitingForArea.remove(id);
            usersReadyForAlarm.remove(id);
            // todo remove from userswaitingforroute
        });
        this.usersWaitingForDisconnection.clear();
    }

    public void sendAlarmToUsers(String routeAsJson) {
        usersReadyForAlarm.forEach((s, ws) -> ws.writeTextMessage(routeAsJson));
    }

    public void sendRouteToUsers(RouteResponse route, String routeAsJson) {
        Pair<Integer, Integer> p = new Pair<>(route.request().fromCell().id(), route.request().toCell().id());
        this.usersWaitingForRoute.get(p).forEach(u -> u.snd.writeTextMessage(routeAsJson));
        this.usersWaitingForRoute.remove(p);
    }
}
