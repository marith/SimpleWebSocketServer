package websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Marit on 25.04.2017.
 */
public class Websocket {
    ServerSocket server;
    Socket client;

    public Websocket(ServerSocket server) throws IOException {
        this.server = server;
    }
    /*CONNECT handshake
    * client handshake request
    * IF - not understood or has an incorrect value: "400 Bad Request", immediately close the socket
    * ELSE - send accept frame
    */
    public void handshake(){
        try {
            client = server.accept();
            InputStream in = client.getInputStream();
            OutputStream out = client.getOutputStream();

            String data = new Scanner(in,"UTF-8").useDelimiter("\\r\\n\\r\\n").next();
            String searchFor ="Sec-WebSocket-Key: ";
            Pattern word = Pattern.compile(searchFor);
            Matcher match = word.matcher(data);
            match.find();
            String key = data.substring(match.end(),match.end()+24);

            //TODO: IF NO MATCH --> client.close();

            Encoding enc = new Encoding();
            String serverKey = enc.encodeKey(key);
            System.out.println(serverKey);

            String response = enc.generateServerResponse(serverKey);

            byte[] responseByte = response.getBytes();
            out.write(responseByte);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //examples
    /*GET /chat HTTP/1.1
    Host: example.com:8000
    Upgrade: websocket
    Connection: Upgrade
    Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==
    Sec-WebSocket-Version: 13
    */



    //recieve message
    //read mask
    //read lenght
    //read data

    //decode and answer


    //CLOSE handshake
    //sends closing frame --> get closing frame back --> close connection
    //recieve closing frame --> send closing frame back



    //TEST
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(3001);
        Websocket ws = new Websocket(ss);
        ws.handshake();


        //byte[] encoded = new byte[] {(byte)198, (byte)131, (byte)130, (byte)182, (byte)194, (byte)135};
    }
}


