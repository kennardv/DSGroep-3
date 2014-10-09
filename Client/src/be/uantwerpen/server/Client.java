package be.uantwerpen.server;

import java.net.*;
import java.net.UnknownHostException;
import java.io.*;
import java.rmi.*;

public class Client{
   public static void main(String argv[]) {

      try {
         String name = "//localhost/nameServer";
         nodeHandlingInterface fi = (nodeHandlingInterface) Naming.lookup(name);

         System.out.println("u ip adress is:" + fi.connect());
      } catch(Exception e) {
         System.err.println("FileServer exception: "+ e.getMessage());
         e.printStackTrace();
      }
   }
}