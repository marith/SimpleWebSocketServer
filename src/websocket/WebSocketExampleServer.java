package websocket;

import java.io.IOException;

        /*
         * This is an example on how to use this WebSocket library
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
