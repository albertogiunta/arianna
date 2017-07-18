package similUser;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.WebSocket;

public class WSClient extends AbstractVerticle {

    private final Vertx     vertx;
    private       WebSocket webSocket;

    public WSClient(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void start() throws Exception {
        // Setting host as localhost is not strictly necessary as it's the default
        vertx.createHttpClient().websocket(8080, "localhost", "/connect", websocket -> {
            if (this.webSocket == null) {
                this.webSocket = websocket;
            }

            websocket.handler(data -> {
                System.out.println("[CLIENT] Received " + data);
            });
        });
    }

    public void sendMessageConnect() {
        System.out.println("[CLIENT] I want to connect");
        webSocket.writeTextMessage("I want to connect");
    }
}
