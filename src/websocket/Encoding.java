package websocket;

import java.security.MessageDigest;
import java.util.Base64;

/**
 * Created by AnitaKristineAune on 25.04.2017.
 *
 * Contains encoding methods
 * Decodes the Sec-Websocket-Key using the GUID-string. Uses SHA-1 and base64 to encode to a new key.
 *
 */

public class Encoding {
    private static String key;

    public String encode(String key) throws Exception{
        MessageDigest md = MessageDigest.getInstance("SHA-1"); // Gets SHA-1 algorithm

        String guid = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

        key = key.concat(guid);
        byte[] keyBytes = key.getBytes("UTF-8");

        md.update(keyBytes);
        byte[] shaBytes = md.digest();

        String encoded = Base64.getEncoder().encodeToString(shaBytes);

        return encoded;
    }
}