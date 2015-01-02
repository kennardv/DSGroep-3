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

		//System.out.println("foundFiles " + foundFiles.size());
		List<File> tmp = Toolkit.listFilesInDir(Constants.MY_FILES_PATH);
		TreeMap<Integer, Boolean> filesOnNode = new TreeMap<Integer, Boolean>();
		
		for (File f : tmp) {
			filesOnNode.put(Toolkit.hashString(f.getName()), false);
		}
		
		
		//if the file wasn't found yet, add it to found list

		Set<Integer> keys = filesOnNode.keySet();
	    Iterator<Integer> itr = keys.iterator();
		//System.out.println("foundfiles: " + foundFiles.size());

	    while(itr.hasNext())
	    {	
	    	int key = itr.next();
	    	if( foundFiles.get(key) == null || foundFiles.size() == 0)
			{
				foundFiles.put(key, false);
				//System.out.println("Adding files to foundFiles " + key);
			}
	    }
		

		//System.out.println("foundfiles: " + foundFiles.size());
		IServerToNode stnI = null;
		INodeToNode ntnI = null;
		try {
			stnI = (IServerToNode) Naming.lookup(this.serverPath);
			String path = stnI.getNodeIPAddress(currentNode);
			path = Toolkit.createBindLocation(path, "ntn");
			ntnI = (INodeToNode) Naming.lookup(path);
			ntnI.updateFileList(foundFiles);
			if(ntnI.getLockRequest() != -1)
			{
				foundFiles.put(ntnI.getLockRequest(),true);
			}
			if(ntnI.getPreviousLock() != -1 && ntnI.getLockRequest() == -1)
			{
				foundFiles.put(ntnI.getPreviousLock(),false);
				ntnI.setPreviousLock(-1);
				
			}
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		}
		
		//if lock request on current node and file not locked -> lock in foundFiles map
		//unlock files downloaded by current node
	}
}
