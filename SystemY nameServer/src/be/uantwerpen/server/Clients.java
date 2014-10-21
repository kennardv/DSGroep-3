package be.uantwerpen.server;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "clients")
@XmlAccessorType (XmlAccessType.FIELD)
public class Clients {
	
	public Clients() {
		
	}

	@XmlElement(name = "client")
	private List<Client> clients = null;

	public List<Client> getClients() {
		return clients;
	}

	public void setClients(List<Client> clients) {
		this.clients = clients;
	}

}
