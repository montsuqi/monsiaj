package org.montsuqi.monsia;

import org.montsuqi.util.ChainedRuntimeException;

public class WidgetBuildingException extends ChainedRuntimeException {

	public WidgetBuildingException() {
		super();
	}

	public WidgetBuildingException(String message) {
		super(message);
	}

	public WidgetBuildingException(String message, Throwable cause) {
		super(message, cause);
	}

	public WidgetBuildingException(Throwable cause) {
		super(cause);
	}
}
