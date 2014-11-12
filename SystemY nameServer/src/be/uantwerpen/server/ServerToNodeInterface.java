package be.uantwerpen.server;

import java.rmi.RemoteException;

public interface ServerToNodeInterface {
	public int[] askPrevAndNextNode(int hash) throws RemoteException;
}
