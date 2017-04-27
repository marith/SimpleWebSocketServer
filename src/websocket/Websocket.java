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
import java.util.Arrays;
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
    ServerSocket server;
    boolean isRunning = true;
    private LinkedBlockingQueue<String> messageQueue = new LinkedBlockingQueue();

    public void connect(int port) throws IOException {
        server = new ServerSocket(port);

        Thread threadHandler = new ThreadHandler();
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
        System.out.println("Server venter på message...");
        try {
            message = messageQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Message: "+message);
        return message;
    }

    public void close() throws IOException {
        isRunning = false;
        server.close();
    }

    // For testing
    public static void main(String[] args) throws IOException {
        Websocket ws = new Websocket();
        ws.connect(3001);

        while(true){
            System.out.println("Server while-løkke!");
            String message = ws.recieveMessage();
            ws.sendMessage(message);
        }
    }


    class ClientConnection extends Thread {
        private Socket client;
        private InputStream in;
        private OutputStream out;

        public ClientConnection(Socket client) throws SocketException {
            this.client = client;
            client.setSoTimeout(4000);
        }

        public void run() {
            try {
                in = client.getInputStream();
                out = client.getOutputStream();
                if (!handshake(in, out)) {
                    throw new Exception("Error connecting to client");
                }
                Encoding enc = new Encoding();

                while (true) {
                    try {
                        byte type = (byte) in.read();
                        int opcode = type & 0x0F;
                        byte[] msgBack = null;

                        //TEXT-FRAME
                        if (opcode == 0x1) {
                            byte[] message = recieveMessage(enc);
                            String messageStr = new String(message, "UTF-8");
                            System.out.println("In thread: "+messageStr);
                            messageQueue.add(messageStr);
                            System.out.println("Peek:"+messageQueue.peek());
                        }
                        //PING-FRAME
                        else if (opcode == 0x9) {//ping
                            byte length = (byte) in.read();
                            System.out.println("Ping! length: " + Integer.toBinaryString(length));
                            msgBack = enc.generateStatusFrame("PONG");
                            sendMessage(msgBack);
                        }
                        //CLOSE-FRAME
                        else if (opcode == 0x8) {//close
                            byte length = (byte) in.read();
                            System.out.println("Close! length: " + Integer.toBinaryString(length));
                            msgBack = enc.generateStatusFrame("CLOSE");
                            sendMessage(msgBack);

                        } else {
                            System.out.println("Unhandled opcode: " + Integer.toBinaryString(opcode));
                            break;
                        }
                        //PING IF SOCKET-TIMEOUT
                    } catch (SocketTimeoutException ste) {
                        byte[] msgBack = enc.generateStatusFrame("PING");
                        sendMessage(msgBack);
                        System.out.println("PING!");

                        byte type = (byte) in.read();
                        int opcode = type & 0x0F;
                        if (opcode == 0xA) {
                            byte length = (byte) in.read();
                            byte[] mask = new byte[4];
                            in.read(mask, 0, 4);
                            System.out.println("PONG!\n");
                        } else {
                            //TODO: feilhåndtering
                            System.out.println("ikke pong.. :(");
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
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

        private byte[] recieveMessage(Encoding enc) throws IOException {
            int size = (0x000000FF) & in.read() - 128;

            if (size == 126) {
                System.out.println("Størrelsen er 126!\n");
                byte ch1 = (byte) in.read();
                byte ch2 = (byte) in.read();
                byte[] byteArr = {ch1, ch2};

                size = ((ch1 << 8) + (ch2 << 0)) & 0xFF;
            } else if (size == 127) {
                byte[] buffer = new byte[8];
                in.read(buffer, 4, 8);
                //size = new size
                System.out.println("size 3: " + new String(buffer));
            }

            byte[] payload = new byte[size + 4];
            in.read(payload);

            byte[] message = enc.maskData(payload);
            return message;
        }

        protected void sendMessage(byte[] message) throws IOException {
            out.write(message, 0, message.length);
            out.flush();
        }

        //Server close
        public void close() {

        }

    }

    class ThreadHandler extends Thread {

        public void run(){
            while(isRunning) {
                Socket connection = null;
                try {
                    connection = server.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Thread client = null;
                try {
                    client = new ClientConnection(connection);
                } catch (SocketException e) {
                    e.printStackTrace();
                }
                syncList.add(client); // Adds the running threads (clients) to a list
                client.start();
            }
        }
    }
}

