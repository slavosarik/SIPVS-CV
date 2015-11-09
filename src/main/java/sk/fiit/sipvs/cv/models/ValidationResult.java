package sk.fiit.sipvs.cv.models;

/**
 * Contains result of validation of an XML file.
 * 
 * @author Dusan Cymorek
 */
public class ValidationResult {
	private Boolean validity;
	private String reason;

	public Boolean getValidity() {
		return validity;
	}

	public String getReason() {
		return reason;
	}

	public ValidationResult(Boolean validity, String reason) {
		this.validity = validity;
		this.reason = reason;
	}
}
