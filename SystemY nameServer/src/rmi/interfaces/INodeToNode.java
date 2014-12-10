package rmi.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface INodeToNode extends Remote {
   public void answerDiscovery(int prev, int next) throws RemoteException;
   public void serverAnswer(int nodes, String[] fileReplicationList) throws RemoteException;
}
