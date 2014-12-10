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
	
	HashMap<String, Boolean> foundFiles = new HashMap<String, Boolean>();
	private int currentNode;
	private String serverPath = null;
	
	public FileListAgent(int currentNode, String serverPath) {
		this.currentNode = currentNode;
		this.serverPath = serverPath;
	}
	
	@Override
	public void run() {
		List<File> tmp = Toolkit.listFilesInDir(Constants.MY_FILES_PATH);
		List<String> filesOnNode = null;
		
		for (File f : tmp) {
			filesOnNode.add(f.getName());
		}
		
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
		
		IServerToNode stnI = null;
		INodeToNode ntnI = null;
		try {
			stnI = (IServerToNode) Naming.lookup(this.serverPath);
			String path = stnI.getNodeIPAddress(currentNode);
			path = Toolkit.createBindLocation(path, "ntn");
			ntnI = (INodeToNode) Naming.lookup(path);
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
