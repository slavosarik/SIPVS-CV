package sk.fiit.sipvs.cv.models;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.LibraryLoader;

/**
 * Contains a wrapper around Ditec .NET library
 * 
 * @author Stefan Smihla
 */
public class DSigAppWrapper {

	private static final String JACOB_FILE_X86 = "jacob-1.14.3-x86.dll";
	private static final String JACOB_FILE_X64 = "jacob-1.14.3-x64.dll";
	
	private static final String PROGID = "DSig.XadesSig";
	private ActiveXComponent dsig_app;

	public DSigAppWrapper() throws IOException {
		dsig_app = loadActiveXComponent();
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
	
	/* PRIVATE */
	
	private Double getDateProperty(String propertyName){
		return dsig_app.getProperty(propertyName).getDate();
	}
	
	private String getStringProperty(String propertyName){
		return dsig_app.getProperty(propertyName).getString();
	}
	
	private ActiveXComponent loadActiveXComponent() throws IOException {

		ActiveXComponent dsig_app;
		File temporaryDll = null;
		InputStream inputStream = null;
		try {
			inputStream = getJacobResource();
			temporaryDll = createTmpResource(inputStream);

			System.setProperty(LibraryLoader.JACOB_DLL_PATH, temporaryDll.getAbsolutePath());
			LibraryLoader.loadJacobLibrary();

			dsig_app = new ActiveXComponent(PROGID);
		} finally {
			if(inputStream != null) {
				inputStream.close();
			}
			
			if(temporaryDll != null) {
				temporaryDll.deleteOnExit();
			}
		}
		
		return dsig_app;
	}

	private InputStream getJacobResource(){
		String filename = System.getProperty("os.arch").equals("amd64") ? JACOB_FILE_X64 : JACOB_FILE_X86;		
		return getClass().getResourceAsStream("/dll/" + filename);
	}
	
	private File createTmpResource(InputStream inputStream) throws IOException {
		File temporaryDll = null;
		FileOutputStream outputStream = null;
		try {
			temporaryDll = File.createTempFile("jacob", ".dll");
			outputStream = new FileOutputStream(temporaryDll);
			byte[] array = new byte[8192];
			for (int i = inputStream.read(array); i != -1; i = inputStream.read(array)) {
				outputStream.write(array, 0, i);
			}
		} finally {
			if(outputStream != null){
				outputStream.close();
			}
		}
		
		return temporaryDll;
	}

}
