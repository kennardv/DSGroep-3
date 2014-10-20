package be.uantwerpen.server;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.lang.Math;

public class nodeHandling extends UnicastRemoteObject implements nodeHandlingInterface {
	private static final long serialVersionUID = 1L;
	HashMap<Integer, Client> nodeMap = new HashMap<Integer, Client>();
	int id = 0;
	
	public nodeHandling() throws RemoteException{
		super();
	}
	public String[] connect(String name, String[] filenames)
	{
		int hashedName = -1;
		String clientIP;
		try {
			hashedName = hashString(name);
			clientIP = getClientHost();
			addToHashMap(hashedName, clientIP, filenames);
			removeFromHashMap(hashedName, clientIP, filenames);
			
			
		} catch(Exception e) { clientIP = "none";}
		String[] infoArray = {hashedName +"", clientIP};
		return infoArray;
	}
	
	public void addToHashMap(int hashedName, String ip, String[] filenames) {
		Client node = new Client();
		node.setId(1); node.setName(hashedName); node.setIpaddress(ip); node.setFiles(filenames);
		if (!nodeMap.containsValue(node)) {
			nodeMap.put(id, node);
			id++;
		}
		System.out.println("nodeMap size: " + nodeMap.size());
		
		//use nodemap to update XML file
		XMLParser xmlParser = new XMLParser();
		//xmlParser.addNode(hashedName, ip);
		//xmlParser.addFilesToNode(hashedName, ip, filenames);
	}
	
	/**
	 * not working
	 * @param hashedName
	 * @param ip
	 * @param filenames
	 */
	public void removeFromHashMap(int hashedName, String ip, String[] filenames) {
		Client node = new Client();
		node.setId(1); node.setName(hashedName); node.setIpaddress(ip); node.setFiles(filenames);
		while( nodeMap.values().remove(node) ) {
			System.out.println("nodeMap size: " + nodeMap.size());
		}
		
		//update xml
	}
	
	public boolean checkForExistence(int hashedName, String ipaddress) {
		return nodeMap.containsKey(hashedName);
	}
	
	public void printMap() {
		// Get a set of the entries
	    Set set = nodeMap.entrySet();
	    // Get an iterator
	    Iterator i = set.iterator();
	    // Display elements
	    while(i.hasNext()) {
	    	Map.Entry me = (Map.Entry)i.next();
	        System.out.print(me.getKey() + ": ");
	        System.out.println(me.getValue());
	    }
	}
	
	/**
	 * This method returns a hash from 0 to 32768
	 * @param str
	 * @return hashed result
	 */
	public int hashString(String str) {
		return Math.abs(str.hashCode()) % 32768;
	}
}