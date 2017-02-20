// Usage:
//        java Server
//
// There is no provision for ending the server gracefully.  It will
// end if (and only if) something exceptional happens.


import java.net.*;
import java.io.*;

public class Server {

  public static void main(String [] args) {
	
    // These tables will be shared by the server threads:
    //ClientTable clientTable = new ClientTable();
    UserTable userTable = new UserTable("userFile.xml");
    GroupTable groupTable = new GroupTable(userTable, "groupFile.xml");
    
    ServerSocket serverSocket = null;
    
    try {
      serverSocket = new ServerSocket(Port.number);
    } 
    catch (IOException e) {
      Report.errorAndGiveUp("Couldn't listen on port " + Port.number);
    }
    
    try { 
      // We loop for ever, as servers usually do.
      while (true) {
        // Listen to the socket, accepting connections from new clients:
        Socket socket = serverSocket.accept(); // Matches AAAAA in Client.java
	
        // This is so that we can use readLine():
        BufferedReader fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        // This is used to communicate with the client using println().
        PrintStream toClient = new PrintStream(socket.getOutputStream());

        // We ask the client what the login type is:
        String loginType = fromClient.readLine(); // Matches TTTTTT in Client.java
        
        
        //checks login type and does the right thing:
        LoginInfo loginInfo = new LoginInfo();
        if(loginType.equals("login")){
        	//LOGGING IN
        	//----------
        	loginUser(userTable, fromClient, toClient, loginInfo);
        }
        else{
        	registerUser(userTable, fromClient, toClient, loginInfo);
        }
        
        //if it is a valid login it says the client is connected and creates the threads
        if(loginInfo.isValid()){
        	 Report.behaviour(loginInfo.getNickname() + " connected");
             
             // We log the client in
             userTable.loginUser(loginInfo.getNickname());
             // We create and start a new thread to read from the client:
             (new ServerReceiver(loginInfo.getNickname(), fromClient, userTable, groupTable)).start();

             // We create and start a new thread to write to the client:
             (new ServerSender(toClient, loginInfo.getNickname(), userTable)).start();
        }

      }
    } 
    catch (IOException e) {
      // Lazy approach:
      Report.error("IO error " + e.getMessage());
      // A more sophisticated approach could try to establish a new
      // connection. But this is beyond this simple exercise.
    }
  }



private static void registerUser(UserTable userTable, BufferedReader fromClient, PrintStream toClient,
		LoginInfo loginInfo) throws IOException {
	//REGISTERING
	//-----------
	//Reads in the username from the user
	loginInfo.setNickname(fromClient.readLine()); //Matches LLLLLNNNNN in login() in Client.java
	
	//Sends back a response saying whether or not this is a valid login
	if(!userTable.exists(loginInfo.getNickname()))
		toClient.println("yes"); //Matches LLLVVVNNN in login() in Client.java
	else{
		toClient.println("no"); //Matches LLLVVVNNN in login() in Client.java
		loginInfo.makeInvalid();
	}
	
	if(loginInfo.isValid()){
		String pass = fromClient.readLine(); //Matches LLLLLPPPPP in login() in Client.java
		toClient.println("yes"); // Matches LLLVVVPPP in login() in Client.java
		//reads in whether the password confirmation was successful
		String confirmed = fromClient.readLine();//Matches CCCPPP in login() in Client.java
		if(confirmed.equals("no"))
			loginInfo.makeInvalid();
		//adds the user to the table if all this is valid
		if(loginInfo.isValid())
			userTable.addUser(loginInfo.getNickname(), pass);
	}
}

  
  
private static void loginUser(UserTable userTable, BufferedReader fromClient, PrintStream toClient,
		LoginInfo loginInfo) throws IOException {
	//reads in the nickname from client
	loginInfo.setNickname(fromClient.readLine()); //Matches LLLLLNNNNN in login() in Client.java
	
	//Checks they exists and if not logged in
	String response = "";
	if(userTable.exists(loginInfo.getNickname())){
		if(userTable.isLoggedin(loginInfo.getNickname())){
			response = "loggedIn";
			loginInfo.makeInvalid();
		}
		else response = "yes";
	}
	else {
		response = "no";
		loginInfo.makeInvalid();
	}
	//sends response to client
	toClient.println(response); //Matches LLLVVVNNN in login() in Client.java
	
	if(loginInfo.isValid()){
		String pass = fromClient.readLine(); //Matches LLLLLPPPPP in login() in Client.java
		//checks if password is correct and sends response to client
	    if(userTable.checkPass(loginInfo.getNickname(), pass))
	    	toClient.println("yes"); // Matches LLLVVVPPP in login() in Client.java
		else {
			toClient.println("no"); // Matches LLLVVVPPP in login() in Client.java
		    loginInfo.makeInvalid();
		}
		
	}
}
}
