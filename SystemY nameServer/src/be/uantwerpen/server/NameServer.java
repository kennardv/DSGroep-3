package be.uantwerpen.server;

import rmi.implementations.*;
import rmi.interfaces.*;

import java.io.*;
import java.net.*;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

import networking.UDPUtil;

/**
 * 
 * @author Kennard
 *
 */
public class NameServer extends Thread {
	ClientMap clientMap = new ClientMap();
	ServerToNode stn = null;
	INodeToNode ntnI;
	IServerToNode stvI;
	private Registry registry = null;

	public NameServer() {
		init();
		bootstrap();
		
		//application close event
		Runtime.getRuntime().addShutdownHook(this);
		
		waitForClients();
		//ListenForPacket();
	}
	
	/**
	 * 
	 */
	void init() {
		try {
			registry = LocateRegistry.createRegistry(Constants.REGISTRY_PORT);
		} catch (RemoteException e) {
		}
	}
	
	/**
	 * 
	 */
	void bootstrap() {
		// bind rmi object
		try {
			stn = new ServerToNode(this.clientMap);
			String path = "//" + InetAddress.getLocalHost().getHostAddress() + "/" + Constants.RMI_SUFFIX_SERVER;
			Naming.bind(path, stn);
			System.out.println("Bound remote object at " + path);
		} catch (MalformedURLException | RemoteException | UnknownHostException | AlreadyBoundException e) {
			e.printStackTrace();
		}
		ntnI = null;
	}
	
	/**
	 * 
	 */
	public void waitForClients() {
		while (true) {
			//UDPUtil SHOULD BE A THREAD THAT CAN RETURN LIST
			//HELP? :)
			UDPUtil udpUtil = new UDPUtil();
			List message = udpUtil.listenForPackets();
			
			int hashedName = (int) message.get(0);
			String ip = (String) message.get(1);
			List<Integer> filenames = (List<Integer>) message.get(2);
			
			this.clientMap.add(hashedName, ip, filenames);
			
			String[] fileReplicateLocation = getFileReplicationLocation(hashedName, filenames);
			
			try {
				notifyClient(ip, fileReplicateLocation);
			} catch (MalformedURLException | RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String[] getFileReplicationLocation(int hashedName, List<Integer> filenames) {
		String[] fileReplicateLocation = null;
		if(this.clientMap.getClientMap().size() > 1)
		{
			fileReplicateLocation = new String[filenames.size()];
			for (int i = 0; i < filenames.size(); i++) {
				int previousNode = 0;
				boolean done = false;

			    Set<Integer> keys = this.clientMap.getClientMap().keySet();
			    Iterator<Integer> itr = keys.iterator();
			    while(itr.hasNext() && done == false)
			    {	
			    	previousNode = itr.next();
			    	//need to set the length to i
			    	//nick
			    	
			    	if((filenames.get(filenames.size() - 1) > previousNode) && (hashedName != previousNode ))
				    {
			    		done = true;
				    }
			    }
			    Client c = this.clientMap.getClientMap().get(previousNode);
			    fileReplicateLocation[i] = c.getIpaddress();
			}
		}
		return fileReplicateLocation;
	}
	
	/**
	 * 
	 * @param ipaddress
	 * @param fileReplicateLocation
	 * @throws MalformedURLException
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	public void notifyClient(String ipaddress, String[] fileReplicateLocation) throws MalformedURLException, RemoteException, NotBoundException {
		//notify client about amount of nodes
		String path = "//" + ipaddress + "/" + Constants.RMI_SUFFIX_NODE;
		ntnI = null;
		ntnI = (INodeToNode) Naming.lookup(path);
		ntnI.serverAnswer(this.clientMap.getClientMap().size(), fileReplicateLocation);
		System.out.println("Amount of clients: " + this.clientMap.getClientMap().size());
	}
	
	/**
	 * NOT TESTED
	 * This method is used in the shutdown hook
	 *  to clean up when the application is closed
	 */
	public void run() {
		this.clientMap.clear();
	}

	public static void main(String[] argv) throws RemoteException, ClassNotFoundException {
		NameServer nameServer = new NameServer();
	}
}