package websocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Created by AnitaKristineAune on 25.04.2017.
 *
 */

public class Encoding {
    private static String key;

    // Encodes the sec-websocket-key using the GUID-string. Uses SHA-1 and base64 to encode to a new key.
    public String encodeKey(String key) throws Exception{
        MessageDigest md = MessageDigest.getInstance("SHA-1"); // Gets SHA-1 algorithm

        String guid = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

        key = key.concat(guid);
        byte[] keyBytes = key.getBytes("UTF-8");

        md.update(keyBytes);
        byte[] shaBytes = md.digest();

        String encode = Base64.getEncoder().encodeToString(shaBytes);

        return encode;
    }

    // Masks the payload with XOR encryption
    public byte[] maskData(byte[] payload){
        byte[] maskingKey = new byte[4]; // first 32 bits of input is masking key

        for(int i = 0; i < maskingKey.length; i++){
            maskingKey[i] = payload[i];
        }

        byte[] result = new byte[payload.length-4];

        for(int i = 4; i < payload.length; i++){
           result[i] = (byte) (payload[i] ^ maskingKey[i%4]);
        }

        return result;
    }

    public byte[] generateTextFrame(String text){ // Payload generated from string

        byte[] textBytes = text.getBytes(); // The payload
        byte opcode = (byte)0b10000001;
        byte[] mask = {0x0,0x0,0x0,0x0};
        byte[] length = null;

        byte[] frame = null;

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {

            if(textBytes.length < 126) {
                length = new byte[1];
                length[0] = (byte) textBytes.length;

            } else if (textBytes.length >= 126 && textBytes.length < Math.pow(2,16)) {
                length = new byte[2];
                length[0] = (byte) 126;

                for(int i = 1; i <= 16; i+=8) {
                    length[i] = (byte) ((textBytes.length >>> i) & 0xFF);
                }

            } else if (textBytes.length >= Math.pow(2,16) && textBytes.length < Math.pow(2, 64)-1){
                length = new byte[8];
                length[0] = (byte) 127;

                for(int i = 1; i <= 64; i+=8){
                    length[i] = (byte) ((textBytes.length >>> i) & 0xFF);
                }

        } else {
            System.err.print("Do some error handling here");
        }

        output.write(opcode);
        output.write(length);
        output.write(mask);
        output.write(textBytes);

        frame = output.toByteArray();

        } catch (IOException e) {
            System.err.println("Error: IOException");
        }

        return frame;
    }

    public byte[] generateFrame(byte[] input){ // Payload from byte array, msg is text

        byte opcode = (byte)0b10000001;
        byte[] mask = {0x0,0x0,0x0,0x0};
        byte[] length = null;

        byte[] frame = null;

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {

            if(input.length < 126) {
                length = new byte[1];
                length[0] = (byte) input.length;

            } else if (input.length >= 126 && input.length < Math.pow(2,16)) {
                length = new byte[2];
                length[0] = (byte) 126;

                for(int i = 1; i <= 16; i+=8) {
                    length[i] = (byte) ((input.length >>> i) & 0xFF);
                }

            } else if (input.length >= Math.pow(2,16) && input.length < Math.pow(2, 64)-1){
                length = new byte[8];
                length[0] = (byte) 127;

                for(int i = 1; i <= 64; i+=8){
                    length[i] = (byte) ((input.length >>> i) & 0xFF);
                }

            } else {
                System.err.print("Do some error handling here");
            }

            output.write(opcode);
            output.write(length);
            output.write(mask);
            output.write(input);

            frame = output.toByteArray();

        } catch (IOException e) {
            System.err.println("Error: IOException");
        }

        return frame;
    }

    // Generates a server initiated closing frame
    public byte[] generateStatusFrame(String status){
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

    // Generates response handshake from server
    public String generateServerResponse(String key) throws Exception{
        Encoding accept = new Encoding();
        String newKey = accept.encodeKey(key);

        String handshake = "HTTP/1.1 101 Switch Protocols\r\n" +
                            "Upgrade: websocket\r\n" +
                            "Connection: Upgrade\r\n" +
                            "Sec-WebSocket-Accept: " + newKey+"\r\n\r\n";
        return handshake;
    }

    public static void main(String[] args) {
        Encoding enc = new Encoding();

        byte[] res = enc.generateTextFrame("Hellolawdlskflawdscdkldms");
        for(int i = 0; i < res.length; i++) {
            System.out.println((res[i])+"\n");
        }
    }
}