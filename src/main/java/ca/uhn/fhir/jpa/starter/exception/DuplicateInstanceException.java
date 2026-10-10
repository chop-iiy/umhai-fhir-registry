package ca.uhn.fhir.jpa.starter.exception;

public class DuplicateInstanceException extends RuntimeException {

	public DuplicateInstanceException(String message) {
		super(message);
	}
}
