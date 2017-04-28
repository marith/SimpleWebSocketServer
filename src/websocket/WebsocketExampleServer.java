package websocket;

import java.io.IOException;

/**
 * Created by Marit on 28.04.2017.
 */
public class WebsocketExampleServer {
    public static void main(String[] args) throws IOException {
        Websocket ws = new Websocket();
        ws.connect(3001);

        while(true){
            String message = ws.recieveMessage();
            ws.sendMessage(message);
        }
        //ws.close();
    }
}
