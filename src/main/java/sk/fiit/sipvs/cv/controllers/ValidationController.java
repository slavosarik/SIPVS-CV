package sk.fiit.sipvs.cv.controllers;

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import sk.fiit.sipvs.cv.models.ValidationResult;

/**
 * Contains logic for XML file validation against an XML Schema Definition (XSD)
 * file.
 * 
 * @author Dusan Cymorek
 */
public class ValidationController {

	private Logger logger = LogManager.getLogger(ValidationController.class.getName());

	/**
	 * Validates XML file against an XML Schema Definition (XSD) file.
	 * @param xmlFile XML file
	 * @param xsdFile XML Schema Definition file
	 * @return validity of the XML file. If the file is not valid, reason is also provided.
	 * @see ValidationResult
	 */
	public ValidationResult validateXML(File xmlFile, File xsdFile) {
		Source _xmlFile = new StreamSource(xmlFile);
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		try {
			Schema schema = schemaFactory.newSchema(xsdFile);
			Validator validator = schema.newValidator();

			logger.info("Starting XML validation of file: " + xmlFile.getName() + " (XSD: " + xsdFile.getName() + ").");
			validator.validate(_xmlFile);

			logger.info("XML validation of file: " + xmlFile.getName() + " (XSD: " + xsdFile.getName()
					+ ") has been successful.");
			return new ValidationResult(true, null);
		} catch (SAXException | IOException ex) {
			logger.error("XML validation of file: " + xmlFile.getName() + " (XSD: " + xsdFile.getName()
					+ ") has not been successful.");
			return new ValidationResult(false, ex.getLocalizedMessage());
		}
	}

	public static void main(String[] args) {
		ValidationController vc = new ValidationController();

		vc.validateXML(new File("xml_examples/valid_example.xml"), new File("src/main/resources/biography_schema.xsd"));
	}
}
