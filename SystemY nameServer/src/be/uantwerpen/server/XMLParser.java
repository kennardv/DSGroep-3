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
import org.w3c.dom.NodeList;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.xml.sax.SAXException;

public class XMLParser {

	private static String path = "ip-list.xml";

	
	public static String name;
	public static String ipaddress;
	public static String[] filename;
	
	XMLParser (String pName, String pIpaddress, String[] pFilename) {
		
		name = pName;
		ipaddress = pIpaddress;
		filename = pFilename;
	}
	
	
	public static void main(String[] args) {
	
		
		//kijkt of het bestand op de server al bestaat, indien niet wordt er een bestand aangemaakt
		File f = new File(path);
		if (f.exists()) {
			nodeToevoegen(name, ipaddress, filename);
			System.out.println("gelukt, bestand was aanwezig");
		} else {
			bestandMakenEnNodeToevoegen(name, ipaddress, filename);
			System.out
					.println("gelukt, bestand was niet aanwezig, er is een nieuw bestand aangemaakt");
		}

	}
	
	public static void nodeToevoegen(String invoerNaam, String invoerIpaddress, String invoerFilename) {
		try {
			File xmlFile = new File(path);
			// Create the documentBuilderFactory
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
					.newInstance();

			// Create the documentBuilder
			DocumentBuilder documentBuilder = documentBuilderFactory
					.newDocumentBuilder();

			// Create the Document by parsing the file
			Document document = documentBuilder.parse(xmlFile);

			// Get the root element of the xml Document;
			Element documentElement = document.getDocumentElement();

			// Get childNodes of the rootElement
			// Create a textNode element
			Element ipaddress = document.createElement("ipaddress");
			ipaddress.setTextContent(invoerIpaddress);
			
			Element name = document.createElement("name");
			name.setTextContent(invoerNaam);

			Element bestandsnaam = document.createElement("bestandsnaam");
			bestandsnaam.setTextContent(invoerFilename);

			// Create a Node element
			Element nodeElement = document.createElement("node");
			Attr attr = document.createAttribute("id");

			// autoincrement
			NodeList ns = document.getElementsByTagName("node");
			int el = ns.getLength() + 1;

			attr.setValue(el + "");
			nodeElement.setAttributeNode(attr);

			// append textNode to Node element;
			nodeElement.appendChild(name);
			nodeElement.appendChild(ipaddress);
			nodeElement.appendChild(bestandsnaam);
			
			// append Node to rootNode element
			documentElement.appendChild(nodeElement);
			document.replaceChild(documentElement, documentElement);
			Transformer tFormer = TransformerFactory.newInstance()
					.newTransformer();

			// Set output file to xml
			tFormer.setOutputProperty(OutputKeys.METHOD, "xml");

			// Write the document back to the file
			Source source = new DOMSource(document);
			Result result = new StreamResult(xmlFile);
			tFormer.transform(source, result);

		} catch (TransformerException ex) {
			Logger.getLogger(XMLParser.class.getName()).log(Level.SEVERE, null,
					ex);
		} catch (SAXException ex) {
			Logger.getLogger(XMLParser.class.getName()).log(Level.SEVERE, null,
					ex);
		} catch (IOException ex) {
			Logger.getLogger(XMLParser.class.getName()).log(Level.SEVERE, null,
					ex);
		} catch (ParserConfigurationException ex) {
			Logger.getLogger(XMLParser.class.getName()).log(Level.SEVERE, null,
					ex);
		}

	}

	public static void bestandMakenEnNodeToevoegen(String invoerNaam, String invoerIpaddress, String invoerFilename) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
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

			// name elements
			Element name = doc.createElement("name");
			name.appendChild(doc.createTextNode(invoerNaam));
			node.appendChild(name);

			// ip address elements
			Element ipaddress = doc.createElement("ipaddress");
			ipaddress.appendChild(doc.createTextNode(invoerIpaddress));
			node.appendChild(ipaddress);
			
			// bestandsnaam elements
			Element filename = doc.createElement("filename");
			filename.appendChild(doc.createTextNode(invoerFilename));
			node.appendChild(filename);

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(path));

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