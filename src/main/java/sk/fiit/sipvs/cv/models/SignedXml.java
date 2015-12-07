package sk.fiit.sipvs.cv.models;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import exceptions.SignVerificationException;

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
	
	// XAdES ZEP 4.3.1.3
	private static final Map<String, String> REFERENCE_TYPES;
	static {
		REFERENCE_TYPES = new HashMap<String, String>();
		REFERENCE_TYPES.put("ds:KeyInfo", "http://www.w3.org/2000/09/xmldsig#Object");
		REFERENCE_TYPES.put("ds:SignatureProperties", "http://www.w3.org/2000/09/xmldsig#SignatureProperties");
		REFERENCE_TYPES.put("xades:SignedProperties", "http://uri.etsi.org/01903#SignedProperties");
		REFERENCE_TYPES.put("ds:Manifest", "http://www.w3.org/2000/09/xmldsig#Manifest");
	}
	
	private static final Map<String, String> DIGESTS;
	static {
		DIGESTS = new HashMap<String, String>();
		DIGESTS.put("http://www.w3.org/2000/09/xmldsig#sha1", "SHA-1");
		DIGESTS.put("http://www.w3.org/2001/04/xmldsig-more#sha224", "SHA-224");
		DIGESTS.put("http://www.w3.org/2001/04/xmlenc#sha256", "SHA-256");
		DIGESTS.put("http://www.w3.org/2001/04/xmldsig-more#sha384", "SHA-384");
		DIGESTS.put("http://www.w3.org/2001/04/xmlenc#sha512", "SHA-512");
	}
	
	private static final Map<String, String> SIGNERS;
	static {
		SIGNERS = new HashMap<String, String>();
		SIGNERS.put("http://www.w3.org/2000/09/xmldsig#dsa-sha1", "SHA1withDSA");
		SIGNERS.put("http://www.w3.org/2000/09/xmldsig#rsa-sha1", "SHA1withRSA/ISO9796-2");
		SIGNERS.put("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", "SHA256withRSA");
		SIGNERS.put("http://www.w3.org/2001/04/xmldsig-more#rsa-sha384", "SHA384withRSA");
		SIGNERS.put("http://www.w3.org/2001/04/xmldsig-more#rsa-sha512", "SHA512withRSA");
	}
	
	public SignedXml(File xmlFile) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		Security.addProvider(new BouncyCastleProvider());
		org.apache.xml.security.Init.init();

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
				else if ("xades".equals(prefix)) return "http://uri.etsi.org/01903/v1.3.2#";
				else if ("xml".equals(prefix)) return XMLConstants.XML_NS_URI;
				return XMLConstants.NULL_NS_URI;
			}

			public String getPrefix(String uri) {
				throw new UnsupportedOperationException();
			}

			@SuppressWarnings("rawtypes")
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
		verifyCoreReferences(); // Rule 4
		verifyCoreSignature(); // Rule 5
		verifySignature(); // Rule 6, 7
		verifySignatureValue(); // Rule 8
		verifyReferences(); // Rule 9, 10, 11, 12
		verifyKeyInfo(); // Rule 13, 14, 15
		verifySignatureProperties(); // Rule 16, 17, 18
		verifyManifests(); // Rule 19, 20, 21, 22, 23, 24, 25
		
		// Read timestamp, CRL
		TimeStampToken token = getTimestampToken();
		X509CRL crl = getCRL();

		verifyTimestamp(token, crl); // Rule 26, 27
		verifyCertificate(token, crl); // Rule 28
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
	 *	Verify core references - Rule 4
	 */
	private void verifyCoreReferences() throws SignVerificationException, XPathExpressionException {
		// Rule 4
		ArrayList<Element> manifestReferences = querySelectorAll("//ds:Signature/ds:SignedInfo/ds:Reference[@Type='http://www.w3.org/2000/09/xmldsig#Manifest']");
		for (Element e : manifestReferences) {
			// Dereference URI
			String urlTarget = getAttributeValue(e, "URI").substring(1);
			Element target = querySelector(String.format("//ds:Manifest[@Id='%s']", urlTarget),
					String.format("Cannot find referenced 'ds:Manifest' element with 'Id' = '%s' (Rule 4).", urlTarget));
			byte[] targetBytes = serializeElement(target).getBytes();
			
			// Reference digest method
			Element digestMethod = querySelector("ds:DigestMethod", e, "Cannot find 'ds:DigestMethod' inside 'ds:Reference' (Rule 4).");
			shouldHaveAttributeValueFrom(digestMethod, "Algorithm", DIGEST_REFERENCES, "Invalid digest method (Rule 4).");

			// Apply transforms (canonicalization)
			ArrayList<Element> transforms = querySelectorAll("ds:Transforms/ds:Transform", e);
			for (Element t : transforms) {
				shouldHaveAttributeValueFrom(t, "Algorithm", REFERENCE_TRANSFORM_REFERENCES, "Invalid transform (Rule 4).");
				
				String transAlgorithm = getAttributeValue(t, "Algorithm");
				if (transAlgorithm.equals("http://www.w3.org/TR/2001/REC-xml-c14n-20010315")) {
					// Apply canonicalization
					try {
						Canonicalizer canon = Canonicalizer.getInstance(transAlgorithm);
						targetBytes = canon.canonicalize(targetBytes);
					} catch (InvalidCanonicalizerException | CanonicalizationException | ParserConfigurationException | IOException | SAXException e1) {
						throw new SignVerificationException("Cannot apply canonicalization (Rule 4).");
					}
				}
			}
			
			// Calculate digest of referenced element
			MessageDigest messageDigest = null;
			try {
				messageDigest = MessageDigest.getInstance(DIGESTS.get(getAttributeValue(digestMethod, "Algorithm")), "BC");
			} catch (NoSuchAlgorithmException | NoSuchProviderException e2) {
				throw new SignVerificationException("Unsupported digest type (Rule 4).");
			}
			String targetDigest = new String(Base64.encode(messageDigest.digest(targetBytes)));
			
			// Retrieve expected digest
			Element digestValue = querySelector("ds:DigestValue", e, "Cannot find 'ds:DigestValue' inside 'ds:Reference' (Rule 4).");
			String expectedDigest = digestValue.getTextContent();

			if (!targetDigest.equals(expectedDigest)) {
				throw new SignVerificationException(String.format("Element 'ds:Reference' with reference to '%s' digest check failure (Rule 4).", urlTarget));
			}
		}
	}

	/**
	 *	Verify core signature - Rule 5
	 */
	private void verifyCoreSignature() throws SignVerificationException, XPathExpressionException {
		// Rule 5
		Element signatureValueElem = querySelector("//ds:Signature/ds:SignatureValue", "Cannot find 'ds:SignatureValue' element (Rule 5).");
		Element signatureMethodElem = querySelector("//ds:Signature/ds:SignedInfo/ds:SignatureMethod",
				"Cannot find 'ds:SignatureMethod' element (Rule 5).");
		Element c14MethodElem = querySelector("//ds:Signature/ds:SignedInfo/ds:CanonicalizationMethod",
				"Cannot find 'ds:CanonicalizationMethod' element (Rule 5).");
		Element signedInfo = querySelector("//ds:Signature/ds:SignedInfo", "Cannot find 'ds:SignedInfo' element (Rule 5).");
		
		X509CertificateObject cert = getCertificate();

		// Apply canonicalization
		byte[] targetBytes = serializeElement(signedInfo).getBytes();
		try {
			Canonicalizer canon = Canonicalizer.getInstance(getAttributeValue(c14MethodElem, "Algorithm"));
			targetBytes = canon.canonicalize(targetBytes);
		} catch (InvalidCanonicalizerException | CanonicalizationException | ParserConfigurationException | IOException | SAXException e1) {
			throw new SignVerificationException("Cannot apply canonicalization (Rule 5).");
		}

		// Create signer and verify signature
		try {
			Signature signer = Signature.getInstance(SIGNERS.get(getAttributeValue(signatureMethodElem, "Algorithm")), "BC");
			signer.initVerify(cert.getPublicKey());
			signer.update(targetBytes);
			if (!signer.verify(Base64.decode(signatureValueElem.getTextContent().getBytes()))) {
				throw new SignVerificationException("Signature verification failed (Rule 5).");	
			}
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			throw new SignVerificationException("Invalid signature method (Rule 5).");
		} catch (InvalidKeyException | SignatureException e) {
			throw new SignVerificationException("Invalid certificate key; cannot verify signature (Rule 5).");
		}
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
		ArrayList<Element> references = querySelectorAll("//ds:Signature/ds:SignedInfo/ds:Reference");

		for (Element e : references) {
			String targetId = getAttributeValue(e, "URI").substring(1);
			String type = getAttributeValue(e, "Type");

			// Check existence of referenced element
			Element targetElement = querySelector(String.format("//ds:Signature//*[@Id='%s']", targetId),
					String.format("Cannot find element with 'Id' of '%s' (Rule 9-12).", targetId));
			String targetNodeName = targetElement.getNodeName();

			// Check if reference is for supported elements only
			if (REFERENCE_TYPES.containsKey(targetNodeName)) {
				// Check if reference has correct type
				if (!REFERENCE_TYPES.get(targetNodeName).equals(type)) {
					throw new SignVerificationException(String.format("Element 'ds:Reference' has invalid 'Type' value - '%s' for element '%s' (Rule 9-12).",
							type, targetNodeName));
				}
			} else {
				throw new SignVerificationException(String.format("Found reference to unsupported element '%s' (Rule 12).", targetNodeName));
			}

			// Check existence of referencing element (XAdES 4.3.1.3)
			/*String id = getAttributeValue(e, "Id");
			querySelector(String.format("//xades:DataObjectFormat[@Id='%s']", id),
					String.format("Cannot find 'xades:DataObjectFormat' that references 'ds:Reference' with 'Id' = '%s' (Rule 9-12).", id));*/
		}

		// Check existence of mandatory references
		querySelector("//ds:Signature/ds:SignedInfo/ds:Reference[@Type='http://www.w3.org/2000/09/xmldsig#Object']",
				"Cannot find 'ds:Reference' to 'ds:KeyInfo' (Rule 9).");
		querySelector("//ds:Signature/ds:SignedInfo/ds:Reference[@Type='http://www.w3.org/2000/09/xmldsig#SignatureProperties']",
				"Cannot find 'ds:Reference' to 'ds:SignatureProperties' (Rule 10).");
		querySelector("//ds:Signature/ds:SignedInfo/ds:Reference[@Type='http://uri.etsi.org/01903#SignedProperties']",
				"Cannot find 'ds:Reference' to 'xades:SignedProperties' (Rule 11).");
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
		querySelector("//ds:Signature/ds:KeyInfo/ds:X509Data/ds:X509Certificate",
				"Cannot find 'ds:X509Certificate' element (Rule 14).");
		querySelector("//ds:Signature/ds:KeyInfo/ds:X509Data/ds:X509IssuerSerial",
				"Cannot find 'ds:X509IssuerSerial' element (Rule 14).");
		Element x509SubjectName = querySelector("//ds:Signature/ds:KeyInfo/ds:X509Data/ds:X509SubjectName",
				"Cannot find 'ds:X509SubjectName' element (Rule 14).");

		// Rule 15
		Element x509IssuerName = querySelector("//ds:Signature/ds:KeyInfo/ds:X509Data/ds:X509IssuerSerial/ds:X509IssuerName",
				"Cannot find 'ds:X509IssuerName' element (Rule 15).");
		Element x509SerialNumber = querySelector("//ds:Signature/ds:KeyInfo/ds:X509Data/ds:X509IssuerSerial/ds:X509SerialNumber",
				"Cannot find 'ds:X509SerialNumber' element (Rule 15).");

		X509CertificateObject cert = getCertificate();		
		String certIssuerName = cert.getIssuerX500Principal().toString().replaceAll("ST=", "S="); // Fix ST/S issue
		String certSerialNumber = cert.getSerialNumber().toString();
		String certSubjectName = cert.getSubjectX500Principal().toString();

		if (!certIssuerName.equals(x509IssuerName.getTextContent())) {
			throw new SignVerificationException("Invalid certificate issuer name (Rule 15).");
		}

		if (!certSerialNumber.equals(x509SerialNumber.getTextContent())) {
			throw new SignVerificationException("Invalid certificate serial number (Rule 15).");
		}
		
		if (!certSubjectName.equals(x509SubjectName.getTextContent())) {
			throw new SignVerificationException("Invalid certificate subject name (Rule 15).");
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
	 *	Verify manifests - Rule 19, 20, 21, 22, 23, 24, 25
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

		ArrayList<Element> references = querySelectorAll("//ds:Signature/ds:Object/ds:Manifest/ds:Reference");
		for (Element e : references) {
			// Rule 22
			shouldHaveAttributeValue(e, "Type", "http://www.w3.org/2000/09/xmldsig#Object",
					"Attribute 'Type' of 'ds:Reference' has invalid value (Rule 22).");
			
			// Dereference URI
			// Rule 24
			String urlTarget = getAttributeValue(e, "URI").substring(1);		
			Element target = querySelector(String.format("/xzep:DataEnvelope/ds:Object[@Id='%s']", urlTarget),
					String.format("Cannot find referenced 'ds:Object' element with 'Id' = '%s' (Rule 24, 25).", urlTarget));
			byte[] targetBytes = serializeElement(target).getBytes();
			
			// Reference digest method
			Element digestMethod = querySelector("ds:DigestMethod", e, "Cannot find 'ds:DigestMethod' inside 'ds:Reference' (Rule 21).");
			// Rule 21			
			shouldHaveAttributeValueFrom(digestMethod, "Algorithm", DIGEST_REFERENCES, "Invalid digest method (Rule 21).");
			
			// Reference transforms
			ArrayList<Element> transforms = querySelectorAll("ds:Transforms/ds:Transform", e);
			for (Element t : transforms) {
				// Rule 20
				shouldHaveAttributeValueFrom(t, "Algorithm", MANIFEST_TRANSFORM_REFERENCES, "Invalid transform (Rule 20).");

				// Rule 24
				String transAlgorithm = getAttributeValue(t, "Algorithm");
				if (transAlgorithm.equals("http://www.w3.org/TR/2001/REC-xml-c14n-20010315")) {
					// Apply canonicalization
					try {
						Canonicalizer canon = Canonicalizer.getInstance(transAlgorithm);
						targetBytes = canon.canonicalize(targetBytes);
					} catch (InvalidCanonicalizerException | CanonicalizationException | ParserConfigurationException | IOException | SAXException e1) {
						throw new SignVerificationException("Cannot apply canonicalization (Rule 24).");
					}
				} else if (transAlgorithm.equals("http://www.w3.org/2000/09/xmldsig#base64")) {
					// Apply base64 decode
					targetBytes = Base64.decode(targetBytes);
				}
			}
			
			// Rule 25
			// Calculate digest of referenced element
			MessageDigest messageDigest = null;
			try {
				messageDigest = MessageDigest.getInstance(DIGESTS.get(getAttributeValue(digestMethod, "Algorithm")), "BC");
			} catch (NoSuchAlgorithmException | NoSuchProviderException e2) {
				throw new SignVerificationException("Unsupported digest type (Rule 25).");
			}
			String targetDigest = new String(Base64.encode(messageDigest.digest(targetBytes)));
			
			// Retrieve expected digest
			Element digestValue = querySelector("ds:DigestValue", e, "Cannot find 'ds:DigestValue' inside 'ds:Reference' (Rule 25).");
			String expectedDigest = digestValue.getTextContent();			

			if (!targetDigest.equals(expectedDigest)) {
				throw new SignVerificationException(String.format("Element 'ds:Reference' with reference to '%s' digest check failure (Rule 25).", urlTarget));
			}
		}
	}

	/**
	 *	Verify timestamp - Rule 26, 27
	 */
	private void verifyTimestamp(TimeStampToken token, X509CRL crl) throws SignVerificationException, XPathExpressionException {
		// Read timestamp signature certificate
		X509CertificateHolder signCert = getTimestampSignatureCertificate(token);
		if (signCert == null) {
			throw new SignVerificationException("Cannot retrieve timestamp signature certificate (Rule 26).");
		}

		// Rule 26 - Validity
		// Check current certificate validity
		if (!signCert.isValidOn(new Date())) {
			throw new SignVerificationException("Timestamp signature certificate is not valid now (Rule 26).");
		}

		// Check against CRL
		X509CRLEntry entry = crl.getRevokedCertificate(signCert.getSerialNumber());
		if (entry != null) {
			throw new SignVerificationException("Timestamp signature certificate is revoked (Rule 26).");
		}

		// Rule 27 - Message imprint
		byte[] timestampDigest = token.getTimeStampInfo().getMessageImprintDigest();
		String hashAlgorithm = token.getTimeStampInfo().getHashAlgorithm().getAlgorithm().getId();

		Element signature = querySelector("//ds:Signature/ds:SignatureValue", "Cannot find element 'ds:SignatureValue' (Rule 27).");
		byte[] signatureValue = Base64.decode(signature.getTextContent().getBytes());

		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance(hashAlgorithm, "BC");
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			throw new SignVerificationException("Unsupported digest type (Rule 27).");
		}

		byte[] signatureDigest = messageDigest.digest(signatureValue);
		if (!Arrays.equals(timestampDigest, signatureDigest)) {
			throw new SignVerificationException("Timestamp MessageImprint check failure (Rule 27).");
		}
	}
	
	/**
	 *	Verify certificate - Rule 28
	 */
	private void verifyCertificate(TimeStampToken token, X509CRL crl) throws SignVerificationException, XPathExpressionException {
		X509CertificateObject cert = getCertificate();

		// Check validity at timestamp
		try {
			cert.checkValidity(token.getTimeStampInfo().getGenTime());
		} catch (CertificateExpiredException e) {
			throw new SignVerificationException("Document certificate was expired at signing time (Rule 28).");
		} catch (CertificateNotYetValidException e) {
			throw new SignVerificationException("Document certificate was not valid yet at signing time (Rule 28).");
		}

		// Check against CRL
		X509CRLEntry entry = crl.getRevokedCertificate(cert.getSerialNumber());
		if (entry != null && entry.getRevocationDate().before(token.getTimeStampInfo().getGenTime())) {
			throw new SignVerificationException("Document certificate was revoked at signing time (Rule 28).");
		}
	}

	/**
	 *	Utilities - Node traversal
	 */
	private Element querySelector(String selector, Element context, String customError) throws SignVerificationException, XPathExpressionException {
		Element element = (Element) this.xpath.compile(selector).evaluate(context, XPathConstants.NODE);

		if (element == null) {
			throw new SignVerificationException(customError);
		}
		
		return element;
	}

	private Element querySelector(String selector, String customError) throws SignVerificationException, XPathExpressionException {
		return querySelector(selector, doc.getDocumentElement(), customError);
	}
	
	private ArrayList<Element> querySelectorAll(String selector, Element context) throws XPathExpressionException {
		ArrayList<Element> elementList = new ArrayList<Element>();
		
		NodeList nodes = (NodeList) xpath.compile(selector).evaluate(context, XPathConstants.NODESET);
		
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				elementList.add(element);
			}
		}
		
		return elementList;
	}
	
	private ArrayList<Element> querySelectorAll(String selector) throws XPathExpressionException {
		return querySelectorAll(selector, doc.getDocumentElement());
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
	
	/**
	 *	Utilities - Assertions
	 */
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
	
	/**
	 *	Utilities - Serialize element to string
	 */
	private String serializeElement(Element element) throws SignVerificationException {
		try {
			StreamResult xmlOutput = new StreamResult(new StringWriter());
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(new DOMSource(element), xmlOutput);
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			return xmlOutput.getWriter().toString();
		} catch (TransformerFactoryConfigurationError | TransformerException e) {
			throw new SignVerificationException("Cannot serialize XML element (Rule 24).");
		}
	}
	
	/**
	 *	Utilities - Read certificate from document 
	 */
	private X509CertificateObject getCertificate() throws XPathExpressionException, SignVerificationException {
		Element x509Certificate = querySelector("//ds:Signature/ds:KeyInfo/ds:X509Data/ds:X509Certificate",
				"Cannot find 'ds:X509Certificate' element (Rule 4, 5, 15, 28).");

		X509CertificateObject cert = null;
		ASN1InputStream is = null;
		try {
			is = new ASN1InputStream(new ByteArrayInputStream(Base64.decode(x509Certificate.getTextContent())));
			ASN1Sequence sq = (ASN1Sequence) is.readObject();
			cert = new X509CertificateObject(Certificate.getInstance(sq));
		} catch (IOException | CertificateParsingException e) {
			throw new SignVerificationException("Cannot read certificate (Rule 4, 5, 15, 28).");
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					throw new SignVerificationException("Cannot read certificate (Rule 4, 5, 15, 28).");
				}
			}
		}

		return cert;
	}

	
	/**
	 *	Utilities - Extract signature certificate from timestamp token
	 */
	private X509CertificateHolder getTimestampSignatureCertificate(TimeStampToken token) throws SignVerificationException {
		X509CertificateHolder signCert = null;

		@SuppressWarnings("unchecked")
		Store<X509CertificateHolder> certs = token.getCertificates();
		Collection<X509CertificateHolder> certList = certs.getMatches(null);

		for (X509CertificateHolder cert : certList) {
			String certIssuerName = cert.getIssuer().toString();
			String signerIssuerName = token.getSID().getIssuer().toString();
			String certSerialNumber = cert.getSerialNumber().toString();
			String signerSerialNumber = token.getSID().getSerialNumber().toString(); 
			
			if (certIssuerName.equals(signerIssuerName) && certSerialNumber.equals(signerSerialNumber)) {
				signCert = cert;
				break;
			}
		}

		return signCert;
	}

	/**
	 *	Utilities - Read timestamp token from document
	 */
	private TimeStampToken getTimestampToken() throws SignVerificationException, XPathExpressionException {
		Element timestamp = querySelector("//xades:EncapsulatedTimeStamp", "Cannot find 'xades:EncapsulatedTimeStamp' element (Rule 26, 27, 28).");
		TimeStampToken token = null;
		try {
			token = new TimeStampToken(new CMSSignedData(Base64.decode(timestamp.getTextContent())));
		} catch (DOMException | TSPException | IOException | CMSException e) {
			throw new SignVerificationException("Cannot read timestamp (Rule 26, 27, 28).");
		}
		
		return token;
	}
	
	/**
	 *	Utilities - Download and parse CRL
	 */
	private X509CRL getCRL() throws SignVerificationException {
		ByteArrayInputStream crlStream = readURL("http://test.monex.sk/DTCCACrl/DTCCACrl.crl");
		if (crlStream == null) {
			throw new SignVerificationException("Cannot read CRL (Rule 26, 27, 28).");
		}

		CertificateFactory certFactory;
		X509CRL crl;
		try {
			certFactory = CertificateFactory.getInstance("X.509", "BC");
			crl = (X509CRL) certFactory.generateCRL(crlStream);
		} catch (CertificateException | CRLException | NoSuchProviderException e) {
			throw new SignVerificationException("Cannot parse CRL (Rule 26, 27, 28).");
		}

		return crl;
	}

	/**
	 *	Utilities - Read data from URL to byte stream
	 */
	private ByteArrayInputStream readURL(String url) {	
		URL urlData = null;
		try {
			urlData = new URL(url);
		} catch (MalformedURLException e) {
			return null;
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		InputStream is = null;
		try {
			is = urlData.openStream();
			byte[] byteChunk = new byte[4096];
			int n;
			
			while ((n = is.read(byteChunk)) > 0) {
				outputStream.write(byteChunk, 0, n);
			}
		} catch (IOException e) {
			return null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return new ByteArrayInputStream(outputStream.toByteArray());
	}
	
	/*public static void main(String[] args) {
		try {
			//File a = new File("xml_examples/signed_examples/clean2.xml");
			//File a = new File("xml_examples/signed_examples/01XadesT.xml");
			SignedXml sx = new SignedXml(a);
			sx.verify();
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}*/

}
