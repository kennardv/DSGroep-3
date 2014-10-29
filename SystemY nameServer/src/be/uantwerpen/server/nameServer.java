package be.uantwerpen.server;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class nameServer {
	static ClientMap clientMap = new ClientMap();
	static HashMap<Integer, Client> nodeMap = new HashMap<Integer, Client>();
	
	static XMLMarshaller marshaller = new XMLMarshaller();
	
	static int id = 0;
	public static int k = 0;
	
	public nameServer() {
		
	}
	
	// main functie: aanroepen bij opstart van de server
    public static void main(String[] argv) throws RemoteException, ClassNotFoundException {
    	NodeToNodeInterface ntnI = null;
    	String name = null;
    	
    	// locatie van nameserver
       String bindLocation = "//localhost/nameServer";
       
       try { 
    	   byte[] inBuf = new byte[256];
    	   DatagramPacket dgram = new DatagramPacket(inBuf, inBuf.length);
    	   MulticastSocket socket = new MulticastSocket(4545); // must bind receive side
    	   socket.joinGroup(InetAddress.getByName("226.100.100.125"));
		    while(true) {
		      socket.receive(dgram); // blocks until a datagram is received
		      ByteArrayInputStream bis = new ByteArrayInputStream(inBuf);
		      ObjectInput in = null;
		      try {
		        in = new ObjectInputStream(bis);
		        Object o = in.readObject(); 
		        //String[] stringValues = (String[])o;
		        List message = (List)o;
		        String[] clientStats = (String[]) message.get(0);
		        String[] filenamesArr = (String[])message.get(1);
		        List<String> filenames = new ArrayList<String>();
		        for (int i = 0; i < filenamesArr.length; i++) {
					filenames.add(filenamesArr[i]);
				}
				//addToHashMap(Integer.parseInt(clientStats[0]), clientStats[1], filenames);
		        //removeFromHashMap(Integer.parseInt(clientStats[0]));

			    System.err.println("hash: " + clientStats[0]);
			     
		      } finally {
		        try {
		          bis.close();
		        } catch (IOException ex) {
		          // ignore close exception
		        }
		        try {
		          if (in != null) {
		            in.close();
		          }
		        } catch (IOException ex) {
		          // ignore close exception
		        }
		      }
		      dgram.setLength(inBuf.length); // must reset length field!
		      System.err.println("nummer van clients: " + k);
		      try {
		      name = "//localhost/ntn";
		      ntnI = null;
              ntnI = (NodeToNodeInterface) Naming.lookup(name);
              ntnI.serverAnswer(k);
              k++;
              System.err.println("nummer van clients: " + k);
	          } catch(Exception e) {
		                System.err.println("FileServer exception: "+ e.getMessage());
		                e.printStackTrace();
		      }
              System.err.println("nummer van clients: " + k);
		    }
       }
       catch(UnknownHostException e){}
       catch(IOException e){}
       
    }
    
	/**
	 * not working
	 * @param key
	 * This is the hashed name for this particular map.
	 */
	public static void removeFromHashMap(int key) {
		try {
			clientMap.removeKeyValuePair(key);
		} catch (Exception e) {
			System.out.println(e);
		}
		
		//update xml
		marshaller.jaxbObjectToXML(clientMap);
	}

    public static void addToHashMap(int hashedName, String ip, List<String> filenames) {
    	try {
    	Client node = new Client();
		node.setId(1); node.setName(hashedName); node.setIpaddress(ip); node.setFiles(filenames);
    	nodeMap.put(hashedName, node);
		
    	clientMap.setClientMap(nodeMap);
		
		//use nodemap to update XML file
		marshaller.jaxbObjectToXML(clientMap);
    	} catch(Exception e) {
    		
    	}
	}
    
    public void initHashMapFromXML() {
    	clientMap = marshaller.jaxbXMLToObject();
    }
}	