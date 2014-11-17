package be.uantwerpen.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServerToNode extends UnicastRemoteObject implements ServerToNodeInterface {

	//NameServer ns;
	
	public ServerToNode(/*NameServer ns*/) throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public int[] askPrevAndNextNode(int hash) throws RemoteException {
		//this.ns.look for hash
		
		return null;
	}

}
