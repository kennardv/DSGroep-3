package networking;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

import com.sun.corba.se.impl.orbutil.RepIdDelegator;

import be.uantwerpen.server.Constants;
import rmi.implementations.NodeToNode;
import rmi.interfaces.INodeToNode;
import rmi.interfaces.IServerToNode;
import utils.Callback;
import utils.Toolkit;
import enumerations.Mode;
import networking.*;

public class ReplicaterUtil extends Thread{
	 
	private NodeToNode ntn;
	private String myIPAddress;
	private int userName;
	private Callback failureCallback;
	
	/**
	 * 
	 * @param ntn
	 * @param myIPAddress
	 * @param userName
	 * @param callback
	 */
	public ReplicaterUtil(NodeToNode ntn, String myIPAddress, int userName, Callback callback)
	{
		this.ntn = ntn;
		this.myIPAddress = myIPAddress;
		this.userName = userName;
		this.failureCallback = callback;
	}
	
    public void run()
    {
    	while(true)
    	{
    		updater(ntn, myIPAddress, userName);
    	}
    }
    
    public void replicate(String[] fileReplicateList, List<File> files ) {
		//get files to replicate
    	System.out.println("number of files:" + files.size());
		if (ntn.numberOfNodes() > 1) {	
			fileReplicateList = ntn.replicationAnswer();
			for( int i = 0; i< fileReplicateList.length; i++ )
			{
				String name = Toolkit.createBindLocation(fileReplicateList[i], Constants.SUFFIX_NODE_RMI);
				TCPUtil tcpSender = null;
				try {
					tcpSender = new TCPUtil(null, Mode.SEND, files.get(i), null);
				} catch (IOException e) {
					e.printStackTrace();
				}
				Thread t = new Thread(tcpSender);
				t.start();
				INodeToNode ntnI = null;
				try {
					ntnI = (INodeToNode) Naming.lookup(name);
				} catch (MalformedURLException | RemoteException | NotBoundException e) {
					//FAILURE() MUST BE CALLED HERE
					//USE IT LIKE THIS
					//this.callback.invoke(hash)
					e.printStackTrace();
				}
				try {
					ntnI.startReceive(myIPAddress, files.get(i).getName());
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		Callback callback = new Callback(this, "failure");
		ReplicaterUtil r = new ReplicaterUtil(ntn, myIPAddress, userName, callback);
		Thread t2 = new Thread(r);
		t2.start();
	}
    
    /**
     * WERKWOORD!
     * @param ntn
     * @param myIPAddress
     * @param userName
     */
    public void updater(NodeToNode ntn, String myIPAddress, int userName )
    {
        Path myDir = Paths.get(Constants.MY_FILES_PATH);       
        
        WatchService watcher = null;
		try {
			watcher = myDir.getFileSystem().newWatchService();
			myDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, 
			StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
		} catch (IOException e) {

			e.printStackTrace();
		}

        WatchKey watckKey = null;
		try {
			watckKey = watcher.take();
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
		
		List<WatchEvent<?>> events = watckKey.pollEvents();
        for (WatchEvent event : events) {
        	System.out.println("grfygf");
            if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
            	File newFile = new File(".\\src\\resources\\myfiles\\" + event.context().toString() );
                IServerToNode stni = null;
				
                try {
					stni = (IServerToNode) Naming.lookup(Constants.SERVER_PATH_RMI);
				} catch (MalformedURLException | RemoteException | NotBoundException e) {
					//FAILURE() MUST BE CALLED HERE
					//USE IT LIKE THIS
					//this.callback.invoke(hash)
					e.printStackTrace();
				}
                int previousNode;
                String name = null;
				try {
					previousNode = stni.getnewFileReplicationNode(Toolkit.hashString(newFile.getName()), userName);
					name = Toolkit.createBindLocation(stni.getNodeIPAddress(previousNode),  Constants.SUFFIX_NODE_RMI);
				} catch (RemoteException e) {
					//FAILURE() MUST BE CALLED HERE
					//USE IT LIKE THIS
					//this.callback.invoke(hash)
					e.printStackTrace();
				}
        			
				TCPUtil tcpSender = null;
				try {
					tcpSender = new TCPUtil(null, Mode.SEND, newFile, null);
				} catch (IOException e) {
					e.printStackTrace();
				}
				Thread t = new Thread(tcpSender);
				t.start();
				INodeToNode ntnI = null;
				try {
					ntnI = (INodeToNode) Naming.lookup(name);
				} catch (MalformedURLException | RemoteException | NotBoundException e) {
					//FAILURE() MUST BE CALLED HERE
					//USE IT LIKE THIS
					//this.callback.invoke(hash)
					e.printStackTrace();
				}
				try {
					ntnI.startReceive(myIPAddress, newFile.getName());
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
        }
    }
    
    
    public void shutdownRep(){
    	
    }
}