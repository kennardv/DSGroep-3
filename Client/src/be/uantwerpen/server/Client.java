package be.uantwerpen.server;

import enumerations.*;
import networking.*;
import rmi.implementations.*;
import rmi.interfaces.*;
import utils.Callback;
import utils.Consolelistener;
import utils.Toolkit;

import java.net.*;
import java.net.UnknownHostException;
import java.io.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

import agents.FileListAgent;

public class Client {

	/************* Set this for lonely testing ******************/
	/************************************************************/
	/************************************************************/
	boolean useLocalHost = true;
	/************************************************************/
	/************************************************************/
	/************************************************************/


	//Info
	String nameClient = null;
	List<File> files = null;
	int[] filenames = null;

	//my hashes
	private int previousHash, currentHash, nextHash;

	//RMI vars
	private Registry registry = null;
	private NodeToNode ntn = null;
	private INodeToNode ntnI = null;
	//private IServerToNode stnI = null;
	private String rmiBindLocation = null;

	//TCP vars
	private String multicastAddress = null;

	private String myIPAddress = null;

	private Protocol sendProtocol;
	private Protocol receiveProtocol;

	//UDP vars
	private UDPUtil udpUtilListener = null;
	private Consolelistener conslisten;

	//file replication
	public String[] fileReplicateList = null;

	//Agents
	private FileListAgent fileListAgent = null;


	//ctor
	public Client() throws RemoteException, InterruptedException, IOException, ClassNotFoundException {
		if (!useLocalHost) {
			myIPAddress = Inet4Address.getLocalHost().getHostAddress();
		} else {
			myIPAddress = "localhost";
		}

		///////////// INIT VARIABLES HERE /////////////
		//create registry if it doesn't exist yet
		try {
			registry = LocateRegistry.createRegistry(1099);
		} catch (RemoteException e) {
		}
		//lookup server remote object
		//serverPath = Toolkit.createBindLocation(serverIp, this.rmiSuffixServer);
		try {
			System.out.println(Constants.SERVER_PATH_RMI);
			System.out.println(myIPAddress);
			Constants.ISERVER_TO_NODE = (IServerToNode) Naming.lookup(Constants.SERVER_PATH_RMI);
			//stnI = (IServerToNode) Naming.lookup(Constants.SERVER_PATH_RMI);
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		this.ntn = new NodeToNode();

		//Give client a name from console input
        this.nameClient = readFromConsole("(UNIQUE NAMES) Please enter client name: ");
		//set own to hashed own name
		this.currentHash = Toolkit.hashString(this.nameClient);

		//get all file paths
		this.files = Toolkit.listFilesInDir(Constants.MY_FILES_PATH);

		this.filenames = new int[this.files.size()];
		for (int i = 0; i< files.size(); i++) {
			this.filenames[i] = Toolkit.hashString(this.files.get(i).getName());
		}

		//bind remote object
		bootstrap();
		//multicast and process answers
		discover(InetAddress.getByName(Constants.MULTICAST_IP), Constants.SOCKET_PORT_UDP);

		//replicate files
		Callback callback = new Callback(this, "failure");
		ReplicaterUtil replicaterUtil = new ReplicaterUtil(ntn, this.myIPAddress, this.currentHash, callback);
	    replicaterUtil.replicate(fileReplicateList, files );

	    //listen for packets
		this.udpUtilListener = new UDPUtil(this, Mode.RECEIVE);
		Thread t = new Thread(this.udpUtilListener);
		t.start();

		this.conslisten = new Consolelistener(this, this.currentHash);
		Thread t2 = new Thread(this.conslisten);
		t2.start();
	}

	/**
	 * Bind a remote object
	 * @return remote object's bind location
	 * @throws UnknownHostException 
	 */
	void bootstrap() throws UnknownHostException {
		//bind remote object at location

		this.rmiBindLocation = Toolkit.createBindLocation(InetAddress.getLocalHost().getHostAddress(), Constants.SUFFIX_NODE_RMI);
		bindRemoteObject(this.rmiBindLocation, this.ntn);
	}

	/**
	 * Send a discovery message to the nameserver and all nodes
	 * @param message
	 * Contains my info
	 * @param ip
	 * Multicast address?
	 * @param port
	 * Port to send on
	 */
	void discover(InetAddress ip, int port) {
		//fill array with info
		//List<Object> message = createDiscoveryMessage(this.currentHash, this.filenames);

		//create message and multicast it
		UDPUtil udpUtil = new UDPUtil(this, ip, Mode.SEND, Protocol.DISCOVERY);
		udpUtil.createDiscoveryMessage(this.currentHash, this.filenames);
		Thread t = new Thread(udpUtil);
		t.start();
		//sendDatagramPacket(message, ip, port);

		while ((ntn.nextHash() == -1 || ntn.numberOfNodes() == -1))
		{
			System.out.println("Waiting, next hash: "+ntn.nextHash() + " # of nodes: " + ntn.numberOfNodes());
			
			//if there are no neighbour nodes 
			if (ntn.numberOfNodes() == 1)
			{
				System.out.println("No neighbours! All hashes set to own");
				//set next and previous hash equal to own hash
				ntn.setNextHash(this.currentHash);
				ntn.setPreviousHash(this.currentHash);
			} else if (ntn.numberOfNodes() > 1) {
				System.out.println(ntn.numberOfNodes() + " neighbours. Setting hashes to hashes from previous node.");
				this.nextHash = ntn.nextHash();
				this.previousHash = ntn.previousHash();
			}
			try {
				//wait 100 ms
				Thread.sleep(100);
				
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}

		System.out.println("Total connected clients: " + (ntn.numberOfNodes())); //waarom +1?

		//set client's hash fields
		this.nextHash = ntn.nextHash();
		this.previousHash = ntn.previousHash();
		System.out.println("Hashes: Previous: " + this.previousHash + ". Own: " + this.currentHash + ". Next: " + this.nextHash);

		//Agent initialization
		/*if(ntn.numberOfNodes() == 2){
			System.out.println("Start file list agent");
			this.fileListAgent = new FileListAgent(this.currentHash, Constants.SERVER_PATH_RMI);
			this.ntn.startFileListAgent(this.fileListAgent, this.stnI, this.currentHash, Constants.SUFFIX_NODE_RMI);
 		}*/

		//unbind object from location
		if (useLocalHost) {
			unbindRemoteObject(this.rmiBindLocation);
		}
	}

	/**
	 * NOT DONE
	 */
	void replicate() {
		//get files to replicate
		if (fileReplicateList == null) {
			return;
		}
		fileReplicateList = ntn.replicationAnswer();
		for( int i = 0; i< fileReplicateList.length; i++ )
		{
			String name = Toolkit.createBindLocation(fileReplicateList[i], Constants.SUFFIX_NODE_RMI);
			try {
				TCPUtil tcpSender = new TCPUtil(null, Mode.SEND, files.get(i), null);
				Thread t = new Thread(tcpSender);
				t.start();
				INodeToNode ntnI = (INodeToNode) Naming.lookup(name);
				ntnI.startReceive(myIPAddress, files.get(i).getName());
				t.join();
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}
	}

	void failure(int hash){
		//variables
		int[] neighbourHashes = null;
		String previousPath = null;
		String nextPath = null;
		String previousIP = null;
		String nextIP = null;
		List<Object> messagePreviousNode = null;
		List<Object> messageNextNode = null;

		UDPUtil udpUtilPrevious = null;
		UDPUtil udpUtilNext = null;

		try {
			//get previous and next node of failing node
			neighbourHashes = Constants.ISERVER_TO_NODE.getPreviousAndNextNodeHash(hash); //stnI.getPreviousAndNextNodeHash(hash);
			//compute paths for nodes to update
			previousPath = Toolkit.createBindLocation(Constants.ISERVER_TO_NODE.getNodeIPAddress(neighbourHashes[0]), Constants.SUFFIX_NODE_RMI);
			nextPath = Toolkit.createBindLocation(Constants.ISERVER_TO_NODE.getNodeIPAddress(neighbourHashes[1]), Constants.SUFFIX_NODE_RMI);

			//get ip of neighbour nodes
			previousIP = Constants.ISERVER_TO_NODE.getNodeIPAddress(neighbourHashes[0]);
			nextIP = Constants.ISERVER_TO_NODE.getNodeIPAddress(neighbourHashes[1]);

		} catch (RemoteException e) {
			e.printStackTrace();
		}

		InetAddress inetAddressPrevious = null;
		InetAddress inetAddressNext = null;
		try {
			//create inetAddress vars
			inetAddressPrevious = InetAddress.getByName(previousIP);
			inetAddressNext = InetAddress.getByName(nextIP);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		//create failure messages
		//send message to previous and next neighbour
		udpUtilPrevious = new UDPUtil(this, inetAddressPrevious, Mode.SEND, Protocol.FAILURE);
		udpUtilNext = new UDPUtil(this, inetAddressNext, Mode.SEND, Protocol.FAILURE);
		udpUtilPrevious.createFailureMessage(Position.PREVIOUS);
		udpUtilNext.createFailureMessage(Position.NEXT);
		Thread t1 = new Thread(udpUtilPrevious);
		t1.start();
		Thread t2 = new Thread(udpUtilNext);
		t2.start();

		try {
			//update previous node's next hash
			ntnI = (INodeToNode) Naming.lookup(previousPath);
			ntnI.updateNextHash(neighbourHashes[1]);

			//update next node's previous hash
			ntnI = (INodeToNode) Naming.lookup(nextPath);
			ntnI.updatePreviousHash(neighbourHashes[0]);

		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}

		try {
			//remove node from server
			Constants.ISERVER_TO_NODE.removeNode(hash);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		//pingen naar gefailde hash
		InetAddress host = null;
		try {
			try {
				host = InetAddress.getByName(Constants.ISERVER_TO_NODE.getNodeIPAddress(hash));
				System.out.println(InetAddress.getByName(Constants.ISERVER_TO_NODE.getNodeIPAddress(hash)));
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			System.out.println("host.isReachable(1000) = " + host.isReachable(1000));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	void failure(){
		//variables

		try {
			//remove node from server
			Constants.ISERVER_TO_NODE.removeNode(this.currentHash);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

    public void shutdown(int hash){
        System.out.println("Shutting down..");

		//variables
		int[] neighbourHashes = null;
		String previousPath = null;
		String nextPath = null;
		String previousIP = null;
		String nextIP = null;

		UDPUtil udpUtilPrevious = null;
		UDPUtil udpUtilNext = null;

		try {
			System.out.println("Getting previous and next node of node that's shutting down");
			//get previous and next node of failing node
			neighbourHashes = Constants.ISERVER_TO_NODE.getPreviousAndNextNodeHash(hash);
			//compute paths for nodes to update
			previousPath = Toolkit.createBindLocation(Constants.ISERVER_TO_NODE.getNodeIPAddress(neighbourHashes[0]), Constants.SUFFIX_NODE_RMI);
			nextPath = Toolkit.createBindLocation(Constants.ISERVER_TO_NODE.getNodeIPAddress(neighbourHashes[1]), Constants.SUFFIX_NODE_RMI);

			//get ip of neighbour nodes
			previousIP = Constants.ISERVER_TO_NODE.getNodeIPAddress(neighbourHashes[0]);
			nextIP = Constants.ISERVER_TO_NODE.getNodeIPAddress(neighbourHashes[1]);

		} catch (RemoteException e) {
			e.printStackTrace();
		}

		InetAddress inetAddressPrevious = null;
		InetAddress inetAddressNext = null;
		try {
			//create inetAddress vars
			inetAddressPrevious = InetAddress.getByName(previousIP);
			inetAddressNext = InetAddress.getByName(nextIP);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		//create shutdown messages
		//send message to previous and next neighbour
        System.out.println("Sending message to previous and next neighbour");
		udpUtilPrevious = new UDPUtil(this, inetAddressPrevious, Mode.SEND, Protocol.SHUTDOWN);
		udpUtilNext = new UDPUtil(this, inetAddressNext, Mode.SEND, Protocol.SHUTDOWN);
		udpUtilPrevious.createShutdownMessage(Position.PREVIOUS);
		udpUtilNext.createShutdownMessage(Position.NEXT);
		Thread t1 = new Thread(udpUtilPrevious);
		t1.start();
		Thread t2 = new Thread(udpUtilNext);
		t2.start();

		try {
			//update previous node's next hash
			System.out.println("prevpath:" + previousPath);
			ntnI = (INodeToNode) Naming.lookup(previousPath);
			ntnI.updateNextHash(neighbourHashes[1]);

			//update next node's previous hash
			ntnI = (INodeToNode) Naming.lookup(nextPath);
			ntnI.updatePreviousHash(neighbourHashes[0]);

		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}

		try {
			System.out.println("Removing node from nameserver");
			//remove node from server
			Constants.ISERVER_TO_NODE.removeNode(hash);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

        System.out.println("Closing client");
        System.exit(1);
	}

    public void checkForNTNUpdate(Position position) {
		if (position == Position.PREVIOUS){
			//wait until property is updated
			while(ntn.nextHash() == -1){
				
			}
			this.nextHash = ntn.nextHash();
		} else if(position == Position.NEXT) {
			//wait until property is updated
			while(ntn.nextHash() == -1){
				
			}
			this.previousHash = ntn.previousHash();
		}

		try {
			//wait 100 ms
			Thread.sleep(100);	
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		System.out.println("checkForNTNUpdate hashes set : Previous: " + this.previousHash + ". Current: " + this.currentHash + ". Next: " + this.nextHash);
	}

	/**
	 * Algorithm to decide about my hashes, and inform the sender of the discovery message
	 * @param receivedHash
	 * Discovery sender's hashed name
	 * @param receivedIPAddress
	 * used to create bind location for remote object
	 * @param neighbours
	 */
	public void updateHashes(int receivedHash, String receivedIPAddress, int[] neighbours) {
		try {
			String name = Toolkit.createBindLocation(receivedIPAddress, Constants.SUFFIX_NODE_RMI);
			ntnI = (INodeToNode) Naming.lookup(name);
			
			//I am the only node -- SPECIAL CASE FOR FIRST NODE
			if (this.previousHash == this.currentHash && this.nextHash == this.currentHash) {
				//set all hashes to my own because i'm the only node
				this.nextHash = receivedHash;
				this.previousHash = receivedHash;
				ntnI.answerDiscovery(this.currentHash, this.currentHash);
			}
			//I am the previous node
			if (receivedHash > this.currentHash) {
				//I am the largest node
				if (receivedHash < this.nextHash || ((receivedHash > this.currentHash) && (this.currentHash > this.nextHash))) {
					//after this I'm not the largest anymore, I sit in between, the new node becomes the largest
					int tmp = this.nextHash;
					this.nextHash = receivedHash;
					ntnI.answerDiscovery(this.currentHash, tmp);
				}
				//I am the lowest node
				else if((receivedHash > this.previousHash) && (this.previousHash >= this.nextHash)) {
					//set my previous to the largest -> smallest connects to largest
					this.previousHash = receivedHash;
				}
			}
			//I am the next node
			else if ((receivedHash > this.previousHash) && (receivedHash < this.currentHash)) {
				//something bigger than my previous hash but smaller than me came in
				this.previousHash = receivedHash;
			}
			
			System.out.println("waitForClients hashes set : Previous: " + this.previousHash + ". Current: " + this.currentHash + ". Next: " + this.nextHash);

			//////////////////////////////////////////////////////////////////////////
			//////////////////////////// LEFTOVER OLD CODE ///////////////////////////
			//////////////////////////////////////////////////////////////////////////
			/*if(neighbours != null){
				if(nextHash == receivedHash){
			        //System.out.println("Changing next node from " + nextHash + " to " + neighbours[0]);
					nextHash = neighbours[0];
				}
				else if(previousHash == receivedHash){
					//System.out.println("Changing next node from " + previousHash + " to " + neighbours[1]);
					previousHash = neighbours[1];
				}
				System.out.println(previousHash + " "  + ownHash + " " + nextHash);
			}*/
			//////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////

		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			failure(receivedHash);
			e.printStackTrace();
		}
	}

    /////////////// UTILITY METHODS ///////////////

	/**
     * Bind the specified object to a location
     * @param path
     * @param ntn
     * Remote object to bind
     */
    void bindRemoteObject(String path, NodeToNode ntn) {
    	try {
			Naming.bind(path, ntn);
		} catch (MalformedURLException | RemoteException
				| AlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * Unbind remote object at specified location
     * @param path
     */
    void unbindRemoteObject(String path) {
    	try {
			Naming.unbind(path);
		} catch (RemoteException | MalformedURLException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * This method blocks until console receives input
     */
    String readFromConsole(String message) {
    	/******************************************/
		/* enter client name in console and enter */
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(message);
        String str = null;
        try {
        	
        	str = reader.readLine().toUpperCase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /* end console input */
        /******************************************/
        return str;
    }

	public static void main(String argv[]) throws InterruptedException, IOException, ClassNotFoundException {
		Client client = new Client();
		
	}
}
