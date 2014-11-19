package be.uantwerpen.server;

import java.net.*;
import java.net.UnknownHostException;
import java.io.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;



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
	private NodeToNodeInterface ntnI = null;
	private ServerToNodeInterface stnI = null;
	private String rmiBindLocation = null;
	private String[] clientStats = new String[2];
	
	//TCP vars
	private String multicastAddress = null;
	private int socketPort = 4545;
	
	String myIPAddress = null;
	String multicastIp = "226.100.100.125";
	String serverIp = "192.168.1.1";
	private Protocol sendProtocol;
	private Protocol receiveProtocol;
	
	public Client client;
	public String[] fileReplicateList = null;
	
	HashMap<File, Boolean> allFiles = new HashMap<File, Boolean>();
	
	String myFilesFolderName = "myfiles";
	
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
		
		this.ntn = new NodeToNode();
		
		//Give client a name from console input
        this.nameClient = readFromConsole("(UNIQUE NAMES) Please enter client name: ");
		//set own to hashed own name
		this.currentHash = hashString(this.nameClient);
		
		//get all file paths
		this.files = listFilesInDir("C:\\Users");
		
		this.filenames = new int[this.files.size()];
		for (int i = 0; i< files.size(); i++) {
			this.filenames[i] = hashString(this.files.get(i).getName());
		}
		
		///////////////////////////////////////////////
		
		
		//bind remote object
		bootstrap(this.myIPAddress);
		//multicast and process answers
		discover(InetAddress.getByName(multicastIp), socketPort);
		//REPLICATE FILES NOT DONE
		replicate();
	    
	    listenForPackets();
	}
	
	/**
	 * Bind a remote object
	 * @param ip
	 * @return remote object's bind location
	 */
	void bootstrap(String ip) {
		//bind remote object at location
		this.rmiBindLocation = createBindLocation(ip);
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
		List<Object> message = createDiscoveryMessage(this.currentHash, filenames);
		
		//create message and multicast it
		sendDatagramPacket(message, ip, port);
		
		//NS or other nodes answering on remote object
		//keep looping as long as nextHash isn't changed or number of nodes isn't changed
		while (ntn.nextHash() == -1 || ntn.numberOfNodes() == -1)
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
		
		//unbind object from location
		if (useLocalHost) {
			unbindRemoteObject(this.rmiBindLocation);
		}
	}
	
	/**
	 * NOT DONE
	 */
	void replicate() {
		fileReplicateList = ntn.replicationAnswer();
		for( int i = 0; i< fileReplicateList.length; i++ )
		{
			String name = "//" + clientStats[1] + "/ntn";
			try {
				TCPUtil tcpSender = new TCPUtil(null, 20000, TCPUtil.Mode.SEND, files.get(i), null);
				Thread t = new Thread(tcpSender);
				t.start();
				NodeToNodeInterface ntnI = (NodeToNodeInterface) Naming.lookup(name);
				ntnI.getReceiverIp(fileReplicateList[i], 20000, files.get(i).getName());
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
		
		try {
			//get previous and next node of failing node
			neighbourHashes = stnI.getPreviousAndNextNodeHash(hash);
			//compute paths for nodes to update
			previousPath = createBindLocation(stnI.getNodeIPAddress(neighbourHashes[0]));
			nextPath = createBindLocation(stnI.getNodeIPAddress(neighbourHashes[1]));
			
			//get ip of neighbour nodes
			previousIP = stnI.getNodeIPAddress(neighbourHashes[0]);
			nextIP = stnI.getNodeIPAddress(neighbourHashes[1]);
			
			//create failure messages
			messagePreviousNode = createFailureMessage("previous");
			messageNextNode = createFailureMessage("next");
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
		
		//send message to previous and next neighbour
		sendDatagramPacket(messagePreviousNode, inetAddressPrevious, this.socketPort);
		sendDatagramPacket(messageNextNode, inetAddressNext, this.socketPort);
		
		try {
			//update previous node's next hash
			ntnI = (NodeToNodeInterface) Naming.lookup(previousPath);
			ntnI.updateNextHash(neighbourHashes[1]);
			
			//update next node's previous hash
			ntnI = (NodeToNodeInterface) Naming.lookup(nextPath);
			ntnI.updatePreviousHash(neighbourHashes[0]);
			
			//lookup server remote object
			String serverPath = createBindLocation(serverIp);
			stnI = (ServerToNodeInterface) Naming.lookup(serverPath);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
		

		try {
			//remove node from server
			stnI.removeNode(hash);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
    public void shutdown(List<Object> message) throws IOException {
        System.out.println("Shutting down..");

        ntn.decreaseNumberOfNodes(1);
        Boolean shutdown = true;
        int[] neighbours = {nextHash, previousHash};
        
        System.out.println("Sending Multicast");
        //create message and multicast it
        Object obj = message; 
        message.remove(2);
        message.add(2, shutdown);
        message.add(3, neighbours);
        DatagramSocket socket = new DatagramSocket();
        ByteArrayOutputStream byteArr = new ByteArrayOutputStream();
        ObjectOutput objOut = new ObjectOutputStream(byteArr);
        objOut.writeObject(obj);
        System.out.println("Object written");
        byte[] b = byteArr.toByteArray();
        DatagramPacket dgram;
        dgram = new DatagramPacket(b, b.length, InetAddress.getByName(multicastIp), socketPort);

        socket.send(dgram);
        System.out.println("send");
        
        System.out.println("Closing client");
        System.exit(1);
	}

    /**
     * Poll continuously for a discovery message from a new node 
     */
    void listenForPackets() {
		try {
			byte[] inBuf = new byte[256];
			DatagramPacket dgram = new DatagramPacket(inBuf, inBuf.length);
			MulticastSocket socket = new MulticastSocket(socketPort);
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
					switch (this.sendProtocol) {
					case DISCOVERY:
						updateHashes(senderHashedName, dgram.getAddress().getHostAddress(), neighbours);
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
						checkForNTNUpdate((String)message.get(1));
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
    
    private void checkForNTNUpdate(String position) {
		if (position.equals("previous")){
			//wait untill property is updated
			while(ntn.nextHash() == -1){
				
			}
			this.nextHash = ntn.nextHash();
		} else if(position.equals("next"))
		{
			//wait untill property is updated
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
	void updateHashes(int receivedHash, String receivedIPAddress, int[] neighbours) {
		try {
			String name = createBindLocation(receivedIPAddress);
			ntnI = (NodeToNodeInterface) Naming.lookup(name);
			
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
		System.out.println("Datagram sent to " + dgram.getAddress().getHostAddress());
		socket.close();
    	
		//return successful or not
    	return sent;
    }

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
    
    /////////////// UTILITY METHODS ///////////////
    
    /**
     * Create the message that is sent during multicast
     * @param clientInfo
     * containing hashed name and ip address
     * @param filenames
     * array of filenames
     * @param shutdown
     * @return 
     */
    List<Object> createDiscoveryMessage(int clientNameHash, int[] filenames) {
     	List<Object> message = new ArrayList<Object>();
     	//message.add(subject[0]);
     	message.add(Protocol.DISCOVERY);
     	message.add(clientNameHash);
     	message.add(filenames);
     	
     	return message;
     }
    
    List<Object> createFailureMessage(String position) {
     	List<Object> message = new ArrayList<Object>();
     	message.add(Protocol.FAILURE);
     	message.add(position);
     	
     	return message;
     }
    
    List<Object> createShutdownMessage() {
     	List<Object> message = new ArrayList<Object>();
     	message.add(Protocol.SHUTDOWN);
     	//message.add();
     	
     	return message;
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
     * List all the files under a directory
     * @param directoryName to be listed
     */
    public List<File> listFilesInDir(String directoryName){
    	 File[] f = new File(directoryName).listFiles();
    	 List<File> files = new ArrayList<File>();
    	 for (int i = 0; i < f.length; i++) {
    		 if (f[i].isFile()) {
				files.add(f[i]);
			}
    	 }
         return files;
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
    
    /**
     * Helper function to convert the contents of a file to a byte array
     * @param path
     * path to the file
     * @return bFile
     * byte array of the contents of the file
     */
    byte[] fileToByteArr(File f) {
    	FileInputStream fis = null;
    	 
        File file = f;
 
        byte[] bFile = new byte[(int) file.length()];
 
        try {
            //convert file into array of bytes
		    fis = new FileInputStream(file);
		    fis.read(bFile);
		    fis.close();
		    
		    System.out.println("Contents of byte array.");
		    for (int i = 0; i < bFile.length; i++) {
		    	System.out.print((char)bFile[i]);
            }
 
		    System.out.println("Done");
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        return bFile;
    }
    
    /**
     * This method blocks untill console receives input
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
     * Helper method to convert a string to a hash. Range goes from 0 to 32768.
     * @param name
     * String to be hashed
     * @return Returns the hashed inputted string.
     */
    int hashString(String name) {
		return Math.abs(name.hashCode()) % 32768; // berekening van de hash
	}
    
    /**
     * Create a correctly formated location string
     * @param name
     * @return formated location string
     */
    String createBindLocation(String name) {
    	return "//" + name + "/ntn";
    }
    
	public static void main(String argv[]) throws InterruptedException, IOException, ClassNotFoundException {
		Client client = new Client();
		
	}
	

}
