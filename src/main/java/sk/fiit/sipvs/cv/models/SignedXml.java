package sk.fiit.sipvs.cv.models;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateParsingException;
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
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.util.encoders.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import exceptions.SignVerificationException;

/**
 * CHECKLIST & TODO
 *
 *	TODO - overenie hodnoty podpisu ds:SignatureValue a referencií v ds:SignedInfo
 *			- dereferencovanie URI, kanonikalizácia referencovaných ds:Manifest elementov a overenie hodnôt odtlačkov ds:DigestValue
 *			- kanonikalizácia ds:SignedInfo a overenie hodnoty ds:SignatureValue pomocou pripojeného podpisového certifikátu v ds:KeyInfo,
 * 	OK - overenie existencie referencií v ds:SignedInfo a hodnôt atribútov Id a Type voči profilu XAdES_ZEP pre:
 * 			- ds:KeyInfo element,
 * 			- ds:SignatureProperties element,
 * 			- xades:SignedProperties element,
 * 			- všetky ostatné referencie v rámci ds:SignedInfo musia byť referenciami na ds:Manifest elementy,
 * 	TODO 	- hodnoty elementov ds:X509IssuerSerial a ds:X509SubjectName súhlasia s príslušnými hodnatami v certifikáte, ktorý sa nachádza v ds:X509Certificate,
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
	
	private static final String MANIFEST_XPATH = SIGNATURE_XPATH + "/*[local-name()='Object']/*[local-name()='Manifest']";

	// Xades ZEP 4.3.1.1
	private static final Set<String> CANONICALIZATION_REFERENCES = new HashSet<String>(Arrays.asList(
			new String[] {"http://www.w3.org/TR/2001/REC-xml-c14n-20010315"}
	));
	
	// Xades ZEP 4.5
	private static final Set<String> SIGNATURE_REFERENCES = new HashSet<String>(Arrays.asList(
			new String[] {
					"http://www.w3.org/2000/09/xmldsig#dsa-sha1", 
					"http://www.w3.org/2000/09/xmldsig#rsa-sha1",
					"http://www.w3.org/2001/04/xmldsig-more#rsa-sha256",
					"http://www.w3.org/2001/04/xmldsig-more#rsa-sha384",
					"http://www.w3.org/2001/04/xmldsig-more#rsa-sha512"
			}
	));

	// Xades ZEP 4.3.1.3.1
	private static final Set<String> TRANSFORM_METHODS = new HashSet<String>(Arrays.asList(
			new String[] {"http://www.w3.org/TR/2001/REC-xml-c14n-20010315"}
	));

	// Xades ZEP 4.3.4.1
	private static final Set<String> TRANSFORM_METHODS2 = new HashSet<String>(Arrays.asList(
			new String[] {
					"http://www.w3.org/TR/2001/REC-xml-c14n-20010315",
					"http://www.w3.org/2000/09/xmldsig#base64"
			}
	));
	
	// Xades ZEP 4.5
	private static final Set<String> DIGEST_REFERENCES = new HashSet<String>(Arrays.asList(
			new String[] {
					"http://www.w3.org/2000/09/xmldsig#sha1", 
					"http://www.w3.org/2001/04/xmldsig-more#sha224",
					"http://www.w3.org/2001/04/xmlenc#sha256",
					"http://www.w3.org/2001/04/xmldsig-more#sha384",
					"http://www.w3.org/2001/04/xmlenc#sha512"
			}
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
		verifyXmlnsAttributes(); // Rule 1

		verifySignatureAlgorithms(); // Rule 2
		verifyTransformAndDigest(); // Rule 3
		coreValidation(); // Rule 4, 5
		otherElementsValidations(); // Rule 6-25
		
		verifyTimestamp(); // Rule 26, 27
		verifyCrlValidity(); // Rule 28
	}
	
	private void verifyXmlnsAttributes() throws SignVerificationException {
		// Rule 1
		if (!doc.getDocumentElement().getAttribute("xmlns:xzep").equals("http://www.ditec.sk/ep/signature_formats/xades_zep/v1.0")) {
			throw new SignVerificationException("Attribute 'xmlns:xzep' in root element has invalid value. (Rule 1)");
		}
		
		// Rule 1
		if (!doc.getDocumentElement().getAttribute("xmlns:ds").equals("http://www.w3.org/2000/09/xmldsig#")) {
			throw new SignVerificationException("Attribute 'xmlns:ds' in root element has invalid value. (Rule 1)");
		}
	}
	
	private void verifySignatureAlgorithms() throws SignVerificationException, XPathExpressionException {
		// Rule 2
		Element item1 = getElement(SIGNATURE_METHODS_XPATH);
		if (item1 == null) throw new SignVerificationException(String.format("Missing element 'ds:SignatureMethod'. (Rule 2)"));
		String algorithm = parseNodeAttrib(item1, "Algorithm").toLowerCase();
		if (!SIGNATURE_REFERENCES.contains(algorithm)){
			throw new SignVerificationException(String.format("Unsupported reference '%s' in element 'ds:SignatureMethod'. (Rule 2)", algorithm));
		}
		
		// Rule 2
		Element item2 = getElement(CANONICALIZATION_XPATH);
		if (item2 == null) throw new SignVerificationException(String.format("Missing element 'ds:CanonicalizationMethod'. (Rule 2)"));
		if (!CANONICALIZATION_REFERENCES.contains(parseNodeAttrib(item2, "Algorithm"))){
			throw new SignVerificationException(String.format("Unsupported reference '%s' in element 'ds:CanonicalizationMethod' (Rule 2).", parseNodeAttrib(item2, "Algorithm")));
		}
	}
	
	private void verifyTransformAndDigest() throws XPathExpressionException, SignVerificationException {
		// Rule 3
		NodeList nodes = (NodeList) xpath.compile(REFERENCE_XPATH).evaluate(doc, XPathConstants.NODESET);
		
		// Each ds:Reference
		for(int i=0, size=nodes.getLength(); i < size; i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				// Each ds:Transform of ds:Transforms
				NodeList transformNodes = (NodeList) xpath.compile(TRANSFORM_XPATH).evaluate(node, XPathConstants.NODESET);
				for (int j = 0; j < transformNodes.getLength(); j++) {
					Node n = transformNodes.item(j);
					if (n.getNodeType() == Node.ELEMENT_NODE) {
						Element item = (Element) n;
						if (!TRANSFORM_METHODS.contains(parseNodeAttrib(item, "Algorithm"))){
							throw new SignVerificationException(String.format("Unsupported reference '%s' in element 'ds:Transform' (Rule 3).", item.getAttribute("Algorithm")));
						}
					}
				}
				
				// ds:DigestMethod
				Element item = (Element) xpath.compile(DIGEST_XPATH).evaluate(node, XPathConstants.NODE);
				String algorithm = parseNodeAttrib(item, "Algorithm").toLowerCase();
				if (!DIGEST_REFERENCES.contains(algorithm)){
					throw new SignVerificationException(String.format("Unsupported reference '%s' in element 'ds:DigestMethod' (Rule 3).", algorithm));
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
	
	private void otherElementsValidations() throws XPathExpressionException, SignVerificationException {
		// Rule 6, 7
		Element item6 = getElement(SIGNATURE_XPATH);
		if (item6 == null) throw new SignVerificationException(String.format("Missing element 'ds:Signature'. (Rule 6)"));
		if (parseNodeAttrib(item6, "Id").isEmpty()) {
			throw new SignVerificationException(String.format("Attribute 'Id' of 'ds:Signature' is missing or empty. (Rule 6)"));
		}
		if (!parseNodeAttrib(item6, "xmlns:ds").equals("http://www.w3.org/2000/09/xmldsig#")) {
			throw new SignVerificationException(String.format("Attribute 'xmlns:ds' of 'ds:Signature' is missing or has invalid value. (Rule 7)"));		
		}
		
		// Rule 8
		Element item8 = getElement(SIGNATURE_VALUE_XPATH);
		if (item8 == null) throw new SignVerificationException(String.format("Missing element 'SignatureValue'. (Rule 8)"));
		if (parseNodeAttrib(item8, "Id").isEmpty()) {
			throw new SignVerificationException(String.format("Attribute 'Id' of 'ds:SignatureValue' is missing or empty. (Rule 8)"));
		}
		
		checkReferences(); // Rule 9, 10, 11, 12
		validateKeyInfoElements(); // Rule 13, 14, 15
		validateSignatureProperties(); // Rule 16, 17, 18
		validateManifestObjects(); // Rule 19, 20, 21, 22, 23, 24, 25
	}
	
	private void checkReferences() throws XPathExpressionException, SignVerificationException {
		NodeList nodes = (NodeList) xpath.compile(REFERENCE_XPATH).evaluate(doc, XPathConstants.NODESET);
		
		for(int i=0, size=nodes.getLength(); i < size; i++) {
			if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) nodes.item(i);

				validateElementAttribs(element, new String[] {"Id", "Type"}, "Missing '%s' in element 'ds:Reference'.");
				
				/*String reference = parseNodeAttrib(element, "URI");
				String type = parseNodeAttrib(element, "Type");
				if (reference.isEmpty()){
					throw new SignVerificationException(String.format("Empty reference in element 'ds:Reference'.", reference));
				}
				
				element = (Element) getReference(reference.substring(1));
				if (element == null){
					throw new SignVerificationException(String.format("Missing referenced object for Id '%s'.", parseNodeAttrib(element, "Id")));
				} else if (SIGNATURE_TYPES.get(type) != element.getNodeName()) {
					throw new SignVerificationException(String.format("Wrong referenced type: expected '%s', got '%s'.", type, element.getNodeName()));
				}*/
			}
		}
	}
	
	private void validateKeyInfoElements() throws XPathExpressionException, SignVerificationException {
		// Rule 13
		Element element = getElement(KEYINFO_XPATH);
		if (element == null) throw new SignVerificationException(String.format("Missing element 'ds:KeyInfo'. (Rule 13/14/15)"));

		validateElementAttribs(element, new String[] {"Id"}, "Missing '%s' in element 'ds:KeyInfo' (Rule 13)." );
		
		// Rule 14
		Element e14 = getElement(KEYINFO_DATA_XPATH);
		if (e14 == null) throw new SignVerificationException(String.format("Missing element 'ds:X509Data'. (Rule 14)"));
		
		Element e14cert = getElement(X509CERTIFICATE);
		if (e14cert == null) throw new SignVerificationException(String.format("Missing element 'ds:X509Certificate'. (Rule 14)"));

		Element e14issue = getElement(X509ISSUER_SERIAL);
		if (e14issue == null) throw new SignVerificationException(String.format("Missing element 'ds:X509IssuerSerial'. (Rule 14)"));
		
		Element e14subject = getElement(X509SUBJECT_NAME);
		if (e14subject == null) throw new SignVerificationException(String.format("Missing element 'ds:X509SubjectName'. (Rule 14)"));
		
		// Rule 15
		String cert = e14cert.getTextContent();
		String issuerName = getElement(X509ISSUER_NAME).getTextContent();
		String serialNumber = getElement(X509SERIAL_NUMBER).getTextContent();
		String subjectName = e14subject.getTextContent();
		
		ASN1InputStream dis = new ASN1InputStream(new ByteArrayInputStream(Base64.decode(cert)));
	    try {
			Object dobj = dis.readObject();
			ASN1Sequence sq = (ASN1Sequence) dobj;			
			X509CertificateObject co = new X509CertificateObject(Certificate.getInstance(sq));
			
			if (!co.getSerialNumber().toString().equals(serialNumber)) {
				throw new SignVerificationException(String.format("Invalid certificate serial number. (Rule 15)"));
			}

			System.out.println(co.getIssuerX500Principal());
			System.out.println(issuerName);
			if (!co.getIssuerX500Principal().getName().equals(issuerName)) {
				throw new SignVerificationException(String.format("Invalid certificate issuer name. (Rule 15)"));
			}

			if (!co.getSubjectX500Principal().getName().equals(subjectName)) {
				throw new SignVerificationException(String.format("Invalid certificate subject name. (Rule 15)"));
			}
		} catch (IOException | CertificateParsingException e) {
			throw new SignVerificationException(String.format("Cannot decode X509 certificate. (Rule 15)"));
		} finally {
			try {
				dis.close();
			} catch (IOException e) {}
		}
	}
	
	private void validateSignatureProperties() throws XPathExpressionException, SignVerificationException{
		Element element0 = getElement(SIGNATURE_XPATH);
		String signatureId = parseNodeAttrib(element0, "Id");

		// Rule 16
		Element element = getElement(SIGNATURE_PROPERTIES_XPATH);
		if (parseNodeAttrib(element, "Id").isEmpty()) {
			throw new SignVerificationException(String.format("Attribute 'Id' of 'ds:SignatureProperties' is missing or empty. (Rule 16)"));
		}
		
		// Rule 17, 18
		int nodeCount = 0;
		boolean nodeSV = false;
		boolean nodePI = false;
		NodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element item = (Element) node;
				String name = item.getNodeName();

				// Rule 18
				String target = parseNodeAttrib(item, "Target");
				if (target.isEmpty() || !target.substring(1).equals(signatureId)) {
					throw new SignVerificationException(String.format("Element 'ds:SignatureProperty' must contain attribute 'Target' == %s (Rule 18).", signatureId));
				}
				
				// Rule 17
				NodeList children = item.getChildNodes();
				for (int j = 0; j < children.getLength(); j++) {
					Node n = children.item(j);
					if (n.getNodeType() == Node.ELEMENT_NODE) {
						Element e = (Element) n;
						if (e.getNodeName() == "xzep:SignatureVersion") {
							nodeSV = true;
							break;
						} else if (e.getNodeName() == "xzep:ProductInfos") {
							nodePI = true;
							break;
						}
					}
				}
				
				if (name == "ds:SignatureProperty") {
					nodeCount += 1;
				}
			}
		}
		
		if (!nodeSV) throw new SignVerificationException("Element 'xzep:SignatureVersion' inside 'ds:SignatureProperty' is missing (Rule 17).");
		if (!nodePI) throw new SignVerificationException("Element 'xzep:ProductInfos' inside 'ds:SignatureProperty' is missing (Rule 17).");

		if (nodeCount < 2) {
			throw new SignVerificationException("Element 'ds:SignatureProperties' must contain two 'ds:SignatureProperty' elements (Rule 17).");
		}
	}
	
	private void validateManifestObjects() throws XPathExpressionException, SignVerificationException{
		NodeList nodes = (NodeList) xpath.compile(MANIFEST_XPATH).evaluate(doc, XPathConstants.NODESET);

		for (int i = 0; i < nodes.getLength(); i++) { // Each manifest
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;

				// Rule 19
				if (parseNodeAttrib(element, "Id").isEmpty()) {
					throw new SignVerificationException(String.format("Attribute 'Id' of 'ds:Manifest' is missing or empty. (Rule 19)"));
				}
				
				NodeList childNodes = node.getChildNodes();
				int nodeCount = 0;
				for (int j = 0; j < childNodes.getLength(); j++) { // Each "reference"
					Node n = childNodes.item(j);
					if (n.getNodeType() == Node.ELEMENT_NODE) {
						Element e = (Element) n;
						if (e.getNodeName() == "ds:Reference") {						
							// Rule 20
							NodeList transformNodes = (NodeList) xpath.compile(TRANSFORM_XPATH).evaluate(n, XPathConstants.NODESET);
							for (int k = 0; k < transformNodes.getLength(); k++) { // Each ds:Transform of ds:Transforms
								Node n0 = transformNodes.item(k);
								if (n0.getNodeType() == Node.ELEMENT_NODE) {
									Element item0 = (Element) n0;
									if (!TRANSFORM_METHODS2.contains(parseNodeAttrib(item0, "Algorithm"))){
										throw new SignVerificationException(String.format("Unsupported reference '%s' in element 'ds:Transform' (Rule 20).", item0.getAttribute("Algorithm")));
									}
								}
							}
							
							// Rule 21
							Element item = (Element) xpath.compile(DIGEST_XPATH).evaluate(n, XPathConstants.NODE);
							String algorithm = parseNodeAttrib(item, "Algorithm").toLowerCase();
							if (!DIGEST_REFERENCES.contains(algorithm)){
								throw new SignVerificationException(String.format("Unsupported reference '%s' in element 'ds:DigestMethod' (Rule 21).", algorithm));
							}

							// Rule 22
							if (!parseNodeAttrib(e, "Type").equals("http://www.w3.org/2000/09/xmldsig#Object")) {
								throw new SignVerificationException(String.format("Attribute 'Type' of 'ds:Reference' has invalid value. (Rule 22)"));
							}
							
							nodeCount += 1;
						}
					}
				}

				// Rule 23
				if (nodeCount != 1) {
					throw new SignVerificationException(String.format("More than one reference in 'ds:Manifest'. (Rule 23)"));
				}
			}
		}	
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
	
	private void validateElementAttribs(Element element, String[] attribs, String errorText) throws XPathExpressionException, SignVerificationException{
		for (String attr : attribs){
			if (parseNodeAttrib(element, attr).isEmpty()){
				throw new SignVerificationException(String.format(errorText, attr));
			}
		}
	}	

	private Node getReference(String referenceId) throws XPathExpressionException {
		return (Node) xpath.compile(String.format("//*[@Id='%s']", referenceId)).evaluate(doc, XPathConstants.NODE);
	}

	private Element getElement(String xpath) throws XPathExpressionException{
		Element element = (Element) this.xpath.compile(xpath).evaluate(doc, XPathConstants.NODE);

		return element;
	}
	
	private String parseNodeAttrib(Element node, String attribName){
		if (node == null){
			return "";
		}
		
		return node.getAttribute(attribName);
	}
}
