package websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Marit on 25.04.2017.
 */
public class Websocket {
    private static ArrayList<Thread> threads = new ArrayList<>();
    private static List<Thread> syncList = Collections.synchronizedList(threads);

    public Websocket() throws IOException {
    }

    public void connect(int port) throws IOException{

        ServerSocket server = new ServerSocket(port);

        while(true){
            Socket connection = server.accept();
            Thread client = new ClientConnection(connection);
            syncList.add(client); // Adds the running threads (clients) to a list
            client.start();
        }
    }

    public void sendMessage(String message) throws IOException {
        byte[] byteMsg = message.getBytes(Charset.forName("UTF-8"));

        for(int i = 0; i < syncList.size(); i++){
            ClientConnection con = (ClientConnection) syncList.get(i);
            con.sendMessage(byteMsg);
        }
    }

    // For testing
    public static void main(String[] args) throws IOException {
        Websocket ws = new Websocket();
        ws.connect(3001);
    }
}

class ClientConnection extends Thread {
    Socket client;

    public ClientConnection(Socket client){
        this.client = client;

    }

    public void run(){
        try {
            InputStream in = client.getInputStream();
            OutputStream out = client.getOutputStream();
            if(!handshake(in,out)){
                throw new Exception("Error connecting to client");
            }
            Encoding enc = new Encoding();

            while(true){
                byte type = (byte)in.read();
                int opcode = type & 0x0F;
                byte[] msgBack = null;
                if(opcode==0x1){ //text
                    byte[] message = recieveMessage(in, enc);
                    System.out.println(new String(message));
                    msgBack = enc.generateFrame(message);
                    sendMessage(out,msgBack);

                }else if(opcode==0x9){//ping
                    byte length = (byte)in.read();
                    System.out.println("Ping! length: "+Integer.toBinaryString(length));
                    msgBack = enc.generateStatusFrame("PONG");
                    sendMessage(out,msgBack);

                }else if(opcode==0xA){//pong
                    byte length = (byte)in.read();
                    System.out.println("Pong! length: "+Integer.toBinaryString(length));

                }else if(opcode==0x8){//close
                    byte length = (byte)in.read();
                    System.out.println("Close! length: "+Integer.toBinaryString(length));
                    msgBack = enc.generateStatusFrame("CLOSE");
                    sendMessage(out,msgBack);

                }else{
                    System.out.println("Unhandled opcode: "+Integer.toBinaryString(opcode));
                    break;
                }
                msgBack = null;
                msgBack = enc.generateStatusFrame("PING");
                sendMessage(out,msgBack);
                System.out.println("ping sendt: "+ Arrays.toString(msgBack));

                type = (byte)in.read();
                opcode = type & 0x0F;
                byte length = (byte)in.read();
                byte[] mask = new byte[4];
                in.read(mask,0,4);
                System.out.println("Pong! "+Integer.toBinaryString(opcode)+", length: "+Integer.toBinaryString(length));
                System.out.println("Mask: "+ Arrays.toString(mask));

            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
  //              server.close();
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
            boolean isMatch = match.find();

            if(!isMatch){
                return false;
            }
            String key = data.substring(match.end(),match.end()+24);

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

    private byte[] recieveMessage(InputStream in, Encoding enc) throws IOException {
        int size = (0x000000FF) & in.read() -128;

        if(size==126) {
            System.out.println("Størrelsen er 126!\n");
            byte ch1 = (byte) in.read();
            byte ch2 = (byte) in.read();
            byte[] byteArr = {ch1, ch2};

            size = ((ch1 << 8) + (ch2 << 0)) & 0xFF;
        }
        else if(size==127){
            byte[] buffer = new byte[8];
            in.read(buffer,4,8);
            //size = new size
            System.out.println("size 3: "+new String(buffer));
        }

        byte[] payload = new byte[size+4];
        in.read(payload);

        byte[] message = enc.maskData(payload);
        return message;
    }

    protected void sendMessage(byte[] message) throws IOException {
        out.write(message, 0,message.length);
        out.flush();
    }

    //Server close
    public void close(){

    }
}



