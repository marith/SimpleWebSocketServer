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


    public void connection(){
        try {
            client = server.accept();
            InputStream in = client.getInputStream();
            OutputStream out = client.getOutputStream();
            handshake(in,out);
            Encoding enc = new Encoding();

            while(true){
                byte type = (byte)in.read();
                int size = (0x000000FF) & in.read() -128;
                System.out.println("size1: "+size);
                if(size>125){
                    byte[] buffer = new byte[2];
                    buffer[0] = (byte)in.read();
                    buffer[1] = (byte)in.read();
                    System.out.println("size2: "+buffer.toString());
                    //size = new size
                    if(size>126){
                        buffer = new byte[8];
                        in.read(buffer,4,8);
                        //size = new size
                        System.out.println("size3: "+buffer.toString());
                    }
                }

                byte[] payload = new byte[size+4];
                in.read(payload);
                byte[] message = enc.maskData(payload);
                String s = new String(message);
                System.out.println("Message: "+s);

                byte[] msgBack = enc.generateFrame(s);
                out.write(msgBack);
                
                System.out.println("end");
                break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                client.close();
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean handshake(InputStream in, OutputStream out){
        try {
            String data = new Scanner(in,"UTF-8").useDelimiter("\\r\\n\\r\\n").next();
            String searchFor ="Sec-WebSocket-Key: ";
            Pattern word = Pattern.compile(searchFor);
            Matcher match = word.matcher(data);
            match.find();
            String key = data.substring(match.end(),match.end()+24);

            //TODO: IF NO MATCH --> return false

            Encoding enc = new Encoding();
            String response = enc.generateServerResponse(key);
            byte[] responseByte = response.getBytes();
            out.write(responseByte);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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
        ws.connection();

    }
}


