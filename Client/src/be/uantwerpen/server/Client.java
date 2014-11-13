package be.uantwerpen.server;

import java.net.*;
import java.net.UnknownHostException;
import java.io.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class Client {
	
	/************* Set this for lonely testing ******************/
	/************************************************************/
	/************************************************************/
	boolean useLocalHost = true;
	/************************************************************/
	/************************************************************/
	/************************************************************/
	
	String serverIp = "226.100.100.125";
	int port = 4545; 
	
	//Client client;
	public int previousHash, ownHash, nextHash; //declaratie van de type hashes
	public NodeToNode ntn; //declaratie van remote object
	NodeToNodeInterface ntnI = null;
	ServerToNodeInterface stvI = null;
	public Registry registry = null;
	public Client client;
	
	HashMap<File, Boolean> allFiles = new HashMap<File, Boolean>();
	
	String myFilesFolderName = "myfiles";
	List<File> myFiles = null;

	
	String ipaddress = null;
	
	public Client() throws RemoteException, InterruptedException, IOException, ClassNotFoundException {
		if (!useLocalHost) {
			ipaddress = Inet4Address.getLocalHost().getHostAddress();
		} else {
			ipaddress = "localhost";
		}
		
		///////////// INIT VARIABLES HERE /////////////
		
		//create registry if it doesn't exist yet
		try {
			registry = LocateRegistry.createRegistry(1099);
		} catch (RemoteException e) {
			
		}
		
		ntn = new NodeToNode();
		myFiles = listFilesInDir("C:\\Users");
		
		///////////////////////////////////////////////
		
		//Give client a name from console input
        String nameClient = readFromConsole("Please enter client name: ");
        
        //get all file paths
		String[] filenames = new String[myFiles.size()];
		for (int i = 0; i< myFiles.size(); i++) {
			filenames[i] = myFiles.get(i).getName();
		}
		//send TCP and receive TCP test
		String option = readFromConsole("Send, receive or just continue? (S/R/C)");
		if (option.equals("S")) {
			TCPUtil tcpSender = new TCPUtil(null, 20000, true, myFiles.get(0));
			Thread t = new Thread(tcpSender);
			t.start();
		} else if(option.equals("R")) {
			TCPUtil tcpReceiver = new TCPUtil("127.0.0.1", 20000, false, null);
			Thread t = new Thread(tcpReceiver);
			t.start();
		} else if(option.equals("C")) {
			
		}
		
		//set own to hashed own name
		ownHash = hashString(nameClient);
		
		//fill array with info
		String[] clientInfo = { String.valueOf(ownHash), this.ipaddress };
		Boolean shutdown = false;
		
		//list with clientstats arr and filenames arr
		List<Object> message = createDiscoveryMessage(clientInfo, filenames, shutdown);

		//bind remote object at location
		String bindLocation = createBindLocation(this.ipaddress);
		bindRemoteObject(bindLocation, ntn);
		
		//create message and multicast it
		multicastDatagramPacket(message, InetAddress.getByName(serverIp), 4545);
		
		//keep looping as long as nextHash isn't changed or number of nodes isn't changed
		while (ntn.nextHash == -1 || ntn.numberOfNodes == -1)
		{
			System.out.println("Waiting, next hash: "+ntn.nextHash + " # of nodes: " + ntn.numberOfNodes);
			
			//if there are no neighbor nodes 
			if (ntn.numberOfNodes == 1)
			{
				System.out.println("No neighbours! All hashes set to own");
				//set next and previous hash equal to own hash
				ntn.nextHash = ownHash;
				ntn.prevHash = ownHash;
			}
			try {
				//wait 100 ms
				Thread.sleep(100);	
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
		System.out.println("Total connected clients: " + (ntn.numberOfNodes)); //waarom +1?
		
		//set client's hash fields
		nextHash = ntn.nextHash();
		previousHash = ntn.prevHash();
		System.out.println("Hashes: Previous: " + ntn.prevHash + ". Own: " + ownHash + ". Next: " + ntn.nextHash);
		
		//unbind object from location
		unbindRemoteObject(bindLocation);
	    
	    waitForClients();
	}
	
	void failure(int hash){
		try {
			String name = "//" + serverIp + "/ntn";
			stvI = (ServerToNodeInterface) Naming.lookup(name);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int[] hashes = null;
		try {
			hashes = stvI.askPrevAndNextNode(hash);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int next = hashes[0];
		int prev = hashes[1];
	}

	/**
	 * MUUG VEEL IF'EN, HERSCHRIJVEN NAAR IETS NORMAAL AUB :(
	 * @param receivedHash
	 * @param receivedIPAddress
	 * @param neighbours
	 */
	void changeHashes(int receivedHash, String receivedIPAddress, int[] neighbours) {
		try {
			String name = createBindLocation(receivedIPAddress);
			ntnI = (NodeToNodeInterface) Naming.lookup(name);
			
			if(neighbours != null){
				if(nextHash == receivedHash){
			        //System.out.println("Changing next node from " + nextHash + " to " + neighbours[0]);
					nextHash = neighbours[0];
				}
				else if(previousHash == receivedHash){
					//System.out.println("Changing next node from " + previousHash + " to " + neighbours[1]);
					previousHash = neighbours[1];
				}
				//System.out.println(previousHash + " "  + ownHash + " " + nextHash);
			}else{
				
				if (ownHash > nextHash) { //laatste hash 
					if ((previousHash < receivedHash) && (ownHash > receivedHash)) {
						try{
							ntnI.answerDiscovery(previousHash, ownHash); //send my hashes to neighbours via RMI
						}catch(RemoteException e){
							//System.out.println("geen antwoord van vorige hash");
							failure(receivedHash);
						}
						
						previousHash = receivedHash;
						//System.out.println(previousHash + " "  + ownHash + " " + nextHash);
					} 
					else {
						try {
							ntnI.answerDiscovery(ownHash, nextHash); //send my hashes to neighbours via RMI
						} catch (RemoteException e) {
							failure(receivedHash);
						}
						nextHash = receivedHash;
						//System.out.println(previousHash + " "  + ownHash + " " + nextHash);
					}
				} 
				else if(ownHash == nextHash) {
					try {
						ntnI.answerDiscovery(ownHash, ownHash); //send my hashes to neighbours via RMI
					} catch (RemoteException e) {
						failure(receivedHash);
					}
					previousHash = receivedHash;
					nextHash = receivedHash;
					//System.out.println(previousHash + " "  + ownHash + " " + nextHash);
					//doorsturen via RMI
					
				}
				else { 
					if ((previousHash < receivedHash) && (ownHash > receivedHash)) {
						try {
							ntnI.answerDiscovery(previousHash, ownHash); //send my hashes to neighbours via RMI
						} catch (RemoteException e) {
							failure(receivedHash);
						}
						previousHash = receivedHash;
						//System.out.println(previousHash + " "  + ownHash + " " + nextHash);
						//RMI
					} else if ((ownHash < receivedHash) && (nextHash > receivedHash)) {
						try {
							ntnI.answerDiscovery(ownHash, nextHash); //send my hashes to neighbours via RMI
						} catch (RemoteException e) {
							failure(receivedHash);
						}
						nextHash = receivedHash;
						//System.out.println(previousHash + " "  + ownHash + " " + nextHash);
					}
					else if((previousHash == nextHash) || (previousHash > ownHash)) {
						try {
							ntnI.answerDiscovery(previousHash, ownHash); //send my hashes to neighbours via RMI
						} catch (RemoteException e) {
							failure(receivedHash);
						}
						previousHash = receivedHash;
						//System.out.println(previousHash + " "  + ownHash + " " + nextHash);
					}
				}
				
			}
			//System.out.println("waitForClients hashes set : Previous: " + ntn.prevHash + ". Own: " + ownHash + ". Next: " + ntn.nextHash);
			
		} catch(Exception e) {
			System.err.println("Fileserver exception: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	void waitForClients() throws ClassNotFoundException {
		try {
			byte[] inBuf = new byte[256];
			DatagramPacket dgram = new DatagramPacket(inBuf, inBuf.length);
			MulticastSocket socket = new MulticastSocket(4545);
			socket.joinGroup(InetAddress.getByName(serverIp));
			
			//do this forever
			while (true) {
				socket.receive(dgram); //blocks untill package is received
				
				try {
					List<Object> message = unpackDiscoveryMessage(dgram);
					String[] clientStats = (String[]) message.get(0);
					Boolean shutdown = (Boolean) message.get(2);
					System.out.println(shutdown);
					int[] neighbours = null;
					if(shutdown == true){
						neighbours = (int[]) message.get(3);
						System.out.println("next = " + neighbours[0]);
						System.out.println("previous = " + neighbours[1]);
					}
					int receivedHash = Integer.parseInt(clientStats[0]); //get hashesName from message
					System.out.println(receivedHash);
					
					changeHashes(receivedHash, clientStats[1], neighbours);
					
				} finally {

				}
			}
		} catch (IOException e) {
			
		}
	}
	
    public void shutdown(List<Object> message) throws IOException {
        System.out.println("Shutting down..");

        ntn.numberOfNodes--;
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
        dgram = new DatagramPacket(b, b.length, InetAddress.getByName(serverIp), 4545);

        socket.send(dgram);
        System.out.println("send");
        
        System.out.println("Closing client");
        System.exit(1);
        
	}

    /**
    * Send a datagramPacket
    * @param message
    * Contents to send
    * @param ipaddress
    * Destination address
    * @param port
    * What port to use
    * @return successful or not
    */
    boolean multicastDatagramPacket(Object message, InetAddress ipaddress, int port) {
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
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//fill in datagram packet
		dgram = new DatagramPacket(b, b.length, ipaddress, port);
		
		//try to send the packet
		try {
			socket.send(dgram);
			sent = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//close the socket
		System.out.println("Multicast sent");
		socket.close();
    	
		//return successful or not
    	return sent;
    }

    /**
     * Bind the specified object to a location
     * @param bindLocation
     * @param ntn
     * Remote object to bind
     */
    void bindRemoteObject(String bindLocation, NodeToNode ntn) {
    	try {
			Naming.bind(bindLocation, ntn);
		} catch (MalformedURLException | RemoteException
				| AlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /**
     * Unbind remote object at specified location
     * @param bindLocation
     */
    void unbindRemoteObject(String bindLocation) {
    	try {
			Naming.unbind(bindLocation);
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
    List<Object> createDiscoveryMessage(String[] clientInfo, String[] filenames, Boolean shutdown) {
     	List<Object> message = new ArrayList<Object>();
     	message.add(clientInfo);
     	message.add(filenames);
     	message.add(shutdown);
     	
     	return message;
     }
    
    /**
     * Unpack the received datagram packet to a List<Object>
     * @param dgramPacket
     * @return List containing string array with client info, string array with filenames and shutdown boolean
     */
 	List<Object> unpackDiscoveryMessage(DatagramPacket dgramPacket) {
     	byte[] b = null;
     	b = dgramPacket.getData();
     	List<Object> obj = null;
     	try {
 			obj = (ArrayList<Object>)byteArrToObject(b);
 		} catch (ClassNotFoundException | IOException e) {
 			// TODO Auto-generated catch block
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
