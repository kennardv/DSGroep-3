package utils;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import be.uantwerpen.server.ClientMap;

public class XMLMarshaller {

	private String PATH = "ip-list.xml";

	/**
	 * Return data in the XML file
	 * @return All map of all clients
	 */
	public ClientMap jaxbXMLToObject() {
		try {
			JAXBContext context = JAXBContext.newInstance(ClientMap.class);
			Unmarshaller un = context.createUnmarshaller();
			ClientMap clientMap = (ClientMap)un.unmarshal(new File(PATH));
			for (Integer clientId : clientMap.getClientMap().keySet()) {
				System.out.println(clientMap.getClientMap().get(clientId).getName());
				System.out.println(clientMap.getClientMap().get(clientId).getIpaddress());
			}
			return clientMap;
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Convert ClientMap object to XML
	 * @param clientMap
	 * map to be converted
	 */
	public void jaxbObjectToXML(ClientMap clientMap) {

		try {
			JAXBContext context = JAXBContext.newInstance(ClientMap.class);
			Marshaller m = context.createMarshaller();
			
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	

			 //Marshal the employees list in console
		    m.marshal(clientMap, System.out);
		     
		    //Marshal the employees list in file
		    m.marshal(clientMap, new File(PATH));
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
}