package org.montsuqi.monsia;

import org.montsuqi.util.ChainedRuntimeException;

public class InterfaceBuildingException extends ChainedRuntimeException {

	public InterfaceBuildingException() {
		super();
	}

	public InterfaceBuildingException(String message) {
		super(message);
	}

	public InterfaceBuildingException(String message, Throwable cause) {
		super(message, cause);
	}

	public InterfaceBuildingException(Throwable cause) {
		super(cause);
	}
}
