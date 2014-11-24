package rmi.interfaces;

import java.rmi.RemoteException;

public interface ServerToNodeInterface {
	public int[] getPreviousAndNextNodeHash(int hash) throws RemoteException;
	public void removeNode(int nodeHash) throws RemoteException;
	public String getNodeIPAddress(int nodeHash) throws RemoteException;
}
