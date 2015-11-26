package exceptions;

public class SignVerificationException extends Exception {

	private static final long serialVersionUID = 4027002685102372023L;

	public SignVerificationException(String message){
		super(String.format("An error occured during verification:\n\n%s", message));
	}
	
}
