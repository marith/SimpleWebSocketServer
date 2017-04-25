package websocket;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Marit on 25.04.2017.
 */
public class Websocket {
    ServerSocket server;

    public Websocket(int port) throws IOException {
        ServerSocket server = new ServerSocket(port);
    }


    /*CONNECT handshake
    * client handshake request
    * IF - not understood or has an incorrect value: "400 Bad Request", immediately close the socket
    * ELSE - send accept frame
    */
    Socket client = server.accept();

    //recieve message
    InputStream message = client.getInputStream();

    //read mask
    //read lenght
    //read data

    //decode and answer


    //CLOSE handshake
    //sends closing frame --> get closing frame back --> close connection
    //recieve closing frame --> send closing frame back
}


