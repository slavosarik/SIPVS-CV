package sk.fiit.sipvs.cv.controllers;

import java.io.File;
import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
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

		Source xslFile = new StreamSource(xslFileINput);
		Source sourceFile = new StreamSource(sourceFileInput);

		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer(xslFile);

		// zapis do suboru
		if (outputFile != null) {
			transformer.transform(sourceFile, new StreamResult(outputFile));
		}

		// ziskanie obsahu transformacie
		StringWriter outWriter = new StringWriter();
		StreamResult result = new StreamResult(outWriter);
		transformer.transform(sourceFile, result);
		return outWriter.getBuffer().toString();
	}
}
