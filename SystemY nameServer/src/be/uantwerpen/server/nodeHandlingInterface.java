package be.uantwerpen.server;
import java.rmi.Remote;
import java.rmi.RemoteException;


public interface nodeHandlingInterface extends Remote {
   public String connect() throws RemoteException;


}