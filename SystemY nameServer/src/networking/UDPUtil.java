package networking;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

import be.uantwerpen.server.Constants;

/**
 * 
 * @author Kennard
 *
 */
public class UDPUtil {
	
	/**
	 * 
	 * @return List with: int hashedName, string ipaddress, List<Integer> filenames, Protocol protocol 
	 */
	public List listenForPackets() {
		// create socket, buffer and join socket group
		byte[] inBuf = new byte[256];
		DatagramPacket dgram = new DatagramPacket(inBuf, inBuf.length);
		MulticastSocket socket = null;
		
		try {
			socket = new MulticastSocket(Constants.SOCKET_PORT_UDP);
			socket.joinGroup(InetAddress.getByName(Constants.MULTICAST_IP));
			
			// blocks until a datagram is received
	    	System.out.println("Listening for packet");
			socket.receive(dgram);
		} catch (IOException e1) {
			e1.printStackTrace();
		} // must bind receive side
		
		// process received packet
		ByteArrayInputStream bis = new ByteArrayInputStream(inBuf);
		ObjectInput in = null;
		Object o = null;
		System.out.println("Receiving packet");
		try {
			in = new ObjectInputStream(bis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			o = in.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		
		// pull values from message and store
		List tmp = (List) o;
		//if (tmp.get(0) == Protocol.SHUTDOWN) {
		//	return;
		//}
		
		List message = new ArrayList();
		
		message.add(tmp.get(1));
		message.add(dgram.getAddress().getHostAddress());
		
		int[] filenamesArr = (int[])tmp.get(2);
		List<Integer> filenames = new ArrayList<Integer>();
		for (int i = 0; i < filenamesArr.length; i++) {
			filenames.add(filenamesArr[i]);
		}
		message.add(filenames);
		message.add(tmp.get(0));
		
		return message;
	}
}
