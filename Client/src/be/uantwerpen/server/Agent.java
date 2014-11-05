package be.uantwerpen.server;

import java.io.*;
import java.lang.management.LockInfo;
import java.util.*;

public class Agent implements Runnable, Serializable {
	
	HashMap<Integer, Boolean> foundFiles = new HashMap<Integer, Boolean>(); 
	
	@Override
	public void run() {
		File[] filesOnNode;
		File tmp = new File("/src/resources/myfiles");
		
		//list of all owned files for this node
		filesOnNode = tmp.listFiles();
		
		for (File f : filesOnNode) {
			//if the file wasn't found yet, add it to found list
			int k = hashString(f.getName());
			if (!foundFiles.get(k)) {
				foundFiles.put(k, false);
			}
		}
		
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
}
