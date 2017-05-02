package websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A simple WebSocket server
 *
 *
 * @author Anita Kristine Aune
 * @author Marit Holm
 */
public class WebSocket{
    private static ArrayList<Thread> threads = new ArrayList<>();
    private static List<Thread> syncList = Collections.synchronizedList(threads);
    private static LinkedBlockingQueue<String> messageQueue = new LinkedBlockingQueue<String>();
    private static volatile boolean isRunning = true;
    private static ServerSocket server;
    private static Thread threadHandler;
    private static int pingTimeout;

    /**
     * Opens a ServerSocket connection to the specified port
     * and starts ThreadHandler which accepts new client connections
     * @param port          the port number
     * @param pingTimeout   how often the server will ping the client, in milliseconds
     * @throws IOException  throws exception if ServerSocket is unable to connect
     */
    public void connect(int port, int pingTimeout) throws IOException {
        server = new ServerSocket(port);
        this.pingTimeout = pingTimeout;
        threadHandler = new ThreadHandler();
        threadHandler.start();
    }

    /**
     * Opens a ServerSocket connection to the specified port
     * and starts ThreadHandler which accepts new client connections
     * @param port          the port number
     * @param backlog       requested maximum length of the queue of incoming connections.
     * @param bindAddress   the local InetAddress the server will bind to
     * @param pingTimeout   how often the server will ping the client, in milliseconds
     * @throws IOException  throws exception if ServerSocket is unable to connect
     */
    public void connect(int port, int backlog, InetAddress bindAddress, int pingTimeout) throws IOException {
        server = new ServerSocket(port, backlog, bindAddress);
        this.pingTimeout = pingTimeout;
        threadHandler = new ThreadHandler();
        threadHandler.start();
    }


    /**
     * Sends a string message to all clients
     * @param message   String text
     * @return      false if there are no clients connected
     *              otherwise true
     * @throws      IOException if error when writing to client
     */
    public boolean sendMessage(String message) throws IOException {
        DataHandler dh = new DataHandler();
        byte[] byteMsg = message.getBytes(Charset.forName("UTF-8"));
        byte[] framedMsg = dh.generateFrame(byteMsg);

        if(syncList.isEmpty()){
            return false;
        }
        for (int i = 0; i < syncList.size(); i++) {
            ClientConnection con = (ClientConnection) syncList.get(i);
            con.sendMessage(framedMsg);
        }
        return true;
    }

    /**
     * Waits for new message in the message queue,
     * and retrieves it when the message arrives
     * @return      String message from client
     *              message is null if waiting is interrupted
     */
    public String recieveMessage(){
        String message = null;
        try {
            message = messageQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return message;
    }

    /**
     * Interrupts the threadHandler which tells all the client threads to shut down
     * and closes the ServerSocket
     * @throws IOException  if unable to close ServerSocket
     */
    public void close() throws IOException {
        isRunning = false;
        threadHandler.interrupt();
        server.close();
    }

    /**
     * Removes specified client from client list, when it is shutting down
     * @param client ClientConnection object that should be removed
     */
    private void removeClient(ClientConnection client){
        for(int i=0; i<syncList.size();i++){
            if(syncList.get(i).equals(client)){
                syncList.remove(i);
            }
        }
    }

    /**
     * ClientConnection object
     * extends {@link Thread}
     *<p>
     *     a client connection which handles the communication
     *     from server to client
     *</p>
     */
    private class ClientConnection extends Thread {
        private Socket client;
        private InputStream in;
        private OutputStream out;
        private DataHandler dh;
        private volatile boolean isClosing=false;
        private boolean ping=false;

        /**
         *
         * @param client        a Socket connection to a client
         * @param pingTimeout   int milliseconds how often the server should send a
         *                      ping to client to check if connection is alive
         * @throws SocketException if unable to set timeout
         */
        private ClientConnection(Socket client, int pingTimeout) throws SocketException {
            this.client = client;
            client.setSoTimeout(pingTimeout);
            this.dh = new DataHandler();
        }

        /**
         * Sets boolean isClosing to true to tell the thread that it should shut down
         * the connection to the client
         */
        private void setIsClosing(){
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
                while (!isClosing) {  //keeps running while isClosing is false
                    try {
                        //reads first byte and gets opcode
                        byte type = (byte) in.read();
                        int opcode = type & 0x0F;
                        switch (opcode){
                            case 0x1: //Text frame - adds received message to message queue
                                byte[] message = readTextMessage();
                                String messageStr = new String(message, "UTF-8");
                                messageQueue.add(messageStr);
                                break;

                            case 0x9: //ping frame - sends a pong frame back
                                readControlMessage();
                                sendMessage(dh.generateStatusFrame("PONG"));
                                break;
                            case 0xA: //pong frame - when receiveing a pong frame, sets boolean ping to false
                                readControlMessage();
                                ping = false;
                                break;

                            case 0x8: //close frame - sets isClosing is true to end thread and removes client from list
                                readControlMessage();
                                isClosing = true;
                                removeClient(this);
                                break;

                            default:
                                throw new IllegalArgumentException("Unsupported message type.");
                        }

                    }
                    //PING if socket-timeout - sends ping frame and sets boolean ping to true
                    //if ping is already true there was no answer to last ping -> closes connection
                    catch (SocketTimeoutException ste) {
                        if(ping){
                            isClosing=true;
                            System.out.println("ping: har allerede sendt ping...");
                        }else{
                            byte[] msgBack = dh.generateStatusFrame("PING");
                            sendMessage(msgBack);
                            ping = true;
                        }
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally { //shuts down the connection to the client after sending a "close" frame
                if(client.isConnected()){
                    try {
                        byte[] closingframe = dh.generateStatusFrame("CLOSE");
                        sendMessage(closingframe);
                        out.close();
                        in.close();
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * Receives the HTTP request from a client and checks the Sec-Websocket-Key
         * If key is found and is valid, sends a server handshake response back
         * @param in    InputStream for client
         * @param out   OutputStream for client
         * @return      true if handshake was successful
         *              false if IOException or handshake unsuccessful
         */
        private boolean handshake(InputStream in, OutputStream out) {
            try {
                String data = new Scanner(in, "UTF-8").useDelimiter("\\r\\n\\r\\n").next();

                //Checks if the request is a valid websockt
                String get = "GET";
                String httpVersion = "HTTP/1.1";
                String upgrade = "Upgrade: websocket";
                String connection = "Connection: Upgrade";

                Pattern getPat = Pattern.compile(get);
                Pattern httpVersionPat = Pattern.compile(httpVersion);
                Pattern upgradePat = Pattern.compile(upgrade);
                Pattern connectionPat = Pattern.compile(connection);

                Matcher getMatch = getPat.matcher(data);
                Matcher httpVersionMatch = httpVersionPat.matcher(data);
                Matcher upgradeMatch = upgradePat.matcher(data);
                Matcher connectionMatch = connectionPat.matcher(data);

                if(!getMatch.find() || !httpVersionMatch.find() || !upgradeMatch.find() || !connectionMatch.find()){
                    String response = dh.badRequestResponse();
                    byte[] responseByte = response.getBytes();
                    out.write(responseByte);
                    return false;
                }

                String keyName = "Sec-WebSocket-Key: ";
                Pattern keyPat = Pattern.compile(keyName);
                Matcher match = keyPat.matcher(data);
                boolean isMatch = match.find();

                if (!isMatch) {
                    String response = dh.badRequestResponse();
                    byte[] responseByte = response.getBytes();
                    out.write(responseByte);
                    return false;
                }
                String key = data.substring(match.end(), match.end() + 24);

                String response = dh.generateServerResponse(key);
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

        /**
         * Reads the control message from second byte to end of message
         * @return      unmasked byte array with message
         * @throws IllegalArgumentException     if the client has sent an unmasked message
         */
        private byte[] readControlMessage() throws IOException {
            byte lengthRead = (byte)in.read();
            if(((lengthRead >>> 7)&0xFF) == 0){
                throw new IllegalArgumentException("Unmasked message from client");
            }
            int length = (0x000000FF) & lengthRead - 128;
            byte[] input = new byte[4+length];
            in.read(input, 0, input.length);
            return dh.unmaskData(input);
        }


        /**
         * Reads the control message from second byte to end of message
         * supports message length up to 2^16 bytes
         * @return      byte array with unmasked message
         * @throws      IllegalArgumentException if message is longer than 2^16 bytes
         */
        private byte[] readTextMessage() throws IOException {
            int length = (0x000000FF) & in.read() - 128; //if size is <126, this is the length

            if (length == 126) { //if "length" is 126: read next two bytes for real length
                byte[] buffer = new byte[2];
                buffer[0]=(byte) in.read();
                buffer[1]=(byte) in.read();
                length = (buffer[0] & 0xFF) << 8 | (buffer[1] & 0xFF);

            } else if (length == 127) { //if "length" is 127: read next 8 bytes for real length
                //NOT IMPLEMENTED
                /*
                byte[] buffer = new byte[8];
                in.read(buffer, 2, 8);
                long lengthLong =    (buffer[0] & 0xFF) << 56 | (buffer[1] & 0xFF) << 48|
                            (buffer[2] & 0xFF) << 40 | (buffer[3] & 0xFF) << 32|
                            (buffer[4] & 0xFF) << 24 | (buffer[5] & 0xFF) << 16|
                            (buffer[6] & 0xFF) << 8  | (buffer[7] & 0xFF);
                */
                throw new IllegalArgumentException("Message too large.");
            }

            byte[] input = new byte[4 + length]; //read mask + length
            in.read(input);

            return dh.unmaskData(input);
        }

        /**
         * Writes an already framed message to client
         * @param message       byte array with a framed message
         * @throws IOException  if unable to write message
         */
        private void sendMessage(byte[] message) throws IOException {
            out.write(message, 0, message.length);
            out.flush();
        }
    }

    /**
     * The threadhandler which starts and manages the ClientConnection threads
     * Waits for new connections through the ServerSocket
     * When interrupted, tells all clients threads to end connection
     */
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
                    client = new ClientConnection(connection, pingTimeout);
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