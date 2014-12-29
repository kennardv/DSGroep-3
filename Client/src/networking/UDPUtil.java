package networking;

import enumerations.*;

import java.io.*;
import java.net.*;
import java.util.*;

import utils.Toolkit;
import be.uantwerpen.server.Client;
import be.uantwerpen.server.Constants;

public class UDPUtil extends Thread {
	
	private Mode mode;
	
	private Object message;
	private InetAddress receiverIP;
	
	private Protocol sendProtocol;
	private Protocol receiveProtocol;
	
	private Client client;
	
	/**
	 * Listener ctor.
	 * @param client
	 * @param port
	 * @param mode
	 */
	public UDPUtil(Client client, Mode mode) {
		this.client = client;
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
	public UDPUtil(Client client, InetAddress receiverIP, Mode mode, Protocol protocol) {
		this.client = client;
		this.receiverIP = receiverIP;
		this.mode = mode;
	}
	
	public void run() {
		switch (this.mode) {
		case SEND:
			sendDatagramPacket(this.message, this.receiverIP, Constants.SOCKET_PORT_UDP);
			break;
		case RECEIVE:
			listenForPackets(Constants.SOCKET_PORT_UDP);
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
			b = Toolkit.objectToByteArr(message);
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
    void listenForPackets(int port) {
		try {
			System.out.println("Listening for packets in UDPUtil");
			byte[] inBuf = new byte[256];
			DatagramPacket dgram = new DatagramPacket(inBuf, inBuf.length);
			MulticastSocket socket = new MulticastSocket(port);
			socket.joinGroup(InetAddress.getByName(Constants.MULTICAST_IP));
			
			//do this forever
			while (true) {
				//blocks untill package is received
				socket.receive(dgram); 
				
				try {
					List<Object> message = readDatagramPacket(dgram);
					this.receiveProtocol = (Protocol) message.get(0);

					int[] neighbours = null;
					
					//decide what to do depending on protocol
					switch (this.receiveProtocol) {
					case DISCOVERY:
						this.client.updateHashes((int)message.get(1), dgram.getAddress().getHostAddress(), neighbours);
						break;
					case SHUTDOWN:
						System.out.println("Shutdown");
						System.out.println("positie: " + (Position)message.get(1));

						this.client.checkForNTNUpdate((Position)message.get(1));
						break;
					case FAILURE:
						this.client.checkForNTNUpdate((Position)message.get(1));
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
			obj = (ArrayList<Object>)Toolkit.byteArrToObject(b);
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	 	return obj;
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
    
    public void createFailureMessage(Position position) {
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
