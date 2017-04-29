package websocket.example;

import websocket.WebSocket;

import java.io.IOException;

/* Example: Simple chat server.

 * Several clients can be connected.
 * Server disconnects after a total of 5 messages are sent.
 *
 * The server will automatically close the connection under given circumstances, see README.md for further details
 */

public class ExampleChatServer extends WebSocket {

    public void sendToAll(String message) throws IOException {
        sendMessage(message);
    }

    public void displayChat() throws IOException{
        String message = recieveMessage();
        sendMessage(message);
    }

    public void disconnect() throws IOException {
        sendToAll("The server is disconnected.");
        close();
    }

    public static void main(String[] args) throws IOException {
        ExampleChatServer server = new ExampleChatServer();

        try {
            server.connect(3001);

            for(int i = 0; i < 5; i++){
                server.displayChat();
            }

            server.disconnect();

        } catch(IOException e){
            server.close();
        }
    }
}