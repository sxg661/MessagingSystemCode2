// Usage:
//        java Client user-nickname server-hostname
//
// After initializing and opening appropriate sockets, we start two
// client threads, one to send messages, and another one to get
// messages.
//
// A limitation of our implementation is that there is no provision
// for a client to end after we start it. However, we implemented
// things so that pressing ctrl-c will cause the client to end
// gracefully without causing the server to fail.
//
// Another limitation is that there is no provision to terminate when
// the server dies.


import java.io.*;
import java.net.*;

class Client {

  public static void main(String[] args) {
    Quit quit = new Quit();
	while(!quit.userHasQuit()){
		// Check correct usage:
	    if (args.length != 1) {
	      Report.errorAndGiveUp("Usage: java Client user-nickname server-hostname");
	    }

	    // Initialize information:
	    String hostname = args[0];
	    

	    // Open sockets:
	    PrintStream toServer = null;
	    BufferedReader fromServer = null;
	    Socket server = null;
	    //Creates a buffer reader with the user
	    BufferedReader userLogin = null;

	    try {
	      server = new Socket(hostname, Port.number); // Matches AAAAA in Server.java
	      toServer = new PrintStream(server.getOutputStream());
	      fromServer = new BufferedReader(new InputStreamReader(server.getInputStream()));
	      userLogin = new BufferedReader(new InputStreamReader(System.in)); 
	    } 
	    catch (UnknownHostException e) {
	      Report.errorAndGiveUp("Unknown host: " + hostname);
	    }   
	    catch (IOException e) {
	      Report.errorAndGiveUp("The server doesn't seem to be running " + e.getMessage());
	    }
	    
	    String loginType = readLoginType(toServer, userLogin);
	 
	    
	    //Deals with the registration
	    String nickname = "";
	    boolean newUser = true;
	    if(loginType.equals("login"))
	    	newUser = false;
	    nickname = login(toServer, fromServer, userLogin, newUser);
	    
	    
	    // Create two client threads of a diferent nature:
	    ClientSender sender = new ClientSender(nickname,toServer,quit);
	    ClientReceiver receiver = new ClientReceiver(fromServer);

	    sender.start();
	    receiver.start();
	        
	   // Wait for them to end and close sockets.
	   try {
	      sender.join();
	      toServer.close();
	      receiver.join();
	      fromServer.close();
	      server.close();
	    }
	   catch (IOException e) {
		   Report.errorAndGiveUp("Something wrong " + e.getMessage());
	    }
	    catch (InterruptedException e) {
	      Report.errorAndGiveUp("Unexpected interruption " + e.getMessage());
	    }
	   }   
	}





private static String readLoginType(PrintStream toServer, BufferedReader userLogin) {
	//Prompts the user for whether they would like to login or register
	String loginType = "";
	try{
	    System.out.print("Would you like to login or register?: ");
	    loginType = userLogin.readLine();
	    while(!loginType.equals("login") && !loginType.equals("register")){
	    	 System.out.print("Please type 'login' or 'register'");
	    	 loginType = userLogin.readLine();
	    }
	    //Tells the server whether or not they are loggin in
	    toServer.println(loginType);//Matches TTTTTT in Server.java 
	}
	catch(IOException e){
		Report.errorAndGiveUp("Something wrong " + e.getMessage());
	}
	return loginType;
}

   
 
 
  
  //LOGIN
  public static String login(PrintStream toServer, BufferedReader fromServer, BufferedReader userLogin, boolean newUser){
	  
	//attempts to log in the user
	 String nickName = "";
	 try {
		nickName = readNickname(toServer, fromServer, userLogin, newUser);
		
		String passWord = readPassword(toServer, fromServer, userLogin);
		
		confirmPassword(toServer, userLogin, newUser, passWord);
	
	}
	catch (IOException z) {
		Report.errorAndGiveUp("Something wrong " + z.getMessage());
	}
	return nickName;  
  }





private static void confirmPassword(PrintStream toServer, BufferedReader userLogin, boolean newUser, String passWord)
		throws IOException {
	//confirms the new password
	if (newUser) {
		System.out.print("Confirm Password: ");
		String retypedPassWord = userLogin.readLine();
		if(!passWord.equals(retypedPassWord)){
			//tells the server that it didn't work
			toServer.println("no"); //Matches CCCPPP in server.java
			Report.errorAndGiveUp("Registration failed: doesn't match password");
		}
		//tells the server that it did work
		toServer.println("yes"); //Matches CCCPPP in server.java
	}
}





private static String readPassword(PrintStream toServer, BufferedReader fromServer, BufferedReader userLogin)
		throws IOException {
	//gets the password from the user
	String passWord = "";
	System.out.print("Password: ");
	passWord = userLogin.readLine();
	
	//checks with server to see that this is a valid password
	toServer.println(passWord); //Matches LLLLLPPPPP in Server.java
	String correct = fromServer.readLine(); //Matches LLLVVVPPP in server.java
	if(correct.equals("no")){
		Report.errorAndGiveUp("Login failed: incorrect password");
	}
	return passWord;
}





private static String readNickname(PrintStream toServer, BufferedReader fromServer, BufferedReader userLogin,
		boolean newUser) throws IOException {
	String nickName;
	//gets the username from the user
	System.out.print("Username: ");
	nickName = userLogin.readLine();
	
	//checks with the server to see that this is a valid username
	toServer.println(nickName); //Matches LLLLLNNNNN in server.java
	String valid = fromServer.readLine(); //Matches LLLVVVNNN in server.javaZ
	if(valid.equals("no"))
		if (newUser){
			Report.errorAndGiveUp("Registration failed: username already in use");
		}
		else Report.errorAndGiveUp("Login failed: invalid username");
	else if(valid.equals("loggedIn"))
		Report.errorAndGiveUp("Login failed: user is already logged in");
	return nickName;
}
  
}
