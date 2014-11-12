package be.uantwerpen.server;

import java.io.*;
import java.net.*;
import java.net.UnknownHostException;
import java.rmi.*;
import java.util.*;

public class NameServer {
	ClientMap clientMap = new ClientMap();
	public Map<Integer, Client> nodeMap = new TreeMap<Integer, Client>(Collections.reverseOrder());

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
			
			//loop forever
			//check if a packet was received
		    while(true) {
		    	// blocks until a datagram is received
				socket.receive(dgram); 
				String[][] fileReplicateLocation;
				//process received packet
				ByteArrayInputStream bis = new ByteArrayInputStream(inBuf);
				ObjectInput in = null;
				try {
					in = new ObjectInputStream(bis);
					Object o = in.readObject();
					
					//pull values from message and store
					List message = (List) o;
					String[] clientStats = (String[]) message.get(0);
					int[] filenamesArr = (int[])message.get(1);
					int l = filenamesArr.length;
					fileReplicateLocation = new String[l][2];
					List<Integer> filenames = new ArrayList<>();
			        for (int i = 0; i < filenamesArr.length; i++) {
						filenames.add(filenamesArr[i]);
					}
		        
					addToHashMap(Integer.parseInt(clientStats[0]), clientStats[1], filenames);

                
					if(k> 0)
					{
				        for (int i = 0; i < filenamesArr.length; i++) 
				        {
							int previousNode = 0;	
							boolean done = false;
						    Set keys = nodeMap.keySet();
						    Iterator itr = keys.iterator();
						    while(itr.hasNext() && done == false)
						    {	
						    	previousNode = (int) itr.next();
						    	
						    	
						    	if((filenamesArr[1] > previousNode)  &&  (Integer.parseInt(clientStats[0]) != previousNode ))
							    {
						    		done = true;
							    }
						    }
						    Client node = nodeMap.get(previousNode);
						    
						    fileReplicateLocation[i][0] = filenamesArr[i] + "";
						    fileReplicateLocation[i][1] = node.getIpaddress();

						}
					}
					
					
					System.out.println("hash: " + clientStats[0]);
					dgram.setLength(inBuf.length);
					try {
						//notify client about amount of nodes
						name = "//localhost/ntn";
						ntnI = null;
						ntnI = (NodeToNodeInterface) Naming.lookup(name);
						ntnI.serverAnswer(k, fileReplicateLocation);
						k++;
						System.err.println("Amount of clients: " + k);
					} catch (Exception e) {
						System.err.println("FileServer exception: "
								+ e.getMessage());
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
				//reset length field
				
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
	public void addToHashMap(int hashedName, String ip, List<Integer> filenames) {
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