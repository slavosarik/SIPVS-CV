package sk.fiit.sipvs.cv.models.DSigApp;

import java.io.IOException;

import com.jacob.com.Dispatch;

/**
 * Contains a wrapper around Ditec .NET library
 * 
 * @author Stefan Smihla
 */
public class DSigAppXmlPluginWrapper extends AbstractDSigAppWrapper {
	
	private static final String PROGID = "DSig.XmlPlugin";
	
	public DSigAppXmlPluginWrapper() throws IOException {
		super(PROGID);
	}
	
	public Object CreateObject(String objectId, String objectDescription, String sourceXml, String sourceXsd, String namespaceUri, String xsdReference, String sourceXsl, String xslReference){
		return Dispatch.call(dsig_app, "CreateObject", objectId, objectDescription, sourceXml, sourceXsd, namespaceUri, xsdReference, sourceXsl, xslReference);
	}
	
	public Object CreateObject2(String objectId, String objectDescription, String sourceXml, String sourceXsd, String namespaceUri, String xsdReference, String sourceXsl, String xslReference, String transformType){
		return Dispatch.call(dsig_app, "CreateObject2", objectId, objectDescription, sourceXml, sourceXsd, namespaceUri, xsdReference, sourceXsl, xslReference);
	}
	
	public String getErrorMessage() {
		return getStringProperty("ErrorMessage");
	}
	
	public static void main(String[] args) throws IOException {
		DSigAppXmlPluginWrapper ditec = new DSigAppXmlPluginWrapper();
		Object a = ditec.CreateObject("1", "1", "1", "1", "1", "1", "1", "1");
		System.out.println("Toto je len skuska. pokial nebola exception, len sa vyprintoval error, tak dsig wrapper funguje :)");
		System.out.println("   TEST ERROR: " + a + " " + ditec.getErrorMessage());
	}
	
}
