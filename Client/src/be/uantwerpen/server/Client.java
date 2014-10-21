package be.uantwerpen.server;

import java.net.*;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.io.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;


public class Client {
	
	public Client() throws RemoteException{
		super();
	}
	   public static int vorige,huidige,volgende; //declaratie van de type hashes
	   public static NodeToNode ntn; //declaratie van remote object

   public static void main(String argv[]) throws InterruptedException, IOException, ClassNotFoundException {
	  List message = new ArrayList(); //arraylist met positie 0 = clients ip en hash, positie 1 = files array
	   
	   
   	  ntn = new NodeToNode();
   	  Registry registry = null;
	  Client.vorige = 8000;
	  Client.huidige = 9000;
	  Client.volgende = 15000;
	  String nameClient = "Client1"; //naam van de client
	  String[] filenames = {"file1.jpg", "file2.txt", "file3.gif"}; //lijst van files op de clientpc
      String[] clientStats = new String[2];
      clientStats[0] = hashString(nameClient) + "";
      clientStats[1] = Inet4Address.getLocalHost().getHostAddress();
      message.add(clientStats);
      message.add(filenames);
      
      Object obj = message; 
      DatagramSocket socket = new DatagramSocket();
      ByteArrayOutputStream byteArr = new ByteArrayOutputStream();
      ObjectOutput objOut = new ObjectOutputStream(byteArr);
      objOut.writeObject(obj);
      byte[] b = byteArr.toByteArray();
	  DatagramPacket dgram;
	  dgram = new DatagramPacket(b, b.length, InetAddress.getByName("226.100.100.125"), 4545);
	  System.err.println("Sending " + b.length + " bytes to " +
	  dgram.getAddress() + ':' + dgram.getPort());
	  System.err.print("send");
	  String bindLocation = "//localhost/ntn";
	  try { 
	    registry = LocateRegistry.createRegistry(1099);		
	  } catch (Exception e) {}  
	  try { 
	    Naming.bind(bindLocation, ntn);			
	  } catch (Exception e) {}
	  socket.send(dgram);
	    
	  while(ntn.nextHash == 0 || ntn.numberOfNodes == -1) //gaat uit while indien server zegt dat hij eerste node is of 
	  {  												  //indien een client zijn hashes terug geeft
	    	try {
	    	    Thread.sleep(1000);   
	    	    System.err.println("aangekregen next: " + ntn.nextHash);
	    	} catch(InterruptedException ex) {
	    	    Thread.currentThread().interrupt();
	    	}
	  }
	  try { 
	    Naming.unbind(bindLocation);	//unbind van name zodat andere client henm terug kan gebruiken    
      } catch (NotBoundException e) {  
    	 System.err.println("Not bound");
      } 
	     System.err.println("aangekregen next: " + ntn.nextHash);
	     System.err.println("aantal nodes: " + ntn.numberOfNodes);
	     Client.volgende  = ntn.nextHash();
	     Client.vorige  = ntn.prevHash();
	     
	    waitforclients();
      
      
   }
   static int hashString(String name) {
		return Math.abs(name.hashCode()) % 32768; //berekening van de hash
   }


	static void waitforclients() throws ClassNotFoundException
	{
		try { 
	    	   byte[] inBuf = new byte[256];
	    	   DatagramPacket dgram = new DatagramPacket(inBuf, inBuf.length);
	    	   MulticastSocket socket = new MulticastSocket(4545); 
	    	   socket.joinGroup(InetAddress.getByName("226.100.100.125"));
			    while(true) {
			    	
			      socket.receive(dgram); //blokkeerd tot hij een package ontvangt
			      ByteArrayInputStream bis = new ByteArrayInputStream(inBuf);
			      ObjectInput in = null;
			      try {
			        in = new ObjectInputStream(bis);
			        Object o = in.readObject(); 
			        List message = (List)o;
			        String[] clientStats = (String[]) message.get(0);
			        int foo = Integer.parseInt(clientStats[0]); //de hash uit de multicast message halen
			        
			        
			        if(Client.volgende == 40000 &&Client.vorige == -1)
			        {


			        }
			        else if(foo > Client.vorige && foo < Client.volgende)
			        {
			        	try {
			                String name = "//localhost/ntn";
			                NodeToNodeInterface ntnI = (NodeToNodeInterface) Naming.lookup(name);
			                ntnI.answerDiscovery( Client.vorige, Client.volgende);
			                System.err.println("Verzonden" + Client.vorige);

			                System.err.println("Verzonden");

			             } catch(Exception e) {
			                System.err.println("FileServer exception: "+ e.getMessage());
			                e.printStackTrace();
			             }

			        	
			        }
			      } finally {
			        try {
			          bis.close();
			        } catch (IOException ex) {
			          
			        }
			        try {
			          if (in != null) {
			            in.close();
			          }
			        } catch (IOException ex) {
			          
			        }
			      }
			      dgram.setLength(inBuf.length);
			    }
	       }
	       catch(UnknownHostException e){}
	       catch(IOException e){}
	}
   
}

 class IntObj {
    public int value;
}
