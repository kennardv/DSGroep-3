package model;

import enumerations.*;
import networking.*;
import rmi.implementations.*;
import rmi.interfaces.*;
import utils.Callback;
import utils.Consolelistener;
import utils.Toolkit;

import java.net.*;
import java.net.UnknownHostException;
import java.io.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

import be.uantwerpen.server.Constants;
import agents.FileListAgent;

public class Client {

	/************* Set this for lonely testing ******************/
	/************************************************************/
	/************************************************************/
	public boolean useLocalHost = false;
	/************************************************************/
	/************************************************************/
	/************************************************************/


	//Info
	String nameClient = null;
	List<File> files = null;
	int[] filenames = null;

	//my hashes
	private int previousHash, currentHash, nextHash;

	//RMI vars
	private Registry registry = null;
	private NodeToNode ntn = null;
	private INodeToNode ntnI = null;
	//private IServerToNode stnI = null;
	private String rmiBindLocation = null;

	//TCP vars
	private String multicastAddress = null;

	private String myIPAddress = null;

	private Protocol sendProtocol;
	private Protocol receiveProtocol;

	//UDP vars
	private UDPUtil udpUtilListener = null;
	private Consolelistener conslisten;

	//file replication
	public String[] fileReplicateList = null;

	//Agents
	private FileListAgent fileListAgent = null;


	public String getNameClient() {
		return nameClient;
	}

	public void setNameClient(String nameClient) {
		this.nameClient = nameClient;
	}

	public List<File> getFiles() {
		return files;
	}

	public void setFiles(List<File> files) {
		this.files = files;
	}

	public int[] getFilenames() {
		return filenames;
	}

	public void setFilenames(int[] filenames) {
		this.filenames = filenames;
	}

	public int getPreviousHash() {
		return previousHash;
	}

	public void setPreviousHash(int previousHash) {
		this.previousHash = previousHash;
	}

	public int getCurrentHash() {
		return currentHash;
	}

	public void setCurrentHash(int currentHash) {
		this.currentHash = currentHash;
	}

	public int getNextHash() {
		return nextHash;
	}

	public void setNextHash(int nextHash) {
		this.nextHash = nextHash;
	}

	public String[] getFileReplicateList() {
		return fileReplicateList;
	}

	public void setFileReplicateList(String[] fileReplicateList) {
		this.fileReplicateList = fileReplicateList;
	}

	public String getMyIPAddress() {
		return myIPAddress;
	}

	public void setMyIPAddress(String myIPAddress) {
		this.myIPAddress = myIPAddress;
	}

	public Registry getRegistry() {
		return registry;
	}

	public void setRegistry(Registry registry) {
		this.registry = registry;
	}

	public NodeToNode getNtn() {
		return ntn;
	}

	public void setNtn(NodeToNode ntn) {
		this.ntn = ntn;
	}

	public INodeToNode getNtnI() {
		return ntnI;
	}

	public void setNtnI(INodeToNode ntnI) {
		this.ntnI = ntnI;
	}

	public String getRmiBindLocation() {
		return this.rmiBindLocation;
	}

	public void setRmiBindLocation(String rmiBindLocation) {
		this.rmiBindLocation = rmiBindLocation;
	}

	public FileListAgent getFileListAgent() {
		return fileListAgent;
	}

	public void setFileListAgent(FileListAgent fileListAgent) {
		this.fileListAgent = fileListAgent;
	}

	//ctor
	public Client() throws RemoteException, InterruptedException, IOException, ClassNotFoundException {
		//init();

		//bind remote object
		//bootstrap();
		//multicast and process answers
		//discover(InetAddress.getByName(Constants.MULTICAST_IP), Constants.SOCKET_PORT_UDP);

		

	    //listen for packets
		//this.udpUtilListener = new UDPUtil(this, Mode.RECEIVE);
		//Thread t = new Thread(this.udpUtilListener);
		//t.start();

		//this.conslisten = new Consolelistener(this, this.currentHash);
		//Thread t2 = new Thread(this.conslisten);
		//t2.start();
	}
	
	

	/*public static void main(String argv[]) throws InterruptedException, IOException, ClassNotFoundException {
		Client client = new Client();
		
	}*/
}
