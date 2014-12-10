package rmi.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerToNodeInterface extends Remote{
	public int[] getPreviousAndNextNodeHash(int hash) throws RemoteException;
	public int getNextNodeHash(int hash) throws RemoteException;
	public void removeNode(int nodeHash) throws RemoteException;
	public String getNodeIPAddress(int nodeHash) throws RemoteException;
	public int getnewFileReplicationNode(int filehash, int clientHashedName) throws RemoteException;
}
