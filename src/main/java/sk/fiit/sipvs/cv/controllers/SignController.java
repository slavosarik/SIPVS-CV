package sk.fiit.sipvs.cv.controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import sk.fiit.sipvs.cv.models.DSigAppWrapper;
import sk.fiit.sipvs.cv.models.DSigAppXmlPluginWrapper;

public class SignController {

	private static final int NUMBER_OF_FILES_TO_BE_SIGNED = 1;
	private static final String SCHEMA_URL_LOCATION = "https://www.example.com/biography_schema.xsd";
	private static final String TRANSFORMATION_URL_LOCATION = "https://www.example.com/cv-text1.xsd";

	private Logger logger = LogManager.getLogger(SignController.class.getName());

	public String signDocument(File xmlFile, File xsdFile, File xslFile) {
		// Signing
		DSigAppWrapper ditecApp;
		DSigAppXmlPluginWrapper ditecXML;
		String xmlOutput = null;

		try {
			ditecApp = new DSigAppWrapper();
			ditecXML = new DSigAppXmlPluginWrapper();

			// Create XML object
			// Index intentionally starts at 1 (for human readability in Ditec
			// Signer)
			for (int i = 1; i <= NUMBER_OF_FILES_TO_BE_SIGNED; i++) {
				Object xmlObject = ditecXML.CreateObject("id" + i, "CV " + i, readFile(xmlFile.getAbsolutePath()),
						readFile(xsdFile.getAbsolutePath()), "", SCHEMA_URL_LOCATION,
						readFile(xslFile.getAbsolutePath()), TRANSFORMATION_URL_LOCATION);

				if (xmlObject == null) {
					logger.error(ditecXML.getErrorMessage());
					return null;
				}

				int addRes = ditecApp.AddObject(xmlObject);

				if (addRes != 0) {
					logger.error(ditecApp.getErrorMessage());
					logger.error(addRes);
					return null;
				}
			}

			// Sign
			int signRes = ditecApp.Sign("FIIT_STU_Bratislava", "sha256", "urn:oid:1.3.158.36061701.1.2.1");

			if (signRes != 0) {
				logger.error(signRes);
				logger.error(ditecApp.getErrorMessage());
				return null;
			} else {
				logger.info("Successfully signed");
				xmlOutput = ditecApp.getSignedXmlWithEnvelope();
			}
		} catch (IOException e1) {
			logger.error("IOException", e1);
		}

		return xmlOutput;
	}

	public String signDocumentWithTimeStamp(File xmlFile, File xsdFile, File xslFile) {

		String signedDocument = signDocument(xmlFile, xsdFile, xslFile);

		if (signedDocument == null) {
			logger.error("No XML document to sign.");
			return null;
		}
		
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			InputSource source = new InputSource(new StringReader(signedDocument));
			Document document = docBuilder.parse(source);

			// Ziskanie elementu xades:QualifyingProperties
			Node qualifyingProperties = document.getElementsByTagName("xades:QualifyingProperties").item(0);

			if (qualifyingProperties == null) {
				logger.error("Cannot find xades:QualifyingProperties element.");
				return null;
			}

			// Vytvorenie podelementov
			Element unsignedProperties = document.createElement("xades:UnsignedProperties");
			Element unsignedSignatureProperties = document.createElement("xades:UnsignedSignatureProperties");
			Element signatureTimestamp = document.createElement("xades:SignatureTimeStamp");
			Element encapsulatedTimeStamp = document.createElement("xades:EncapsulatedTimeStamp");

			// Priradenie podelementov
			unsignedProperties.appendChild(unsignedSignatureProperties);
			unsignedSignatureProperties.appendChild(signatureTimestamp);
			signatureTimestamp.appendChild(encapsulatedTimeStamp);

			// Ziskanie samotnej peciatky a vlozenie do dokumentu
			Node signatureValue = document.getElementsByTagName("ds:SignatureValue").item(0);

			if (signatureValue == null) {
				logger.error("Cannot find ds:SignatureValue element.");
				return null;
			}

			TSClient tsClient = new TSClient();			
			String timestamp = tsClient.getTimeStamp(signatureValue.getTextContent());

			Text signatureNode = document.createTextNode(timestamp);
			encapsulatedTimeStamp.appendChild(signatureNode);

			qualifyingProperties.appendChild(unsignedProperties);

			// Opatovne vytvorenie XML dokumentu
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource domSource = new DOMSource(document);
			StreamResult result = new StreamResult(new StringWriter());

			transformer.transform(domSource, result);
			return result.getWriter().toString();
		} catch (ParserConfigurationException e) {
			// Catch all the exceptions separately. You know, for the future.
			logger.error(e.getLocalizedMessage());
		} catch (SAXException e) {
			logger.error(e.getLocalizedMessage());
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
		} catch (TransformerConfigurationException e) {
			logger.error(e.getLocalizedMessage());
		} catch (TransformerException e) {
			logger.error(e.getLocalizedMessage());
		}

		return null;
	}

	public Boolean saveDocument(String document, String filename) {

		if (document == null) {
			logger.error("Cannot save empty document.");
			return false;
		}
		
		if (!filename.endsWith(".xml")) {
			filename += ".xml";
		}

		BufferedWriter bWriter = null;
		try {
			bWriter = new BufferedWriter(new FileWriter(filename));
			bWriter.write(document);
			return true;
		} catch (IOException ex) {
			logger.error("Cannot write to file", ex);
		} finally {
			if (bWriter != null) {
				try {
					bWriter.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}

		return false;
	}

	public static void main(String[] args) {

		SignController sc = new SignController();

		String output = sc.signDocumentWithTimeStamp(new File("xml_examples/valid_example.xml"),
				new File("xml_examples/biography_schema.xsd"), new File("xml_examples/cv-text1.xsl"));
		
		sc.saveDocument(output, "xml_examples/timestamp.xml");		
	}

	private String readFile(String path) {

		byte[] encoded;

		try {
			encoded = Files.readAllBytes(Paths.get(path));
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}

		return new String(encoded, Charset.defaultCharset());
	}
}
