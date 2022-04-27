This code was originally written with IntelliJ

Application Name:  CipherChat
description:  CipherChat is a chatting application that allows people to chat with individuals in a secure way.
              The chat session encrypts the messages to and from each chat user.  Current version only handles
              one individual communication at time, so no broadcasting capabilites.

Class definition:  There are three main classes in this project
    a.  Server Class:  This is a multithreaded class that centralizes the client data and client requests to chat between users.
        The following are key functionality of this class:
        1.  Registers all incoming client requests to chat and stores the following data
            a.  username - used as key to find the appropriate information
            b.  session information - the chat web socket information
            c.  public key - the public key for the user, so it can be sent to other users to use for encryption
        2.  Handles a chat requests between users.

    b.  Client<name> Class:  Client class is the client chat software that allows users to communicate with other users.
        The following are key functionality of this class
        1.  Creates the new public and private keys when initially run
        2.  Registers and connects with the server providing a username and the public key to the server for it to store
            and pass on to other users for communictions
        3.  Requests the public key from server for the user that the client wants to communicate with
        4.  Encrypts the message using the  public key before sending the message to the user
        5.  Decrypts the message using the private key sent by other users

    c.  CryptographyUtil Class:  The utility class that helps with the cyrptography API

Directions on use:
The following are configuration you can make
1.  Server Server
    a.  final static int ServerPort = 1234;  //port number to use, note all of the clients need to be on the same ports as server port

2.  Client Class
   final static String HostName="localhost";  //the server name or IP address of the server hosting the chat
    final static int ServerPort = 1234;  //the port number of the websocket, make sure it is same as server port number
    final static String UserName = "David";  //the chat name you want to use and will be recognized by the server and others

3.  Current configuration is set which assumes the server and clients are on the same computer

4.  Run the server first.  You can not run the clients until the server is run

5.  How to chat.
    a.  The main thing you need to do first is to request a secure chat with the user you want to chat with by doing the following call
        <username>#--Secure--      for example:  Betty#--Secure--
    b.  Once you have the secure communication then you can speak with that person by just doing the following:
        <username>#<message>       for example:  Betty#Hello how are things

