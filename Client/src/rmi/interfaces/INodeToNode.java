package rmi.interfaces;

import agents.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public interface INodeToNode extends Remote {

	public void answerDiscovery(int prev, int next) throws RemoteException;
	public void serverAnswer(int nodes, String[] fileReplicationList) throws RemoteException;
	public void startFileListAgent(FileListAgent agent, int currentHash, String suffix) throws RemoteException;
	public void startReceive(String ip, String fileName) throws UnknownHostException, IOException;
	public void startFileRecoveryAgent(FileRecoveryAgent agent) throws RemoteException;
   	public void updatePreviousHash(int hash) throws RemoteException;
   	public void updateNextHash(int hash) throws RemoteException;
   	public void updateFileList(TreeMap<Integer, Boolean> fileList) throws RemoteException;
	public void setLockRequest(int fileHash) throws RemoteException;
	public int getLockRequest() throws RemoteException;
	public void setPreviousLock(int previousLock) throws RemoteException;
	public int getPreviousLock() throws RemoteException;


}