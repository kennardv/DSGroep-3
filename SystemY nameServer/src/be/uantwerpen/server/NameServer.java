package be.uantwerpen.server;

import java.io.*;
import java.net.*;
import java.net.UnknownHostException;
import java.rmi.*;
import java.util.*;

public class NameServer {
	ClientMap clientMap = new ClientMap();
	HashMap<Integer, Client> nodeMap = new HashMap<Integer, Client>();

	XMLMarshaller marshaller = new XMLMarshaller();

	int id = 0;
	public int k = 0;

	NodeToNodeInterface ntnI;
	String name;

	public NameServer() {
		ntnI = null;
		name = null;

		ListenForPacket();
	}

	public void ListenForPacket() {
    	
       try { 
			byte[] inBuf = new byte[256];
			DatagramPacket dgram = new DatagramPacket(inBuf, inBuf.length);
			MulticastSocket socket = new MulticastSocket(4545); // must bind receive side
			socket.joinGroup(InetAddress.getByName("226.100.100.125"));
			// loop receive
		    while(true) {
				socket.receive(dgram); // blocks until a datagram is received
				ByteArrayInputStream bis = new ByteArrayInputStream(inBuf);
				ObjectInput in = null;
				try {
					in = new ObjectInputStream(bis);
					Object o = in.readObject();
					// String[] stringValues = (String[])o;
					List message = (List) o;
					String[] clientStats = (String[]) message.get(0);
		        String[] filenamesArr = (String[])message.get(1);
		        List<String> filenames = new ArrayList<String>();
		        for (int i = 0; i < filenamesArr.length; i++) {
						filenames.add(filenamesArr[i]);
					}
		        //Boolean shutdown = (Boolean) message.get(2);
		        //if(shutdown == true){
				  //    System.err.println("shutdown");
		       // }else{
					 addToHashMap(Integer.parseInt(clientStats[0]), clientStats[1], filenames);
				//}
		        //removeFromHashMap(Integer.parseInt(clientStats[0]));
                
			    System.err.println("hash: " + clientStats[0]);


				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
				} catch (Exception e) {
					System.err.println("FileServer exception: "
							+ e.getMessage());
					e.printStackTrace();
				}
				System.err.println("nummer van clients: " + k);
			}
		} catch (UnknownHostException e) {
		} catch (IOException e) {
		}
	}

	/**
	 * @param key
	 * This is the hashed name for this particular map.
	 */
	public void removeFromHashMap(int key) {
		try {
			clientMap.removeKeyValuePair(key);
		} catch (Exception e) {
			System.out.println(e);
		}

		// update xml
		marshaller.jaxbObjectToXML(clientMap);
	}

	public void addToHashMap(int hashedName, String ip, List<String> filenames) {
		try {
			Client node = new Client();
			node.setId(1);
			node.setName(hashedName);
			node.setIpaddress(ip);
			node.setFiles(filenames);
			nodeMap.put(hashedName, node);

			clientMap.setClientMap(nodeMap);

			// use nodemap to update XML file
			marshaller.jaxbObjectToXML(clientMap);
		} catch (Exception e) {

		}
	}

	public void initHashMapFromXML() {
		clientMap = marshaller.jaxbXMLToObject();
	}

	// main functie: aanroepen bij opstart van de server
	public static void main(String[] argv) throws RemoteException,
			ClassNotFoundException {
		NameServer nameServer = new NameServer();
	}
}