package uk.ac.susx.mlcl.lib;

public class NumberOverflowException extends RuntimeException {

	public NumberOverflowException() {
		super();
	}

	public NumberOverflowException(String message, Throwable cause) {
		super(message, cause);
	}

	public NumberOverflowException(String message) {
		super(message);
	}

	public NumberOverflowException(Throwable cause) {
		super(cause);
	}

}