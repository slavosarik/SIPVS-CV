package sk.fiit.sipvs.cv.controllers;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import exceptions.SignVerificationException;
import sk.fiit.sipvs.cv.models.SignedXml;

public class VerifyController {

	private static File lastDirectory = new File(System.getProperty("user.dir"));
	private static Logger logger = LogManager.getLogger(VerifyController.class.getName());
	
	public static void verifyXmlSignedFile() throws SignVerificationException, XPathExpressionException {
		File xmlFile = openXmlFile();
		
		if (xmlFile != null){
			try {
				SignedXml xml = new SignedXml(xmlFile);
				xml.verify();
			} catch (ParserConfigurationException | SAXException | IOException e) {
				logger.error("Error during verification file", e);
				throw new SignVerificationException(e.getMessage());
			}
		}
	}

	private static File openXmlFile(){

		File xmlFile;
		
		JFileChooser xmlFileChooser = new JFileChooser();
		xmlFileChooser.setAcceptAllFileFilterUsed(false);
		
		FileNameExtensionFilter xmlFilter = new FileNameExtensionFilter("XML files (.xml)", "xml");
		xmlFileChooser = new JFileChooser();
		xmlFileChooser.setAcceptAllFileFilterUsed(false);
		xmlFileChooser.setFileFilter(xmlFilter);
		xmlFileChooser.setFileFilter(xmlFilter);
		
		xmlFileChooser.setCurrentDirectory(lastDirectory);

		int returnVal = showOpenDialog(xmlFileChooser);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			xmlFile = xmlFileChooser.getSelectedFile();

			lastDirectory = xmlFile;
		} else {
			xmlFile = null;
		}
		
		return xmlFile;
	}
	
	private static int showOpenDialog(JFileChooser fileChooser){
		JFrame window = new JFrame();
		window.setTitle("Verify XML");
		window.setBounds(100, 100, 1000, 450);
		window.getContentPane().setLayout(null);
		
		return fileChooser.showOpenDialog(window);
	}
	
}
