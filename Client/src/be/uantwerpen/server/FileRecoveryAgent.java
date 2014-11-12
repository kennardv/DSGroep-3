package be.uantwerpen.server;

import java.io.Serializable;

public class FileRecoveryAgent implements Runnable, Serializable {
	
	public FileRecoveryAgent(int failingNodeId, int startNodeId) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		//get list of files on current node
		//if hash of filename is on failing node: 2 options
		
		
		//send file to new owner. 
		//If new owner isn't owner of file yet and doesn't own file,
		//update new owner's filelist - file download location = location of current node
		
		//if new owner is already owner of file, update new owner with info that file is available on current node
		
		
		//if current node = start node -> terminate agent.
	}
}
