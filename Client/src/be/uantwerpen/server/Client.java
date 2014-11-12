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
	
	//Client client;
	public int previousHash, ownHash, nextHash; //declaratie van de type hashes
	public NodeToNode ntn; //declaratie van remote object
	public Registry registry = null;

	HashMap<File, Boolean> allFiles = new HashMap<File, Boolean>();
	
	String myFilesFolderName = "myfiles";
	List<File> myFiles = null;
	
	String ipaddress = Inet4Address.getLocalHost().getHostAddress();
	
	public Client() throws RemoteException, InterruptedException, IOException, ClassNotFoundException {
		//START OF SYSTEM
		//CREATE FileListAgent
		//EXECTUTE startFileListAgent METHOD IN NodeToNode
		
		//FAIL DETECTED
		//CREATE FileRecoveryAgent
		//EXECTUTE startFileRecoveryAgent METHOD IN NodeToNode
		System.out.println(ipaddress);
		ntn = new NodeToNode();
		myFiles = listFilesInDir("C:\\Users");
		
		//Read from console input
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
		Boolean shutdown = false;
		
		//set own to hashed own name
		ownHash = hashString(nameClient);
		
		//fill array with data
		String[] clientStats = new String[3];
		clientStats[0] = ownHash + ""; //hashed own name
		clientStats[1] = this.ipaddress; //own ip address
		// clientStats[2] = "online"; //status van client 
		
		//list with clientstats arr and filenames arr
		List message = new ArrayList();
		message.add(clientStats);
		message.add(filenames);
		message.add(shutdown);

		//create message and multicast it
		Object obj = message; 
		DatagramSocket socket = new DatagramSocket();
		ByteArrayOutputStream byteArr = new ByteArrayOutputStream();
		ObjectOutput objOut = new ObjectOutputStream(byteArr);
		objOut.writeObject(obj);
		byte[] b = byteArr.toByteArray();
		DatagramPacket dgram;
		dgram = new DatagramPacket(b, b.length, InetAddress.getByName("192.168.1.255"), 4545);
		String bindLocation = "//" + this.ipaddress + "/ntn";
		try {
			registry = LocateRegistry.createRegistry(1099);
		} catch (Exception e) {
		}
		try {
			Naming.bind(bindLocation, ntn);
		} catch (Exception e) {
		}
		socket.send(dgram);
		System.out.println("Multicast sent");
		
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
		
		if(ntn.numberOfNodes == 4){
			shutdown(clientStats, filenames, message);
		}
		
		waitForClients();

	}
	
	void failure(){
		//NodeToNode ntn = new NodeToNode();
		int next = ntn.nextHash;
		int prev = ntn.prevHash;
		//if ()
	}
	
	void waitForClients() throws ClassNotFoundException {
		try {
			byte[] inBuf = new byte[256];
			DatagramPacket dgram = new DatagramPacket(inBuf, inBuf.length);
			MulticastSocket socket = new MulticastSocket(4545);
			socket.joinGroup(InetAddress.getByName("226.100.100.125"));
			
			//do this forever
			while (true) {
				socket.receive(dgram); //blocks untill package is received
				ByteArrayInputStream bis = new ByteArrayInputStream(inBuf);
				ObjectInput in = null;
				
				try {
					in = new ObjectInputStream(bis);
					Object o = in.readObject();
					List message = (List) o;
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
				
					try {
						String name = "//" + clientStats[1] + "/ntn";
						NodeToNodeInterface ntnI = (NodeToNodeInterface) Naming.lookup(name);
						
						if ((this.ownHash < receivedHash) && (receivedHash < this.nextHash)) {
							this.nextHash = receivedHash;
							ntnI.answerDiscovery(ownHash, nextHash);
						} else if ((this.previousHash < receivedHash) && (receivedHash < this.ownHash)) {
							this.previousHash = receivedHash;
						}
						
						
						/*if(neighbours != null){
							if(nextHash == Integer.parseInt(clientStats[0])){
						        System.out.println("Changing next node from " + nextHash + " to " + neighbours[0]);
								nextHash = neighbours[0];
							}
							else if(previousHash == Integer.parseInt(clientStats[0])){
								System.out.println("Changing next node from " + previousHash + " to " + neighbours[1]);
								previousHash = neighbours[1];
							}
							System.out.println(previousHash + " "  + ownHash + " " + nextHash);
						}else{
							
							if (ownHash > nextHash) { //laatste hash 
								if ((previousHash < receivedHash) && (ownHash > receivedHash)) {
									try{
										ntnI.answerDiscovery(previousHash, ownHash); //send my hashes to neighbours via RMI
									}catch(RemoteException e){
										System.out.println("geen antwoord van vorige hash");
									}
									
									previousHash = receivedHash;
									System.out.println(previousHash + " "  + ownHash + " " + nextHash);
								} 
								else {
									ntnI.answerDiscovery(ownHash, nextHash); //send my hashes to neighbours via RMI
									nextHash = receivedHash;
									System.out.println(previousHash + " "  + ownHash + " " + nextHash);
								}
							} 
							else if(ownHash == nextHash) {
								ntnI.answerDiscovery(ownHash, ownHash); //send my hashes to neighbours via RMI
								previousHash = receivedHash;
								nextHash = receivedHash;
								System.out.println(previousHash + " "  + ownHash + " " + nextHash);
								//doorsturen via RMI
								
							}
							else { 
								if ((previousHash < receivedHash) && (ownHash > receivedHash)) {
									ntnI.answerDiscovery(previousHash, ownHash); //send my hashes to neighbours via RMI
									previousHash = receivedHash;
									System.out.println(previousHash + " "  + ownHash + " " + nextHash);
									//RMI
								} else if ((ownHash < receivedHash) && (nextHash > receivedHash)) {
									ntnI.answerDiscovery(ownHash, nextHash); //send my hashes to neighbours via RMI
									nextHash = receivedHash;
									System.out.println(previousHash + " "  + ownHash + " " + nextHash);
								}
								else if((previousHash == nextHash) || (previousHash > ownHash)) {
									ntnI.answerDiscovery(previousHash, ownHash); //send my hashes to neighbours via RMI
									previousHash = receivedHash;
									System.out.println(previousHash + " "  + ownHash + " " + nextHash);
								}
							}
							
						}*/


						//System.out.println("waitForClients hashes set : Previous: " + ntn.prevHash + ". Own: " + ownHash + ". Next: " + ntn.nextHash);
						
					} catch(Exception e) {
						System.err.println("Fileserver exception: " + e.getMessage());
						e.printStackTrace();
					}
				} finally {
					try {
						bis.close();
					} catch (IOException ex) {

					}
					try {
						if (in != null) {
							in.close();
						}
					} catch (IOException ex) {

					}
				}
				dgram.setLength(inBuf.length);
			}
		} catch (UnknownHostException e) {
		} catch (IOException e) {
		}
	}
	

	
    public void shutdown(String[] cs, String[] fn, List<Object> message) throws IOException {
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
        dgram = new DatagramPacket(b, b.length, InetAddress.getByName("226.100.100.125"), 4545);

        socket.send(dgram);
        System.out.println("send");
        
        System.out.println("Closing client");
        System.exit(1);
        
	}
    

    
    /***
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
    
    /**
     * Helper method to convert a string to a hash. Range goes from 0 to 32768.
     * @param name
     * String to be hashed
     * @return Returns the hashed inputted string.
     */
    int hashString(String name) {
		return Math.abs(name.hashCode()) % 32768; // berekening van de hash
	}
	
    /***
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
    
	public static void main(String argv[]) throws InterruptedException, IOException, ClassNotFoundException {
		Client client = new Client();
		
	}
}
