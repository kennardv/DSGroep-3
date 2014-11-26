
package networking;

import enumerations.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class replicationUpdater extends Thread {
	String[] fileNames = null;

	public replicationUpdater(String[] fileNames) throws IOException {

		this.fileNames = fileNames;
	}
	
	public void run() {
		try {
				replicationUpdater(this.fileNames);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
     * Replicates the newly added files
     * @param filenames
     * a string array containing all the filenames 
     * @throws IOException
     */
    void replicationUpdater(String[] filenames) throws UnknownHostException, IOException {
    	
    }
}