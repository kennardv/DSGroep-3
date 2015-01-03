package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.List;

import networking.ReplicaterUtil;
import networking.TCPUtil;
import networking.UDPUtil;
import rmi.implementations.NodeToNode;
import rmi.interfaces.INodeToNode;
import rmi.interfaces.IServerToNode;
import agents.FileListAgent;
import be.uantwerpen.server.Constants;
import enumerations.Mode;
import enumerations.Position;
import model.Client;
import utils.Callback;
import utils.Consolelistener;
import utils.Toolkit;
import view.MainPanel;

/**
 * 
 * @author Kennard
 *
 */
public class MainController {
	
	private Client model;
	private MainPanel view;
	private ActionListener actionListener;
	
	private UDPUtil udpUtilListener = null;
	private Consolelistener conslisten = null;
	
	public MainController(Client model, MainPanel view) {
		this.model = model;
		this.view = view;
		
		this.view.addStartListener(new StartListener());
	}
	
	/**
	 * 
	 * @param username
	 * @throws RemoteException
	 * @throws UnknownHostException
	 * @throws MalformedURLException
	 */
	public void init(String username) throws RemoteException, UnknownHostException, MalformedURLException {
		if (!this.model.useLocalHost) {
			model.setMyIPAddress(Inet4Address.getLocalHost().getHostAddress());
		} else {
			model.setMyIPAddress("localhost");
		}

		///////////// INIT VARIABLES HERE /////////////
		//create registry if it doesn't exist yet
		try {
			model.setRegistry(LocateRegistry.createRegistry(1099));
		} catch (RemoteException e) {
		}
		//lookup server remote object
		try {
			Constants.ISERVER_TO_NODE = (IServerToNode) Naming.lookup(Constants.SERVER_PATH_RMI);
			//stnI = (IServerToNode) Naming.lookup(Constants.SERVER_PATH_RMI);
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		model.setNtn(new NodeToNode());

		//Give client a name from console input
		model.setNameClient(username);
        //this.nameClient = readFromConsole("(UNIQUE NAMES) Please enter client name: ");
		//set own to hashed own name
		model.setCurrentHash(Toolkit.hashString(model.getNameClient()));

		//get all file paths
		model.setFiles(Toolkit.listFilesInDir(Constants.MY_FILES_PATH));

		//this.filenames = new int[this.files.size()];
		int[] tmp = new int[model.getFiles().size()];
		for (int i = 0; i< model.getFiles().size(); i++) {
			tmp[i] = Toolkit.hashString(model.getFiles().get(i).getName());
		}
		model.setFilenames(tmp);
		
		/***********************************************************************************/
		//bind remote object
		bootstrap();
		//multicast and process answers
		discover(InetAddress.getByName(Constants.MULTICAST_IP), Constants.SOCKET_PORT_UDP);

		

	    //listen for packets
		this.udpUtilListener = new UDPUtil(this, Mode.RECEIVE);
		Thread t = new Thread(this.udpUtilListener);
		t.start();

		this.conslisten = new Consolelistener(this, model.getCurrentHash());
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

		model.setRmiBindLocation(Toolkit.createBindLocation(InetAddress.getLocalHost().getHostAddress(), Constants.SUFFIX_NODE_RMI));
		bindRemoteObject(model.getRmiBindLocation(), model.getNtn());
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
		NodeToNode ntn = model.getNtn();
		
		//create message and multicast it
		UDPUtil udpUtil = new UDPUtil(this, ip, Mode.SEND);
		udpUtil.createDiscoveryMessage(model.getCurrentHash(), model.getFilenames());
		Thread t = new Thread(udpUtil);
		t.start();

		while ((ntn.nextHash() == -1 || ntn.numberOfNodes() == -1))
		{
			System.out.println("Waiting, next hash: "+ntn.nextHash() + " # of nodes: " + ntn.numberOfNodes());
			
			//if there are no neighbour nodes 
			if (ntn.numberOfNodes() == 1)
			{
				System.out.println("No neighbours! All hashes set to own");
				//set next and previous hash equal to own hash
				ntn.setNextHash(model.getCurrentHash());
				ntn.setPreviousHash(model.getCurrentHash());
			} else if (ntn.numberOfNodes() > 1) {
				System.out.println(ntn.numberOfNodes() + " neighbours. Setting hashes to hashes from previous node.");
				model.setNextHash(ntn.nextHash());
				model.setPreviousHash(ntn.previousHash());
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
		model.setNextHash(ntn.nextHash());
		model.setPreviousHash(ntn.previousHash());
		//replicate files
		System.out.println("replication start");
		Callback callback = new Callback(this, "failure");
		ReplicaterUtil replicaterUtil = new ReplicaterUtil(ntn, model.getMyIPAddress(), model.getCurrentHash(), callback);
	    replicaterUtil.replicate(model.getFileReplicateList(), model.getFiles() );
		System.out.println("Hashes: Previous: " + model.getPreviousHash() + ". Own: " + model.getCurrentHash() + ". Next: " + model.getNextHash());

		//Agent initialization
		if(ntn.numberOfNodes() == 2){
			System.out.println("Start file list agent");
			model.setFileListAgent(new FileListAgent(model.getCurrentHash(), Constants.SERVER_PATH_RMI));
			model.getNtn().startFileListAgent(model.getFileListAgent(), model.getCurrentHash(), Constants.SUFFIX_NODE_RMI);
 		}

		//unbind object from location
		if (model.useLocalHost) {
			unbindRemoteObject(model.getRmiBindLocation());
		}
	}

	/**
	 * NOT DONE
	 */
	void replicate() {
		//get files to replicate
		if (model.getFileReplicateList() == null) {
			return;
		}
		model.setFileReplicateList(model.getNtn().replicationAnswer());
		for( int i = 0; i< model.getFileReplicateList().length; i++ )
		{
			String name = Toolkit.createBindLocation(model.getFileReplicateList()[i], Constants.SUFFIX_NODE_RMI);
			try {
				TCPUtil tcpSender = new TCPUtil(null, Mode.SEND, model.getFiles().get(i), null);
				Thread t = new Thread(tcpSender);
				t.start();
				INodeToNode ntnI = (INodeToNode) Naming.lookup(name);
				ntnI.startReceive(model.getMyIPAddress(), model.getFiles().get(i).getName());
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
		udpUtilPrevious = new UDPUtil(this, inetAddressPrevious, Mode.SEND);
		udpUtilNext = new UDPUtil(this, inetAddressNext, Mode.SEND);
		udpUtilPrevious.createFailureMessage(Position.PREVIOUS);
		udpUtilNext.createFailureMessage(Position.NEXT);
		Thread t1 = new Thread(udpUtilPrevious);
		t1.start();
		Thread t2 = new Thread(udpUtilNext);
		t2.start();

		try {
			//update previous node's next hash
			model.setNtnI((INodeToNode) Naming.lookup(previousPath));
			model.getNtnI().updateNextHash(neighbourHashes[1]);

			//update next node's previous hash
			model.setNtnI((INodeToNode) Naming.lookup(nextPath));
			model.getNtnI().updatePreviousHash(neighbourHashes[0]);

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
			Constants.ISERVER_TO_NODE.removeNode(model.getCurrentHash());
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
		udpUtilPrevious = new UDPUtil(this, inetAddressPrevious, Mode.SEND);
		udpUtilNext = new UDPUtil(this, inetAddressNext, Mode.SEND);
		udpUtilPrevious.createShutdownMessage(Position.PREVIOUS);
		udpUtilNext.createShutdownMessage(Position.NEXT);
		Thread t1 = new Thread(udpUtilPrevious);
		t1.start();
		Thread t2 = new Thread(udpUtilNext);
		t2.start();

		try {
			//update previous node's next hash
			System.out.println("prevpath:" + previousPath);
			model.setNtnI((INodeToNode) Naming.lookup(previousPath));
			model.getNtnI().updateNextHash(neighbourHashes[1]);

			//update next node's previous hash
			model.setNtnI((INodeToNode) Naming.lookup(nextPath));
			model.getNtnI().updatePreviousHash(neighbourHashes[0]);

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
			while(model.getNtn().nextHash() == -1){
				
			}
			model.setNextHash(model.getNtn().nextHash());
		} else if(position == Position.NEXT) {
			//wait until property is updated
			while(model.getNtn().nextHash() == -1){
				
			}
			model.setPreviousHash(model.getNtn().previousHash());
		}

		try {
			//wait 100 ms
			Thread.sleep(100);	
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		System.out.println("checkForNTNUpdate hashes set : Previous: " + model.getPreviousHash() + ". Current: " + model.getCurrentHash() + ". Next: " + model.getNextHash());
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
			model.setNtnI((INodeToNode) Naming.lookup(name));
			
			//I am the only node -- SPECIAL CASE FOR FIRST NODE
			if (model.getPreviousHash() == model.getCurrentHash() && model.getNextHash() == model.getCurrentHash()) {
				//set all hashes to my own because i'm the only node
				model.setNextHash(receivedHash);
				model.setPreviousHash(receivedHash);
				model.getNtnI().answerDiscovery(model.getCurrentHash(), model.getCurrentHash());
			}
			//I am the previous node
			if (receivedHash > model.getCurrentHash()) {
				//I am the largest node
				if (receivedHash < model.getNextHash() || ((receivedHash > model.getCurrentHash()) && (model.getCurrentHash() > model.getNextHash()))) {
					//after this I'm not the largest anymore, I sit in between, the new node becomes the largest
					int tmp = model.getNextHash();
					model.setNextHash(receivedHash);
					model.getNtnI().answerDiscovery(model.getCurrentHash(), tmp);
				}
				//I am the lowest node
				else if((receivedHash > model.getPreviousHash()) && (model.getPreviousHash() >= model.getNextHash())) {
					//set my previous to the largest -> smallest connects to largest
					model.setPreviousHash(receivedHash);
				}
			}
			//I am the next node
			else if ((receivedHash > model.getPreviousHash()) && (receivedHash < model.getCurrentHash())) {
				//something bigger than my previous hash but smaller than me came in
				model.setPreviousHash(receivedHash);
			}
			
			System.out.println("waitForClients hashes set : Previous: " + model.getPreviousHash() + ". Current: " + model.getCurrentHash() + ". Next: " + model.getNextHash());

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
	
	/**
	 * 
	 * @author Kennard
	 *
	 */
	class StartListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String username = null;
			try {
				username = view.getName();
				if (username.trim().isEmpty() || username.equals("")) {
					throw new IllegalArgumentException("Input can't be empty!");
				}
			} catch (IllegalArgumentException ex) {
				view.setConnectionStatus(ex.getMessage());
			}
			
			try {
				init(username);
				view.setConnectionStatus("Connected");
			} catch (RemoteException | UnknownHostException | MalformedURLException e1) {
				view.setConnectionStatus("Connection failed!");
			}
		}
	}
}
