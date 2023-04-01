import java.net.*;
import java.io.*; 
public class ClientToServer {
    Socket sct;
    DataOutputStream outputStream;
    BufferedReader inputStream;

    String lastMsg = "";
    int jobNum = 0;
    String redyString;
    String[] redyPieces = null;
    String dataString;
    String[] dataPieces = null;
    String recordString;
    String[] recordPieces = null;
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
        //Server replies with "OK"
        System.out.println("Server responds: " + this.inputStream.readLine());
        
        //transmitting authentication
        String username = System.getProperty("user.name");
        transmitMsg("AUTH " + username);
        //Server replies with "OK"
        System.out.println("Server responds: " + this.inputStream.readLine());

        //Beginning system operations
        while (/*jobNum <= 10*/!lastMsg.equals("NONE") || !lastMsg.equals("ERR: ")){
            
            transmitMsg("REDY");
            //Server replies with "JOBN" or "NONE"
            redyString = this.inputStream.readLine();
            System.out.println("Server responds: " + redyString);
            String redyPieces[] = redyString.split(" ");
            lastMsg = redyPieces[0];
            System.out.println("lastMessage = " + lastMsg);

            if (jobNum == 0){
                
                System.out.println("Job Type: " + redyPieces[0] + " submitType: " + redyPieces[1] + 
                                " jobID: " + redyPieces[2] + " estRuntime: " + redyPieces[3] + 
                                " core: " + redyPieces[4] + " memory: " + redyPieces[5] + " disk: " + redyPieces[6]);

                transmitMsg("GETS All");
                dataString = this.inputStream.readLine();
                System.out.println("Server responds: " + dataString);
                String dataPieces[] = dataString.split(" ");
                System.out.println("DATA: " + dataPieces[0] + " nRecs: " + dataPieces[1] + " recLen: " + 
                                    dataPieces[2] + "\n");
                totalServers = Integer.parseInt("10");/*Integer.parseInt(dataPieces[1]);*/


                transmitMsg("OK");

                for (int i = 0; i < totalServers; i++){
                    //Receive each record
                    recordString = this.inputStream.readLine();
                    System.out.println("Server responds: " + recordString);
                    String recordPieces[] = recordString.split(" ");
                    // System.out.println("serverType: " + recordPieces[0] + " serverID: " + recordPieces[1] + 
                    //                     " serverStatus: " + recordPieces[2] + " currentStartTime: " + 
                    //                     recordPieces[3] + " core: " + recordPieces[4] + " memory: " + recordPieces[5] + 
                    //                     " disk: " + recordPieces[6] + " #wJobs: " + recordPieces[7] + " #rJobs: " + recordPieces[8]);
                    //Keep track of the largest server type and the number of servers of that type
                    largestServerType = recordPieces[4];

                }
                if (largestServerType.equals("16")){
                    largestServerType = "xlarge";
                }

                transmitMsg("OK");
            System.out.println("Server responds: " + this.inputStream.readLine());
            }
        
            if (redyPieces[0].equals("JOBN")){
                //Schedule a job
                transmitMsg("SCHD " + jobNum + " " + largestServerType + " " + serverNum);
                System.out.println("Scheduled Server: " + largestServerType + ", JobNo " + jobNum + ", ServerID " + serverNum);
                jobNum += 1;
                serverNum = (serverNum+1)%totalServers;
                System.out.println("Server responds: "+ this.inputStream.readLine());
            }
        }
        //Closing the connection
        transmitMsg("QUIT");
        System.out.println("Server responds: "+ this.inputStream.readLine());
    }

    public void transmitMsg(String message) throws Exception{
        this.outputStream.write( (message + "\n").getBytes("UTF-8"));
    }
}
