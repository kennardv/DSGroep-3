package be.uantwerpen.server;

import java.net.*;
import java.net.UnknownHostException;
import java.io.*;
import java.rmi.*;

public class Client{
   public static void main(String argv[]) {
	   String nameClient = "Test-client";
      try {
         String name = "//localhost/nameServer";
         nodeHandlingInterface fi = (nodeHandlingInterface) Naming.lookup(name);

         String[] arrayInfo = fi.connect(nameClient);
         String[] arrayFiles = {"1.mp3", "xxx.jpg"};         
         fi.giveFiles(arrayInfo[0], arrayInfo[1], arrayFiles);

      } catch(Exception e) {
         System.err.println("FileServer exception: "+ e.getMessage());
         e.printStackTrace();
      }
     
   }
   

}