package networking;

import enumerations.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import be.uantwerpen.server.Constants;

public class TCPUtil extends Thread {
	
	private Mode mode;
	
	ServerSocket ssocket = null;
	Socket socket = null;
	
	String ipaddress = null;
	
	File file = null;
	String fileName = null;
	
	public TCPUtil(String ipaddress, Mode mode, File file, String fileName) throws IOException {
		this.ipaddress = ipaddress;
		this.mode = mode;
		this.file = file;
		this.fileName = fileName;
	}
	
	public void run() {
		try {
			switch (this.mode) {
			case SEND:
				sendFilesOverTCP(this.file, Constants.SOCKET_PORT_TCP);
				break;
			case RECEIVE:
				receiveFilesOverTCP(this.ipaddress, Constants.SOCKET_PORT_TCP, Constants.REPLICATES_PATH + fileName);
				break;
			default:
				break;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
     * Send a file over a socket via TCP
     * @param filenames
     * a string array containing all the filenames 
     * @param port
     * what port to send the files over
     * @throws IOException
     */
    void sendFilesOverTCP(File file, int port) throws IOException  {
    	System.out.println("Starting send file");
    	this.ssocket = new ServerSocket(port);
    	System.out.println("Serversocket opened");
    	this.socket = this.ssocket.accept();
    	
    	File transferFile = file;
    	byte[] bytearray  = new byte [(int)transferFile.length()];
    	FileInputStream fin = null;
    	System.out.println(bytearray.length);
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
		     this.ssocket.close();
		     //socket.close(); don't close server socket
		 } catch(IOException e) {}
		 
		 System.out.println("File transfer complete");
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
    	System.out.println("part1");
    	try {
    		socket = new Socket(ip,port);
    	} catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + ip);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                               + "the connection to: " + ip);
        }
    	System.out.println("part2");

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
}