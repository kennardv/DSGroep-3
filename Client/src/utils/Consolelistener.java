package utils;

import be.uantwerpen.server.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Consolelistener extends Thread {
	
	
	   private Thread t;
	   private String threadName;
	   private Client client;
	   private List<Object> message;
	   
	   Consolelistener(String name, Client clientinput, List<Object> messageinput){
	       threadName = name;
	       System.out.println("Creating " +  threadName );
	       this.client = clientinput;
	   }
	   
	   public void run(){
	        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	        System.out.print("Enter String \n");
	        String s;
			try {
				s = br.readLine();
				this.client.shutdown(this.message);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   }
	   
	   public void start ()
	   {
	      System.out.println("Starting " +  threadName );
	      if (t == null)
	      {
	         t = new Thread (this, threadName);
	         t.start ();
	      }
	   }

	}

