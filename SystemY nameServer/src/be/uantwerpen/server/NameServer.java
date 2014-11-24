package be.uantwerpen.server;

import java.io.*;
import java.net.*;
import java.net.UnknownHostException;
import java.rmi.*;
import java.util.*;

public class NameServer {
	ClientMap clientMap = new ClientMap();
	TreeMap<Integer, Client> nodeMap = new TreeMap<Integer, Client>();

	XMLMarshaller marshaller = new XMLMarshaller();

	int k = 0;

	NodeToNodeInterface ntnI;
	ServerToNodeInterface stvI;
	String name;

	String serverIp = "226.100.100.125";

	/**
	 * 0 = discovery 1 = shutdown 2 = failure
	 */
	private String[] subject = { "discovery", "shutdown", "failure" };

	public NameServer() {
		// bind rmi object
		// Naming.bind("localhost", stvI);
		ntnI = null;
		name = null;

		ListenForPacket();
	}

	public void ListenForPacket() {
		try {
			// create socket, buffer and join socket group
			byte[] inBuf = new byte[256];
			DatagramPacket dgram = new DatagramPacket(inBuf, inBuf.length);
			MulticastSocket socket = new MulticastSocket(4545); // must bind
																// receive side
			socket.joinGroup(InetAddress.getByName(serverIp));

			
			
			int clientHashedName;
			//loop forever
			//check if a packet was received
		    while(true) {
		    	// blocks until a datagram is received
				socket.receive(dgram);
				String[] fileReplicateLocation = null;
				// process received packet
				ByteArrayInputStream bis = new ByteArrayInputStream(inBuf);
				ObjectInput in = null;
				try {
					in = new ObjectInputStream(bis);
					Object o = in.readObject();
					// pull values from message and store
					List message = (List) o;

					clientHashedName = (int)message.get(1);
					
					System.out.println("Received dgram from " + dgram.getAddress().getHostAddress());
					
					int[] filenamesArr = (int[])message.get(2);
					List<Integer> filenames = new ArrayList<Integer>();
					for (int i = 0; i < filenamesArr.length; i++) {
						filenames.add(filenamesArr[i]);
					}

			        
			        //Boolean shutdown = (Boolean) message.get(3);
			        
			        /*if(shutdown == true){
			        	System.err.println("shutdown client: " + clientStats[0]);
			        	removeFromMap(Integer.parseInt(clientStats[0]));
			        }else{*/
			        	//add new values to map
			        addToMap(clientHashedName, dgram.getAddress().getHostAddress(), filenames);
			        //}
			        
			        
					if(k> 0)
					{
						fileReplicateLocation = new String[filenamesArr.length];
						for (int i = 0; i < filenamesArr.length; i++) {
							int previousNode = 0;
							boolean done = false;

						    Set keys = nodeMap.keySet();
						    Iterator itr = keys.iterator();
						    while(itr.hasNext() && done == false)
						    {	
						    	previousNode = (int) itr.next();
						    	
						    	
						    	if((filenamesArr[1] > previousNode) && (clientHashedName != previousNode ))
							    {
						    		done = true;
							    }
						    }
						    Client node = nodeMap.get(previousNode);
						    
						    fileReplicateLocation[i] = node.getIpaddress();

						}
					}
					
					
					System.out.println("hash: " + clientHashedName);
					dgram.setLength(inBuf.length);
					try {
						//notify client about amount of nodes
						name = "//" + dgram.getAddress().getHostAddress() + "/ntn";
						ntnI = null;
						ntnI = (NodeToNodeInterface) Naming.lookup(name);
						ntnI.serverAnswer(clientMap.getClientMap().size(), fileReplicateLocation);
						k++;
						System.err.println("Amount of clients: " + clientMap.getClientMap().size());
					} catch (Exception e) {
						System.err.println("FileServer exception: " + e.getMessage());
						e.printStackTrace();
					}

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

			}
		} catch (UnknownHostException e) {
		} catch (IOException e) {
		}
	}

	/**
	 * Remove a key/value pair with specified key
	 * 
	 * @param key
	 *            This is the hashed name for this particular map.
	 */
	public void removeFromMap(int key) {
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
	 * 
	 * @param hashedName
	 * @param ip
	 * @param filenames
	 */
	public void addToMap(int hashedName, String ip, List<Integer> filenames) {
		try {
			// Instantiate new Client object
			Client node = new Client();
			node.setId(1);
			node.setName(hashedName);
			node.setIpaddress(ip);
			node.setFiles(filenames);

			// add to map
			nodeMap.put(hashedName, node);

			// Save nodeMap to parent map
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

	public static void main(String[] argv) throws RemoteException,
			ClassNotFoundException {
		NameServer nameServer = new NameServer();

	}
}