package org.montsuqi.monsia;

import org.montsuqi.util.ChainedRuntimeException;

class InterfaceBuildingException extends ChainedRuntimeException {

	InterfaceBuildingException() {
		super();
	}

	InterfaceBuildingException(String message) {
		super(message);
	}

	InterfaceBuildingException(String message, Throwable cause) {
		super(message, cause);
	}

	InterfaceBuildingException(Throwable cause) {
		super(cause);
	}
}
