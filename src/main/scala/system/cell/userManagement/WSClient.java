package system.cell.userManagement;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.WebSocket;
import scala.tools.jline_embedded.internal.Log;

public class WSClient extends AbstractVerticle {

    private final Vertx vertx;
    private WebSocket webSocketConnect;
    private WebSocket webSocketRoute;

    public WSClient(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void start() throws Exception {
        // Setting host as localhost is not strictly necessary as it's the default
        vertx.createHttpClient().websocket(8081, "localhost", "/uri1/connect", websocket -> {
            if (this.webSocketConnect == null) {
                this.webSocketConnect = websocket;
            }

            websocket.handler(data -> {
                Log.info("[CLIENT CONNECT] Received " + data);
            });
        });

        vertx.createHttpClient().websocket(8081, "localhost", "/uri1/route", websocket -> {
            if (this.webSocketRoute == null) {
                this.webSocketRoute = websocket;
            }

            websocket.handler(data -> {
                Log.info("[CLIENT ROUTE] Received " + data);
            });
        });
    }

    public void sendMessageFirstConnection() {
        Log.info("[CLIENT CLIENT CONNECT] " + MSGTAkkaVertx.FIRST_CONNECTION());
        webSocketConnect.writeTextMessage(MSGTAkkaVertx.FIRST_CONNECTION());
    }

    public void sendMessageNormalConnection() {
        Log.info("[CLIENT CONNECT] " + MSGTAkkaVertx.NORMAL_CONNECTION());
        webSocketConnect.writeTextMessage(MSGTAkkaVertx.NORMAL_CONNECTION());
    }

    public void sendMessageDisconnect() {
        Log.info("[CLIENT CONNECT] " + MSGTAkkaVertx.DISCONNECT());
        webSocketConnect.close();
    }

    public void sendMessageAskRoute() {
        Log.info("[CLIENT ROUTE] " + "uri1-uri2");
        webSocketRoute.writeTextMessage("uri1-uri2");
    }
}
