// Java implementation for multithreaded chat client
// Save file as Client.java

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.util.Base64;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;

public class ClientBetty
{
    final static String HostName="localhost";
    final static int ServerPort = 1234;
    final static String UserName = "Betty";

    //private CryptographyUtil cryptoUtil = new CryptographyUtil();
    private static KeyPair generateKeyPair;
    private static byte[] publicKey;
    private static byte[] privateKey;
    // Create a HashMap object called capitalCities <user, publickey>
    private static HashMap<String, byte[]> listofPublicKeys = new HashMap<String, byte[]>();

    private static void getCryptoKeys() throws Exception {
        generateKeyPair = CryptographyUtil.generateKeyPair();
        publicKey = generateKeyPair.getPublic().getEncoded();
        privateKey = generateKeyPair.getPrivate().getEncoded();
    }

    private static void addPublicKey(String msg){
        //Only get the Public key
        msg = msg.replace("--SECURE--","");

        StringTokenizer st = new StringTokenizer(msg, "#");
        String recipient = st.nextToken();
        String MsgToSend = st.nextToken();

        //add the public key and by username
        listofPublicKeys.put(recipient, Base64.getDecoder().decode(MsgToSend));
    }

    private static String decryptMsg(String msg) throws Exception{
        String returnValue=msg;

        StringTokenizer st = new StringTokenizer(msg, ":");
        String recipient = st.nextToken().trim();
        String MsgToSend = st.nextToken().trim();
        byte[] encryptedData = Base64.getDecoder().decode(MsgToSend);

        try{
            //Decrypt the message
            byte[] decryptedData = CryptographyUtil.decrypt(privateKey, encryptedData);
            returnValue = "Secure - " + recipient + " : " + new String(decryptedData);
        }catch(Exception e){
            System.out.println("Error:  " + e.getMessage());
        }

        return returnValue;
    }


    private static String encryptMsg(String msg) throws Exception{

        byte[] PublicKey=null;
        String returnValue=msg;

        StringTokenizer st = new StringTokenizer(msg, "#");
        String recipient = st.nextToken();
        String MsgToSend = st.nextToken();

        //find and get public key
        PublicKey = listofPublicKeys.get(recipient);

        if(PublicKey != null) {
            byte[] encryptedData = CryptographyUtil.encrypt(PublicKey, MsgToSend.getBytes());
            returnValue = recipient + "#" + Base64.getEncoder().encodeToString(encryptedData);
        }

        return returnValue;
    }

    public static void main(String args[]) throws UnknownHostException, IOException, Exception
    {
        //Get the Private and publice keys that this user will use for the session
        getCryptoKeys();

        Scanner scn = new Scanner(System.in);
        // getting Host/IP
        InetAddress ip = InetAddress.getByName(HostName);

        // establish the connection
        Socket s = new Socket(ip, ServerPort);

        // obtaining input and out streams
        DataInputStream dis = new DataInputStream(s.getInputStream());
        DataOutputStream dos = new DataOutputStream(s.getOutputStream());

        //register the user and public key
        try {
            // When first creating the session, send the username and public key so the server can store them
            dos.writeUTF(UserName);
            dos.writeUTF(Base64.getEncoder().encodeToString(publicKey));
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // sendMessage thread
        Thread sendMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {
                while (true) {
                    // read the message to deliver.
                    String msg = scn.nextLine();

                    try {
                        //if the message does contains --SECURE-- this is a request to get the publice key.
                        // the message is sent as plain text so the server can handle the request to get the public key
                        //else this is a message that needs to be encrypted
                        if(msg.toUpperCase().contains("--SECURE--")){
                            //send plain request to get public key  in plain text
                            dos.writeUTF(msg);
                        }
                        else{
                            //encrypt message
                            msg  = encryptMsg(msg);
                            dos.writeUTF(msg);
                            System.out.println("Secure Message Sent: " + msg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // readMessage thread
        Thread readMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {

                while (true) {
                    try {
                        //Read the message sent to this client
                        //If message contains --SECURE-- then it is the public key that is being returned and needs
                        //to be stored for later purposes
                        //else it is the encrypted message that is being returned
                        String msg = dis.readUTF();
                        if(msg.contains("--SECURE--")){
                            System.out.println("Received Key now Secure Chat");
                            addPublicKey(msg);
                        }
                        else{
                            msg  = decryptMsg(msg);
                            System.out.println(msg);
                        }


                    } catch (Exception e) {
                        System.out.println("exception:  " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

        sendMessage.start();
        readMessage.start();

    }
}

