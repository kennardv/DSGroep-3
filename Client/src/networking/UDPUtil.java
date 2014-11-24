package networking;

import enumerations.*;

import java.io.*;
import java.net.*;
import java.util.*;

import be.uantwerpen.server.Client;

public class UDPUtil extends Thread {
	
	private UDPMode mode;
	
	private Object message;
	private InetAddress receiverIP;
	private int port = 4545;
	
	private String multicastIp = "226.100.100.125";
	
	private Protocol sendProtocol;
	private Protocol receiveProtocol;
	
	private Client client;
	
	/**
	 * Listener ctor.
	 * @param client
	 * @param port
	 * @param mode
	 */
	public UDPUtil(Client client, int port, UDPMode mode) {
		this.client = client;
		this.port = port;
		this.mode = mode;
	}
	
	/**
	 * Sender ctor. You must call method createXmessage after constructor
	 * @param client
	 * pass in this
	 * @param receiverIP
	 * @param port
	 * @param mode
	 * UDPUtil.Mode.SEND or UDPUtil.Mode.LISTEN
	 * @param protocol
	 * Discovery, shutdown, ...
	 */
	public UDPUtil(Client client, InetAddress receiverIP, int port, UDPMode mode, Protocol protocol) {
		this.client = client;
		this.receiverIP = receiverIP;
		this.port = port;
		this.mode = mode;
	}
	
	public void run() {
		switch (this.mode) {
		case SEND:
			sendDatagramPacket(this.message, this.receiverIP, this.port);
			break;
		case RECEIVE:
			listenForPackets();
			break;
		default:
			break;
		}
	}
	
	/**
    * Send a datagramPacket
    * @param message
    * Contents to send
    * @param receiverIP
    * Destination address
    * @param port
    * What port to use
    * @return successful or not
    */
    boolean sendDatagramPacket(Object message, InetAddress receiverIP, int port) {
    	//init datatypes
    	boolean sent = false;
    	DatagramSocket socket = null;
    	DatagramPacket dgram;
    	byte[] b = null;
    	
    	//create a socket
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//convert message object to byte array
		try {
			b = objectToByteArr(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//create dgram packet
		dgram = new DatagramPacket(b, b.length, receiverIP, port);
		
		//try to send the packet
		try {
			socket.send(dgram);
			sent = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		//close the socket
		System.out.println("Datagram sent with UDPUtil to " + dgram.getAddress().getHostAddress());
		socket.close();
    	
		//return successful or not
    	return sent;
    }
	
	/**
     * Poll continuously for a discovery message from a new node 
     */
    void listenForPackets() {
		try {
			System.out.println("Listening for packets in UDPUtil");
			byte[] inBuf = new byte[256];
			DatagramPacket dgram = new DatagramPacket(inBuf, inBuf.length);
			MulticastSocket socket = new MulticastSocket(this.port);
			socket.joinGroup(InetAddress.getByName(multicastIp));
			
			//do this forever
			while (true) {
				//blocks untill package is received
				socket.receive(dgram); 
				
				try {
					List<Object> message = readDatagramPacket(dgram);
					this.receiveProtocol = (Protocol) message.get(0);
					int senderHashedName = (int)message.get(1);
					int[] neighbours = null;
					
					//decide what to do depending on protocol
					switch (this.receiveProtocol) {
					case DISCOVERY:
						this.client.updateHashes(senderHashedName, dgram.getAddress().getHostAddress(), neighbours);
						break;
					case SHUTDOWN:
						//System.out.println(shutdown);
						/*if(shutdown == true){
							neighbours = (int[]) message.get(4);
							System.out.println("next = " + neighbours[0]);
							System.out.println("previous = " + neighbours[1]);
						}*/
						break;
					case FAILURE:
						this.client.checkForNTNUpdate((String)message.get(1));
						break;
					default:
						break;
					}
				} finally {
				}
			}
		} catch (IOException e) {
		}
	}
    
    /**
     * Unpack the received datagram packet to a List<Object>
     * @param dgramPacket
     * @return List containing string array with client info, string array with filenames and shutdown boolean
     */
 	List<Object> readDatagramPacket(DatagramPacket dgramPacket) {
	 	byte[] b = null;
	 	b = dgramPacket.getData();
	 	List<Object> obj = null;
	 	try {
			obj = (ArrayList<Object>)byteArrToObject(b);
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	 	return obj;
	}
 	
 	/**
     * Helper function to convert a byte array to an object
     * @param b
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    Object byteArrToObject(byte[] b) throws IOException, ClassNotFoundException {
    	ByteArrayInputStream bis = new ByteArrayInputStream(b);
    	ObjectInput in = null;
    	Object obj = null;
    	try {
    	  in = new ObjectInputStream(bis);
    	  obj = in.readObject(); 
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
    	return obj;
    }
    
    /***
     * Helper function to convert an object to a byte array
     * @param path
     * path to the file
     * @return bFile
     * byte array of the contents of the file
     * @throws IOException 
     */
    byte[] objectToByteArr(Object obj) throws IOException {
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	ObjectOutput out = null;
    	byte[] b = null;
    	try {
    	  out = new ObjectOutputStream(bos);   
    	  out.writeObject(obj);
    	  b = bos.toByteArray();
    	} finally {
    	  try {
    	    if (out != null) {
    	      out.close();
    	    }
    	  } catch (IOException ex) {
    	    // ignore close exception
    	  }
    	  try {
    	    bos.close();
    	  } catch (IOException ex) {
    	    // ignore close exception
    	  }
    	}
    	return b;
    }
    
    /**
     * Create the message that is sent during multicast
     * @param clientInfo
     * containing hashed name and ip address
     * @param filenames
     * array of filenames
     * @param shutdown
     * @return 
     * List<Object>.
     * index 0: Protocol 
     * index 1: clientNameHash
     * index 2: filenames
     */
    public void createDiscoveryMessage(int clientNameHash, int[] filenames) {
     	List<Object> message = new ArrayList<Object>();
     	//message.add(subject[0]);
     	message.add(Protocol.DISCOVERY);
     	message.add(clientNameHash);
     	message.add(filenames);
     	
     	System.out.println("Discovery message created");
     	this.message = message;
     }
    
    public void createFailureMessage(String position) {
     	List<Object> message = new ArrayList<Object>();
     	message.add(Protocol.FAILURE);
     	message.add(position);
     	
     	this.message = message;
     }
    
    public void createShutdownMessage() {
     	List<Object> message = new ArrayList<Object>();
     	message.add(Protocol.SHUTDOWN);
     	//message.add();
     	
     	this.message = message;
     }
}
