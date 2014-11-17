package be.uantwerpen.server;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;


public class NodeToNode extends UnicastRemoteObject implements NodeToNodeInterface {
	public int nextHash = -1;
	public int prevHash = -1;
	public int numberOfNodes = -1;
	public String[] replicationAnswer;
	public String ipAdress = "localhost";
	
	
	public NodeToNode() throws RemoteException{
		super();
	}
	
	public void startFileListAgent(FileListAgent agent) {
		//make new thread with argument: agent
		Thread t = new Thread(agent);
		
		//start thread and wait for it to die
		t.start(); 
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//call same method on next node
		String name = "//localhost/ntn";
		NodeToNodeInterface ntnI = null;
		try {
			ntnI = (NodeToNodeInterface) Naming.lookup(name);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			ntnI.startFileListAgent(agent);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void startFileRecoveryAgent(FileRecoveryAgent agent) {
		//make new thread with argument: agent
		Thread t = new Thread(agent);
		
		//start thread and wait for it to die
		t.start(); 
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//call same method on next node
		String name = "//localhost/ntn";
		NodeToNodeInterface ntnI = null;
		try {
			ntnI = (NodeToNodeInterface) Naming.lookup(name);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			ntnI.startFileRecoveryAgent(agent);
			/** UNLESS agent must stop **/
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void answerDiscovery(int prev, int next)
	{
		nextHash = next;
		prevHash = prev;
	}
	
	public void getReceiverIp(String ip, int port, String fileName) throws UnknownHostException, IOException {
		TCPUtil tcpSender = new TCPUtil(ip, port, false, null, fileName);
		Thread t = new Thread(tcpSender);
		t.start();

	}
	    
	   
	
	public void serverAnswer(int nodes, String[] fileReplicationList)
	{
		System.out.println("Answer from server.");
		this.numberOfNodes = nodes;
		this.replicationAnswer = fileReplicationList;
	}
	int nextHash()
	{
		return nextHash;
	}
	int prevHash()
	{
		return prevHash;
	}
	
	int numberOfNodes()
	{
		return numberOfNodes;
	}

	@Override
	public void updateHashes(int previous, int next) throws RemoteException {
		this.prevHash = previous;
		this.nextHash = next;
	}
}