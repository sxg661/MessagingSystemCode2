import java.net.*;
import java.io.*;

// Gets messages from client and puts them in a queue, for another
// thread to forward to the appropriate client.

public class ServerReceiver extends Thread {
  private String myClientsName;
  private BufferedReader myClient;
  private UserTable userTable;
  private GroupTable groupTable;
  private boolean quit;
  

  public ServerReceiver(String n, BufferedReader c, UserTable t, GroupTable g) {
    myClientsName = n;
    myClient = c;
    userTable = t;
    groupTable = g;
    quit = false;
  }

  public void run() {
    try {
      while (!quit) {
    	String command = myClient.readLine(); //Matches ZZZZZ in clientSender.java
    	
    	switch (command){
    	
    	case "message":
    		sendMessage();
    		break;
    	case "add user":
    		addUser(false);
    		break;
    	case "add admin":
    		addUser(true);
    		break;
    	case "remove user":
    		removeUser();
    		break;
    	case "group message":
    		sendToGroup();
    		break;
    	case "create group":
    		makeGroup();
    		break;
    	case "leave group":
    		leaveGroup();
    		break;
    	case "quit":
    		quit();
    		break;
    	default:
    		Report.error("Unexistant command: " + command);
    	}
    	
      // Closes the buffer reader
      }
      myClient.close();
      }
    catch (IOException e) {
      Report.error("Something went wrong with the client " 
                   + myClientsName + " " + e.getMessage()); 
      // No point in trying to close sockets. Just give up.
      // We end this thread (we don't do System.exit(1)).
    }
    
  }

private void makeGroup() throws IOException {
	String groupName = myClient.readLine();
	groupTable.addGroup(groupName, myClientsName);
}

private void quit() {
	Message msg2 = new Message("","",true);
	this.quit = true;
	MessageQueue sendersQueue = userTable.getUserQueue(myClientsName); // Matches EEEER in ServerSenser.java
	sendersQueue.offer(msg2);
}

private void sendToGroup() throws IOException{
	String groupName = myClient.readLine(); // Matches CCCCC in ClientSender.java
	String text = myClient.readLine();      // Matches DDDDD in ClientSender.java
	boolean sent = groupTable.sendToAll(groupName, text, userTable, myClientsName);
	System.out.println(sent);
}

private void sendMessage() throws IOException {
	String recipient = myClient.readLine(); // Matches CCCCC in ClientSender.java
	String text = myClient.readLine();      // Matches DDDDD in ClientSender.java
	if(text != null && recipient != null){
		Message msg = new Message(myClientsName, text, false);
	    MessageQueue recipientsQueue = userTable.getUserQueue(recipient); // Matches EEEER in ServerSenser.java
	    if (recipientsQueue != null){
	      recipientsQueue.offer(msg);
	      userTable.writeAllToFile();
	    }
	    else
	      return;
	}
	else return;
}

private void addUser(boolean admin) throws IOException{
	String groupName = myClient.readLine();
	String nickName = myClient.readLine();
	boolean added = admin ? groupTable.addAdmin(groupName, nickName, myClientsName) :
		groupTable.addUser(groupName, nickName, myClientsName);
}

private void removeUser() throws IOException{
	String groupName = myClient.readLine();
	String nickName = myClient.readLine();
	System.out.println(groupTable.removeUser(myClientsName, nickName, groupName));
}

private void leaveGroup() throws IOException{
	String groupName = myClient.readLine();
	groupTable.leaveGroup(groupName, myClientsName);
}
}

