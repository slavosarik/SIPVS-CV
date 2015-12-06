package sk.fiit.sipvs.cv.models;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateParsingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
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
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import exceptions.SignVerificationException;
import net.sf.saxon.sxpath.XPathVariable;

public class SignedXml {

	private Document doc;
	private Logger logger = LogManager.getLogger(SignedXml.class.getName());
	private XPath xpath;

	// XAdES ZEP 4.5
	private static final Set<String> SIGNATURE_REFERENCES = new HashSet<String>(Arrays.asList(
			new String[] {
					"http://www.w3.org/2000/09/xmldsig#dsa-sha1", 
					"http://www.w3.org/2000/09/xmldsig#rsa-sha1",
					"http://www.w3.org/2001/04/xmldsig-more#rsa-sha256",
					"http://www.w3.org/2001/04/xmldsig-more#rsa-sha384",
					"http://www.w3.org/2001/04/xmldsig-more#rsa-sha512"
			}
	));
	
	// XAdES ZEP 4.3.1.1
	private static final Set<String> C14N_REFERENCES = new HashSet<String>(Arrays.asList(
			new String[] {
					"http://www.w3.org/TR/2001/REC-xml-c14n-20010315"
			}
	));

	// XAdES ZEP 4.3.1.3.1
	private static final Set<String> REFERENCE_TRANSFORM_REFERENCES = new HashSet<String>(Arrays.asList(
			new String[] {
					"http://www.w3.org/TR/2001/REC-xml-c14n-20010315"
			}
	));

	// XAdES ZEP 4.5
	private static final Set<String> DIGEST_REFERENCES = new HashSet<String>(Arrays.asList(
			new String[] {
					"http://www.w3.org/2000/09/xmldsig#sha1", 
					"http://www.w3.org/2001/04/xmldsig-more#sha224",
					"http://www.w3.org/2001/04/xmlenc#sha256",
					"http://www.w3.org/2001/04/xmldsig-more#sha384",
					"http://www.w3.org/2001/04/xmlenc#sha512"
			}
	));
	
	// XAdES ZEP 4.3.4.1
	private static final Set<String> MANIFEST_TRANSFORM_REFERENCES = new HashSet<String>(Arrays.asList(
			new String[] {
					"http://www.w3.org/TR/2001/REC-xml-c14n-20010315",
					"http://www.w3.org/2000/09/xmldsig#base64"
			}
	));
	
	public SignedXml(File xmlFile) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(true);
		
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		xpath = XPathFactory.newInstance().newXPath();
		
		// Make XPath work with our namespaces
		// http://stackoverflow.com/a/6390494
		xpath.setNamespaceContext(new NamespaceContext() {
			public String getNamespaceURI(String prefix) {
				if (prefix == null) throw new NullPointerException("Null prefix");
				else if ("ds".equals(prefix)) return "http://www.w3.org/2000/09/xmldsig#";
				else if ("xzep".equals(prefix)) return "http://www.ditec.sk/ep/signature_formats/xades_zep/v1.0";
				else if ("xml".equals(prefix)) return XMLConstants.XML_NS_URI;
				return XMLConstants.NULL_NS_URI;
			}

			public String getPrefix(String uri) {
				throw new UnsupportedOperationException();
			}

			public Iterator getPrefixes(String uri) {
				throw new UnsupportedOperationException();
			}
		});
		
		doc = dBuilder.parse(xmlFile);
		logger.info(String.format("File '%s' was sucessfully parsed.", xmlFile.getName()));
	}
	
	public void verify() throws SignVerificationException, XPathExpressionException {
		verifyEnvelope(); // Rule 1
		verifySignatureAlgorithms(); // Rule 2
		verifyTransformsAndDigest(); // Rule 3
		verifyCore(); // Rule 4, 5
		verifySignature(); // Rule 6, 7
		verifySignatureValue(); // Rule 8
		verifyReferences(); // Rule 9, 10, 11, 12
		verifyKeyInfo(); // Rule 13, 14, 15
		verifySignatureProperties(); // Rule 16, 17, 18
		verifyManifests(); // Rule 19, 20, 21, 22, 23
	}

	/**
	 *	Verify envelope - Rule 1
	 */
	private void verifyEnvelope() throws SignVerificationException, XPathExpressionException {
		Element envelope = querySelector("/*", "Cannot find root element (Rule 1).");

		shouldHaveAttributeValue(envelope, "xmlns:xzep", "http://www.ditec.sk/ep/signature_formats/xades_zep/v1.0",
				"Attribute 'xmlns:xzep' of 'xzep:DataEnvelope' has invalid value (Rule 1).");
		shouldHaveAttributeValue(envelope, "xmlns:ds", "http://www.w3.org/2000/09/xmldsig#",
				"Attribute 'xmlns:ds' of 'xzep:DataEnvelope' has invalid value (Rule 1).");
	}
	
	/**
	 *	Verify signature algorithms - Rule 2
	 */
	private void verifySignatureAlgorithms() throws SignVerificationException, XPathExpressionException {
		Element signatureMethod = querySelector("//ds:Signature/ds:SignedInfo/ds:SignatureMethod",
				"Cannot find 'ds:SignatureMethod' element (Rule 2).");
		shouldHaveAttributeValueFrom(signatureMethod, "Algorithm", SIGNATURE_REFERENCES,
				"Invalid signature method (Rule 2).");

		Element c14Method = querySelector("//ds:Signature/ds:SignedInfo/ds:CanonicalizationMethod",
				"Cannot find 'ds:CanonicalizationMethod' element (Rule 2).");
		shouldHaveAttributeValueFrom(c14Method, "Algorithm", C14N_REFERENCES,
				"Invalid canonicalization method (Rule 2).");
	}
	
	/**
	 *	Verify transforms and digest - Rule 3
	 */
	private void verifyTransformsAndDigest() throws SignVerificationException, XPathExpressionException {
		ArrayList<Element> transforms = querySelectorAll("//ds:Signature/ds:SignedInfo/ds:Reference/ds:Transforms/ds:Transform");
		for (Element e : transforms) {
			shouldHaveAttributeValueFrom(e, "Algorithm", REFERENCE_TRANSFORM_REFERENCES, "Invalid transform (Rule 3).");
		}
		
		ArrayList<Element> digestMethods = querySelectorAll("//ds:Signature/ds:SignedInfo/ds:Reference/ds:DigestMethod");
		for (Element e : digestMethods) {
			shouldHaveAttributeValueFrom(e, "Algorithm", DIGEST_REFERENCES, "Invalid digest method (Rule 3).");			
		}
	}
	
	/**
	 *	Verify core - Rule 4, 5
	 */
	private void verifyCore() throws SignVerificationException, XPathExpressionException {
		// TODO
	}

	/**
	 *	Verify signature - Rule 6, 7
	 */
	private void verifySignature() throws SignVerificationException, XPathExpressionException {
		Element signature = querySelector("//ds:Signature", "Cannot find 'ds:Signature' element (Rule 6, 7).");

		shouldHaveAttribute(signature, "Id", "Attribute 'Id' of 'ds:Signature' must have value (Rule 6).");
		
		shouldHaveAttributeValue(signature, "xmlns:ds", "http://www.w3.org/2000/09/xmldsig#",
				"Attribute 'xmlns:ds' of 'ds:Signature' has invalid value (Rule 7).");
	}
	
	/**
	 *	Verify signature value - Rule 8
	 */
	private void verifySignatureValue() throws SignVerificationException, XPathExpressionException {
		Element signatureValue = querySelector("//ds:Signature/ds:SignatureValue", "Cannot find 'ds:SignatureValue' element (Rule 8).");

		shouldHaveAttribute(signatureValue, "Id", "Attribute 'Id' of 'ds:SignatureValue' must have value (Rule 8).");
	}

	/**
	 *	Verify references - Rule 9, 10, 11, 12
	 */
	private void verifyReferences() throws SignVerificationException, XPathExpressionException {
		// TODO
	}
	
	/**
	 *	Verify key info - Rule 13, 14, 15
	 */
	private void verifyKeyInfo() throws SignVerificationException, XPathExpressionException {
		// Rule 13
		Element keyInfo = querySelector("//ds:Signature/ds:KeyInfo", "Cannot find 'ds:KeyInfo' element (Rule 13).");

		shouldHaveAttribute(keyInfo, "Id", "Attribute 'Id' of 'ds:KeyInfo' must have value (Rule 13).");
		
		// Rule 14
		querySelector("//ds:Signature/ds:KeyInfo/ds:X509Data", "Cannot find 'ds:X509Data' element (Rule 14).");
		Element x509Certificate = querySelector("//ds:Signature/ds:KeyInfo/ds:X509Data/ds:X509Certificate",
				"Cannot find 'ds:X509Certificate' element (Rule 14).");
		querySelector("//ds:Signature/ds:KeyInfo/ds:X509Data/ds:X509IssuerSerial",
				"Cannot find 'ds:X509IssuerSerial' element (Rule 14).");
		querySelector("//ds:Signature/ds:KeyInfo/ds:X509Data/ds:X509SubjectName",
				"Cannot find 'ds:X509SubjectName' element (Rule 14).");

		// Rule 15
		Element x509IssuerName = querySelector("//ds:Signature/ds:KeyInfo/ds:X509Data/ds:X509IssuerSerial/ds:X509IssuerName",
				"Cannot find 'ds:X509IssuerName' element (Rule 15).");
		Element x509SerialNumber = querySelector("//ds:Signature/ds:KeyInfo/ds:X509Data/ds:X509IssuerSerial/ds:X509SerialNumber",
				"Cannot find 'ds:X509SerialNumber' element (Rule 15).");

		try {
			X509Certificate cert = X509Certificate.getInstance(new ByteArrayInputStream(Base64.decode(x509Certificate.getTextContent())));
			
			String certIssuerName = cert.getIssuerDN().getName().replaceAll("ST=", "S="); // Fix ST/S issue
			String certSerialNumber = cert.getSerialNumber().toString();
			
			if (!certIssuerName.equals(x509IssuerName.getTextContent())) {
				throw new SignVerificationException("Invalid certificate issuer name (Rule 15).");
			}

			if (!certSerialNumber.equals(x509SerialNumber.getTextContent())) {
				throw new SignVerificationException("Invalid certificate serial number (Rule 15).");
			}
		} catch (DOMException | CertificateException e) {
			throw new SignVerificationException("Cannot read certificate (Rule 15).");
		}
	}
	
	/**
	 *	Verify signature properties - Rule 16, 17, 18
	 */
	private void verifySignatureProperties() throws SignVerificationException, XPathExpressionException {
		// Rule 16
		Element signatureProperties = querySelector("//ds:Signature/ds:Object/ds:SignatureProperties",
				"Cannot find 'ds:SignatureProperties' element (Rule 16).");

		shouldHaveAttribute(signatureProperties, "Id", "Attribute 'Id' of 'ds:SignatureProperties' must have value (Rule 16).");
	
		// Rule 17
		Element signatureVersion = querySelector("//ds:Signature/ds:Object/ds:SignatureProperties/ds:SignatureProperty/xzep:SignatureVersion",
				"Cannot find 'xzep:SignatureVersion' inside 'ds:SignatureProperty' element (Rule 17).");
		Element productInfos = querySelector("//ds:Signature/ds:Object/ds:SignatureProperties/ds:SignatureProperty/xzep:ProductInfos",
				"Cannot find 'xzep:ProductInfos' inside 'ds:SignatureProperty' element (Rule 17).");
		
		// Rule 18
		Element signature = querySelector("//ds:Signature", "Cannot find 'ds:Signature element (Rule 18).");
		String signatureId = getAttributeValue(signature, "Id");

		String targetSV = getAttributeValue((Element) signatureVersion.getParentNode(), "Target");
		if (!targetSV.equals("#" + signatureId)) {
			throw new SignVerificationException("'Target' attribute of 'ds:SignatureProperty' (SignatureVersion) has invalid value (Rule 19).");
		}
		
		String targetPI = getAttributeValue((Element) productInfos.getParentNode(), "Target");
		if (!targetPI.equals("#" + signatureId)) {
			throw new SignVerificationException("'Target' attribute of 'ds:SignatureProperty' (ProductInfos) has invalid value (Rule 19).");
		}
	}
	
	/**
	 *	Verify manifests - Rule 19, 20, 21, 22, 23
	 */
	private void verifyManifests() throws SignVerificationException, XPathExpressionException {
		// Rule 19
		ArrayList<Element> manifests = querySelectorAll("//ds:Signature/ds:Object/ds:Manifest");
		for (Element e : manifests) {
			shouldHaveAttribute(e, "Id", "Attribute 'Id' of 'ds:Manifest' must have value (Rule 19).");
			
			// Rule 23
			int referenceCount = 0;
			ArrayList<Element> children = getChildren(e);
			for (Element ch : children) {
				if (ch.getNodeName().equals("ds:Reference")) referenceCount += 1;
			}
			if (referenceCount != 1) {
				throw new SignVerificationException("Element 'ds:Manifest' contains 0 or more than 2 references (Rule 23).");
			}
		}
		
		// Rule 20
		ArrayList<Element> transforms = querySelectorAll("//ds:Signature/ds:Object/ds:Manifest/ds:Reference/ds:Transforms/ds:Transform");
		for (Element e : transforms) {
			shouldHaveAttributeValueFrom(e, "Algorithm", MANIFEST_TRANSFORM_REFERENCES, "Invalid transform (Rule 20).");
		}
		
		// Rule 21
		ArrayList<Element> digestMethods = querySelectorAll("//ds:Signature/ds:Object/ds:Manifest/ds:Reference/ds:DigestMethod");
		for (Element e : digestMethods) {
			shouldHaveAttributeValueFrom(e, "Algorithm", DIGEST_REFERENCES, "Invalid digest method (Rule 21).");
		}
		
		// Rule 22
		ArrayList<Element> references = querySelectorAll("//ds:Signature/ds:Object/ds:Manifest/ds:Reference");
		for (Element e : references) {
			shouldHaveAttributeValue(e, "Type", "http://www.w3.org/2000/09/xmldsig#Object",
					"Attribute 'Type' of 'ds:Reference' has invalid value (Rule 22).");
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 *	Utilities
	 */
	private Element querySelector(String selector, String customError) throws SignVerificationException, XPathExpressionException {
		Element element = (Element) this.xpath.compile(selector).evaluate(doc, XPathConstants.NODE);
		
		if (element == null) {
			throw new SignVerificationException(customError);
		}
		
		return element;
	}
	
	private ArrayList<Element> querySelectorAll(String selector) throws XPathExpressionException {
		ArrayList<Element> elementList = new ArrayList<Element>();
		
		NodeList nodes = (NodeList) xpath.compile(selector).evaluate(doc, XPathConstants.NODESET);
		
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				elementList.add(element);
			}
		}
		
		return elementList;
	}
	
	private ArrayList<Element> getChildren(Element parent) {
		ArrayList<Element> elementList = new ArrayList<Element>();
		
		NodeList nodes = parent.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				elementList.add(element);
			}
		}
		
		return elementList;
	}
	
	private String getAttributeValue(Element element, String attrName) {
		if (element == null) {
			return "";
		}

		return element.getAttribute(attrName);
	}
	
	private void shouldHaveAttribute(Element element, String attrName, String customError) throws SignVerificationException {
		String attrValue = getAttributeValue(element, attrName);
		if (attrValue.isEmpty()) {
			throw new SignVerificationException(customError);
		}
	}
	
	private void shouldHaveAttributeValue(Element element, String attrName, String value, String customError) throws SignVerificationException {
		String attrValue = getAttributeValue(element, attrName);
		if (!attrValue.equals(value)) {
			throw new SignVerificationException(customError + "\n\nInvalid value: " + attrValue);
		}
	}

	private void shouldHaveAttributeValueFrom(Element element, String attrName, Set<String> values, String customError) throws SignVerificationException {
		String attrValue = getAttributeValue(element, attrName);
		if (!values.contains(attrValue)) {
			throw new SignVerificationException(customError + "\n\nInvalid value: " + attrValue);
		}
	}
	
	
	
	
	
	

	
	
	
	public static void main(String[] args) {
		try {
			File a = new File("C:\\dev\\sipvs\\SIPVS-CV\\xml_examples\\signed_examples\\clean.xml");
			SignedXml sx = new SignedXml(a);
			sx.verify();
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

}
