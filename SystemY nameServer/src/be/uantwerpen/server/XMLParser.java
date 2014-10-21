package be.uantwerpen.server;

import java.io.File;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class XMLParser {

	private static String PATH = "ip-list.xml";

	public static String name;
	public static String ipaddress;
	public static String[] filename;


	public XMLParser() {		
	}

	public static Client jaxbXMLToObject() {
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

	public static void jaxbObjectToXML(Clients clients) {

		try {
			JAXBContext context = JAXBContext.newInstance(Clients.class);
			Marshaller m = context.createMarshaller();
			
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	

			 //Marshal the employees list in console
		    m.marshal(clients, System.out);
		     
		    //Marshal the employees list in file
		    m.marshal(clients, new File(PATH));
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Add a new node with a name and ipaddress
	 * 
	 * public static void addNode(int hashedName, String invoerIpaddress) { try
	 * { File xmlFile = new File(path); // Create the documentBuilderFactory
	 * DocumentBuilderFactory documentBuilderFactory =
	 * DocumentBuilderFactory.newInstance();
	 * 
	 * // Create the documentBuilder DocumentBuilder documentBuilder =
	 * documentBuilderFactory.newDocumentBuilder();
	 * 
	 * // Create the Document by parsing the file Document document =
	 * documentBuilder.parse(xmlFile);
	 * 
	 * // Get the root element of the xml Document; Element documentElement =
	 * document.getDocumentElement();
	 * 
	 * // Get childNodes of the rootElement // Create a textNode element Element
	 * ipaddress = document.createElement("ipaddress");
	 * ipaddress.setTextContent(invoerIpaddress);
	 * 
	 * Element name = document.createElement("name");
	 * name.setTextContent(hashedName + "");
	 * 
	 * /*Element bestandsnaam = document.createElement("filename"); for (int i =
	 * 0; i < filename.length; i++) { Element valueFileName =
	 * document.createElement("value");
	 * valueFileName.appendChild(document.createTextNode(filename[i]));
	 * bestandsnaam.appendChild(valueFileName); }
	 * 
	 * 
	 * 
	 * // Create a Node element Element nodeElement =
	 * document.createElement("node"); Attr attr =
	 * document.createAttribute("id");
	 * 
	 * // autoincrement NodeList ns = document.getElementsByTagName("node"); int
	 * el = ns.getLength() + 1;
	 * 
	 * attr.setValue(el + ""); nodeElement.setAttributeNode(attr);
	 * 
	 * // append textNode to Node element; nodeElement.appendChild(name);
	 * nodeElement.appendChild(ipaddress);
	 * //nodeElement.appendChild(bestandsnaam);
	 * 
	 * // append Node to rootNode element
	 * documentElement.appendChild(nodeElement);
	 * document.replaceChild(documentElement, documentElement); Transformer
	 * tFormer = TransformerFactory.newInstance() .newTransformer();
	 * 
	 * // Set output file to xml tFormer.setOutputProperty(OutputKeys.METHOD,
	 * "xml");
	 * 
	 * // Write the document back to the file Source source = new
	 * DOMSource(document); Result result = new StreamResult(xmlFile);
	 * tFormer.transform(source, result);
	 * 
	 * } catch (TransformerException ex) {
	 * Logger.getLogger(XMLParser.class.getName()).log(Level.SEVERE, null, ex);
	 * } catch (SAXException ex) {
	 * Logger.getLogger(XMLParser.class.getName()).log(Level.SEVERE, null, ex);
	 * } catch (IOException ex) {
	 * Logger.getLogger(XMLParser.class.getName()).log(Level.SEVERE, null, ex);
	 * } catch (ParserConfigurationException ex) {
	 * Logger.getLogger(XMLParser.class.getName()).log(Level.SEVERE, null, ex);
	 * } }
	 * 
	 * 
	 * Needs review /*File f = new File(path); if (!f.exists()) { try {
	 * DocumentBuilderFactory docFactory = DocumentBuilderFactory
	 * .newInstance(); DocumentBuilder docBuilder =
	 * docFactory.newDocumentBuilder();
	 * 
	 * // root elements Document doc = docBuilder.newDocument(); Element
	 * rootElement = doc.createElement("ip-list"); doc.appendChild(rootElement);
	 * doc.replaceChild(documentElement, documentElement); Transformer tFormer =
	 * TransformerFactory.newInstance().newTransformer();
	 * 
	 * // Set output file to xml tFormer.setOutputProperty(OutputKeys.METHOD,
	 * "xml");
	 * 
	 * // Write the document back to the file Source source = new
	 * DOMSource(doc); Result result = new StreamResult(f);
	 * tFormer.transform(source, result);
	 * 
	 * System.out.println("File created!"); } catch
	 * (ParserConfigurationException pce) { pce.printStackTrace(); } catch
	 * (TransformerException tfe) { tfe.printStackTrace(); } } Add files to a
	 * specific node identified by name and ipaddress
	 * 
	 * public static void addFilesToNode(int hashedName, String ipaddress,
	 * String[] filenames) { try { File xmlFile = new File(path);
	 * 
	 * DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	 * DocumentBuilder docBuilder = docFactory.newDocumentBuilder(); Document
	 * doc = docBuilder.parse(xmlFile);
	 * 
	 * // Get the root element of the xml Document; Element root =
	 * doc.getDocumentElement();
	 * 
	 * //get name and ip elements in the document NodeList names =
	 * root.getElementsByTagName("name"); Element nameEl = null;
	 * 
	 * nameElements: { for (int i = 0; i < names.getLength(); i++) { nameEl =
	 * (Element)names.item(i); if (nameEl.getTextContent() == hashedName + "") {
	 * //break out for loop break nameElements; } } } NodeList ipaddresses =
	 * root.getElementsByTagName("ipaddress"); Element ipEl = null;
	 * 
	 * ipElements : { for (int i = 0; i < ipaddresses.getLength(); i++) { ipEl =
	 * (Element)ipaddresses.item(i); if (ipEl.getTextContent() == ipaddress) {
	 * break ipElements; } } }
	 * 
	 * //check if name and ip belong to same node if(nameEl.getParentNode() ==
	 * ipEl.getParentNode()) { Element parent = (Element)nameEl.getParentNode();
	 * Element files = (Element)parent.getLastChild(); if (files.getNodeName()
	 * != "files") { files = doc.createElement("files");
	 * parent.appendChild(files); } for (int i = 0; i < filenames.length; i++) {
	 * Element name = doc.createElement("name");
	 * name.appendChild(doc.createTextNode(filenames[i]));
	 * files.appendChild(name); }
	 * 
	 * // append Node to rootNode element root.appendChild(parent);
	 * doc.replaceChild(root, root); Transformer tFormer =
	 * TransformerFactory.newInstance() .newTransformer();
	 * 
	 * // Set output file to xml tFormer.setOutputProperty(OutputKeys.METHOD,
	 * "xml");
	 * 
	 * // Write the document back to the file Source source = new
	 * DOMSource(doc); Result result = new StreamResult(xmlFile);
	 * tFormer.transform(source, result); } } catch (Exception e) {} }
	 */
}