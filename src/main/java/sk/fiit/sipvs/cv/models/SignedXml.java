package sk.fiit.sipvs.cv.models;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import exceptions.SignVerificationException;

public class SignedXml {

	private static final String CANONICALIZATION_XPATH = "//DataEnvelope/*[name()='ds:Signature']/*[name()='ds:SignedInfo']/*[name()='ds:CanonicalizationMethod']";
	private static final String SIGNATURE_XPATH = "//DataEnvelope/*[name()='ds:Signature']/*[name()='ds:SignedInfo']/*[name()='ds:SignatureMethod']";
	
	// Xades ZEP 4.3.1.1
	private static final Set<String> CANONICALIZATION_METHODS = new HashSet<String>(Arrays.asList(
			new String[] {"http://www.w3.org/TR/2001/REC-xml-c14n-20010315"}
	));
	
	// Xades ZEP 4.5
	private static final Set<String> SIGNATURE_METHODS = new HashSet<String>(Arrays.asList(
			new String[] {"dsa-sha1", "rsa-sha1", "rsa-sha256", "rsa-sha384", "rsa-sha512"}
	));
	
	private Document doc;
	private Logger logger = LogManager.getLogger(SignedXml.class.getName());

	private XPath xpath;
	private XPathExpression xPathCanonicalizationMethod;
	private XPathExpression xPathSignatureMethod;
	
	public SignedXml(File xmlFile) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(true);
		
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		xpath = XPathFactory.newInstance().newXPath();
		
		xpath.setNamespaceContext(getNameSpaceContext());
		compileXPathExpressions();
		
		doc = dBuilder.parse(xmlFile);
		logger.info(String.format("File '%s' was sucessfully parsed.", xmlFile.getName()));
	}

	private NamespaceContext getNameSpaceContext(){
		return new NamespaceContext() {
		    public String getNamespaceURI(String prefix) {
		    	if (prefix.equals("xzep")){
		    		return "http://www.ditec.sk/ep/signature_formats/xades_zep/v1.0";
		    	} else if (prefix.equals("ds")) {
		    		return "http://www.w3.org/2000/09/xmldsig";
		    	} else {
		    		return null;
		    	}
		    }

		    public Iterator<?> getPrefixes(String val) {
		        return null;
		    }

		    public String getPrefix(String uri) {
		        return null;
		    }
		};
	}
	
	private void compileXPathExpressions() throws XPathExpressionException {
		xPathCanonicalizationMethod = xpath.compile(CANONICALIZATION_XPATH);
		xPathSignatureMethod = xpath.compile(SIGNATURE_XPATH);
		// xpath_xmls_ds = (XPathExpression) xpath.compile("/xzep:DataEnvelope/");
	}
	
	public void verify() throws SignVerificationException, XPathExpressionException {
		verifyXmlnsAttributes();
		verifySignatureAlgorithms();
	}
	
	/*
	 * koreňový element musí obsahovať atribúty xmlns:xzep a xmlns:ds podľa profilu XADES_ZEP.
	 */
	private void verifyXmlnsAttributes() throws SignVerificationException {
		if(doc.getDocumentElement().getAttribute("xmlns:xzep").isEmpty()){
			throw new SignVerificationException("Attribute 'xmlns:xzep' is missing in root element.'");
		}
		
		NodeList childs = doc.getDocumentElement().getChildNodes();
		for(int i=0, size=childs.getLength(); i < size; i++) {
			Node node = childs.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element item = (Element) node;
				if(item.getAttribute("xmlns:ds").isEmpty()){
					throw new SignVerificationException(String.format("Attribute 'xmlns:ds' is missing in '%s' element.", item.getNodeName()));
				}
			}
			
        }
	}
	
	/*
	 * kontrola obsahu ds:SignatureMethod a ds:CanonicalizationMethod – musia obsahovať URI niektorého
	 * z podporovaných algoritmov pre dané elementy podľa profilu XAdES_ZEP
	 */
	private void verifySignatureAlgorithms() throws SignVerificationException, XPathExpressionException {
		logger.debug("//xzep:DataEnvelope/*[name()='ds:Signature']/*[name()='ds:SignedInfo']/*[name()='ds:CanonicalizationMethod']");
		
		// Node node = (Node) xpath.evaluate("//xzep:DataEnvelope/*[name()='ds:Signature']/*[name()='ds:SignedInfo']/*[name()='ds:CanonicalizationMethod']", doc, XPathConstants.NODE);
		Element item = (Element) xPathCanonicalizationMethod.evaluate(doc, XPathConstants.NODE);
		if (!CANONICALIZATION_METHODS.contains(item.getAttribute("Algorithm"))){
			throw new SignVerificationException(String.format("Unsupported algorithm '%s' in element 'ds:CanonicalizationMethod'.", item.getAttribute("Algorithm")));
		}
		
		item = (Element) xPathSignatureMethod.evaluate(doc, XPathConstants.NODE);
		String algorithm = item.getAttribute("Algorithm").substring(item.getAttribute("Algorithm").lastIndexOf('#') + 1).toLowerCase();
		if (!SIGNATURE_METHODS.contains(algorithm)){
			throw new SignVerificationException(String.format("Unsupported algorithm '%s' in element 'ds:SignatureMethod'.", algorithm));
		}
	}
}
