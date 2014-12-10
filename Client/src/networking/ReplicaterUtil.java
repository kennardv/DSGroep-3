package networking;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.rmi.Naming;
import java.util.List;

import be.uantwerpen.server.Constants;
import rmi.implementations.NodeToNode;
import rmi.interfaces.NodeToNodeInterface;
import rmi.interfaces.ServerToNodeInterface;
import utils.Toolkit;
import enumerations.Mode;
import networking.*;
public class ReplicaterUtil {
	
	
    
    public void replicate(String[] fileReplicateList, NodeToNode ntn, List<File> files, String myIPAddress, int userName  )
    {
		//get files to replicate
		if (fileReplicateList == null) {
			return;
		}
		fileReplicateList = ntn.replicationAnswer();
		for( int i = 0; i< fileReplicateList.length; i++ )
		{
			String name = Toolkit.createBindLocation(fileReplicateList[i], Constants.SUFFIX_NODE_RMI);
			try {
				//TCPUtil tcpSender = new TCPUtil(null, 20000, Mode.SEND, files.get(i), null);
				TCPUtil tcpSender = new TCPUtil(null, Mode.SEND, files.get(i), null);
				Thread t = new Thread(tcpSender);
				t.start();
				NodeToNodeInterface ntnI = (NodeToNodeInterface) Naming.lookup(name);
				ntnI.startReceive(myIPAddress, files.get(i).getName());
				t.join();
			} catch (Exception e) {
				e.printStackTrace();
			}	
			
		}
		updater( ntn, myIPAddress, userName);
	}
    

    public void updater( NodeToNode ntn, String myIPAddress, int userName )
    {
        Path myDir = Paths.get(".\\src\\resources\\myfiles\\");       
        System.out.println("testing");
        try {
           WatchService watcher = myDir.getFileSystem().newWatchService();
           myDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, 
           StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

           WatchKey watckKey = watcher.take();

           List<WatchEvent<?>> events = watckKey.pollEvents();
           for (WatchEvent event : events) {
        	   System.out.println("grfygf");
                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                	
                    File newFile = new File(".\\src\\resources\\myfiles\\" + event.context().toString() );
                    ServerToNodeInterface stni = (ServerToNodeInterface) Naming.lookup(Constants.SERVER_PATH_RMI);
                    int previousNode = stni.getnewFileReplicationNode(Toolkit.hashString(newFile.getName()), userName);
                    String name = Toolkit.createBindLocation(stni.getNodeIPAddress(previousNode),  Constants.SUFFIX_NODE_RMI);
        			try {
        				TCPUtil tcpSender = new TCPUtil(null, Mode.SEND, newFile, null);
        				Thread t = new Thread(tcpSender);
        				t.start();
        				NodeToNodeInterface ntnI = (NodeToNodeInterface) Naming.lookup(name);
        				ntnI.startReceive(myIPAddress, newFile.getName());
        				t.join();
        			} catch (Exception e) {
        				e.printStackTrace();
        			}
                }
            }
           
        } catch (Exception e) {
            System.out.println("Error: " + e.toString());
        }
    }
}