package be.uantwerpen.server;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.lang.Math;
import java.net.*;

public class nodeHandling extends UnicastRemoteObject implements nodeHandlingInterface {
	private static final long serialVersionUID = 1L;
	HashMap nodeMap = new HashMap();
	
	public nodeHandling() throws RemoteException{
		super();
	}
	public String connect(String name)
	{
		int hashedName = -1;
		String clientIP;
		try {
			hashedName = hashString(name);
			clientIP = getClientHost();
			nodeMap.put(hashedName, clientIP);
			//Update XML file here
			
		    System.out.println("New client connected. Hashed name: " + hashedName + " IP: " + clientIP); // display message
		} catch(Exception e) { clientIP = "none";}
		return "Hashed name: " + hashedName + " IP: " + clientIP;
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
	
	public int hashString(String name) {
		return Math.abs(name.hashCode()) % 32768;
	}

}