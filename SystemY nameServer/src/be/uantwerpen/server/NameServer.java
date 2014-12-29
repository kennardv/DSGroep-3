package be.uantwerpen.server;

import rmi.implementations.*;
import rmi.interfaces.*;

import java.io.*;
import java.net.*;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

import enumerations.Protocol;

public class NameServer {
	ClientMap clientMap = new ClientMap();
	ServerToNode stn = null;
	INodeToNode ntnI;
	IServerToNode stvI;
	private Registry registry = null;

	public NameServer() {
		try {
			registry = LocateRegistry.createRegistry(Constants.REGISTRY_PORT);
		} catch (RemoteException e) {
		}
		// bind rmi object
		try {
			stn = new ServerToNode(this.clientMap);
			String path = "//" + InetAddress.getLocalHost().getHostAddress() + "/" + Constants.RMI_SUFFIX_SERVER;
			Naming.bind(path, stn);
			System.out.println("Bound remote object at " + path);
		} catch (MalformedURLException | RemoteException | UnknownHostException | AlreadyBoundException e) {
			e.printStackTrace();
		}
		ntnI = null;

		ListenForPacket();
	}

	public void ListenForPacket() {
		try {
			// create socket, buffer and join socket group
			byte[] inBuf = new byte[256];
			DatagramPacket dgram = new DatagramPacket(inBuf, inBuf.length);
			MulticastSocket socket = new MulticastSocket(Constants.SOCKET_PORT_UDP); // must bind receive side
			socket.joinGroup(InetAddress.getByName(Constants.MULTICAST_IP));

			int clientHashedName;
			//loop forever
			//check if a packet was received
		    while(true) {
		    	// blocks until a datagram is received
		    	System.out.println("Listening for packet");
				socket.receive(dgram);
				String[] fileReplicateLocation = null;
				// process received packet
				ByteArrayInputStream bis = new ByteArrayInputStream(inBuf);
				ObjectInput in = null;
				try {
					System.out.println("Receiving packet");
					in = new ObjectInputStream(bis);
					Object o = in.readObject();
					// pull values from message and store
					List message = (List) o;
					if (message.get(0) == Protocol.SHUTDOWN) {
						return;
					}
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
			        this.clientMap.add(clientHashedName, dgram.getAddress().getHostAddress(), filenames);
			        //addToMap(clientHashedName, dgram.getAddress().getHostAddress(), filenames);
			        //}
			        
			        
					if(this.clientMap.getClientMap().size() > 1)
					{
						fileReplicateLocation = new String[filenamesArr.length];
						for (int i = 0; i < filenamesArr.length; i++) {
							int previousNode = 0;
							boolean done = false;

						    Set<Integer> keys = this.clientMap.getClientMap().keySet();
						    Iterator<Integer> itr = keys.iterator();
						    while(itr.hasNext() && done == false)
						    {	
						    	previousNode = itr.next();
						    	//need to set the length to i
						    	//nick
						    	
						    	if((filenamesArr[filenamesArr.length - 1] > previousNode) && (clientHashedName != previousNode ))
							    {
						    		done = true;
							    }
						    }
						    Client c = this.clientMap.getClientMap().get(previousNode);
						    fileReplicateLocation[i] = c.getIpaddress();
						}
					}
					
					System.out.println("hash: " + clientHashedName);
					dgram.setLength(inBuf.length);
					try {
						//notify client about amount of nodes
						String path = "//" + dgram.getAddress().getHostAddress() + "/" + Constants.RMI_SUFFIX_NODE;
						ntnI = null;
						ntnI = (INodeToNode) Naming.lookup(path);
						ntnI.serverAnswer(this.clientMap.getClientMap().size(), fileReplicateLocation);
						System.out.println("Amount of clients: " + this.clientMap.getClientMap().size());
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


	public static void main(String[] argv) throws RemoteException,
			ClassNotFoundException {
		NameServer nameServer = new NameServer();

	}
}