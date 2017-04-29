package websocket;

import org.junit.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/*
 * JUnit tests for DataHandler
 */

public class DataHandlerTest {
    private DataHandler dh;


    @Before
    public void setUp() throws Exception {
        dh = new DataHandler();

    }

    @Test
    public void testEncodeKey() throws Exception {
        String key = "dGhlIHNhbXBsZSBub25jZQ==";
        String res = dh.encodeKey(key);

        String validRes = "s3pPLMBiTxaQ9kYGzzhZRbK+xOo="; // Valid result
        String invalidRes = "w5pfFvEi2y6Q2kGGbBhtRbJLxOo="; // Invalid result

        assertEquals(res,validRes);
        assertNotEquals(res,invalidRes);
    }

    @Test
    public void testUnmaskData() throws Exception {

        byte[] input = {(byte)0b10001100, (byte) 0b11010111, (byte) 0b01001111, (byte) 0b11011101, (byte) 0b10110000,
                (byte) 0b10100100, (byte) 0b00111111, (byte) 0b10111100, (byte) 0b11100010, (byte) 0b11101001,
                (byte) 0b01110011, (byte) 0b10111111, (byte) 0b10110010, (byte) 0b10010110, (byte) 0b00100001,
                (byte) 0b10110100, (byte) 0b11111000, (byte) 0b10110110, (byte) 0b01110011, (byte) 0b11110010,
                (byte) 0b11101110, (byte) 0b11101001, (byte) 0b01100011, (byte) 0b11111101, (byte) 0b10110000,
                (byte) 0b10111110, (byte) 0b01110001, (byte) 0b11101100, (byte) 0b10111101, (byte) 0b11101101,
                (byte) 0b01111010, (byte) 0b11101110, (byte) 0b10110110, (byte) 0b11100100, (byte) 0b01111110,
                (byte) 0b11111101, (byte) 0b10110110, (byte) 0b11110111, (byte) 0b01110011, (byte) 0b11110010,
                (byte) 0b11100101, (byte) 0b11101001, (byte) 0b00000111, (byte) 0b10111000, (byte) 0b11100101,
                (byte) 0b11101011, (byte) 0b01100000, (byte) 0b10101110, (byte) 0b11111100, (byte) 0b10110110,
                (byte) 0b00100001, (byte) 0b11100011, (byte) 0b10110000, (byte) 0b10110101, (byte) 0b00111101,
                (byte) 0b11100011};

        byte[]res = dh.unmaskData(input);

        String validRes = "<span><b>Anita</b>, <i>11:53:31 : </i>Hei</span><br>";
        String invalidRes = "Hei";
        String strRes = new String(res, Charset.forName("UTF-8"));

        assertEquals(strRes,validRes);
        assertNotEquals(strRes,invalidRes);
    }

    @Test
    public void testGenerateFrame() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Payload length < 126:
        byte[] smallInput =
                {(byte)0b10001100, (byte) 0b11010111, (byte) 0b01001111, (byte) 0b11011101, (byte) 0b10110000,
                (byte) 0b10100100, (byte) 0b00111111, (byte) 0b10111100, (byte) 0b11100010, (byte) 0b11101001,
                (byte) 0b01110011, (byte) 0b10111111, (byte) 0b10110010, (byte) 0b10010110, (byte) 0b00100001,
                (byte) 0b10110100, (byte) 0b11111000, (byte) 0b10110110, (byte) 0b01110011, (byte) 0b11110010,
                (byte) 0b11101110, (byte) 0b11101001, (byte) 0b01100011, (byte) 0b11111101, (byte) 0b10110000,
                (byte) 0b10111110, (byte) 0b01110001, (byte) 0b11101100, (byte) 0b10111101, (byte) 0b11101101,
                (byte) 0b01111010, (byte) 0b11101110, (byte) 0b10110110, (byte) 0b11100100, (byte) 0b01111110,
                (byte) 0b11111101, (byte) 0b10110110, (byte) 0b11110111, (byte) 0b01110011, (byte) 0b11110010,
                (byte) 0b11100101, (byte) 0b11101001, (byte) 0b00000111, (byte) 0b10111000, (byte) 0b11100101,
                (byte) 0b11101011, (byte) 0b01100000, (byte) 0b10101110, (byte) 0b11111100, (byte) 0b10110110,
                (byte) 0b00100001, (byte) 0b11100011, (byte) 0b10110000, (byte) 0b10110101, (byte) 0b00111101,
                (byte) 0b11100011, (byte) 0b11100011, (byte) 0b10110000, (byte) 0b10110101, (byte) 0b00111101,
                (byte) 0b11100011, (byte) 0b10110000, (byte) 0b10110101};


        // Small input
        byte[] smallResult = dh.generateFrame(smallInput);
        byte[] smallExpResult;

        byte[] expSmallFrame = {(byte)0b10000001, (byte) 63};
        out.write(expSmallFrame);
        out.write(smallInput);
        smallExpResult = out.toByteArray();
        out.reset();

        assertArrayEquals(smallResult,smallExpResult);
        assertNotEquals(smallInput.length, smallResult.length);


        // Medium input - payload length 126
        byte[] mediumInput;
        out.write(smallInput);
        out.write(smallInput);
        mediumInput = out.toByteArray();
        out.reset();

        byte[] mediumResult = dh.generateFrame(mediumInput);

        byte[] expMediumFrame = {(byte)0b10000001, (byte) 126, (byte) (126 >>> 8) & 0xFF, (byte) (126 & 0xFF)};

        byte[] mediumExpResult;
        out.write(expMediumFrame);
        out.write(mediumInput);
        mediumExpResult = out.toByteArray();
        out.reset();

        assertArrayEquals(mediumResult,mediumExpResult);
        assertNotEquals(mediumInput.length, mediumResult.length);

    }

    @Test
    public void testGeneratePingStatusFrame() throws Exception {
        byte[] pingRes = dh.generateStatusFrame("PING");
        byte[] expPing = {(byte)0b10001001, 0b00000000};

        assertArrayEquals(pingRes,expPing);

    }
    @Test
    public void testGeneratePongStatusFrame() throws Exception {
        byte[] pongRes = dh.generateStatusFrame("PONG");
        byte[] expPong = {(byte) 0b10001010, 0b00000000};

        assertArrayEquals(pongRes,expPong);
    }

    @Test
    public void testGenerateClosingFrame() throws Exception {
        byte[] closeRes = dh.generateStatusFrame("CLOSE");
        byte[] expClose = {(byte) 0b10001000, 0b00000000};

        assertArrayEquals(closeRes,expClose);
    }

    @Test
    public void testGenerateServerResponse() throws Exception {
        String validHandshake = "HTTP/1.1 101 Switch Protocols\r\n" +
                "Upgrade: java.no.ntnu.websocket\r\n" +
                "Connection: Upgrade\r\n" +
                "Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=\r\n\r\n";

        String invalidHandshake = "HTTP/1.1 101 Switch Protocols\r\n" +
                "Upgrade: java.no.ntnu.websocket\r\n" +
                "Connection: Upgrade\r\n" +
                "Sec-WebSocket-Accept: w5pfFvEi2y6Q2kGGbBhtRbJLxOo=\r\n\r\n";

        String resHandshake = dh.generateServerResponse("dGhlIHNhbXBsZSBub25jZQ==");

        assertEquals(resHandshake,validHandshake);
        assertNotEquals(resHandshake,invalidHandshake);
    }
}