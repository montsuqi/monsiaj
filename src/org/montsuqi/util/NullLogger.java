package org.montsuqi.util;

public class NullLogger extends Logger {

	private static Logger instance  = new NullLogger();

	public static synchronized Logger getLogger(String name) {
		return instance;
	}

	private NullLogger() {
	}

	public void info(String message) {
		// do nothing 
	}

	public void warn(String message) {
		// do nothing
	}

	public void fatal(String message) {
		// do nothing
	}
}

