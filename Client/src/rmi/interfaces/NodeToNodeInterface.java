package rmi.interfaces;

import agents.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface NodeToNodeInterface extends Remote {

	public void answerDiscovery(int prev, int next) throws RemoteException;
	public void serverAnswer(int nodes, String[] fileReplicationList) throws RemoteException;
	public void startFileListAgent(FileListAgent agent, ServerToNodeInterface stnI, int currentHash, String suffix) throws RemoteException;
	public void startReceive(String ip, String fileName) throws UnknownHostException, IOException;
	public void startFileRecoveryAgent(FileRecoveryAgent agent) throws RemoteException;
   	public void updatePreviousHash(int hash) throws RemoteException;
   	public void updateNextHash(int hash) throws RemoteException;
   	public void updateFileList(List<String> fileList) throws RemoteException;
}