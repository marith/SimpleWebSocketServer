package websocket;

import java.io.IOException;

/**
 * Created by Marit on 28.04.2017.
 */
public class WebSocketExampleServer {
    public static void main(String[] args) throws IOException {
        WebSocket ws = new WebSocket();
        ws.connect(3001);

        while(true){
            String message = ws.recieveMessage();
            ws.sendMessage(message);
        }
        //ws.close();
    }
}
