package org.montsuqi.util;

import java.util.HashMap;
import java.util.Map;

public class StdErrLogger extends Logger {

	private static Map loggers;

	static {
		loggers = new HashMap();
	}
	
	public static synchronized Logger getLogger(String name) {
		Logger logger = null;
		if ( ! loggers.containsKey(name)) {
			synchronized (Log4JLogger.class) {
				if ( ! loggers.containsKey(name)) {
					logger = new StdErrLogger();
					loggers.put(name, logger);
				}
			}
		}
		return (Logger)loggers.get(name);
	}

	private StdErrLogger() {
	}

	public void info(String message) {
		System.err.println("INFO:" + message);
	}

	public void warn(String message) {
		System.err.println("WARN:" + message);
	}

	public void fatal(String message) {
		System.err.println("FATAL: " + message);
	}
}
