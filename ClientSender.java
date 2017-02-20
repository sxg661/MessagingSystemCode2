import java.io.*;


// Repeatedly reads recipient's nickname and text from the user in two
// separate lines, sending them to the server (read by ServerReceiver
// thread).

public class ClientSender extends Thread {

  private String nickname;
  private PrintStream server;
  private boolean quit;
  private Quit client;

  ClientSender(String nickname, PrintStream server, Quit q) {
    this.nickname = nickname;
    this.server = server;
    this.quit = false;
    //this is so client can see to check if it should keep taking commands
    this.client = q;

  }

  public void run() {
    // So that we can use the method readLine:
    BufferedReader user = new BufferedReader(new InputStreamReader(System.in));

    try {
      // then loop until recipient is called quit sending stuff to server
      while (!quit) {
    	  
    	  //reads in the command and does the right thing for each case
    	  String command = user.readLine();
    	  switch(command){
    	  
    	  case "message":
    		  doublePrompt(user, command, "Recipient: ", "Message: ");
    		  break;
    	  case "add user":
    		  doublePrompt(user, command, "Group Name: ", "Nickname: ");
    		  break;
    	  case "add admin":
    		  doublePrompt(user, command, "Group Name: ", "Nickname: ");
    		  break;
    	  case "group message":
    		  doublePrompt(user, command, "Group Name: ", "Message: ");
    		  break;
    	  case "remove user":
    		  doublePrompt(user, command, "Group Name: ", "NickName: ");
    		  break;
    	  case "create group":
    		  singlePrompt(user, command, "Group Name: ");
    		  break;
    	  case "leave group":
    		  singlePrompt(user, command, "Group Name: ");
    		  break;
    	  case "logout":
    		  logout();
    		  break;
    	  case "quit":
    		  logout();
          	  client.quit();
          	  break;
    	  default:
    		  System.out.println("Unknown command: " + command);
    	  }

      }
    }
    catch (IOException e) {
      Report.errorAndGiveUp("Communication broke in ClientSender" 
                        + e.getMessage());
    }
  }

  
  
private void logout() {
	  server.println("quit"); // Matches ZZZZZ in serverReciever.java
	  quit = true;
}


private void singlePrompt(BufferedReader user, String command, String prompt) throws IOException {
	System.out.print(prompt);
	String info = user.readLine();
	if(info != null){
		server.println(command);
		server.println(info);
	}
}


private void doublePrompt(BufferedReader user, String command, String prompt1, String prompt2) throws IOException {
	  System.out.print(prompt1);
	  String info1 = user.readLine();
	  System.out.print(prompt2);
	  String info2 = user.readLine();
	  if(info1 != null && info2 != null){
		  server.println(command); // Matches ZZZZZ in serverReciever.java
		  server.println(info1); // Matches CCCCC in ServerReceiver.java
		  server.println(info2);      // Matches DDDDD in ServerReceiver.java
	  }
	  else{
		  System.out.println("Message not sent: name or message not entered");
	  }
}
}
