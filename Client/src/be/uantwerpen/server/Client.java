package be.uantwerpen.server;

import java.net.*;
import java.net.UnknownHostException;
import java.io.*;
import java.rmi.*;

public class Client{
   public static void main(String argv[]) {
	   String nameClient = "Client1";
	   String[] filenames = {"file1.jpg", "file2.txt", "file3.gif"};
	   
	   try {
		   String name = "//localhost/nameServer";
		   nodeHandlingInterface fi = (nodeHandlingInterface) Naming.lookup(name);
		   try {
			   String[] arrayInfo = fi.connect(nameClient, filenames);
		   } catch (RemoteException e) {
			   System.out.println(e);
		   }

      } catch(Exception e) {
    	  System.err.println("FileServer exception: "+ e.getMessage());
    	  e.printStackTrace();
      }
     
   }
}