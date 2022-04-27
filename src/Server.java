// Java implementation of  Server side
// It contains two classes : Server and ClientHandler
// Save file as Server.java

import java.io.*;
import java.util.*;
import java.net.*;

// Server class
public class Server
{

    // Vector to store active clients
    static Vector<ClientHandler> ar = new Vector<>();
    final static int ServerPort = 1234;

    public static void main(String[] args) throws IOException
    {
        // server is listening port
        ServerSocket ss = new ServerSocket(ServerPort);

        Socket s;

        // running infinite loop for getting
        // client request
        while (true)
        {
            // Accept the incoming request
            s = ss.accept();

            System.out.println("New client request received : " + s);

            // obtain input and output streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            System.out.println("Creating a new handler for this client...");

            // Create a new handler object for handling this request.
            ClientHandler mtch = new ClientHandler(s, dis, dos);

            // Create a new Thread with this object.
            Thread t = new Thread(mtch);

            System.out.println("Adding this client to active client list");

            // add this client to active clients list
            ar.add(mtch);

            // start the thread.
            t.start();

        }
    }
}

// ClientHandler class
class ClientHandler implements Runnable
{
    Scanner scn = new Scanner(System.in);
    private String name;
    private String publicKey;
    final DataInputStream dis;
    final DataOutputStream dos;
    Socket s;
    boolean isloggedin;

    // constructor
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.s = s;
        this.isloggedin=true;

        try {
            String str = "";
            //get username and publicKey
            this.name = dis.readUTF();
            this.publicKey = dis.readUTF();
            System.out.println(this.name + ": Now connected");
            System.out.println("Key is:  " + this.publicKey);

        }catch (Exception e){
            System.out.println("Error:  " + e.getMessage());
        }
    }

    @Override
    public void run() {

        String received;
        while (true)
        {
            try
            {
                // receive the string
                received = dis.readUTF();

                System.out.println(received);

                if(received.equals("logout")){
                    this.isloggedin=false;
                    this.s.close();
                    break;
                }

                // break the string into message and recipient part
                StringTokenizer st = new StringTokenizer(received, "#");
                String recipient = st.nextToken();
                String MsgToSend = st.nextToken();

                System.out.println("server side Message:  " + received);

                // search for the recipient in the connected devices list.
                // ar is the vector storing client of active users
                for (ClientHandler mc : Server.ar)
                {
                    // if the recipient is found, write on its
                    // output stream
                    if (mc.name.equals(recipient) && mc.isloggedin==true)
                    {
                        if(MsgToSend.toUpperCase().equals("--SECURE--")) {
                            System.out.println(mc.name + "#--SECURE--" + mc.publicKey);
                            dos.writeUTF(mc.name + "#--SECURE--" + mc.publicKey);
                            System.out.println("Key:  " + MsgToSend);
                        }
                        else {
                            mc.dos.writeUTF(this.name + " : " + MsgToSend);
                            System.out.println("messgage:  " + MsgToSend);
                        }
                        break;
                    }
                }
            } catch (IOException e) {

                e.printStackTrace();
            }

        }
        try
        {
            // closing resources
            this.dis.close();
            this.dos.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}