package agents;

import java.io.*;
import java.lang.management.LockInfo;
import java.util.*;

public class FileListAgent implements Runnable, Serializable {
	
	HashMap<String, Boolean> foundFiles = new HashMap<String, Boolean>();
	
	@Override
	public void run() {
		List<String> filesOnNode = new ArrayList<String>();
		File tmp = new File(".\\src\\resources\\myfiles");
		
		//list of all owned files for this node
		File[] files = tmp.listFiles();
		for (int i = 0; i < files.length; i++) {
			filesOnNode.add(files[i].getName());
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
		
		//if lock request on current node and file not locked -> lock in foundFiles map
		System.out.println("Agentzzz");
		//unlock files downloaded by current node
	}
}
