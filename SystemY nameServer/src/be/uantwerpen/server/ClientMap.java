package be.uantwerpen.server;

import java.util.List;
import java.util.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "clients")
@XmlAccessorType (XmlAccessType.FIELD)
public class ClientMap {
	
	private Map<Integer, Client> clientMap = new HashMap<Integer, Client>();

	public Map<Integer, Client> getClientMap() {
		return clientMap;
	}

	public void setClientMap(Map<Integer, Client> clientMap) {
		this.clientMap = clientMap;
	}
	
	public void removeKeyValuePair(int key) {
		clientMap.remove(key);
	}
}
