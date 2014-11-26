package rmi.interfaces;

import agents.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NodeToNodeInterface extends Remote {

	public void answerDiscovery(int prev, int next) throws RemoteException;
	public void serverAnswer(int nodes, String[] fileReplicationList) throws RemoteException;
	public void startFileListAgent(FileListAgent agent) throws RemoteException;
	public void startReceive(String ip, int port, String fileName) throws UnknownHostException, IOException;
	public void startFileRecoveryAgent(FileRecoveryAgent agent) throws RemoteException;
   	public void updatePreviousHash(int hash) throws RemoteException;
   	public void updateNextHash(int hash) throws RemoteException;
}