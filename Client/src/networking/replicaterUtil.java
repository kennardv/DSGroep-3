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

import rmi.implementations.NodeToNode;
import rmi.interfaces.NodeToNodeInterface;
import utils.Toolkit;
import enumerations.Mode;

public class replicaterUtil {

    public static void main(String[] args) {

        //define a folder root

    }
    
    public void replicate(String[] fileReplicateList, NodeToNode ntn, String rmiSuffixNode, List<File> files, String myIPAddress  )
    {
		//get files to replicate
		if (fileReplicateList == null) {
			return;
		}
		fileReplicateList = ntn.replicationAnswer();
		for( int i = 0; i< fileReplicateList.length; i++ )
		{
			String name = Toolkit.createBindLocation(fileReplicateList[i], rmiSuffixNode);
			try {
				TCPUtil tcpSender = new TCPUtil(null, 20000, Mode.SEND, files.get(i), null);
				Thread t = new Thread(tcpSender);
				t.start();
				NodeToNodeInterface ntnI = (NodeToNodeInterface) Naming.lookup(name);
				ntnI.startReceive(myIPAddress, 20000, files.get(i).getName());
				t.join();
			} catch (Exception e) {
				e.printStackTrace();
			}	
			
		}
		updater(fileReplicateList, ntn, rmiSuffixNode, files, myIPAddress);
	}
    

    public void updater(String[] fileReplicateList, NodeToNode ntn, String rmiSuffixNode, List<File> files, String myIPAddress )
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
                    String name = Toolkit.createBindLocation(fileReplicateList[i], rmiSuffixNode);
        			try {
        				TCPUtil tcpSender = new TCPUtil(null, 20000, Mode.SEND, newFile, null);
        				Thread t = new Thread(tcpSender);
        				t.start();
        				NodeToNodeInterface ntnI = (NodeToNodeInterface) Naming.lookup(name);
        				ntnI.startReceive(myIPAddress, 20000, newFile.getName());
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