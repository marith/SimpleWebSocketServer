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

        byte[] result = new byte[payload.length];

        for(int i = 0; i < payload.length; i++){
           result[i] = (byte) (payload[i] ^ maskingKey[i%4]);
        }

        return result;
    }

    public byte[] generateFrame(String text){

        byte[] textBytes = text.getBytes(); // The payload

        byte first = (byte)0b10000001;

        byte[] frame = new byte[1+textBytes.length]; // Size of frame is determined by 8 bits of information + the payload

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            output.write(first);
            output.write(textBytes);

            frame = output.toByteArray();

        } catch (IOException e) {
            System.err.println("Error: IOException");
        }

        return frame;
    }
}