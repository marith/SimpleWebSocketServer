package websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.*;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Marit on 25.04.2017.
 */

public class Websocket{
    private static ArrayList<Thread> threads = new ArrayList<>();
    private static List<Thread> syncList = Collections.synchronizedList(threads);
    private static LinkedBlockingQueue<String> messageQueue = new LinkedBlockingQueue<String>();
    private static volatile boolean isRunning = true;
    private static ServerSocket server;
    private static Thread threadHandler;

    public void connect(int port) throws IOException {
        server = new ServerSocket(port);
        threadHandler = new ThreadHandler();
        threadHandler.start();
    }

    public void sendMessage(String message) throws IOException {
        Encoding enc = new Encoding();
        byte[] byteMsg = message.getBytes(Charset.forName("UTF-8"));
        byte[] framedMsg = enc.generateFrame(byteMsg);

        for (int i = 0; i < syncList.size(); i++) {
            ClientConnection con = (ClientConnection) syncList.get(i);
            con.sendMessage(framedMsg);
        }
    }

    public String recieveMessage(){
        String message = null;
        try {
            message = messageQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return message;
    }

    public void close() throws IOException {
        System.out.println("SERVER IS CLOSING");
        isRunning = false;
        threadHandler.interrupt();
        server.close();
    }

    private void removeClient(ClientConnection client){
        for(int i=0; i<syncList.size();i++){
            if(syncList.get(i).equals(client)){
                syncList.remove(i);
            }
        }
    }

    private class ClientConnection extends Thread {
        private Socket client;
        private InputStream in;
        private OutputStream out;
        private Encoding enc;
        private volatile boolean isClosing=false;
        private boolean ping=false;

        public ClientConnection(Socket client) throws SocketException {
            this.client = client;
            client.setSoTimeout(5000);
            this.enc = new Encoding();
        }
        public void setIsClosing(){
            this.isClosing = true;
        }

        public void run() {
            //Set inputstream and outputstream, and perform handshake
            try {
                in = client.getInputStream();
                out = client.getOutputStream();
                if (!handshake(in, out)) {
                    throw new IOException("Error connecting to client");
                }
                while (!isClosing) {
                    try {
                        byte type = (byte) in.read();
                        int opcode = type & 0x0F;
                        switch (opcode){
                            case 0x1: //Text frame
                                byte[] message = readTextMessage();
                                String messageStr = new String(message, "UTF-8");
                                messageQueue.add(messageStr);
                                break;

                            case 0x9: //ping frame
                                readControlMessage();
                                sendMessage(enc.generateStatusFrame("PONG"));
                                break;
                            case 0xA: //pong frame
                                readControlMessage();
                                ping = false;
                                break;

                            case 0x8: //close frame
                                readControlMessage();
                                isClosing = true;
                                removeClient(this);
                                break;

                            default:
                                throw new IOException("Unsupported message type.");
                        }

                    } //PING IF SOCKET-TIMEOUT
                    catch (SocketTimeoutException ste) {
                        if(ping){
                            isClosing=true;
                            System.out.println("ping: har allerede sendt ping...");
                        }else{
                            byte[] msgBack = enc.generateStatusFrame("PING");
                            sendMessage(msgBack);
                            ping = true;
                        }
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if(client.isConnected()){
                    try {
                        System.out.println("Connectin to client closing with closing frame.");
                        byte[] closingframe = enc.generateStatusFrame("CLOSE");
                        sendMessage(closingframe);
                        client.close();
                    } catch (IOException e) {
                        System.out.println("Couldn't close!");
                        e.printStackTrace();
                    }
                }
            }
        }

        private boolean handshake(InputStream in, OutputStream out) {
            try {
                String data = new Scanner(in, "UTF-8").useDelimiter("\\r\\n\\r\\n").next();
                String searchFor = "Sec-WebSocket-Key: ";
                Pattern word = Pattern.compile(searchFor);
                Matcher match = word.matcher(data);
                boolean isMatch = match.find();

                if (!isMatch) {
                    return false;
                }
                String key = data.substring(match.end(), match.end() + 24);

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

        private byte[] readControlMessage() throws IOException {
            byte lengthRead = (byte)in.read();
            if((lengthRead >>> 7) == 0){
                throw new IOException("Unmasked message from client");
            }
            int length = (0x000000FF) & lengthRead - 128;
            byte[] input = new byte[4+length];
            in.read(input, 0, input.length);
            return enc.unmaskData(input);
        }

        private byte[] readTextMessage() throws IOException {
            int length = (0x000000FF) & in.read() - 128; //if size is <126, this is the length

            if (length == 126) { //if "length" is 126: read next two bytes for real length
                byte ch1 = (byte) in.read();
                byte ch2 = (byte) in.read();
                length = ((ch1 << 8) + (ch2)) & 0xFF;

            } else if (length == 127) { //if "length" is 127: read next 8 bytes for real length
                byte[] buffer = new byte[8];
                in.read(buffer, 4, 8);
                //size = new size TODO: implementer 64bit lengde
                System.out.println("size 3: " + new String(buffer));
            }

            byte[] payload = new byte[4 + length]; //read mask + length
            in.read(payload);

            return enc.unmaskData(payload);
        }

        private void sendMessage(byte[] message) throws IOException {
            out.write(message, 0, message.length);
            out.flush();
        }

        public void close() {
            isClosing = true;
        }
    } //ClientConnection end

    private class ThreadHandler extends Thread {
        public void run(){
            while(isRunning) {
                Socket connection = null;
                try {
                    connection = server.accept();
                } catch (SocketException e) {
                    System.out.println("Server is closed");
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Thread client = null;
                try {
                    client = new ClientConnection(connection);
                } catch (SocketException e) {
                    e.printStackTrace();
                }
                if (!(client == null)) {
                    syncList.add(client); // Adds the running threads (clients) to a list
                    client.start();
                }
            }
            for(int i=0; i<syncList.size();i++){
                ClientConnection client = (ClientConnection) syncList.get(i);
                client.setIsClosing();
            }
        }
    }
}

