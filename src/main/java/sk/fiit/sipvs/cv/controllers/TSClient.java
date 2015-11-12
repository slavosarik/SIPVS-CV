package sk.fiit.sipvs.cv.controllers;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;
import org.springframework.security.crypto.codec.Base64;

import sk.ditec.TS;

public class TSClient {

	final Logger logger = LogManager.getLogger(TSClient.class.getName());

	public String getTimeStamp(String message) {

		String timeStampBase64 = null;

		// send and receive data from ditec endpoint
		logger.info("Requesting timestamp for: " + message);

		try {
			// send and receive data from ditec endpoint
			TS timeStampClient = new TS();
			timeStampBase64 = timeStampClient.getTSSoap().getTimestamp(message);
		} catch (Exception e) {
			logger.error("SOAP error: No response to request: " + message, e);
		}		

		logger.info("Timestamp: " + timeStampBase64);

		return timeStampBase64;
	}

	public TimeStampToken getTimeStampToken(String timeStampBase64) {

		TimeStampToken timeStampToken = null;
		byte[] responseByteData = Base64.decode(timeStampBase64.getBytes());

		try {
			TimeStampResponse response = new TimeStampResponse(responseByteData);
			timeStampToken = response.getTimeStampToken();
			
			logger.info("Serial number: " + timeStampToken.getTimeStampInfo().getSerialNumber());
			logger.info("TSA: " + timeStampToken.getTimeStampInfo().getTsa());
			logger.info("Gen time: " + timeStampToken.getTimeStampInfo().getGenTime());
		} catch (TSPException | IOException e) {
			logger.error("Cannot retrieve timestamp token: " + e.getLocalizedMessage());
		}

		return timeStampToken;
	}

	public static void main(String[] args) {
		String message = "hello ditec";
		String messageBase64 = new String(Base64.encode(message.getBytes()));

		TSClient client = new TSClient();
		String timeStampBase64 = client.getTimeStamp(messageBase64);
		client.getTimeStampToken(timeStampBase64);	
	}
}