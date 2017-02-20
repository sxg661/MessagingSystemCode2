

public class User {
	//Contains all the information about a registered user
	private boolean loggedIn;
	private String userName;
	private String passWord;
	private MessageQueue messageQueue;
	
	public User(String userName, String passWord){
		this.userName = userName;
		this.passWord = passWord;
		this.loggedIn = false;
		this.messageQueue = new MessageQueue();
	}
	
	public String getNickname(){
		return userName;
	}
	
	public MessageQueue getQueue(){
		return messageQueue;
	}
	
	public String getPassword(){
		return passWord;
	}
	
	public void logout(){
		loggedIn = false;
	}
	
	public void login(){
		loggedIn = true;
	}
	
	public boolean isLoggedIn(){
		return loggedIn;
	}
	

}
