

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class UserTable {
	
	//this table will store all the users who currently exists within the system
	//it also handles writing to and from files
	private ConcurrentMap<String,User> userTable;
	private String userFile; 
	
	//CONSTRUCTOR
	public UserTable(String ufl){
		this.userTable = new ConcurrentHashMap<String,User>();
		this.userFile = ufl;
		readAllFromFile();
	}
	
	public void addUser(String nickname, String password){
		//adds the user to the table
		userTable.put(nickname, new User(nickname, password));
		//updates the file containing users and passwords
		writeAllToFile();
	}
	
	public boolean exists(String nickname){
		//checks user exists in system
		return userTable.containsKey(nickname);
	}
	
	public boolean isLoggedin(String nickname){
		//checks if an existing user is logged in
		return userTable.get(nickname).isLoggedIn();
	}
	
	public void loginUser(String nickname){
		//logs in an existing user
		userTable.get(nickname).login();
	}
	
	public void logoutUser(String nickname){
		//logs out an existing user
		userTable.get(nickname).logout();
	}
	
	public MessageQueue getUserQueue(String nickname){
		//gets the message queue for a specific user
		return userTable.get(nickname).getQueue();
	}
	
	
	public boolean checkPass(String nickname, String password){
		//checks to see if a password entered is correct
		String realPass = userTable.get(nickname).getPassword();
		return (password.equals(realPass));
	}
	
	//FOR TESTING REMOVE BEFORE SUBMITTING
	public void displayUsers(){
		Iterator<?> it = userTable.entrySet().iterator();
		while(it.hasNext()){
			ConcurrentMap.Entry e = (ConcurrentMap.Entry)it.next();
			System.out.println(e.getKey() + " -> " + ((User) e.getValue()).getPassword());
		}
	}
	
	private void readAllFromFile(){
		try {
			//gets the xml file and gets the root element
			File xmlFile = new File(userFile);
			Document doc;
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
			doc.getDocumentElement().normalize();
			
			//loops through group elements
			NodeList uElements = doc.getElementsByTagName("user");
			for(int i = 0; i < uElements.getLength(); i++){
				Node uNode = uElements.item(i);
				Element uElement = (Element) uNode;
				String nickname = uElement.getAttribute("name");
				String password = uElement.getAttribute("password");
				User user = new User(nickname, password);
				
				//loops through the messages 
				NodeList mElements = uElement.getElementsByTagName("message");
				for(int j = 0; j < mElements.getLength(); j++){
					Node mNode = mElements.item(j);
					Element mElement = (Element) mNode;
					String sender = mElement.getAttribute("sender");
					String text = mElement.getTextContent();
					user.getQueue().offer(new Message(sender, text, false));
				}
				
				//adds the user to the user table
				userTable.put(nickname, user);
			}
			
			
		} 
        catch(FileNotFoundException e){
        	Report.errorAndGiveUp("Server start failed: User file not found");
        }
		catch (IOException e) {
			Report.errorAndGiveUp("Something went wrong reading user file: " + e);
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public synchronized boolean writeAllToFile(){
		
		try {
			
			System.out.println("writing");
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			
			Element rootElement = doc.createElement("userInfo");
			
			//creates group elements
			Iterator it = userTable.entrySet().iterator();
			while(it.hasNext()){
				
				//gets the user
				ConcurrentMap.Entry userEntry = (ConcurrentMap.Entry)it.next();
				User user = ((User) userEntry.getValue());
				
				//creates user element
				Element uElement = doc.createElement("user");
				uElement.setAttribute("name", user.getNickname());
				uElement.setAttribute("password", user.getPassword());
				
				//creates message elements
				Iterator itM = user.getQueue().getIterator();
				while(itM.hasNext()){
					//gets the message
					Message message = (Message) itM.next();
					
					//creates the message element
					Element mElement = doc.createElement("message");
					mElement.setAttribute("sender", message.getSender());
					mElement.setTextContent(message.getText());
				    uElement.appendChild(mElement);
				}
				
				rootElement.appendChild(uElement);
			}
			
			doc.appendChild(rootElement);
			
			//writes results to file
			System.out.println("HELLO!");
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(userFile));
			transformer.transform(source, result);
			
		} 
		
		catch(Exception e){
			
		}
		
		return true;
	}
}
