package websocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/*
 * DataHandler for WebSocket server
 *
 * @author Anita Kristine Aune
 * @author Marit Holm
 *
 */

class DataHandler {
    /**
     * Takes a Sec-WebSocket-Key as a String and encodes it according to RFC6455 WebSocket protocol.
     * @param   key         Sec-WebSocket-Key recieved from client
     * @return              encoded key
     * @throws  Exception   thrown if unknown algorithm or unsupported encoding
     */
    protected String encodeKey(String key) {
        MessageDigest md = null; // Gets SHA-1 algorithm
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        String guid = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

        key = key.concat(guid);
        byte[] keyBytes = new byte[0];
        try {
            keyBytes = key.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        md.update(keyBytes);
        byte[] shaBytes = md.digest();

        String enc = Base64.getEncoder().encodeToString(shaBytes);

        return enc;
    }

    /**
     * Unmasks the payload recieved from client. Uses XOR encryption according to RFC6455 WebSocket protocol.
     * @param input byte array containing masking key and payload
     * @return      byte array with the unmasked data
     */
    protected byte[] unmaskData(byte[] input){
        byte[] maskingKey = new byte[4]; // first 4 bytes of input is masking key

        for(int i = 0; i < maskingKey.length; i++){
            maskingKey[i] = input[i];
        }

        byte[] result = new byte[input.length-4];

        for(int i = 0; i < result.length; i++){
           result[i] = (byte) (input[i+4] ^ maskingKey[i%4]);
        }

        return result;
    }


    /**
     * Generates frame for sending messages from server to client.
     * @param input     array containing the message in bytes
     * @return          byte array with framed message
     */
    protected byte[] generateFrame(byte[] input) throws IOException { // Payload from byte array, msg is text

        byte opcode = (byte)0b10000001;
        byte[] length = null;
        byte[] frame = null;

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {

            if(input.length < 126) {
                length = new byte[1];
                length[0] = (byte) input.length;

            } else if (input.length >= 126 && input.length < Math.pow(2,16)) {
                length = new byte[3];
                length[0] = (byte) 0b01111110 ; //126

                for(int i = 0; i < 2; i++) {
                    length[2-i] = (byte) ((input.length >>> i*8) & 0xFF);
                }

            } else if (input.length >= Math.pow(2,16) && input.length < Math.pow(2, 64)){
                length = new byte[9];
                length[0] = (byte) 0b01111111; //127

                for(int i = 0; i < 8; i++){
                    length[8-i] = (byte) ((input.length >>> i*8) & 0xFF);
                }

            } else {
                throw new IOException("Message too long");
            }
            output.write(opcode);
            output.write(length);
            output.write(input);
            frame = output.toByteArray();

        } catch (IOException e) {
            System.err.println("Error: IOException");
        }

        return frame;
    }

    /**
     * Generates a server initiated status frame.
     * CLOSE: Used to initiate or answer close connection
     * PING: Check if connection is still alive
     * PONG: Answer to PING
     * @param status   status frame choices: "CLOSE", "PING", "PONG"
     * @return         the specified status frame
     */
    // Generates a server initiated closing frame
    protected byte[] generateStatusFrame(String status){
        byte opcode = (byte) 0;
        byte payloadSize = (byte) 0;

        switch(status) {
            case "CLOSE":
                opcode = (byte) 0b10001000; // 0x8
                payloadSize = (byte) 0b00000000;
                break;

            case "PING":
                opcode = (byte) 0b10001001; // 0x9
                payloadSize = (byte) 0b00000000;
                break;

            case "PONG":
                opcode = (byte) 0b10001010; // 0xA
                payloadSize = (byte) 0b00000000;
                break;

            default:
                break;
        }

        byte[] frame;

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(opcode);
        output.write(payloadSize);

        frame = output.toByteArray();

        return frame;
    }

    /**
     * Generates response handshake from server after the client has initiated connection.
     *
     * @param key         the Sec-WebSocket-Key from the client.
     * @return            server handshake response with encoded key
     * @see               #encodeKey(String key)
     */
    protected String generateServerResponse(String key) {
        DataHandler accept = new DataHandler();
        String newKey = accept.encodeKey(key);

        String handshake = "HTTP/1.1 101 Switch Protocols\r\n" +
                            "Upgrade: websocket\r\n" +
                            "Connection: Upgrade\r\n" +
                            "Sec-WebSocket-Accept: " + newKey+"\r\n\r\n";
        return handshake;
    }
}