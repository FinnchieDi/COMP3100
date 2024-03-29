
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
    String serverType = "";
    String serverNum = "";

    int totalServers = 0;

    boolean isNotAvail = false;

    // Constructor for defining the Server's IP and Port Address
    public Stage2(String ip, int port) throws Exception {
        sct = new Socket(ip, port);
        outputStream = new DataOutputStream(sct.getOutputStream());
        inputStream = new BufferedReader(new InputStreamReader(sct.getInputStream()));
    }

    public static void main(String[] args) throws Exception {
        Stage2 client = new Stage2("192.168.0.123", 50000);
        // Running the server operation
        client.ClientConnect();

        // Closing the connection between client and server
        client.sct.close();
        client.inputStream.close();
        client.outputStream.close();
    }

    public void ClientConnect() throws Exception {
        // Sending initial message
        transmitMsg("HELO");
        // Server replies with "OK"
        this.inputStream.readLine();

        // transmitting authentication
        String username = System.getProperty("user.name");
        transmitMsg("AUTH " + username);
        // Server replies with "OK"
        this.inputStream.readLine();

        // Beginning system operations
        while (!lastMsg.equals("NONE")) {
            transmitMsg("REDY");
            // Server replies with "JOBN", "JCPL" or "NONE"
            redyString = this.inputStream.readLine();
            String redyPieces[] = redyString.split(" ");
            lastMsg = redyPieces[0];

            if (lastMsg.equals("JCPL")) {
                // Make sure there are no awaiting jobs in the queue after a server has been
                // opened up
                transmitMsg("LSTQ GQ #");
                String listTotalString = this.inputStream.readLine();
                String listTotalPieces[] = listTotalString.split("");
                int listTotal = Integer.parseInt(listTotalPieces[0]);

                if (listTotal == 0) {
                    isNotAvail = false; // There are servers waiting for new jobs, but no jobs in the queue
                    continue;
                } else {
                    transmitMsg("DEQJ GQ 0"); // Dequeue the job from the queue, FCFS, so the first leaves first
                    isNotAvail = false;
                    this.inputStream.readLine();
                    continue;
                }

            }
            if (lastMsg.equals("NONE")) {
                break; // Leave the loop
            }
            if (lastMsg.equals("CHKQ")) {
                continue; //Just checking the queue, continue through the loop
            }

            int jobNum = Integer.parseInt(redyPieces[2]); // Catch the jobNum in case there are servers with less
                                                          // resource requirements that can take a server, but are ahead
                                                          // of larger servers in the job list

            transmitMsg("GETS Avail " + redyPieces[4] + " " + redyPieces[5] + " " + redyPieces[6]);
            dataString = this.inputStream.readLine();
            String dataPieces[] = dataString.split(" ");
            totalServers = Integer.parseInt(dataPieces[1]);

            // Send OK to recieve the list of Servers
            transmitMsg("OK");
            // Capture the first line to check if there are servers are available
            String availString = this.inputStream.readLine();

            if (availString.equals(".")) {
                isNotAvail = true; // There are no servers available
                transmitMsg("OK");
                this.inputStream.readLine();
            } else {
                isNotAvail = false; // There must be servers available
            }

            // if there are not any available servers, continue through the code and assign
            // the job to the global queue

            if (isNotAvail) {
                transmitMsg("ENQJ GQ"); // enqueue the job into the global queue
                this.inputStream.readLine();
                transmitMsg("OK");
                for (int i = 0; i < 3; i++) {
                    this.inputStream.readLine();
                } // To ensure that i recieve the correct REDY message after the ENQJ, must have
                  // this in place
            } else {
                for (int i = 0; i < totalServers; i++) {
                    // Receive each record but only use the first recieved (FCFS)
                    if (i == 0) {
                        recordString = availString; // Make sure that the record string manages to grab the first server
                                                    // even after the check
                    } else {
                        recordString = this.inputStream.readLine();
                    }

                    String recordPieces[] = recordString.split(" ");
                    if (i == 0) {
                        serverNum = recordPieces[1];
                        serverType = recordPieces[0];
                    }

                }
                // Send the OK message
                transmitMsg("OK");
                // Get the "." in return
                this.inputStream.readLine();

                if (lastMsg.equals("JOBN") || lastMsg.equals("JOBP")) {
                    // Schedule a job for either a new or returning job
                    transmitMsg("SCHD " + jobNum + " " + serverType + " " + serverNum);
                    // Increment Job Number and Server ID
                    jobNum += 1;
                    // No need to increment the serverID
                    this.inputStream.readLine();
                }
            }
        }
        // Closing the connection
        transmitMsg("QUIT");
        this.inputStream.readLine();
    }

    public void transmitMsg(String message) throws Exception {
        this.outputStream.write((message + "\n").getBytes("UTF-8"));
    }
}