package agents;

import java.io.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;

import rmi.interfaces.NodeToNodeInterface;
import rmi.interfaces.ServerToNodeInterface;
import utils.Toolkit;

public class FileListAgent implements Runnable, Serializable {
	
	HashMap<String, Boolean> foundFiles = new HashMap<String, Boolean>();
	private int currentNode;
	private String serverPath = null;
	
	public FileListAgent(int currentNode, String serverPath) {
		this.currentNode = currentNode;
		this.serverPath = serverPath;
	}
	
	@Override
	public void run() {
		List<File> tmp = Toolkit.listFilesInDir(".\\src\\resources\\myfiles");
		List<String> filesOnNode = null;
		
		//if the file wasn't found yet, add it to found list
		for (String f : filesOnNode) {
			//int k = hashString(f);
			if (!foundFiles.get(f)) {
				foundFiles.put(f, false);
			}
		}
		
		//Update list on current node
		for (String key : foundFiles.keySet()) {
			if (!filesOnNode.contains(key)) {
				filesOnNode.add(key);
			}
		}
		
		ServerToNodeInterface stnI = null;
		NodeToNodeInterface ntnI = null;
		try {
			stnI = (ServerToNodeInterface) Naming.lookup(this.serverPath);
			String path = stnI.getNodeIPAddress(currentNode);
			path = Toolkit.createBindLocation(path, "ntn");
			ntnI = (NodeToNodeInterface) Naming.lookup(path);
			ntnI.updateFileList(filesOnNode);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//if lock request on current node and file not locked -> lock in foundFiles map
		System.out.println("Agentzzz");
		//unlock files downloaded by current node
	}
}
