package org.montsuqi.monsia;

import org.montsuqi.util.ChainedRuntimeException;

public class HandlerInvocationException extends ChainedRuntimeException {

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
