package org.montsuqi.monsia;

import org.montsuqi.util.ChainedRuntimeException;

class WidgetBuildingException extends ChainedRuntimeException {

	WidgetBuildingException() {
		super();
	}

	WidgetBuildingException(String message) {
		super(message);
	}

	WidgetBuildingException(String message, Throwable cause) {
		super(message, cause);
	}

	WidgetBuildingException(Throwable cause) {
		super(cause);
	}
}
