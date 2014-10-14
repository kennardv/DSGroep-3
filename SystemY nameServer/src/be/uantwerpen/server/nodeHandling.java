package be.uantwerpen.server;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.*;
import java.lang.Math;
import java.net.*;

public class nodeHandling extends UnicastRemoteObject implements nodeHandlingInterface {
	private static final long serialVersionUID = 1L;
	HashMap hashMap = new HashMap();
	
	public nodeHandling() throws RemoteException{
		super();
	}
	public String connect(String name)
	{
		int hashedName = HashName(name);
		String clientIP = null;
		try {
		    System.out.println(clientIP = getClientHost()); // display message
		    //hashMap.put(hashedName, clientIP);
		} catch(Exception e) { clientIP = "none";}
		return hashedName + "";
	}
	public void PrintMap() {
		// Get a set of the entries
	    Set set = hashMap.entrySet();
	    // Get an iterator
	    Iterator i = set.iterator();
	    // Display elements
	    while(i.hasNext()) {
	    	Map.Entry me = (Map.Entry)i.next();
	        System.out.print(me.getKey() + ": ");
	        System.out.println(me.getValue());
	    }
	}
	
	public int HashName(String name) {
		return Math.abs(name.hashCode()) % 32768;
	}

}