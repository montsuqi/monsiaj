package org.montsuqi.monsia;

import org.montsuqi.util.ChainedRuntimeException;

class WidgetOperationException extends ChainedRuntimeException {

	WidgetOperationException() {
		super();
	}

	WidgetOperationException(String message) {
		super(message);
	}

	WidgetOperationException(String message, Throwable cause) {
		super(message, cause);
	}

	WidgetOperationException(Throwable cause) {
		super(cause);
	}

}
