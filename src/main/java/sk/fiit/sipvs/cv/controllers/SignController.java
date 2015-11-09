package sk.fiit.sipvs.cv.controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sk.fiit.sipvs.cv.models.DSigAppWrapper;
import sk.fiit.sipvs.cv.models.DSigAppXmlPluginWrapper;

public class SignController {

	private static final int NUMBER_OF_FILES_TO_BE_SIGNED = 1;

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
			// Index intentionally starts at 1 (for human readability in Ditec Signer)
			for (int i = 1; i <= NUMBER_OF_FILES_TO_BE_SIGNED; i++) {
				Object xmlObject = ditecXML.CreateObject("id" + i, "CV " + i, readFile(xmlFile.getAbsolutePath()),
						readFile(xsdFile.getAbsolutePath()), "", "http://www.w3.org/2001/XMLSchema",
						readFile(xslFile.getAbsolutePath()), "http://www.w3.org/1999/XSL/Transform");

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
	
	public Boolean saveDocument(String document, String filename) {
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
