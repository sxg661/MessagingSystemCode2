import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class GroupTable {
	//this class stores all the groups. It also has a pointer to the userTable
	//so it can access the message queues.
	
	private ConcurrentMap<String, Group> groupTable;
	private UserTable userTable;
	private String clientFile;
	
	public GroupTable(UserTable userTable, String clientFile){
		this.groupTable = new ConcurrentHashMap<String, Group>();
		this.userTable = userTable;
		this.clientFile = clientFile;
		//reads in all the groups that are already created from the file
		readAllFromFile();
	}

	
	public boolean addGroup(String groupName, String adminUser){
		if(groupTable.containsKey(groupName))
			return false;
		
		Group newGroup = new Group(groupName, adminUser);
		groupTable.put(groupName, newGroup);
		
		//updates the file
		writeAllToFile();
		return true;
	}
	
	public boolean addUser(String groupName, String nickName, String user){
		if(groupTable.containsKey(groupName) && userTable.exists(nickName)){
			Group group = groupTable.get(groupName);
			if(!group.isInGroup(nickName) && (group.isAdmin(user))){
				group.addUser(nickName);
				//updates the file
				writeAllToFile();
				return true;
			}
		}
		
		return false;
	}
	
	public boolean addAdmin(String groupName, String nickName, String user){
		if(groupTable.containsKey(groupName)){
			Group group = groupTable.get(groupName);
			if(!group.isAdmin(nickName) && group.isAdmin(user)){
				group.addAdmin(nickName);
				if(group.isInGroup(nickName))
					group.removeUser(nickName);
				//updates the file
				writeAllToFile();
				return true;
			}
			
			
				
		}
		
		return false;
	}
	
	public String removeUser(String user, String userToRemove, String groupName){
		//returns a specific message which will be sent back to the user doing the deleteing
		if(!groupTable.containsKey(groupName))
			return("No group with name: " + groupName);
		
		if(!groupTable.get(groupName).isAdmin(user) || groupTable.get(groupName).isAdmin(userToRemove))
			return("You do not have permission to do this");
		
		if(!groupTable.get(groupName).isInGroup(userToRemove))
			return("Group doesn't contain user: " + userToRemove);
		
		groupTable.get(groupName).removeUser(userToRemove);
		
		//updates the file
		writeAllToFile();
		
		return("success");
	}
	
	
	public boolean leaveGroup(String groupName, String nickName){
		if(groupTable.containsKey(groupName)){
			Group group = groupTable.get(groupName);
			if(group.isInGroup(nickName)){
				//only removes admin if not only admin
				//a group without admins wouldn't work
				if(group.isAdmin(nickName) &&  group.getAdmins().size() > 1){
					group.removeAdmin(nickName);
					writeAllToFile();
					return true;
				}
				else if (!group.isAdmin(nickName)){
					group.removeUser(nickName);
					writeAllToFile();
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean sendToAll(String groupName, String text, UserTable userTable, String sender){
		if(!groupTable.containsKey(groupName))
			return false;
		
		if(!groupTable.get(groupName).isInGroup(sender))
			return false;
		
		groupTable.get(groupName).sendToAll(text, userTable, groupName + ": " + sender);
		return true;
	}
	
	
	private void readAllFromFile(){
		   try {
			   //gets the xml file and gets the root element
			   File xmlFile = new File(clientFile);
			   Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
			   doc.getDocumentElement().normalize();
			   
			   //loops through group elements
			   NodeList groups = doc.getElementsByTagName("group");
			   for(int i = 0; i < groups.getLength(); i++){
				   Node gNode = groups.item(i);
				   Element gElement = (Element) gNode;
				   Group group = new Group(
						   gElement.getAttribute("name"));
				   
				   //loops through the admins
				   NodeList aNodes = gElement.getElementsByTagName("admin");
				   for(int j = 0; j < aNodes.getLength(); j++){
					   Node aNode = aNodes.item(j);
					   Element aElement = (Element) aNode;
					   group.addAdmin(aNode.getTextContent());
				   }
				   
				   
				   //loops through the members
				   NodeList mNodes = gElement.getElementsByTagName("member");
				   for(int k = 0; k < mNodes.getLength(); k++){
					   Node mNode = mNodes.item(k);
					   Element mElement = (Element) mNode;
					   group.addUser(mNode.getTextContent());
				   }
				   
				   
				   
				   groupTable.put(group.getName(), group);
				   
			   }
			  
		   }  
		   catch(FileNotFoundException e){
			   System.out.println("Server start failed; client file not found: " + clientFile);
			   //e.printStackTrace();
		   }
		   catch(IOException e){
			   System.out.println("Something went wrong when reading client file");
			   e.printStackTrace();
		   }
		   catch (Exception e) {
			    System.out.println("Something went wrong when reading client file");
			    e.printStackTrace();
		   } 
	}
	
	private synchronized void writeAllToFile(){
		try{
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			
			//creates root element
			Element rootElement = doc.createElement("groupInfo");
			
			//creates group elements
			Iterator it = groupTable.entrySet().iterator();
			while(it.hasNext()){
				
				//gets the group
				ConcurrentMap.Entry groupEntry = (ConcurrentMap.Entry)it.next();
				Group group = ((Group) groupEntry.getValue());
				
				
				Element gElement = doc.createElement("group");
				
				gElement.setAttribute("name", group.getName());
				//creates admins
				for(String admin: group.getAdmins()){
					Element aElement = doc.createElement("admin");
					aElement.appendChild(doc.createTextNode(admin));
					gElement.appendChild(aElement);
				}
				
				//creates members 
				for(String member: group.getMembers()){
					Element mElement = doc.createElement("member");
					mElement.appendChild(doc.createTextNode(member));
					gElement.appendChild(mElement);
				}
				
				rootElement.appendChild(gElement);
				
			}
			
			//adds the root element to the doc
			doc.appendChild(rootElement);
			
			//writes results to file
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(clientFile));
			transformer.transform(source, result);
			
		}
		catch(Exception e){
			System.out.println("Something went wrong when reading client file");
			e.printStackTrace();
		}
		
	}
	
	
}
