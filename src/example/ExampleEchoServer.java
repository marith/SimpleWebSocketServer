package example;

import websocket.WebSocket;
import java.io.IOException;

/* Example: Simple echo server
 *
 * @author Anita Kristine Aune
 * @author Marit Holm
 *
 * Server echoes the recieved message in uppercase.
 *
 * The server will automatically close the connection under given circumstances, see README.md for further details
 */

public class ExampleEchoServer extends WebSocket {

    public void echoMessage() throws IOException{
        while(true) {
            String message = recieveMessage();
            sendMessage("You said: " + message);
            message = message.toUpperCase();
            sendMessage("Echo: " +message);
        }
    }

    public static void main(String[] args) throws IOException {
        ExampleEchoServer server = new ExampleEchoServer();
        server.connect(3001,5000);

        server.echoMessage();
    }
}