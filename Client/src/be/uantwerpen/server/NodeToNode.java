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
	private int nextHash = -1;
	private int previousHash = -1;
	private int numberOfNodes = -1;
	private String[] replicationAnswer;
	private String ipAddress = "localhost";
	
	
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
		previousHash = prev;
	}
	
	public void startReceive(String ip, int port, String fileName) throws UnknownHostException, IOException {
		TCPUtil tcpReiver = new TCPUtil(ip, port, TCPUtil.Mode.RECEIVE, null, fileName);
		Thread t = new Thread(tcpReiver);
		t.start();

	}
	
	public void serverAnswer(int nodes, String[] fileReplicationList)
	{
		System.out.println("Answer from server.");
		this.numberOfNodes = nodes;
		this.replicationAnswer = fileReplicationList;
	}

	@Override
	public void updatePreviousHash(int hash) throws RemoteException {
		this.previousHash = hash;
	}

	@Override
	public void updateNextHash(int hash) throws RemoteException {
		this.nextHash = hash;
	}
	
	public String[] replicationAnswer() {
		return this.replicationAnswer;
	}
	public void setReplicationAnswer(String[] answer) {
		this.replicationAnswer = answer;
	}
	
	public String iPAddress() {
		return this.ipAddress;
	}
	public void setIPAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public int nextHash()
	{
		return this.nextHash;
	}
	public void setNextHash(int hash) {
		this.nextHash = hash;
	}
	
	public int previousHash()
	{
		return this.previousHash;
	}
	public void setPreviousHash(int hash) {
		this.previousHash = hash;
	}
	
	public int numberOfNodes()
	{
		return this.numberOfNodes;
	}
	public void setNumberOfNodes(int amount) {
		this.numberOfNodes = amount;
	}
	public void decreaseNumberOfNodes(int amountToSubtract) {
		this.numberOfNodes--;
	}
	
	public void resetHashes() {
		this.previousHash = -1;
		this.nextHash = -1;
	}
}