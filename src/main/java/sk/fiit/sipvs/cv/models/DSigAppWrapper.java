package sk.fiit.sipvs.cv.models;

import java.io.IOException;
import com.jacob.com.Dispatch;

/**
 * Contains a wrapper around Ditec .NET library
 * 
 * @author Stefan Smihla
 */
public class DSigAppWrapper extends AbstractDSigAppWrapper {
	
	private static final String PROGID = "DSig.XadesSig";
	
	public DSigAppWrapper() throws IOException {
		super(PROGID);
	}
	
	public void SetWindowSize(int width, int height){
		Dispatch.call(dsig_app, "SetWindowSize", width, height);
	}

	public void SetSigningTimeProcessing(boolean displayGui, boolean includeSigningTime){
		Dispatch.call(dsig_app, "SetSigningTimeProcessing", displayGui, includeSigningTime);
	}
	
	public int Sign(String signatureId, String digestAlgUri, String signaturePolicyIdentifier){
		return Dispatch.call(dsig_app, "Sign", signatureId, digestAlgUri, signaturePolicyIdentifier).getInt();
	}

	public int Sign11(String signatureId, String digestAlgUri, String signaturePolicyIdentifier, String dataEnvelopeId, String dataEnvelopeURI, String dataEnvelopeDescr){
		return Dispatch.call(dsig_app, "Sign11", signatureId, digestAlgUri, signaturePolicyIdentifier).getInt();
	}

	public int Sign20(String signatureId, String digestAlgUri, String signaturePolicyIdentifier, String dataEnvelopeId, String dataEnvelopeURI, String dataEnvelopeDescr){
		return Dispatch.call(dsig_app, "Sign20", signatureId, digestAlgUri, signaturePolicyIdentifier).getInt();
	}
	
	public int AddObject(Object obj){
		return Dispatch.call(dsig_app, "AddObject", obj).getInt();
	}
	
	public String getErrorMessage() {
		return getStringProperty("ErrorMessage");
	}
	
	public String getSignedXmlWithEnvelope() {
		return getStringProperty("SignedXmlWithEnvelope");
	}
	
	public String getSignedXmlWithEnvelopeBase64() {
		return getStringProperty("SignedXmlWithEnvelopeBase64");
	}
	
	public String getSignedXmlWithEnvelopeGZipBase64() {
		return getStringProperty("SignedXmlWithEnvelopeGZipBase64");
	}
	
	public Double getSigningTimeUtc() {
		return getDateProperty("SigningTimeUtc");
	}

	public String getSigningTimeUtcString() {
		return getStringProperty("SigningTimeUtcString");
	}

	public String getSignerIdentification() {
		return getStringProperty("SignerIdentification");
	}
	
	public static void main(String[] args) throws IOException {
		DSigAppWrapper ditec = new DSigAppWrapper();
		int a = ditec.Sign("1", "2", "3");
		System.out.println("Toto je len skuska. pokial nebola exception, len sa vyprintoval error, tak dsig wrapper funguje :)");
		System.out.println("   TEST ERROR: " + a + " " + ditec.getErrorMessage());
	}

}
