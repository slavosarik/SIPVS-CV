package sk.fiit.sipvs.cv.controllers;

import java.io.File;
import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import net.sf.saxon.TransformerFactoryImpl;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * 
 * @author slavo
 *
 */
public class TransformController {

	/*- 
	 * Transformácia do TXT (voliteľne HTML), ktorá je veľmi podobná s aplikáciou. Hlavne MEDZERE!
	 * Transformuj (pomocou XSLT)
	  - výber, ktorý súbor chcem transformovať
	  - výber, podľa ktorého súboru chcem transformovať
	  - výstup TXT
	*/

	/**
	 * Transform xml accoring to attached xsl file
	 * 
	 * @param sourceFileInput
	 * @param xslFileINput
	 * @param outputFile
	 * @return outputString
	 * @throws TransformerException
	 */
	public String transform(File sourceFileInput, File xslFileINput, File outputFile) throws TransformerException {

		Source xslFileSource = new StreamSource(xslFileINput);
		Source sourceFileSource = new StreamSource(sourceFileInput);
		
		TransformerFactory factory = TransformerFactoryImpl.newInstance();
		Transformer transformer = factory.newTransformer(xslFileSource);

		// zapis do suboru
		if (outputFile != null) {
			transformer.transform(sourceFileSource, new StreamResult(outputFile));
		}

		// ziskanie obsahu transformacie
		StringWriter outWriter = new StringWriter();
		StreamResult result = new StreamResult(outWriter);
		transformer.transform(sourceFileSource, result);
		return outWriter.getBuffer().toString();
	}
	

	public static void main(String[] args) {
		TransformController tc = new TransformController();
		try {
			System.out.println(tc.transform(new File("xml_examples/valid_example.xml"), new File("xml_examples/cv-text.xsl"), new File("text.txt")));
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

}
