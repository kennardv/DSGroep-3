package be.uantwerpen.server;

import java.util.*;
import javax.xml.bind.annotation.*;

@XmlRootElement(name = "nodes")
@XmlAccessorType (XmlAccessType.FIELD)
@XmlType(propOrder = {"id", "name", "ipaddress", "filename"})
public class Client {
	//id geen property denk ik maar als atribute voor node
	//anders werkt de check niet om duplicates in de hashmap te vermijden
	private int id;
	private int name;
	private String ipaddress;
	
	@XmlElementWrapper(name="filenames")
    @XmlElement
	private List<Integer> filename;


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getName() {
		return name;
	}

	public void setName(int name) {
		this.name = name;
	}

	public String getIpaddress() {
		return ipaddress;
	}

	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}
	
	public List<Integer> getFiles() {
		return this.filename;
	}
	
	public void setFiles(List<Integer> files) {
		//this.filesObj = new Files();
		this.filename = files;
	}
}
