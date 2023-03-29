import java.net.*;  
import java.io.*; 
public class ClientToServer {
    Socket sct;
    DataOutputStream outputStream;
    BufferedReader inputStream;

    String lastMsg = "";
    int jobNum = 0;


    //Constructor for defining the Server's IP and Port Address
    public ClientToServer(String ip, int port) throws Exception{
        sct = new Socket(ip, port);
        outputStream = new DataOutputStream(sct.getOutputStream());
        inputStream = new BufferedReader( new InputStreamReader(sct.getInputStream()));
    }

    public static void main(String[] args) throws Exception{
        ClientToServer client = new ClientToServer("192.168.0.122", 50000);
        //running the server operation
        client.ClientConnect();
        //closing the connection between client and server
        client.sct.close();
        client.inputStream.close();
        client.outputStream.close();
    }

    public void ClientConnect () throws Exception{
        //Sending initial message
        transmitMsg("HELO");
        System.out.println("Server responds: " + this.inputStream.readLine());
        
        //transmitting authentication
        String username = System.getProperty("user.name");
        transmitMsg("AUTH " + username);
        System.out.println("Server responds: " + this.inputStream.readLine());

        //Beginning system operations
        //while (lastMsg != "NONE"){
            if (jobNum == 0){
                transmitMsg("REDY");
                System.out.println("Server responds: " + this.inputStream.readLine());

                transmitMsg("GETS All");
                String dataString = this.inputStream.readLine();
                System.out.println("Server responds: " + this.inputStream.readLine());

                String dataPieces[] = dataString.split("@", 3);
                System.out.println("DATA " + dataPieces[0] + " nRecs " + dataPieces[1] + " recLen " + dataPieces[2] + "\n");
            }
        
        //}

        //Closing the connection
        transmitMsg("QUIT");
        System.out.println("Server says: "+ this.inputStream.readLine());
    }

    public void transmitMsg(String message) throws Exception{
        this.outputStream.write( (message + "\n").getBytes("UTF-8"));
    }
}
