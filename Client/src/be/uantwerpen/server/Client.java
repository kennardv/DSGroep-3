package be.uantwerpen.server;

import java.net.*;
import java.net.UnknownHostException;
import java.io.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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
      Client.huidige =  hashString(nameClient);
      clientStats[0] = Client.huidige + "";
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
	  String bindLocation = "//localhost/ntn";
	  try { 
	    registry = LocateRegistry.createRegistry(1099);		
	  } catch (Exception e) {}  
	  try { 
	    Naming.bind(bindLocation, ntn);			
	  } catch (Exception e) {}
	  socket.send(dgram);
	  System.out.println("multicast is send");

	  while(ntn.nextHash == -1 || ntn.numberOfNodes == -1) //gaat uit while indien server zegt dat hij eerste node is of 
	  {  
		     System.out.println(ntn.nextHash);

		  
		  if(ntn.numberOfNodes == 0)
		  {
			  ntn.nextHash = Client.huidige;
			  ntn.prevHash = Client.huidige;
		  }
	    	try {
	    	    Thread.sleep(100);   
	    	} catch(InterruptedException ex) {
	    	    Thread.currentThread().interrupt();
	    	}
	  }
	  
	  try { 
	    Naming.unbind(bindLocation);	//unbind van name zodat andere client henm terug kan gebruiken    
      } catch (NotBoundException e) {  
    	 System.err.println("Not bound");
      } 
	     System.out.println("Client number: " + (ntn.numberOfNodes + 1));
	     System.out.println("previoushash: " + ntn.prevHash + "; my hash: " + Client.huidige + "; nexthash: " + ntn.nextHash);

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
			        
			        
			        if(Client.vorige == Client.volgende && foo < Client.huidige)
			        {
			        	try {
			                String name = "//localhost/ntn";
			                NodeToNodeInterface ntnI = (NodeToNodeInterface) Naming.lookup(name);
			                ntnI.answerDiscovery( Client.vorige, Client.huidige);
			                Client.vorige = foo;
			                System.out.println("NEW: previoushash: " + Client.vorige + "; my hash: " + Client.huidige + "; nexthash: " + Client.volgende);

			             } catch(Exception e) {
			                System.err.println("FileServer exception: "+ e.getMessage());
			                e.printStackTrace();
			             } 

			        }
			        else if(foo < Client.huidige && foo > Client.vorige)
			        {
		                Client.vorige = foo;
		                System.out.println("NEW: previoushash: " + Client.vorige + "; my hash: " + Client.huidige + "; nexthash: " + Client.volgende);	
			        }
			        
			        else if(foo < Client.volgende && foo > Client.huidige)
			        {
			        	try {
			                String name = "//localhost/ntn";
			                NodeToNodeInterface ntnI = (NodeToNodeInterface) Naming.lookup(name);
			                ntnI.answerDiscovery( Client.huidige, Client.volgende);
			                Client.volgende = foo;
			                System.out.println("NEW: previoushash: " + Client.vorige + "; my hash: " + Client.huidige + "; nexthash: " + Client.volgende);

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
