package org.montsuqi.monsia;

import org.montsuqi.util.ChainedRuntimeException;

public class WidgetOperationException extends ChainedRuntimeException {

	public WidgetOperationException() {
		super();
	}

	public WidgetOperationException(String message) {
		super(message);
	}

	public WidgetOperationException(String message, Throwable cause) {
		super(message, cause);
	}

	public WidgetOperationException(Throwable cause) {
		super(cause);
	}

}
