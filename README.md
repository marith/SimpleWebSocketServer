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
* <i>connect(int port, int timeout)</i> - sets up a server which allows several clients to connect. Timeout is ping frequency in milliseconds.
* <i>sendMessage(String message)</i> - sends String message to all connected clients
* <i>recieveMessage()</i> - waits for client to send message, and returns String message
* <i>close()</i> - disconnects the server and closes all client connections
<br>

## <a name="code"></a>Code examples
For code examples you can visit the src/examples folder, or go directly:
* Chat server example:  <a href="https://github.com/marith/Websocket/tree/master/src/example/ExampleChatServer.java">ExampleChatServer.java</a>
* Echo server example:  <a href="https://github.com/marith/Websocket/tree/master/src/example/ExampleEchoServer.java">ExampleEchoServer.java</a>


If you wish to test the examples, you can run the index.html file located in web folder.

<br>

## <a name="intallation"></a>Installation
* Download WebsocketServerLibrary.zip
* Unzip file and add .jar file to your project's build path.

<br>

## <a name="api"></a>API Reference
For Javadoc documentation of the project, visit
* <a href=https://marith.github.io/Websocket>Websocket Javadoc</a>
<br>

## <a name="protocol"></a>WebSocket Protocol Implementations
The server supports Opening Handshake, and shuts down the connection if the handshake is unsuccessful.



## <a name="credits"></a>Credits
Reference list: 
* <a href=https://tools.ietf.org/html/rfc6455>IETF - The Websocket Protocol</a>
* <a href=https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_WebSocket_servers>Mozilla Foundations - Writing websocket servers</a>
<br>





