package be.uantwerpen.server;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
 
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class writeXML {
	public static void main(String argv[]) {
	  try {
		  
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("ip-list");
		doc.appendChild(rootElement);

		// staff elements
		Element node = doc.createElement("node");
		rootElement.appendChild(node);

		// set attribute to name element
		Attr attr = doc.createAttribute("id");
		attr.setValue("1");
		node.setAttributeNode(attr);
		
		// ip address elements
		Element name = doc.createElement("name");
		name.appendChild(doc.createTextNode("9914"));
		node.appendChild(name);

		// ip address elements
		Element ipaddress = doc.createElement("ipaddress");
		ipaddress.appendChild(doc.createTextNode("192.168.56.1"));
		node.appendChild(ipaddress);


		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File("C:\\Users\\asush\\Desktop\\file.xml"));

		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);

		transformer.transform(source, result);

		System.out.println("File saved!");

	  } catch (ParserConfigurationException pce) {
		pce.printStackTrace();
	  } catch (TransformerException tfe) {
		tfe.printStackTrace();
	  }
	}

}