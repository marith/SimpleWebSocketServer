# Simple WebSocket server library
This is a simple WebSocket server Java library, created as a project in the course TDAT2004 Data Communications and Network Programming at NTNU in Trondheim. 
The library is based off the WebSocket protocol RFC6455 by IETF. It has support for multiple clients via multithreading, and can send and recieve text messages up to 65 000 bytes.
<br>
<br>
## Table of Contents
* [Team Members](#team-members)
* [Usage](#usage)
* [Code examples](#code)
* [Installation](#installation)
* [API Reference](#api)
* [WebSocket Protocol Implementations](#protocol)
* [Credits](#credits)
<br>

## <a name="team-members"></a>Team Members
* Anita Kristine Aune -  <anitakra@stud.ntnu.no>
* Marit Holm - <marith1@stud.ntnu.no>
<br>

## <a name="usage"></a>Usage
You can use this library to set up a simple WebSocket server. 

After installing the library and creating a WebSocket object, you can use the following methods: 
* `connect(int port, int timeout)` - sets up a server which allows several clients to connect. Timeout is ping frequency in milliseconds.
* `sendMessage(String message)` - sends String message to all connected clients
* `recieveMessage()` - waits for client to send message, and returns String message
* `close()` - disconnects the server and closes all client connections
<br>

## <a name="code"></a>Code examples
For code examples you can visit `src/examples`, or go directly:
* Chat server example:  <a href="https://github.com/marith/Websocket/tree/master/src/example/ExampleChatServer.java">ExampleChatServer.java</a>
* Echo server example:  <a href="https://github.com/marith/Websocket/tree/master/src/example/ExampleEchoServer.java">ExampleEchoServer.java</a>


If you wish to test the examples, you can run the index.html file located in web folder.

<br>

## <a name="intallation"></a>Installation
* Download `WebsocketServerLibrary.zip`
* Unzip file and add `Websocket.jar` to your project's build path.

<br>

## <a name="api"></a>API Reference
For Javadoc documentation of the project, visit
* <a href=https://marith.github.io/SimpleWebSocketServer>WebSocket server Javadoc</a>
<br>

## <a name="protocol"></a>WebSocket Protocol Implementations
The server supports opening handshake.
It also has support for sending and recieving ping and pong frames, which is used to check if the connection is still alive. Ping frequency can be specified.

The library also supports multiple clients via multithreading. 

The WebSocket connection automatically closes if the opening handshake is unsuccessful.
It also closes the connection under given circumstances. In any of these cases, the server sends a closing frame in return, and closes the connection:
  - If the client is inresponsive (no pong is recieved)
  - Unsupported data (opcode) is recieved
  - Closing frame is recieved
 
<br>


## <a name="credits"></a>Credits
Reference list: 
* <a href=https://tools.ietf.org/html/rfc6455>IETF - The Websocket Protocol</a>
* <a href=https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_WebSocket_servers>Mozilla Foundations - Writing websocket servers</a>
<br>





