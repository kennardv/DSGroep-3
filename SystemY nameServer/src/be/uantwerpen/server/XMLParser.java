package be.uantwerpen.server;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.Node;
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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.SAXException;

public class XMLParser {

	private static File xmlFile = null;
	private static DocumentBuilderFactory documentBuilderFactory = null;
	private static DocumentBuilder documentBuilder = null;
	private static Document document = null;
	
	private static String path = "ip-list.xml";

	public static String name;
	public static String ipaddress;
	public static String[] filename;

	XMLParser() {
		//Needs review
		/*File f = new File(path);
		if (!f.exists()) {
			try {
				DocumentBuilderFactory docFactory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

				// root elements
				Document doc = docBuilder.newDocument();
				Element rootElement = doc.createElement("ip-list");
				doc.appendChild(rootElement);
				doc.replaceChild(documentElement, documentElement);
				Transformer tFormer = TransformerFactory.newInstance().newTransformer();

				// Set output file to xml
				tFormer.setOutputProperty(OutputKeys.METHOD, "xml");

				// Write the document back to the file
				Source source = new DOMSource(doc);
				Result result = new StreamResult(f);
				tFormer.transform(source, result);

				System.out.println("File created!");
			} catch (ParserConfigurationException pce) {
				pce.printStackTrace();
			} catch (TransformerException tfe) {
				tfe.printStackTrace();
			}
		}*/
	}
	
	/** 
	Add a new node with a name and ipaddress
	*/
	public static void addNode(int hashedName, String invoerIpaddress) {
		try {
			xmlFile = new File(path);
			// Create the documentBuilderFactory
			documentBuilderFactory = DocumentBuilderFactory.newInstance();

			// Create the documentBuilder
			documentBuilder = documentBuilderFactory.newDocumentBuilder();

			// Create the Document by parsing the file
			document = documentBuilder.parse(xmlFile);

			// Get the root element of the xml Document;
			Element documentElement = document.getDocumentElement();

			// Get childNodes of the rootElement
			// Create a textNode element
			Element ipaddress = document.createElement("ipaddress");
			ipaddress.setTextContent(invoerIpaddress);

			Element name = document.createElement("name");
			name.setTextContent(hashedName + "");

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
			//nodeElement.appendChild(bestandsnaam);

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
	
	/** 
	Add files to a specific node identified by name and ipaddress
	*/
	public static void addFilesToNode(int hashedName, String ipaddress, String[] filenames) {
		try {
		xmlFile = new File(path);
		
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilder = documentBuilderFactory.newDocumentBuilder();
		document = documentBuilder.parse(xmlFile);

		// Get the root element of the xml Document;
		Element root = document.getDocumentElement();
		
		//get name and ip elements in the document
	    NodeList names = root.getElementsByTagName("name");
	    Element nameEl = null;
	    
	    nameElements: {
		    for (int i = 0; i < names.getLength(); i++) {
				nameEl = (Element)names.item(i);
				if (nameEl.getTextContent() == hashedName + "") {
					//break out for loop
					break nameElements;
				}
			}
	    }
	    NodeList ipaddresses = root.getElementsByTagName("ipaddress");
	    Element ipEl = null;
	    
	    ipElements : {
	    	for (int i = 0; i < ipaddresses.getLength(); i++) {
				ipEl = (Element)ipaddresses.item(i);
				if (ipEl.getTextContent() == ipaddress) {
					break ipElements;
				}
			}
	    }
	    
	    //check if name and ip belong to same node
	    if(nameEl.getParentNode() == ipEl.getParentNode()) {
	    	Element parent = (Element)nameEl.getParentNode();
	    	Element files = (Element)parent.getLastChild();
	    	if (files.getNodeName() != "files") {
	    		files = document.createElement("files");
	    		parent.appendChild(files);
	    	}
			for (int i = 0; i < filenames.length; i++) {
				Element name = document.createElement("name");
				name.appendChild(document.createTextNode(filenames[i]));
				files.appendChild(name);
			}
			
			// append Node to rootNode element
	 		root.appendChild(parent);
	 		document.replaceChild(root, root);
	 		Transformer tFormer = TransformerFactory.newInstance()
	 				.newTransformer();

	 		// Set output file to xml
	 		tFormer.setOutputProperty(OutputKeys.METHOD, "xml");

	 		// Write the document back to the file
	 		Source source = new DOMSource(document);
	 		Result result = new StreamResult(xmlFile);
	 		tFormer.transform(source, result);
	    }
		} catch (Exception e) {}
	}
}