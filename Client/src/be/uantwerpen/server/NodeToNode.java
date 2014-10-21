package be.uantwerpen.server;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.lang.Math;
import java.net.*;

public class NodeToNode extends UnicastRemoteObject implements NodeToNodeInterface {
	public int nextHash = 40000;
	public int prevHash = -1;
	public int numberOfNodes = -1;
	
	
	public NodeToNode() throws RemoteException{
		super();
	}
	public void answerDiscovery(int prev, int next)
	{
		nextHash = 15;
		prevHash = 20;
		
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
		return prevHash;
	}
	

	



}