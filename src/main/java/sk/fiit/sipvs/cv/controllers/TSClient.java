package sk.fiit.sipvs.cv.controllers;

import java.io.IOException;

import javax.xml.soap.SOAPException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.tsp.TSPAlgorithms;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;
import org.springframework.security.crypto.codec.Base64;

import sk.ditec.TS;

public class TSClient {

	final Logger logger = LogManager.getLogger(MainClass.class.getName());

	public TimeStampToken getTimeStampToken(byte[] byteMessage) {

		SHA1Digest messageDigest = new SHA1Digest();
		messageDigest.update(byteMessage, 0, byteMessage.length);
		byte[] reuqestDigest = new byte[32];
		messageDigest.doFinal(reuqestDigest, 0);

		TimeStampRequestGenerator tsReqGenerator = new TimeStampRequestGenerator();
		TimeStampRequest tsRequest = tsReqGenerator.generate(TSPAlgorithms.SHA256, reuqestDigest);

		byte[] requestBytes = null;
		try {
			requestBytes = tsRequest.getEncoded();
		} catch (IOException e) {
			logger.error("Failed to encode timeStamp request");
		}

		// encoding byte array into base 64 array
		byte[] encodedBaseArray = Base64.encode(requestBytes);
		String requestBase64StringData = new String(encodedBaseArray);
		String responseBase64StringData = null;

		TimeStampToken timeStampToken = null;

		try {
			// send and receive data from ditec endpoint
			responseBase64StringData = getTSAResponse(requestBase64StringData);

			byte[] responseByteData = Base64.decode(responseBase64StringData.getBytes());

			TimeStampResponse response = new TimeStampResponse(responseByteData);
			timeStampToken = response.getTimeStampToken();

			logger.info("Request message imprint: " + tsRequest.getMessageImprintDigest());
			logger.info("Response message imprint: " + timeStampToken.getTimeStampInfo().getMessageImprintDigest());

			// TOTO NETUSIM PRECO NEJDE ZVALIDOVAT....
			// Hladal som nieco suvisiace s "response for different message
			// imprint digest", ale nenasiel som dovod, preco by nemali byt
			// rovnake (pozri logger).
			response.validate(tsRequest);

			logger.info("Serial number: " + response.getTimeStampToken().getTimeStampInfo().getSerialNumber());
			logger.info("TSA: " + response.getTimeStampToken().getTimeStampInfo().getTsa());
			logger.info("Timestamp: " + response.getTimeStampToken().getTimeStampInfo().getGenTime());

			return response.getTimeStampToken();
		} catch (TSPException | IOException e) {
			logger.error("Timestamp error: " + e);

		} catch (Exception e) {
			logger.error("SOAP error: No response to request: " + requestBase64StringData, e);
		}

		return timeStampToken;

	}

	private String getTSAResponse(String request) {
		String response = null;

		// send and receive data from ditec endpoint
		TS timeStampClient = new TS();
		logger.info("Request: " + request);
		response = timeStampClient.getTSSoap().getTimestamp(request);

		return response;
	}
	
	public static void main(String[] args) {
		String message = "hello ditec";
		byte[] byteMessage = message.getBytes();

		TSClient client = new TSClient();
		TimeStampToken token = client.getTimeStampToken(byteMessage);
		System.out.println(token.getTimeStampInfo().getGenTime());
	}

}