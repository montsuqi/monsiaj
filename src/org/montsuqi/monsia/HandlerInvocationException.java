package org.montsuqi.monsia;

public class HandlerInvocationException extends RuntimeException {

	public HandlerInvocationException() {
		super();
	}

	public HandlerInvocationException(String message) {
		super(message);
	}

	public HandlerInvocationException(String message, Throwable cause) {
		super(message, cause);
	}

	public HandlerInvocationException(Throwable cause) {
		super(cause);
	}

}
