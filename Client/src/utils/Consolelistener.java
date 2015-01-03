package utils;

import be.uantwerpen.server.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import controller.MainController;
import model.Client;

public class Consolelistener extends Thread {
	
	   private String threadName;
	   private MainController client;
       private String s = null;
       private int hash;

	   public Consolelistener(MainController client, int hash){
	       System.out.println("Creating " +  threadName );
	       this.client = client;
	       this.hash = hash;
	   }
	   
	   public void run(){
	        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	        System.out.print("Enter String: ");
			try {
				this.s = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(this.s.equals("shutdown")){
				this.client.shutdown(this.hash);
			}
	   }
}

