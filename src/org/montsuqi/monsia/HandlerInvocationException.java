package org.montsuqi.monsia;

import org.montsuqi.util.ChainedRuntimeException;

class HandlerInvocationException extends ChainedRuntimeException {

	HandlerInvocationException() {
		super();
	}

	HandlerInvocationException(String message) {
		super(message);
	}

	HandlerInvocationException(String message, Throwable cause) {
		super(message, cause);
	}

	HandlerInvocationException(Throwable cause) {
		super(cause);
	}

}
