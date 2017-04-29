package websocket;

import java.io.IOException;

/* Example: Simple echo server
 *
 * Server echoes the recieved message in uppercase 3 times before disconnecting.
 *
 * The server will automatically close the connection under given circumstances, see README.md for further details
 */

public class ExampleEchoServer extends WebSocket{

    public void echoMessage(int number) throws IOException{
        String message = recieveMessage();
        sendMessage("You said: " + message);
        message.toUpperCase();
        for(int i = 0; i < number; i++) {
            sendMessage("Echo:" +message);
        }
    }

    public static void main(String[] args) throws IOException {
        ExampleEchoServer server = new ExampleEchoServer();
        server.connect(3001);

        server.echoMessage(3);

        server.sendMessage("Server disconnected.");
        server.close();
    }
}