package sk.fiit.sipvs.cv.models;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
//import org.springframework.security.crypto.codec.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import exceptions.SignVerificationException;

/**
 * CHECKLIST & TODO
 *
 *
 *	OK - koreňový element musí obsahovať atribúty xmlns:xzep a xmlns:ds podľa profilu XADES_ZEP.
 *	OK - kontrola obsahu ds:SignatureMethod a ds:CanonicalizationMethod – musia obsahovať URI niektorého z podporovaných algoritmov pre dané elementy podľa profilu XAdES_ZEP
 *	OK - kontrola obsahu ds:Transforms a ds:DigestMethod vo všetkých referenciách v ds:SignedInfo – musia obsahovať URI niektorého z podporovaných algoritmov podľa profilu XAdES_ZEP
 *	TODO - overenie hodnoty podpisu ds:SignatureValue a referencií v ds:SignedInfo
 *			- dereferencovanie URI, kanonikalizácia referencovaných ds:Manifest elementov a overenie hodnôt odtlačkov ds:DigestValue
 *			- kanonikalizácia ds:SignedInfo a overenie hodnoty ds:SignatureValue pomocou pripojeného podpisového certifikátu v ds:KeyInfo,
 * 	OK - ds:Signature musí mať Id atribút, musí mať špecifikovaný namespace xmlns:ds
 * 	OK - ds:SignatureValue – musí mať Id atribút
 * 	OK - overenie existencie referencií v ds:SignedInfo a hodnôt atribútov Id a Type voči profilu XAdES_ZEP pre:
 * 			- ds:KeyInfo element,
 * 			- ds:SignatureProperties element,
 * 			- xades:SignedProperties element,
 * 			- všetky ostatné referencie v rámci ds:SignedInfo musia byť referenciami na ds:Manifest elementy,
 * 	OK - overenie obsahu ds:KeyInfo:
 * 			OK - musí mať Id atribút,
 * 			OK - musí obsahovať ds:X509Data, ktorý obsahuje elementy: ds:X509Certificate, ds:X509IssuerSerial, ds:X509SubjectName,
 * 	TODO 	- hodnoty elementov ds:X509IssuerSerial a ds:X509SubjectName súhlasia s príslušnými hodnatami v certifikáte, ktorý sa nachádza v ds:X509Certificate,
 *	OK - overenie obsahu ds:SignatureProperties:
 * 			- musí mať Id atribút,
 * 			- musí obsahovať dva elementy ds:SignatureProperty pre xzep:SignatureVersion a xzep:ProductInfos,
 * 			- obidva ds:SignatureProperty musia mať atribút Target nastavený na ds:Signature,
 * 	TODO - overenie ds:Manifest elementov:
 * 			- každý ds:Manifest element musí mať Id atribút,
 * 			- ds:Transforms musí byť z množiny podporovaných algoritmov pre daný element podľa profilu XAdES_ZEP,
 * 			- ds:DigestMethod – musí obsahovať URI niektorého z podporovaných algoritmov podľa profilu XAdES_ZEP,
 * 			- overenie hodnoty Type atribútu voči profilu XAdES_ZEP,
 * 			- každý ds:Manifest element musí obsahovať práve jednu referenciu na ds:Object,
 * 	TODO - overenie referencií v elementoch ds:Manifest
 * 			- dereferencovanie URI, aplikovanie príslušnej ds:Transforms transformácie (pri base64 decode),
 * 			- overenie hodnoty ds:DigestValue,
 * 	TODO - Overenie časovej pečiatky:
 * 			- overenie platnosti podpisového certifikátu časovej pečiatky voči času UtcNow a voči platnému poslednému CRL.
 * 			- overenie MessageImprint z časovej pečiatky voči podpisu ds:SignatureValue
 * 	TODO - Overenie platnosti podpisového certifikátu:
 * 			- overenie platnosti podpisového certifikátu dokumentu voči času T z časovej pečiatky a voči platnému poslednému CRL.
 * 
 */
public class SignedXml {

	private static final String SIGNATURE_XPATH = "//*[local-name()='DataEnvelope']/*[local-name()='Signature']";
	private static final String SIGNATURE_VALUE_XPATH = SIGNATURE_XPATH + "/*[local-name()='SignatureValue']";
	
	private static final String CANONICALIZATION_XPATH = SIGNATURE_XPATH + "/*[local-name()='SignedInfo']/*[local-name()='CanonicalizationMethod']";
	private static final String SIGNATURE_METHODS_XPATH = SIGNATURE_XPATH + "/*[local-name()='SignedInfo']/*[local-name()='SignatureMethod']";

	private static final String REFERENCE_XPATH = SIGNATURE_XPATH + "/*[local-name()='SignedInfo']/*[local-name()='Reference']";
	private static final String TRANSFORM_XPATH = "*[local-name()='Transforms']/*[local-name()='Transform']";
	private static final String DIGEST_XPATH = "*[local-name()='DigestMethod']";

	private static final String KEYINFO_XPATH = SIGNATURE_XPATH + "/*[local-name()='KeyInfo']";
	private static final String KEYINFO_DATA_XPATH = KEYINFO_XPATH + "/*[local-name()='X509Data']";
	private static final String X509CERTIFICATE = KEYINFO_DATA_XPATH + "/*[local-name()='X509Certificate']";
	private static final String X509ISSUER_SERIAL = KEYINFO_DATA_XPATH + "/*[local-name()='X509IssuerSerial']";
	private static final String X509SUBJECT_NAME = KEYINFO_DATA_XPATH + "/*[local-name()='X509SubjectName']";
	
	private static final String X509ISSUER_NAME = X509ISSUER_SERIAL + "/*[local-name()='X509IssuerName']";
	private static final String X509SERIAL_NUMBER = X509ISSUER_SERIAL + "/*[local-name()='X509SerialNumber']";
	
	private static final String SIGNATURE_PROPERTIES_XPATH = SIGNATURE_XPATH + "/*[local-name()='Object']/*[local-name()='SignatureProperties']";

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
	
	private static final Map<String, String> SIGNATURE_TYPES;
    static
    {
    	SIGNATURE_TYPES = new HashMap<String, String>();
    	SIGNATURE_TYPES.put("http://www.w3.org/2000/09/xmldsig#Object", "ds:KeyInfo");
    	SIGNATURE_TYPES.put("http://www.w3.org/2000/09/xmldsig#SignatureProperties", "ds:SignatureProperties");
    	SIGNATURE_TYPES.put("http://uri.etsi.org/01903#SignedProperties", "xades:SignedProperties");
    	SIGNATURE_TYPES.put("http://www.w3.org/2000/09/xmldsig#Manifest", "ds:Manifest");
    }
	
	
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
		coreValidation();
		otherElementsValidations();
		
		verifyTimestamp();
		verifyCrlValidity();
	}
	
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
	
	private void verifySignatureAlgorithms() throws SignVerificationException, XPathExpressionException {

		Element item = getElement(CANONICALIZATION_XPATH, "ds:CanonicalizationMethod");
		if (!CANONICALIZATION_METHODS.contains(parseNodeAttrib(item, "Algorithm"))){
			throw new SignVerificationException(String.format("Unsupported algorithm '%s' in element 'ds:CanonicalizationMethod'.", parseNodeAttrib(item, "Algorithm")));
		}
		
		item = getElement(SIGNATURE_METHODS_XPATH, "ds:SignatureMethod");
		String algorithm = parseNodeAttrib(item, "Algorithm").substring(parseNodeAttrib(item, "Algorithm").lastIndexOf('#') + 1).toLowerCase();
		if (!SIGNATURE_METHODS.contains(algorithm)){
			throw new SignVerificationException(String.format("Unsupported algorithm '%s' in element 'ds:SignatureMethod'.", algorithm));
		}
	}
	
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
	
	private void coreValidation(){
		/*
		 * TODO Core validation (podľa špecifikácie XML Signature) – overenie hodnoty 
		 * podpisu ds:SignatureValue a referencií v ds:SignedInfo:
		 * 		dereferencovanie URI, kanonikalizácia referencovaných ds:Manifest elementov a overenie hodnôt odtlačkov ds:DigestValue,
		 * 		kanonikalizácia ds:SignedInfo a overenie hodnoty ds:SignatureValue pomocou pripojeného podpisového certifikátu v ds:KeyInfo,
		 */
	}
	
	private void validateElementAttribs(Element element, String[] attribs, String elementName) throws XPathExpressionException, SignVerificationException{
		for (String attr : attribs){
			if (parseNodeAttrib(element, attr).isEmpty()){
				throw new SignVerificationException(String.format("Missing '%s' in element '%s'.", attr, elementName));
			}
		}
	}
	
	private void otherElementsValidations() throws XPathExpressionException, SignVerificationException {
		validateElementAttribs(
				getElement(SIGNATURE_XPATH, "ds:Signature"),
				new String[] {"Id", "xmlns:ds"}, 
				"ds:Signature"
		);
		
		validateElementAttribs(
				getElement(SIGNATURE_VALUE_XPATH, "ds:SignatureValue"),
				new String[] {"Id"},
				"ds:SignatureValue"
		);
		
		checkReferences();
		validateKeyInfoElements();
		validateSignatureProperties();
		validateManifestObjects();
		validateManifestReferences();
	}
	
	private void checkReferences() throws XPathExpressionException, SignVerificationException {
		NodeList nodes = (NodeList) xpath.compile(REFERENCE_XPATH).evaluate(doc, XPathConstants.NODESET);
		
		for(int i=0, size=nodes.getLength(); i < size; i++) {
			if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) nodes.item(i);
				validateElementAttribs(element, new String[] {"Id", "Type", "URI"}, "ds:Reference");
				
				String reference = parseNodeAttrib(element, "URI");
				String type = parseNodeAttrib(element, "Type");
				if (reference.isEmpty()){
					throw new SignVerificationException(String.format("Empty refrence in element 'ds:Reference'.", reference));
				}
				
				element = (Element) getReference(reference.substring(1));
				if (element == null){
					throw new SignVerificationException(String.format("Missing referenced object for Id '%s'.", parseNodeAttrib(element, "Id")));
				} else if (SIGNATURE_TYPES.get(type) != element.getNodeName()) {
					throw new SignVerificationException(String.format("Wrong referenced type: expected '%s', got '%s'.", type, element.getNodeName()));
				}
			}
		}
	}
	
	private void validateKeyInfoElements() throws XPathExpressionException, SignVerificationException{
		Element element = getElement(KEYINFO_XPATH, "ds:KeyInfo");
		String issuerName, serialNumber, subjectName, certificateValue;
		// byte[] certificateDecoded;

		validateElementAttribs(
				element,
				new String[] {"Id"},
				"ds:KeyInfo"
		);
		
		getElement(KEYINFO_DATA_XPATH, "ds:X509Data");
		
		certificateValue = getElement(X509CERTIFICATE, "ds:X509Certificate").getTextContent();
		if (certificateValue == null){
			throw new SignVerificationException(String.format("Missing value for 'ds:X509Certificate' element."));
		}
		// certificateDecoded = Base64.decode(certificateValue.getBytes());
		
		getElement(X509ISSUER_SERIAL, "ds:X509IssuerSerial");
		issuerName = getElement(X509ISSUER_NAME, "ds:X509IssuerName").getTextContent();
		if (issuerName == null){
			throw new SignVerificationException(String.format("Missing value for 'ds:X509IssuerName' element."));
		}

		serialNumber = getElement(X509SERIAL_NUMBER, "ds:X509SerialNumber").getTextContent();
		if (serialNumber == null){
			throw new SignVerificationException(String.format("Missing value for 'ds:X509SerialNumber' element."));
		}
		
		subjectName = getElement(X509SUBJECT_NAME, "ds:X509SubjectName").getTextContent();
		if (subjectName == null){
			throw new SignVerificationException(String.format("Missing value for 'ds:X509SubjectName' element."));
		}
		
		/* 
		 * TODO hodnoty elementov ds:X509IssuerSerial a ds:X509SubjectName súhlasia s príslušnými
		 * hodnatami v certifikáte, ktorý sa nachádza v ds:X509Certificate.
		 */
	}
	
	private void validateSignatureProperties() throws XPathExpressionException, SignVerificationException{
		Element element = getElement(SIGNATURE_XPATH, "ds:Signature");
		String signatureId = parseNodeAttrib(element, "Id");
		int nodeCount = 0;
		
		validateElementAttribs(
				getElement(SIGNATURE_VALUE_XPATH, "ds:SignatureValue"),
				new String[] {"Id"},
				"ds:SignatureValue"
		);
		
		element = getElement(SIGNATURE_PROPERTIES_XPATH, "ds:SignatureProperties");
		NodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++){
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element item = (Element) node;
				if (item == null || item.getNodeName() != "ds:SignatureProperty"){
					throw new SignVerificationException("Element 'ds:SignatureProperties' must contain only 'ds:SignatureProperty' element.");
				}
				
				String nodeTarget = parseNodeAttrib(item, "Target");
				if (nodeTarget.isEmpty() || !nodeTarget.substring(1).equals(signatureId)){
					throw new SignVerificationException(String.format("Element 'ds:SignatureProperty' must contain attribute 'Target == %s.", signatureId));
				}
				
				nodeCount += 1;
				if (nodeCount == 1){
					item = (Element) xpath.compile("*[local-name()='SignatureVersion']").evaluate(item, XPathConstants.NODE);
					if (!item.getNodeName().equals("xzep:SignatureVersion")){
						throw new SignVerificationException("First element 'ds:SignatureProperty' must contain 'xzep:SignatureVersion' element.");
					}					
				} else if (nodeCount == 2){
					
					item = (Element) xpath.compile("*[local-name()='ProductInfos']").evaluate(item, XPathConstants.NODE);
					if (!item.getNodeName().equals("xzep:ProductInfos")){
						throw new SignVerificationException("Second element 'ds:SignatureProperty' must contain 'xzep:ProductInfos' element.");
					}
				}
			}
		}
		
		if (nodeCount != 2){
			throw new SignVerificationException("Element 'ds:SignatureProperties' must contain exactly two 'ds:SignatureProperty' elements.");
		}
	}
	
	private void validateManifestObjects(){
		/*
		 * TODO každý ds:Manifest element musí mať Id atribút,
		 * 			- ds:Transforms musí byť z množiny podporovaných algoritmov pre daný element podľa profilu XAdES_ZEP,
		 * 			- ds:DigestMethod – musí obsahovať URI niektorého z podporovaných algoritmov podľa profilu XAdES_ZEP,
		 * 			- overenie hodnoty Type atribútu voči profilu XAdES_ZEP,
		 * 			- každý ds:Manifest element musí obsahovať práve jednu referenciu na ds:Object,
		 */
	}
	
	private void validateManifestReferences(){
		/*
		 * TODO overenie referencií v elementoch ds:Manifest:
		 * 			dereferencovanie URI, aplikovanie príslušnej ds:Transforms transformácie (pri base64 decode),
		 * 			overenie hodnoty ds:DigestValue,
		 */
	}
	
	private void verifyTimestamp(){
		/*
		 * TODO Overenie časovej pečiatky:
		 * 			overenie platnosti podpisového certifikátu časovej pečiatky voči času UtcNow a voči platnému poslednému CRL.
		 * 			overenie MessageImprint z časovej pečiatky voči podpisu ds:SignatureValue
		 */
	}

	private void verifyCrlValidity(){
		/*
		 * TODO Overenie platnosti podpisového certifikátu:
		 * 			overenie platnosti podpisového certifikátu dokumentu voči času T z časovej pečiatky a voči platnému poslednému CRL.
		 */
	}
	
	private Node getReference(String referenceId) throws XPathExpressionException {
		return (Node) xpath.compile(String.format("//*[@Id='%s']", referenceId)).evaluate(doc, XPathConstants.NODE);
	}

	private Element getElement(String xpath, String elementName) throws SignVerificationException, XPathExpressionException{
		Element element = (Element) this.xpath.compile(xpath).evaluate(doc, XPathConstants.NODE);
		
		if (element == null){
			throw new SignVerificationException(String.format("Missing element '%s'.", elementName));
		}
		
		return element;
	}
	
	private String parseNodeAttrib(Element node, String attribName){
		if (node == null){
			return "";
		}
		
		return node.getAttribute(attribName);
	}
}
