package be.uantwerpen.server;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.lang.Math;
import java.net.*;

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
			//Update XML file here
			
		    System.out.println("New client connected. Hashed name: " + hashedName + " IP: " + clientIP); // display message
		} catch(Exception e) { clientIP = "none";}
		String[] infoArray = {hashedName +"", clientIP};
		return infoArray;
	}
	
	public void addToHashMap(int hashedName, String ip, String[] filenames) {
		Client node = new Client();
		node.setId(1); node.setName(hashedName); node.setIpaddress(ip); //node.setFiles(filenames);
		if (!nodeMap.containsValue(node)) {
			nodeMap.put(id, node);
			id++;
		}
		System.out.println("nodeMap size: " + nodeMap.size());
		
		Clients clients = new Clients();
		List<Client> clientList = new ArrayList<Client>();
		for (Client client : nodeMap.values()) {
			clientList.add(client);
		}
		clients.setClients(clientList);
		
		//use nodemap to update XML file
		XMLParser parser = new XMLParser();
		parser.jaxbObjectToXML(clients);
		//XMLParser xmlParser = new XMLParser();
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
		node.setId(1); node.setName(hashedName); node.setIpaddress(ip); //node.setFiles(filenames);
		while( nodeMap.values().remove(node) ) {
			System.out.println("nodeMap size: " + nodeMap.size());
		}
		
		//update xml
	}
	
	public int hashString(String name) {
		return Math.abs(name.hashCode()) % 32768;
	}
	


}