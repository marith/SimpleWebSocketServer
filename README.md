# Simple WebSocket server library
This is a simple WebSocket server Java library, created as a project in the course Network programming (TDAT2004) at NTNU, Trondheim. 
The library is based off the WebSocket protocol RFC6455 by IETF. It has support for multiple clients via multithreading, and can send and recieve text messages up to 65 000 bytes.
<br>
<br>
## Table of Contents
* [Team Members](#team-members)
* [Usage](#code)
* [Installation](#installation)
* [API Reference](#api)
* [Credits](#credits)
<br>

## <a name="team-members"></a>Team Members
* Anita Kristine Aune -  <anitakra@stud.ntnu.no>
* Marit Holm - <marith1@stud.ntnu.no>
<br>

## <a name="code"></a>Usage
You can use this library to set up a simple WebSocket server. 

After installing the library and creating a WebSocket object, you can use the following methods: 
* connect(int port, int timeout) - sets up a server which allows several clients to connect
* sendMessage(String message) - sends String message to all connected clients
* recieveMessage() - waits for client to send message, and returns String message
* close() - disconnects the server and closes all client connections

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

## <a name="credits"></a>Credits
Reference list: 
* <a href=https://tools.ietf.org/html/rfc6455>IETF - The Websocket Protocol</a>
* <a href=https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_WebSocket_servers>Mozilla Foundations - Writing websocket servers</a>
<br>





