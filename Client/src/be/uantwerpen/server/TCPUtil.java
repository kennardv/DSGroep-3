package be.uantwerpen.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class TCPUtil extends Thread {
	
	ServerSocket ssocket = null;
	Socket socket = null;
	
	String[] ipaddress = null;
	int port = 0;
	boolean send = false;
	File[] file = null;
	
	public TCPUtil(String[] ipaddress, int port, boolean send, List<File> files ) throws IOException {
		this.ipaddress = ipaddress;
		this.port = port;
		this.send = send;
		this.file = file;
	}
	public TCPUtil(int port, boolean send) throws IOException {
		this.port = port;
		this.send = send;
	}
	public void run() {
		if (send) {
			try {
				int i = 0;
				boolean done = false;
				for(i=0;i< file.length; i++)
				{
				 sendFilesOverTCP(file[i], this.port);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				for(int i=0;i< file.length; i++)
				{
					receiveFilesOverTCP(ipaddress[i], this.port, "C:\\Users\\Kennard\\test2.txt");
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
     * Send multiple files over a socket via TCP
     * @param filenames
     * a string array containing all the filenames 
     * @param port
     * what port to send the files over
     * @throws IOException
     */
    boolean sendFilesOverTCP(File file, int port) throws IOException  {
    	this.ssocket = new ServerSocket(this.port);
    	this.socket = this.ssocket.accept();
    	
    	File transferFile = file;
    	byte[] bytearray  = new byte [(int)transferFile.length()];
    	FileInputStream fin = null;
    	
    	try {
    		fin = new FileInputStream(transferFile);
    	} catch (IOException e) { }
    	
    	BufferedInputStream bin = null;
    	OutputStream os = null;
    	
    	try {
    		bin = new BufferedInputStream(fin);
    	} catch (Exception e) { }
    	try {
    		bin.read(bytearray,0,bytearray.length);
    		os = this.socket.getOutputStream();
		 } catch (IOException e) {}
		 
		 
		 System.out.println("Sending Files...");
		 try {
			 os.write(bytearray,0,bytearray.length);
			 os.flush();
		     this.socket.close();
		     //socket.close(); don't close server socket
		 } catch(IOException e) {}
		 
		 System.out.println("File transfer complete");
		 return true;
    }
    
    /**
     * Receive multiple files over a socket via TCP
     * @param ip
     * What host to receive on, passing null is equal to loopback 
     * @param port
     * @throws UnknownHostException
     * @throws IOException
     */
    void receiveFilesOverTCP(String ip, int port, String pathToSaveTo) throws UnknownHostException, IOException {
    	
    	Socket socket = null;
    	
    	try {
    		socket = new Socket(ip,port);
    	} catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + ip);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                               + "the connection to: " + ip);
        }
    	
    	int filesize=1022386; 
	    int bytesRead;
	    int currentTot = 0;
	    
	    byte[] bytearray  = new byte[filesize];
	    InputStream is = socket.getInputStream();
	    FileOutputStream fos = new FileOutputStream(pathToSaveTo);
	    BufferedOutputStream bos = new BufferedOutputStream(fos);
	    bytesRead = is.read(bytearray,0,bytearray.length);
	    currentTot = bytesRead;

	    do {
	       bytesRead =
	          is.read(bytearray, currentTot, (bytearray.length-currentTot));
	       if(bytesRead >= 0) currentTot += bytesRead;
	    } while(bytesRead > -1);

	    bos.write(bytearray, 0 , currentTot);
	    bos.flush();
	    bos.close();
	    socket.close();
    }
    
    /***
     * Helper function to convert the contents of a file to a byte array
     * @
     * param path
     * path to the file
     * @return bFile
     * byte array of the contents of the file
     */
    byte[] fileToByteArr(File f) {
    	FileInputStream fis = null;
    	 
        File file = f;
 
        byte[] bFile = new byte[(int) file.length()];
 
        try {
            //convert file into array of bytes
		    fis = new FileInputStream(file);
		    fis.read(bFile);
		    fis.close();
		    
		    System.out.println("Contents of byte array.");
		    for (int i = 0; i < bFile.length; i++) {
		    	System.out.print((char)bFile[i]);
            }
 
		    System.out.println("Done");
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        return bFile;
    }
    
    byte[] intToBytes(int my_int) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeInt(my_int);
        out.close();
        byte[] int_bytes = bos.toByteArray();
        bos.close();
        return int_bytes;
    }
}
