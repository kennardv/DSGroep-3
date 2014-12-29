package rmi.implementations;

import agents.*;
import be.uantwerpen.server.Constants;
import rmi.interfaces.*;
import utils.Toolkit;
import networking.*;
import enumerations.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class NodeToNode extends UnicastRemoteObject implements INodeToNode {
	private int nextHash = -1;
	private int previousHash = -1;
	private int numberOfNodes = -1;
	private String[] replicationAnswer;
	private String ipAddress = "localhost";
	private List<Integer> fileList;
	
	public NodeToNode() throws RemoteException{
		super();
	}
	
	public void startFileListAgent(FileListAgent agent, int currentHash, String suffix) {
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
		int nextNode = 0;
		String name = null;
		try {
			nextNode = Constants.ISERVER_TO_NODE.getNextNodeHash(currentHash);
			name = Constants.ISERVER_TO_NODE.getNodeIPAddress(nextNode);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		name = Toolkit.createBindLocation(name, suffix); //"//" + stnI.getNextNodeHash(currentHash) + "/ntn";
		INodeToNode ntnI = null;
		try {
			ntnI = (INodeToNode) Naming.lookup(name);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			ntnI.startFileListAgent(agent, nextNode, suffix);
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
		INodeToNode ntnI = null;
		try {
			ntnI = (INodeToNode) Naming.lookup(name);
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
	
	public void startReceive(String ip, String fileName) throws UnknownHostException, IOException {
		TCPUtil tcpReceiver = new TCPUtil(ip, Mode.RECEIVE, null, fileName);
		Thread t = new Thread(tcpReceiver);
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

	@Override
	public void updateFileList(List<Integer> fileList) throws RemoteException {
		this.fileList = fileList;
		System.out.println("fileList " + this.fileList.size());
	}
}