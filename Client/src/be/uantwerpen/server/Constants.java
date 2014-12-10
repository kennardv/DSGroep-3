package be.uantwerpen.server;

import java.net.InetAddress;

import utils.Toolkit;

public final class Constants {
	public static final String SUFFIX_NODE_RMI = "ntn";
	public static final String SUFFIX_SERVER_RMI = "stn";
	public static final String SERVER_IP = "192.168.1.1"; //localhost or 192.168.1.x
	public static final String SERVER_PATH_RMI = Toolkit.createBindLocation(SERVER_IP, SUFFIX_SERVER_RMI);
	
	//public static final String CLIENT_PATH_RMI = Toolkit.createBindLocation(InetAddress.getLocalHost().getHostAddress(), RMI_SUFFIX_NODE);
	
	public static final String MY_FILES_PATH = ".\\src\\resources\\myfiles";
	public static final String REPLICATES_PATH = ".\\src\\resources\\replicates\\";
	
	public static final String MULTICAST_IP = "226.100.100.125";
	public static final int SOCKET_PORT_UDP = 4545;
	public static final int SOCKET_PORT_TCP = 20000;
	public static final int REGISTRY_PORT = 1099;
}
