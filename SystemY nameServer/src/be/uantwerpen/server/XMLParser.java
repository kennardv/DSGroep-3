package be.uantwerpen.server;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLParser {

	private static String PATH = "ip-list.xml";

	public static String name;
	public static String ipaddress;
	public static String[] filename;

	XMLParser() {
		
	}

	public static void main(String[] args) {
				try{
					
			
			    JAXBContext context = JAXBContext.newInstance(Client.class);

			    Marshaller m = context.createMarshaller();
			    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			    String[] exampleStringArray = {"tekst.txt", "geluid.mp3", "worddoc.docx"};
			    Client object = new Client();
			    object.setId(0);
			    object.setName(45646513);
			    object.setIpaddress("192.168.1.2");
			    object.setFiles(exampleStringArray);
			    jaxbObjectToXML(object);
			    m.marshal(object, System.out);
			    
				}catch(JAXBException e){
					 System.err.println("Caught IOException: " + e.getMessage());
				}
	}
	
	  private static Client jaxbXMLToObject() {
	        try {
	            JAXBContext context = JAXBContext.newInstance(Client.class);
	            Unmarshaller un = context.createUnmarshaller();
	            Client client = (Client) un.unmarshal(new File(PATH));
	            return client;
	        } catch (JAXBException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }
	 
	  private static void jaxbObjectToXML(Client client) {
		  
	        try {
	            JAXBContext context = JAXBContext.newInstance(Client.class);
	            Marshaller m = context.createMarshaller();
	            //for pretty-print XML in JAXB
	            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	 
	            // Write to System.out for debugging
	            // m.marshal(client, System.out);
	 
	            // Write to File
	            m.marshal(client, new File(PATH));
	        } catch (JAXBException e) {
	            e.printStackTrace();
	        }
	    }
	
	/** 
	Add a new node with a name and ipaddress
	
	public static void addNode(int hashedName, String invoerIpaddress) {
		try {
			File xmlFile = new File(path);
			// Create the documentBuilderFactory
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

			// Create the documentBuilder
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

			// Create the Document by parsing the file
			Document document = documentBuilder.parse(xmlFile);

			// Get the root element of the xml Document;
			Element documentElement = document.getDocumentElement();

			// Get childNodes of the rootElement
			// Create a textNode element
			Element ipaddress = document.createElement("ipaddress");
			ipaddress.setTextContent(invoerIpaddress);

			Element name = document.createElement("name");
			name.setTextContent(hashedName + "");

			/*Element bestandsnaam = document.createElement("filename");
			for (int i = 0; i < filename.length; i++) {
				Element valueFileName = document.createElement("value");
				valueFileName.appendChild(document.createTextNode(filename[i]));
				bestandsnaam.appendChild(valueFileName);
			}



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
	
	
	 Needs review
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
		}
	Add files to a specific node identified by name and ipaddress
	
	public static void addFilesToNode(int hashedName, String ipaddress, String[] filenames) {
		try {
		File xmlFile = new File(path);
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(xmlFile);

		// Get the root element of the xml Document;
		Element root = doc.getDocumentElement();
		
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
	    		files = doc.createElement("files");
	    		parent.appendChild(files);
	    	}
			for (int i = 0; i < filenames.length; i++) {
				Element name = doc.createElement("name");
				name.appendChild(doc.createTextNode(filenames[i]));
				files.appendChild(name);
			}
			
			// append Node to rootNode element
	 		root.appendChild(parent);
	 		doc.replaceChild(root, root);
	 		Transformer tFormer = TransformerFactory.newInstance()
	 				.newTransformer();

	 		// Set output file to xml
	 		tFormer.setOutputProperty(OutputKeys.METHOD, "xml");

	 		// Write the document back to the file
	 		Source source = new DOMSource(doc);
	 		Result result = new StreamResult(xmlFile);
	 		tFormer.transform(source, result);
	    }
		} catch (Exception e) {}
	}
	*/
}