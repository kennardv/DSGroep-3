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
			//create socket, buffer and join socket group
			byte[] inBuf = new byte[256];
			DatagramPacket dgram = new DatagramPacket(inBuf, inBuf.length);
			MulticastSocket socket = new MulticastSocket(4545); // must bind receive side
			socket.joinGroup(InetAddress.getByName("226.100.100.125"));
			
			
			String[] clientStats = null;
			//loop forever
			//check if a packet was received
		    while(true) {
		    	// blocks until a datagram is received
				socket.receive(dgram); 
				
				//process received packet
				ByteArrayInputStream bis = new ByteArrayInputStream(inBuf);
				ObjectInput in = null;
				try {
					in = new ObjectInputStream(bis);
					Object o = in.readObject();
					
					//pull values from message and store
					List message = (List) o;
					clientStats = (String[]) message.get(0);
					String[] filenamesArr = (String[])message.get(1);
					List<String> filenames = new ArrayList<String>();
			        for (int i = 0; i < filenamesArr.length; i++) {
						filenames.add(filenamesArr[i]);
					}
			        Boolean shutdown = (Boolean) message.get(2);
			        
			        if(shutdown == true){
			        	System.err.println("shutdown client: " + clientStats[0]);
			        	removeFromHashMap(Integer.parseInt(clientStats[0]));
			        	k--;
			        }else{
			        	//add new values to map
			        	addToHashMap(Integer.parseInt(clientStats[0]), clientStats[1], filenames);
						k++;
			        }
                
					System.out.println("hash: " + clientStats[0]);


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
				//reset length field
				dgram.setLength(inBuf.length);
				try {
					//notify client about amount of nodes
					System.out.println("k = " + k);
					name = "//" + clientStats[1] + "/ntn";
					ntnI = null;
					ntnI = (NodeToNodeInterface) Naming.lookup(name);
					System.out.println("lookup done = " + name);
					ntnI.serverAnswer(k);
					System.err.println("Amount of clients: " + k);
				} catch (Exception e) {
					System.err.println("FileServer exception: " + e.getMessage());
					e.printStackTrace();
				}
			}
		} catch (UnknownHostException e) {
		} catch (IOException e) {
		}
	}

	/**
	 * Remove a key/value pair with specified key
	 * @param key
	 * This is the hashed name for this particular map.
	 */
	public void removeFromHashMap(int key) {
		try {
	        System.out.println("Delete node with key : " + key);
			clientMap.removeKeyValuePair(key);
		} catch (Exception e) {
			System.out.println(e);
		}

		// update xml
		marshaller.jaxbObjectToXML(clientMap);
	}
	
	/**
	 * Add a key/value pair with passed data. Value is of type Client
	 * @param hashedName
	 * @param ip
	 * @param filenames
	 */
	public void addToHashMap(int hashedName, String ip, List<String> filenames) {
		try {
			//Instantiate new Client object
			Client node = new Client();
			node.setId(1);
			node.setName(hashedName);
			node.setIpaddress(ip);
			node.setFiles(filenames);
			
			//add to map
			nodeMap.put(hashedName, node);
			
			//Save nodeMap to parent map
			clientMap.setClientMap(nodeMap);

			// use parent clientMap to update XML file
			marshaller.jaxbObjectToXML(clientMap);
		} catch (Exception e) {

		}
	}
	
	/**
	 * Fill the hashmap with data from the XML file
	 */
	public void initHashMapFromXML() {
		clientMap = marshaller.jaxbXMLToObject();
	}

	public static void main(String[] argv) throws RemoteException, ClassNotFoundException {
		NameServer nameServer = new NameServer();
		
	}
}