package sk.fiit.sipvs.cv.models;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
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

	private static final String CANONICALIZATION_XPATH = "//*[local-name()='DataEnvelope']/*[local-name()='Signature']/*[local-name()='SignedInfo']/*[local-name()='CanonicalizationMethod']";
	private static final String SIGNATURE_XPATH = "//*[local-name()='DataEnvelope']/*[local-name()='Signature']/*[local-name()='SignedInfo']/*[local-name()='SignatureMethod']";
	
	private static final String REFERENCE_XPATH = "//*[local-name()='DataEnvelope']/*[local-name()='Signature']/*[local-name()='SignedInfo']/*[name()='Reference']";
	private static final String TRANSFORM_XPATH = "*[local-name()='Transforms']/*[local-name()='Transform']";
	private static final String DIGEST_XPATH = "*[local-name()='DigestMethod']";
	
	// Xades ZEP 4.3.1.1
	private static final Set<String> CANONICALIZATION_METHODS = new HashSet<String>(Arrays.asList(
			new String[] {"http://www.w3.org/TR/2001/REC-xml-c14n-20010315"}
	));
	
	// Xades ZEP 4.5
	private static final Set<String> SIGNATURE_METHODS = new HashSet<String>(Arrays.asList(
			new String[] {"dsa-sha1", "rsa-sha1", "rsa-sha256", "rsa-sha384", "rsa-sha512"}
	));

	// Xades ZEP 4.3.1.3.1
	private static final Set<String> TRANSFORM_METHODS = new HashSet<String>(Arrays.asList(
			new String[] {"http://www.w3.org/TR/2001/REC-xml-c14n-20010315"}
	));

	// Xades ZEP 4.5
	private static final Set<String> DIGEST_METHODS = new HashSet<String>(Arrays.asList(
			new String[] {"sha1", "sha224", "sha256", "sha384", "sha512"}
	));
	
	private Document doc;
	private Logger logger = LogManager.getLogger(SignedXml.class.getName());

	private XPath xpath;
	
	public SignedXml(File xmlFile) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(true);
		
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		xpath = XPathFactory.newInstance().newXPath();
		
		doc = dBuilder.parse(xmlFile);
		logger.info(String.format("File '%s' was sucessfully parsed.", xmlFile.getName()));
	}
		
	public void verify() throws SignVerificationException, XPathExpressionException {
		verifyXmlnsAttributes();
		verifySignatureAlgorithms();
		verifyTransformAndDigest();
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

		Element item = (Element) xpath.compile(CANONICALIZATION_XPATH).evaluate(doc, XPathConstants.NODE);
		logger.debug(item);
		if (!CANONICALIZATION_METHODS.contains(parseNodeAttrib(item, "Algorithm"))){
			throw new SignVerificationException(String.format("Unsupported algorithm '%s' in element 'ds:CanonicalizationMethod'.", parseNodeAttrib(item, "Algorithm")));
		}
		
		item = (Element) xpath.compile(SIGNATURE_XPATH).evaluate(doc, XPathConstants.NODE);
		String algorithm = parseNodeAttrib(item, "Algorithm").substring(parseNodeAttrib(item, "Algorithm").lastIndexOf('#') + 1).toLowerCase();
		if (!SIGNATURE_METHODS.contains(algorithm)){
			throw new SignVerificationException(String.format("Unsupported algorithm '%s' in element 'ds:SignatureMethod'.", algorithm));
		}
	}
	
	/*
	 * kontrola obsahu ds:Transforms a ds:DigestMethod vo všetkých referenciách 
	 * v ds:SignedInfo – musia obsahovať URI niektorého z podporovaných algoritmov podľa profilu XAdES_ZEP,
	 */
	private void verifyTransformAndDigest() throws XPathExpressionException, SignVerificationException {
		
		NodeList nodes = (NodeList) xpath.compile(REFERENCE_XPATH).evaluate(doc, XPathConstants.NODESET);
		
		for(int i=0, size=nodes.getLength(); i < size; i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element item = (Element) xpath.compile(TRANSFORM_XPATH).evaluate(node, XPathConstants.NODE);
				if (!TRANSFORM_METHODS.contains(parseNodeAttrib(item, "Algorithm"))){
					throw new SignVerificationException(String.format("Unsupported algorithm '%s' in element 'ds:Transform'.", item.getAttribute("Algorithm")));
				}
				
				item = (Element) xpath.compile(DIGEST_XPATH).evaluate(node, XPathConstants.NODE);
				String algorithm = parseNodeAttrib(item, "Algorithm").substring(item.getAttribute("Algorithm").lastIndexOf('#') + 1).toLowerCase();
				if (!DIGEST_METHODS.contains(algorithm.replace("-", ""))){
					throw new SignVerificationException(String.format("Unsupported algorithm '%s' in element 'ds:DigestMethod'.", algorithm));
				}
			}
		}
	}
	
	private String parseNodeAttrib(Element node, String attribName){
		if (node == null){
			return "";
		}
		
		return node.getAttribute(attribName);
	}
}
