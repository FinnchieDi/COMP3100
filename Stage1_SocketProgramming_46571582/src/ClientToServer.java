import java.net.*;

import javax.swing.ToolTipManager;

import java.io.*; 
public class ClientToServer {
    Socket sct;
    DataOutputStream outputStream;
    BufferedReader inputStream;

    String lastMsg = "";
    int jobNum = 0;
    String redyString;
    String[] redyPieces = new String[7];
    String dataString;
    String[] dataPieces = new String[3];
    String largestServerType = "";
    int serverNum = 0;
    int totalServers = 0;


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
        while (lastMsg != "NONE"){
            
            transmitMsg("REDY");
            System.out.println("Server responds: " + this.inputStream.readLine());
            redyString = this.inputStream.readLine();
            String redyPieces[] = redyString.split("@", 7);

            if (jobNum == 0){
                transmitMsg("GETS All");
                System.out.println("Server responds: " + this.inputStream.readLine());
                dataString = this.inputStream.readLine();
                String dataPieces[] = dataString.split("@", 3);
                System.out.println("DATA " + dataPieces[0] + " nRecs " + dataPieces[1] + " recLen " + dataPieces[2] + "\n");
                totalServers = Integer.parseInt(dataPieces[1]);


                transmitMsg("OK");
                for (int i = 0; i < totalServers; i++){
                    //Receive each record
                    //Keep track of the largest server type and the number of servers of that type
                    System.out.println("Server responds: " + this.inputStream.readLine());
                }
            }

            transmitMsg("OK");
            System.out.println("Server responds: " + this.inputStream.readLine());

            if (redyPieces[0] == "JOBN"){
                //Schedule a job
                transmitMsg("SCHD " + jobNum + " " + largestServerType + " " + serverNum);
                jobNum += 1;
                serverNum = (serverNum+1)%totalServers;
            }
        
        }
        //Closing the connection
        System.out.println("Server says: "+ this.inputStream.readLine());
        transmitMsg("QUIT");
        System.out.println("Server says: "+ this.inputStream.readLine());
    }

    public void transmitMsg(String message) throws Exception{
        this.outputStream.write( (message + "\n").getBytes("UTF-8"));
    }
}
