package org.montsuqi.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;

class Log4JLogger extends Logger {

	private static Map loggers;

	static {
		loggers = new HashMap();
		BasicConfigurator.configure();
	}
	
	public static synchronized Logger getLogger(String name) {
		Logger logger = null;
		if ( ! loggers.containsKey(name)) {
			synchronized (Log4JLogger.class) {
				if ( ! loggers.containsKey(name)) {
					logger = new Log4JLogger(org.apache.log4j.Logger.getLogger(name));
					loggers.put(name, logger);
				}
			}
		}
		return (Logger)loggers.get(name);
	}

	private Log4JLogger(org.apache.log4j.Logger logger) {
		this.logger = logger;
	}

	public void trace(String message) {
		logger.debug(message);
	}

	public void debug(String message) {
		logger.debug(message);
	}

	public void info(String message) {
		logger.info(message);
	}

	public void warn(String message) {
		logger.warn(message);
	}

	public void fatal(String message) {
		logger.fatal(message);
	}

	private final org.apache.log4j.Logger logger;
}
