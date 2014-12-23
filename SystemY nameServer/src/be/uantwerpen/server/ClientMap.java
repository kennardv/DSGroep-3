package be.uantwerpen.server;

import utils.*;

import java.util.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


@XmlRootElement(name = "clients")
@XmlAccessorType (XmlAccessType.FIELD)
public class ClientMap {
	
	private TreeMap<Integer, Client> clientMap = new TreeMap<Integer, Client>();
	@XmlTransient
	private XMLMarshaller marshaller;
	@XmlTransient
	private int lastAddedKey = -1;
	
	public ClientMap() {
		this.marshaller = new XMLMarshaller();
	}

	public Map<Integer, Client> getClientMap() {
		return clientMap;
	}

	public void setClientMap(TreeMap<Integer, Client> clientMap) {
		this.clientMap = clientMap;
	}
	
	/**
	 * Remove a key/value pair with specified key and update XML
	 * @param key
	 * This is the hashed name for this particular map.
	 */
	public void remove(int key) {
		this.clientMap.remove(key);
		
		marshaller.jaxbObjectToXML(this);
		System.out.println("Removed key from clientMap and updated XML");
	}
	
	/**
	 * Add a key/value pair with passed data and update XML. Value is of type Client
	 * @param hashedName
	 * @param ip
	 * @param filenames
	 */
	public void add(int hashedName, String ip, List<Integer> filenames) {
		//Instantiate new Client object
		Client c = new Client();
		c.setId(1);
		c.setName(hashedName);
		c.setIpaddress(ip);
		c.setFiles(filenames);
		
		//add to map
		this.clientMap.put(hashedName, c);

		// use parent clientMap to update XML file
		marshaller.jaxbObjectToXML(this);
		
		this.lastAddedKey = hashedName;
	}
	
	public int getLastAddedKey() {
		return this.lastAddedKey;
	}
	
	/**
	 * This method clears the TreeMap member variable 
	 * and clears the XML file
	 */
	public void clear() {
		this.clientMap.clear();
		marshaller.jaxbObjectToXML(this);
		System.out.println("Cleared XML file");
	}
}
