package org.montsuqi.util;

/**
 * We don't have Exception chaining facility before J2SE 1.4.
 */
public class ChainedRuntimeException extends RuntimeException {

	private Throwable chainedException;

	public ChainedRuntimeException() {
		this(null, null);
	}

	public ChainedRuntimeException(String message) {
		this(message, null);
	}

	public ChainedRuntimeException(String message, Throwable cause) {
		super(message);
		chainedException = cause;
	}

	public ChainedRuntimeException(Throwable cause) {
		this(null, cause);
	}

	public Throwable getCause() {
		return chainedException;
	}
}
