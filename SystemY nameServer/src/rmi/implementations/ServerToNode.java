package rmi.implementations;

import be.uantwerpen.server.*;
import rmi.interfaces.*;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.Set;

public class ServerToNode extends UnicastRemoteObject implements ServerToNodeInterface {
	
	ClientMap clientMap;
	
	public ServerToNode(ClientMap clientMap) throws RemoteException {
		super();
		this.clientMap = clientMap;
	}

	/***
	 * Get an array containing the neighbours of the specified node. Index 0 = previous, index 1 = next
	 */
	public int[] getNeighbourNodes(int nodeHash) throws RemoteException {
		Object[] tmp = null;
		int[] keys = null;
		int[] neighbours = null;
		
		//can't get index for key in treemap so we use a custom implementation
		//copy map to an array of type object
		tmp = this.clientMap.getClientMap().keySet().toArray();
		keys = new int[keys.length];
		neighbours = new int[2];
		//parsing from object[] to int[] isn't possible so
		//individualy parse keys to int
		for (int i = 0; i < tmp.length; i++) {
			keys[i] = (int)tmp[i];
		}
		
		for (int i = 0; i < keys.length; i++) {
			if (keys[i] == nodeHash) {
				//if index is on first element, previous neighbour = last element
				if (i == 0) {
					neighbours[0] = keys[keys.length - 1];
					neighbours[1] = keys[i + 1];
				}
				//if index is on last element, next neighbour = first element
				else if (i == keys.length - 1) {
					neighbours[0] = keys[i - 1];
					neighbours[1] = keys[0];
				}
				//normal case
				else {
					neighbours[0] = keys[i - 1];
					neighbours[1] = keys[i + 1];
				}
			}
		}
		
		return neighbours;
	}

	@Override
	public void removeNode(int nodeHash) throws RemoteException {
		if ((nodeHash == this.clientMap.getLastAddedKey()) && (this.clientMap.getLastAddedKey() != -1)) {
			Iterator<Integer> it = this.clientMap.getClientMap().keySet().iterator();

			while (it.hasNext())
			{
			  it.next();
			  if (!it.equals(nodeHash))
			    it.remove();
			}
			String name = "//" + this.clientMap.getClientMap().get(nodeHash) + "/ntn";
			NodeToNodeInterface ntnI = null;
			try {
				ntnI = (NodeToNodeInterface) Naming.lookup(name);
			} catch (MalformedURLException | NotBoundException e) {
				e.printStackTrace();
			}
			ntnI.serverAnswer(this.clientMap.getClientMap().size(), null);
		} else {
			this.clientMap.remove(nodeHash);
		}
	}

	@Override
	public String getNodeIPAddress(int nodeHash) throws RemoteException {
		Client c = null;
		c = this.clientMap.getClientMap().get(nodeHash);
		return c.getIpaddress();
	}
	@Override
	public String getnewFileReplicationNode(int filehash) throws RemoteException {
		int previousNode = 0;
		boolean done = false;

	    Set<Integer> keys = this.clientMap.getClientMap().keySet();
	    Iterator<Integer> itr = keys.iterator();
	    while(itr.hasNext() && done == false)
	    {	
	    	previousNode = itr.next();
	    	
	    	
	    	if((filenamesArr[filenamesArr.length - 1] > previousNode) && (clientHashedName != previousNode ))
		    {
	    		done = true;
		    }
	    }
	    Client c = this.clientMap.getClientMap().get(previousNode);
	    
	    fileReplicateLocation[i] = c.getIpaddress();
		return c.getIpaddress();
	}
	@Override
	public int[] getPreviousAndNextNodeHash(int hash) throws RemoteException {
		//Object type because can't cast to int array
		
		Object[] tmp = this.clientMap.getClientMap().keySet().toArray();
		int[] keys = new int[tmp.length];
		System.out.println("lengte:" + tmp.length);
		if(tmp.length <= 1)
		{
			return null;
		}
		//cast all elements to int
		for (int i = 0; i < tmp.length; i++) {
			keys[i] = (int)tmp[i];
		}
		//previous and next node array
		int[] hashes = new int[2];
		for (int i = 0; i < keys.length; i++) {
			if (keys[i] == hash) {
				if (i == 0) {
					hashes[0] = keys[keys.length - 1];
					hashes[1] = keys[i + 1];
				} else if (i == keys.length -1) {
					hashes[0] = keys[i - 1];
					hashes[1] = keys[0];
				} else {
					hashes[0] = keys[i - 1];
					hashes[1] = keys[i + 1];
				}
			}
		}
		return hashes;
	}
}
