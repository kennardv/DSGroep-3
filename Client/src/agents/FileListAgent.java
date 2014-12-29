package agents;

import java.io.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;

import be.uantwerpen.server.Constants;
import rmi.interfaces.INodeToNode;
import rmi.interfaces.IServerToNode;
import utils.Toolkit;

public class FileListAgent implements Runnable, Serializable {
	
	TreeMap<Integer, Boolean> foundFiles = new TreeMap<Integer, Boolean>();
	private int currentNode;
	private String serverPath = null;
	
	public FileListAgent(int currentNode, String serverPath) {
		this.currentNode = currentNode;
		this.serverPath = serverPath;
	}
	
	public void setCurrentNode(int hash) {
		this.currentNode = hash;
	}
	
	@Override
	public void run() {
		System.out.println("current node: " + currentNode);
		List<File> tmp = Toolkit.listFilesInDir(Constants.MY_FILES_PATH);
		TreeMap<Integer, Boolean> filesOnNode = new TreeMap<Integer, Boolean>();
		
		for (File f : tmp) {
			filesOnNode.put(Toolkit.hashString(f.getName()), false);
		}
		
		
		//if the file wasn't found yet, add it to found list
		Set<Integer> keys = filesOnNode.keySet();
	    Iterator<Integer> itr = keys.iterator();
	    while(itr.hasNext())
	    {	
	    	if( foundFiles.get(itr.next()) == null || foundFiles.size() == 0)
			{
				foundFiles.put(itr.next(), false);	
			}
	    }
		
		//Update list on current node
		/*for (Integer key : foundFiles.keySet()) {
			if (filesOnNode.get(key) == null || filesOnNode.size() == 0) {
				filesOnNode.put(key, false);
			}
		}*/
		
		IServerToNode stnI = null;
		INodeToNode ntnI = null;
		try {
			stnI = (IServerToNode) Naming.lookup(this.serverPath);
			String path = stnI.getNodeIPAddress(currentNode);
			path = Toolkit.createBindLocation(path, "ntn");
			ntnI = (INodeToNode) Naming.lookup(path);
			ntnI.updateFileList(foundFiles);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//if lock request on current node and file not locked -> lock in foundFiles map
		//unlock files downloaded by current node
	}
}
