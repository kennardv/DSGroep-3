package be.uantwerpen.server;

import java.net.*;
import java.net.UnknownHostException;
import java.io.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

public class Client {
	
	//Client client;
	public int previousHash, ownHash, nextHash; //declaratie van de type hashes
	public NodeToNode ntn; //declaratie van remote object

	public Client() throws RemoteException, InterruptedException, IOException, ClassNotFoundException {

		ntn = new NodeToNode();
		Registry registry = null;
		
		/* enter client name in console and enter */
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Please enter client name: ");
        String nameClient = null;
        try {
        	nameClient = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /* end console input */
        
        
        
        
        
        
        


        File[] files = new File("c:\\Users").listFiles();
		String[] filenames = new String[files.length];

        //If this pathname does not denote a directory, then listFiles() returns null. 
        int index = 0;
        for (File file : files) {
            if (file.isFile()) {
                filenames[index] = file.getName();
                index++;
            }
        }
        
        
        
        
       
		String[] clientStats = new String[2];
		ownHash = hashString(nameClient); //set current to hash of own name
		clientStats[0] = ownHash + ""; //hashed own name
		clientStats[1] = Inet4Address.getLocalHost().getHostAddress(); //own ip address
		clientStats[2] = "online"; //status van client 
		
		List message = new ArrayList(); //arraylist met positie 0 = clients ip en hash, positie 1 = files array
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
		dgram = new DatagramPacket(b, b.length, InetAddress.getByName("226.100.100.125"), 4545);
		String bindLocation = "//localhost/ntn";
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
		
		
		while (ntn.nextHash == -1 || ntn.numberOfNodes == -1) //keep looping as long as nextHash isn't changed or number of nodes isn't changed
		{
			System.out.println("Waiting, next hash: "+ntn.nextHash + " # of nodes: " + ntn.numberOfNodes);
			
			if (ntn.numberOfNodes == 0) //if there are no neighbor nodes 
			{
				System.out.println("No neighbours! All hashes set to own");
				//set next and previous hash equal to own hash
				ntn.nextHash = ownHash;
				ntn.prevHash = ownHash;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}

		try {
			Naming.unbind(bindLocation); //unbind to free for other nodes
		} catch (NotBoundException e) {
			System.err.println("Not bound");
		}
		System.out.println("Total connected clients: " + (ntn.numberOfNodes + 1)); //waarom +1?
		
		//set client's hash fields
		nextHash = ntn.nextHash();
		previousHash = ntn.prevHash();
		System.out.println("Hashes: Previous: " + ntn.prevHash + ". Own: " + ownHash + ". Next: " + ntn.nextHash);
		
		if(ntn.numberOfNodes == 2){
			//shutdown(previousHash, nextHash, clientStats, filenames, message);
		}
		
		
		waitForClients();

	}
	
	public void run() {
		try {
			main(null);
		} catch (ClassNotFoundException | InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	

	int hashString(String name) {
		return Math.abs(name.hashCode()) % 32768; // berekening van de hash
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
					int receivedHash = Integer.parseInt(clientStats[0]); //get hashesName from message
				
					try {
						String name = "//localhost/ntn";
					
							NodeToNodeInterface ntnI = (NodeToNodeInterface) Naming.lookup(name);

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
	
    public void shutdown(int previoushashnode, int nexthashnode, String[] cs, String[] fn, List<Object> message) throws IOException {
        System.out.println("Shutting down..");

    	previousHash = nexthashnode;

        System.out.println("Sending id from next node to previous node..");

    	System.out.printf("Client %d down!", ownHash);
    	
        previousHash = nexthashnode;
        System.out.println("Changing info from next node in previous node..");

        System.out.printf("the next client's previous hash is changed to %d \n", previousHash);
        System.out.println("Sending id from previous node to next node..");
        System.out.printf("the previous client's next hash is changed to %d \n", nextHash);
        System.out.println("Delete node at nameserver..");

        nextHash = previoushashnode;

        ntn.numberOfNodes--;
        Boolean shutdown = true;
        //create message and multicast it
        Object obj = message; 
        message.remove(shutdown);
        message.add(shutdown);
        DatagramSocket socket = new DatagramSocket();
        ByteArrayOutputStream byteArr = new ByteArrayOutputStream();
        ObjectOutput objOut = new ObjectOutputStream(byteArr);
        objOut.writeObject(obj);
        byte[] b = byteArr.toByteArray();
        DatagramPacket dgram;
        dgram = new DatagramPacket(b, b.length, InetAddress.getByName("226.100.100.125"), 4545);
        String bindLocation = "//localhost/ntn";
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
        } catch (Exception e) {
        }
        try {
            Naming.bind(bindLocation, ntn);
        } catch (Exception e) {
        }
        socket.send(dgram);
        System.out.println("Multicast sent");
        System.exit(1);

	}
	
	
	public static void main(String argv[]) throws InterruptedException, IOException, ClassNotFoundException {
		Client client = new Client();
		
	}
}
