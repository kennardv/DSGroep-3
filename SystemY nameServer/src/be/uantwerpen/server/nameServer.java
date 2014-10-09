package be.uantwerpen.server;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class nameServer {
    public static void main(String[] argv) throws RemoteException {
    	nodeHandling fileImpl = new nodeHandling();
       String bindLocation = "//localhost/nameServer";
       try { 
			LocateRegistry.createRegistry(1099);
			Naming.bind(bindLocation, fileImpl);
	        System.out.println("FileServer Server is ready at:" + bindLocation);
            System.out.println("java RMI registry created.");
        } catch (MalformedURLException | AlreadyBoundException e) {
            System.out.println("java RMI registry already exists.");
        }
       
    }
}