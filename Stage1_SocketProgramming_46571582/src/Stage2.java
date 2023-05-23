
import java.net.*;
import java.io.*; 
public class Stage2 {
    Socket sct;
    DataOutputStream outputStream;
    BufferedReader inputStream;

    String lastMsg = "";
    String redyString;
    String[] redyPieces = null;
    String dataString;
    String[] dataPieces = null;
    String recordString;
    String[] recordPieces = null;
    String largestServerType = "";
    int jobNum = 0;
    int largestCores = 0;
    int serverNum = 0;
    int totalServers = 0;
    int largestServerTotal = 0;
    String methodCase = "Custom Algorithm";
    

    //Constructor for defining the Server's IP and Port Address
    public Stage2(String ip, int port) throws Exception{
        sct = new Socket(ip, port);
        outputStream = new DataOutputStream(sct.getOutputStream());
        inputStream = new BufferedReader( new InputStreamReader(sct.getInputStream()));
    }

    public static void main(String[] args) throws Exception{
        Stage2 client = new Stage2("192.168.0.123", 50000);
        //Running the server operation
        client.ClientConnect();
        
        //Closing the connection between client and server
        client.sct.close();
        client.inputStream.close();
        client.outputStream.close();
    }

    public void ClientConnect () throws Exception{
        //Sending initial message
        transmitMsg("HELO");
        //Server replies with "OK"
        this.inputStream.readLine();
        
        //transmitting authentication
        String username = System.getProperty("user.name");
        transmitMsg("AUTH " + username);
        //Server replies with "OK"
        this.inputStream.readLine();

        //Beginning system operations
        while (!lastMsg.equals("NONE")){
            transmitMsg("REDY");
            //Server replies with "JOBN", "JCPL" or "NONE"
            redyString = this.inputStream.readLine();
            String redyPieces[] = redyString.split(" ");
            lastMsg = redyPieces[0];

            if (lastMsg.equals("JCPL")){
                //Need to say REDY again, skip through the rest of the loop
                continue;
            }
            if (lastMsg.equals("NONE")){
                break;
            }

            if (methodCase.equals("Largest Round Robin")){
                largestRoundRobin();
            }
            else if (methodCase.equals("First Capable")){
                firstCapable(redyPieces);
            }
            else if (methodCase.equals("Custom Algorithm")){
                customAlgorthim(redyPieces);
            }

        }
        //Closing the connection
        transmitMsg("QUIT");
        this.inputStream.readLine();
    }

    public void largestRoundRobin() throws Exception{
        if (jobNum == 0){

            //Recieve information regarding the server list
            transmitMsg("GETS All");
            dataString = this.inputStream.readLine();
            String dataPieces[] = dataString.split(" ");
            totalServers = Integer.parseInt(dataPieces[1]);

            //Send OK to recieve the list of Servers
            transmitMsg("OK");

            for (int i = 0; i < totalServers; i++){
                //Receive each record
                recordString = this.inputStream.readLine();
                String recordPieces[] = recordString.split(" ");

                //Keep track of the largest server type and the number of servers of that type
                int currCores = Integer.parseInt(recordPieces[4]);
                String currLargestST = recordPieces[0];

                //Finding the largest server type based on cores
                if (largestCores != currCores){
                    if (largestCores < currCores){
                        //If the cores are larger than the already defined larger, 
                        //switch them around and reset the count of total servers in
                        //the largest server type.
                        largestCores = currCores;
                        largestServerType = currLargestST;
                        largestServerTotal = 1;
                    }
                }else if (largestServerType.equals(currLargestST)){
                    //Finding the total servers of the Largest Server Type
                    largestServerTotal ++;
                }
            }
            //Send the OK message
            transmitMsg("OK");
            //Get the "." in return
            this.inputStream.readLine();
        }
    
        if (lastMsg.equals("JOBN")){
            //Schedule a job
            transmitMsg("SCHD " + jobNum + " " + largestServerType + " " + serverNum);
            //Increment Job Number and Server ID
            jobNum += 1;
            //When incrementing serverNum, be sure to wrap the count back to "0" when you reach the last server
            serverNum = (serverNum+1)%largestServerTotal;
            this.inputStream.readLine();
        }
    }

    public void firstCapable(String redyPieces[]) throws Exception{
        //Recieve information regarding the server list
        transmitMsg("GETS Capable " + redyPieces[4] + " " + redyPieces[5] + " " + redyPieces[6]);
        dataString = this.inputStream.readLine();
        String dataPieces[] = dataString.split(" ");
        totalServers = Integer.parseInt(dataPieces[1]);

        //Send OK to recieve the list of Servers
        transmitMsg("OK");

        for (int i = 0; i < totalServers; i++){
            //Receive each record
            recordString = this.inputStream.readLine();
            String recordPieces[] = recordString.split(" ");

            if (i == 0){
                largestServerType = recordPieces[0];
                largestServerTotal = 0;
            }
        }
        //Send the OK message
        transmitMsg("OK");
        //Get the "." in return
        this.inputStream.readLine();
    
        if (lastMsg.equals("JOBN")){
            //Schedule a job
            transmitMsg("SCHD " + jobNum + " " + largestServerType + " " + serverNum);
            //Increment Job Number and Server ID
            jobNum += 1;
            //No need to increment the serverID
            this.inputStream.readLine();
        }
    }

    public void customAlgorthim(String redyPieces[]) throws Exception{
        transmitMsg("GETS Capable " + redyPieces[4] + " " + redyPieces[5] + " " + redyPieces[6]);
        dataString = this.inputStream.readLine();
        String dataPieces[] = dataString.split(" ");
        totalServers = Integer.parseInt(dataPieces[1]);

        //Send OK to recieve the list of Servers
        transmitMsg("OK");

        for (int i = 0; i < totalServers; i++){
            //Receive each record
            recordString = this.inputStream.readLine();
            String recordPieces[] = recordString.split(" ");

            if (i == 0){
                largestServerType = recordPieces[0];
                largestServerTotal = 0;
            }
        }
        //Send the OK message
        transmitMsg("OK");
        //Get the "." in return
        this.inputStream.readLine();
    
        if (lastMsg.equals("JOBN")){
            //Schedule a job
            transmitMsg("SCHD " + jobNum + " " + largestServerType + " " + serverNum);
            //Increment Job Number and Server ID
            jobNum += 1;
            //No need to increment the serverID
            this.inputStream.readLine();
        }
    }

    public void transmitMsg(String message) throws Exception{
        this.outputStream.write( (message + "\n").getBytes("UTF-8"));
    }
}

