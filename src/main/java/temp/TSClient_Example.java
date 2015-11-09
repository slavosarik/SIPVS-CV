package temp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.tsp.TimeStampResp;
import org.bouncycastle.tsp.TSPAlgorithms;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.springframework.security.crypto.codec.Base64;

import sk.ditec.TS;

public class TSClient_Example {

	static String ocspUrl = "http://timestamping.edelweb.fr/service/tsp";

	private static String getTSAResponse(String request) {
		String response = null;
		TS timeStampClient = new TS();
		response = timeStampClient.getTSSoap().getTimestamp(request);
		return response;
	}

	private static InputStream getDitecTSAResponse(byte request[]) {
		String stringRequest = new String(Base64.encode(request));
		String stringResponse = getTSAResponse(stringRequest);
		
		byte[] responseByteData = Base64.decode(stringResponse.getBytes());
		InputStream in = new ByteArrayInputStream(responseByteData);
		
		return in;
	}

	private static InputStream getStandardTSAREsponse(byte request[]) {
		InputStream in = null;
		try {

			OutputStream out = null;
			URL url = new URL(ocspUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-type", "application/timestamp-query");
			con.setRequestProperty("Content-length", String.valueOf(request.length));
			out = con.getOutputStream();
			out.write(request);
			out.flush();

			if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new IOException(
						"Received HTTP error: " + con.getResponseCode() + " - " + con.getResponseMessage());
			}
			
			in = con.getInputStream();
		} catch (Exception e) {
			System.out.println(e);
		}
		return in;
	}

	public static void main(String[] args) throws NoSuchAlgorithmException {
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
		messageDigest.update("Hello ditec".getBytes());
		byte[] digest = messageDigest.digest();

		try {
			TimeStampRequestGenerator reqgen = new TimeStampRequestGenerator();
			TimeStampRequest req = reqgen.generate(TSPAlgorithms.SHA1, digest);
			byte request[] = req.getEncoded();

			InputStream in = getStandardTSAREsponse(request);
			//InputStream in = getDitecTSAResponse(request);

			TimeStampResp resp = TimeStampResp.getInstance(new ASN1InputStream(in).readObject());
			TimeStampResponse response = new TimeStampResponse(resp);
			response.validate(req);
			System.out.println(response.getTimeStampToken().getTimeStampInfo().getGenTime());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
