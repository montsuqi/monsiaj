package org.montsuqi.util;

public class OptionParserException extends ChainedRuntimeException {

	public OptionParserException() {
		super();
	}

	public OptionParserException(String message) {
		super(message);
	}

	public OptionParserException(String message, Throwable cause) {
		super(message, cause);
	}

	public OptionParserException(Throwable cause) {
		super(cause);
	}

}
