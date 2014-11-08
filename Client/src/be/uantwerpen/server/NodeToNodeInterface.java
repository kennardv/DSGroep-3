package be.uantwerpen.server;

import java.rmi.Remote;
import java.rmi.RemoteException;



public interface NodeToNodeInterface extends Remote {
   public void answerDiscovery(int prev, int next) throws RemoteException;
   public void serverAnswer(int nodes) throws RemoteException;
   public void startFileListAgent(FileListAgent agent) throws RemoteException;
   public void startFileRecoveryAgent(FileRecoveryAgent agent) throws RemoteException;
}