package sk.fiit.sipvs.cv.controllers;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.tsp.TimeStampResp;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.tsp.TSPAlgorithms;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;

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
		byte[] encodedBaseArray = Base64.encodeBase64(requestBytes);
		String requestBase64StringData = new String(encodedBaseArray);
		String responseBase64StringData = null;

		try {

			// send and receive data from ditec endpoint
			TS timeStampClient = new TS();
			logger.info("Request: " + requestBase64StringData);
			responseBase64StringData = timeStampClient.getTSSoap().getTimestamp(requestBase64StringData);

		} catch (Exception e) {
			logger.error("SOAP error: No response to request: " + requestBase64StringData, e);
		}

		logger.info("Response: " + responseBase64StringData);
		byte[] responseByteData = Base64.decodeBase64(responseBase64StringData.getBytes());

		TimeStampToken timeStampToken = null;

		try {
			TimeStampResp resp = TimeStampResp.getInstance(responseByteData);
			TimeStampResponse response = new TimeStampResponse(resp);
			timeStampToken = response.getTimeStampToken();

			// response.validate(tsRequest);
			// TOTO NETUSIM PRECO NEJDE ZVALIDOVAT....

			logger.info("Serial number: " + response.getTimeStampToken().getTimeStampInfo().getSerialNumber());
			logger.info("TSA: " + response.getTimeStampToken().getTimeStampInfo().getTsa());
			logger.info("Timestamp: " + response.getTimeStampToken().getTimeStampInfo().getGenTime());

			return response.getTimeStampToken();
		} catch (TSPException | IOException e) {
			logger.error("Timestamp error: ", e);
		}

		return timeStampToken;

	}

}