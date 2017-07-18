package cell.core;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import akka.actor.ActorRef;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import ontologies.messages.AreaForCell;

public class WSServer extends AbstractVerticle {

    private final Vertx                        vertx;
    private final ActorRef                     userActor;
    private       Map<String, ServerWebSocket> usersWaitingForArea;
    private       Map<String, ServerWebSocket> usersWaitingForConnectionOk;


    public WSServer(Vertx vertx, ActorRef userActor) {
        this.vertx = vertx;
        this.userActor = userActor;
        this.usersWaitingForArea = new HashMap();
        this.usersWaitingForConnectionOk = new HashMap();
    }

    @Override
    public void start() throws Exception {
        String base = "/uri1";
        vertx.createHttpServer().websocketHandler(ws -> {
            System.out.println("[SERVER] PATH " + ws.path() + " " + ws.uri() + " " + ws.query());

            if (ws.path().equals(base + "/connect")) {
                ws.handler(data -> {
                    System.out.println("[SERVER] GOT NEW USER | " + data.toString());
                    if (data.toString().equalsIgnoreCase("connect")) {
                        this.usersWaitingForConnectionOk.put(ws.textHandlerID(), ws);
                    } else if (data.toString().equalsIgnoreCase("firstconnection")) {
                        this.usersWaitingForArea.put(ws.textHandlerID(), ws);
                    } else if (data.toString().equalsIgnoreCase("disconnect")) {
//                        this.usersWaitingForConnectionOk.put(ws.textHandlerID(), ws);
                    }
                    userActor.tell(data.toString(), ActorRef.noSender());
                });
            } else if (ws.path().equals("/route")) {
                ws.handler(data -> {
                });
            } else if (ws.path().equals("/position-update")) {
                ws.handler(data -> {
                });
            } else {
                ws.reject();
            }
        }).listen(8080);
    }

    public void sendOkToNewUser() {
        System.out.println("[N USERS OK] " + usersWaitingForConnectionOk.size());
        usersWaitingForConnectionOk.values().forEach(ws -> ws.writeTextMessage("ok"));
        usersWaitingForConnectionOk.clear();
        System.out.println("[N USERS OK] " + usersWaitingForConnectionOk.size());
    }

    public void sendAreaToNewUser(String area) {
        System.out.println("[N USERS AREA] " + usersWaitingForArea.size() + area);
        usersWaitingForArea.values().forEach(ws -> ws.writeTextMessage(area));

    public void sendAreaToNewUser(String area) {
        System.out.println("[N USERS AREA] " + usersWaitingForArea.size());
        usersWaitingForArea.values().forEach(ws -> ws.writeTextMessage(area));
        usersWaitingForArea.clear();
        System.out.println("[N USERS AREA] " + usersWaitingForArea.size());
    }

    public void disconnectUser(String id) {
        usersWaitingForConnectionOk.remove(id);
        usersWaitingForArea.remove(id);
        // TODO remove from alarms
    }
}
