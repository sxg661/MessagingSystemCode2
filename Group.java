import java.util.LinkedList;

public class Group {
	 private String groupName; 
	 private LinkedList<String> admins;
	 private LinkedList<String> members;
	 
	 public Group(String groupName, String adminUser){
		 this.groupName = groupName;
		 this.admins = new LinkedList<String>();
		 this.admins.add(adminUser);
		 this.members = new LinkedList<String>();
	 }
	 
	 public Group(String groupName){
		 this.groupName = groupName;
		 this.admins = new LinkedList<String>();
		 this.members = new LinkedList<String>();
	 }
	 
	 public String getName(){
		 return groupName;
	 }
	 
	 public boolean isAdmin(String nickname){
		 return admins.contains(nickname);
	 }
	 
	 public void addUser(String nickName){
		 members.add(nickName);
	 }
	 
	 public void addAdmin(String nickName){
		 admins.add(nickName);
	 }
	 
	 public boolean isInGroup(String nickName){
		return members.contains(nickName) || admins.contains(nickName);
	 }
	 
	 public LinkedList<String> getMembers(){
		 return members;
	 }
	 
	 public LinkedList<String> getAdmins(){
		 return admins;
	 }
	 
	 public void removeUser(String nickName){
		 members.remove(nickName);
	 }
	 
	 public void removeAdmin(String nickName){
		 admins.remove(nickName);
	 }
	 
	 public void sendToAll(String text, UserTable userTable, String sender){
		 //at the moment it send to the person who sent the message too,
		 //but I may change this
		 
		 //makes the message to be sent
		 Message message = new Message(sender, text, false);
		 
		 //sends to admins
		 for(String admin: admins){
			 System.out.println("sending to " + admin);
			 userTable.getUserQueue(admin).offer(message);
		 }
		 
		 
		 //then it's sent to all the members
		 for(String member : members){		 
			System.out.println("sending to " + member);
		    userTable.getUserQueue(member).offer(message); 
		 }
	
	 }
	 
	 
}
