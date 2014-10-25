package be.uantwerpen.server;

import java.net.*;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.io.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Client {

	public Client() throws RemoteException {
		super();
	}

	public static int previous, current, next; //declaratie van de type hashes
	public static NodeToNode ntn; //declaratie van remote object

	public static void main(String argv[]) throws InterruptedException, IOException, ClassNotFoundException {
		List message = new ArrayList(); //arraylist met positie 0 = clients ip en hash, positie 1 = files array

		ntn = new NodeToNode();
		Registry registry = null;
		Client.previous = 8000;
		Client.current = 9000;
		Client.next = 15000;
		String nameClient = "Client1"; // naam van de client
		String[] filenames = { "file1.jpg", "file2.txt", "file3.gif" }; //lijst van files op de clientpc
		String[] clientStats = new String[2];
		Client.current = hashString(nameClient);
		clientStats[0] = Client.current + "";
		clientStats[1] = Inet4Address.getLocalHost().getHostAddress();
		message.add(clientStats);
		message.add(filenames);

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
		System.out.println("multicast is send");

		while (ntn.nextHash == -1 || ntn.numberOfNodes == -1) //keep looping as long as nextHash isn't filled in or number of nodes isn't filled in
		{
			System.out.println(ntn.nextHash);
			
			if (ntn.numberOfNodes == 0) //if there are no neighbor nodes 
			{
				//set next and previous hash equal to own hash
				ntn.nextHash = Client.current;
				ntn.prevHash = Client.current;
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
		System.out.println("Client number: " + (ntn.numberOfNodes + 1));
		System.out.println("previoushash: " + ntn.prevHash + "; my hash: " + Client.current + "; nexthash: " + ntn.nextHash);
		
		//set client's hash fields
		Client.next = ntn.nextHash();
		Client.previous = ntn.prevHash();

		waitforclients();

	}

	static int hashString(String name) {
		return Math.abs(name.hashCode()) % 32768; // berekening van de hash
	}

	static void waitforclients() throws ClassNotFoundException {
		try {
			byte[] inBuf = new byte[256];
			DatagramPacket dgram = new DatagramPacket(inBuf, inBuf.length);
			MulticastSocket socket = new MulticastSocket(4545);
			socket.joinGroup(InetAddress.getByName("226.100.100.125"));
			while (true) {

				socket.receive(dgram); // blokkeerd tot hij een package ontvangt
				ByteArrayInputStream bis = new ByteArrayInputStream(inBuf);
				ObjectInput in = null;
				try {
					in = new ObjectInputStream(bis);
					Object o = in.readObject();
					List message = (List) o;
					String[] clientStats = (String[]) message.get(0);
					int receivedHash = Integer.parseInt(clientStats[0]); //de hash uit de multicast message halen
					
					try {
						if (Client.current > Client.next) {
							if ((receivedHash > Client.current) && (receivedHash < Client.next)) {
								Client.next = receivedHash;
							}
						} else {
							if ((receivedHash < Client.current) && (receivedHash > Client.previous)) {
								Client.previous = receivedHash;
							}
						}
						String name = "//localhost/ntn";
						NodeToNodeInterface ntnI = (NodeToNodeInterface) Naming.lookup(name);
						ntnI.answerDiscovery(Client.previous, Client.current);
						
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

}

class IntObj {
	public int value;
}
