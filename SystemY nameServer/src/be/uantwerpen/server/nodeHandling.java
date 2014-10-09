package be.uantwerpen.server;

import java.net.InetAddress;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

public class nodeHandling extends UnicastRemoteObject implements nodeHandlingInterface {
	private static final long serialVersionUID = 1L;
	
	public nodeHandling() throws RemoteException{
		super();
	}
	public String connect()
	{
		String area = null;
		try{
		    System.out.println(area = getClientHost()); // display message
		}catch(Exception e){ area = "none";}
		
		return area;
	}


}