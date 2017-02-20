import java.net.*;
import java.io.*;

// Continuously reads from message queue for a particular client,
// forwarding to the client.

public class ServerSender extends Thread {
  private MessageQueue clientQueue;
  private String myClientName;
  private PrintStream client;
  private boolean quit;
  private UserTable userTable;

  public ServerSender( PrintStream c, String nickname, UserTable ut) { 
    client = c;
    myClientName = nickname;
    quit = false;
    userTable = ut;
    clientQueue = ut.getUserQueue(nickname);
  }

  public void run() {
    while (!quit) {
      Message msg = clientQueue.take(); // Matches EEEEE in ServerReceiver
      userTable.writeAllToFile();
      //checks to see if it is a quit message to close the thread
      if(msg.isQuitMessage()){
    	  //reports on server that the client had disconnected
    	  Report.behaviour(myClientName + " disconnected");
    	  quit = true;
      }
      client.println(msg); // Matches FFFFF in ClientReceiver
    }
  //logout the user
  userTable.logoutUser(myClientName);
  //close the PrintStream
  client.close();
  }
}
