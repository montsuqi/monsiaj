package org.montsuqi.util;

class OptionParserException extends ChainedRuntimeException {

	OptionParserException() {
		super();
	}

	OptionParserException(String message) {
		super(message);
	}

	OptionParserException(String message, Throwable cause) {
		super(message, cause);
	}

	OptionParserException(Throwable cause) {
		super(cause);
	}

}
