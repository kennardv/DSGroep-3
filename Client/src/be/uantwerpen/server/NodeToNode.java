package be.uantwerpen.server;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;


public class NodeToNode extends UnicastRemoteObject implements NodeToNodeInterface {
	public int nextHash = -1;
	public int prevHash = -1;
	public int numberOfNodes = -1;
	
	
	public NodeToNode() throws RemoteException{
		super();
	}
	
	public void startAgent(Agent agent) {
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
			ntnI.startAgent(agent);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void answerDiscovery(int prev, int next)
	{
		nextHash = next;
		prevHash = prev;
		//System.out.println("Setting neighbour hashes in NodeToNode. nextHash: " + nextHash + " prevHash: " + prevHash);
	}
	
	public void serverAnswer(int nodes)
	{
		numberOfNodes = nodes;
		
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
}