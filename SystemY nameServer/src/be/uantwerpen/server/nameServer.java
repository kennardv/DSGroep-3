package be.uantwerpen.server;
import java.io.File;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class nameServer {
	
	static nodeHandling fileImpl;
	
	/* GET/SET */
	public static nodeHandling getFileImpl() {
		return fileImpl;
	}
	public static void setFileImpl(nodeHandling nodeHandling) {
		fileImpl = nodeHandling;
	}
	
    public static void main(String[] argv) throws RemoteException {
    	setFileImpl(new nodeHandling());
       String bindLocation = "//localhost/nameServer";
       try { 
			LocateRegistry.createRegistry(1099);
			Naming.bind(bindLocation, getFileImpl());
			//ReadXML();
	        System.out.println("FileServer Server is ready at:" + bindLocation);
            System.out.println("java RMI registry created.");
        } catch (MalformedURLException | AlreadyBoundException e) {
            System.out.println("java RMI registry already exists.");
        }
       
    }
    
    public static void ReadXML() {
    	try {
    		 
    		File fXmlFile = new File("ip-list.xml");
    		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    		Document doc = dBuilder.parse(fXmlFile);

    		doc.getDocumentElement().normalize();
    	 
    	 
    		NodeList nList = doc.getElementsByTagName("clients");
    	 
    	 
    		for (int temp = 0; temp < nList.getLength(); temp++) {
    	 
    			Node nNode = nList.item(temp);
    	 
    			//System.out.println("\nCurrent Element :" + nNode.getNodeName());
    			
    			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
    	 
    				Element eElement = (Element) nNode;
    				
    				fileImpl.nodeMap.put(eElement.getElementsByTagName("hashedName").item(0).getTextContent(), eElement.getElementsByTagName("IP").item(0).getTextContent());
    			}
    		}
    		fileImpl.printMap();
    	    } catch (Exception e) {
    	    	e.printStackTrace();
    	    }
    	
    }
}	