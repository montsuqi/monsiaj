package org.montsuqi.util;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

class StdErrLogger extends Logger {

	private static Map loggers;
	private PrintStream log;

	static {
		loggers = new HashMap();
	}
	
	public static synchronized Logger getLogger(String name) {
		Logger logger = null;
		if ( ! loggers.containsKey(name)) {
			synchronized (StdErrLogger.class) {
				if ( ! loggers.containsKey(name)) {
					logger = new StdErrLogger();
					loggers.put(name, logger);
				}
			}
		}
		return (Logger)loggers.get(name);
	}

	private StdErrLogger() {
		log = null;
		String logName = System.getProperty("monsia.logger.stderr.file");
		if (logName != null) {
			try {
				log = new PrintStream(new FileOutputStream(logName));
			} catch (Exception e) {
				// ignore
			}
		}
	}

	private void writeLog(String message) {
		System.err.println(message);
		if (log != null) {
			log.println(message);
		}
	}

	private void writeStackTrace(Throwable e) {
		e.printStackTrace(System.err);
		if (log != null) {
			e.printStackTrace(log);
		}
	}

	public void trace(String message) {
		writeLog("TRACE:" + message);
	}

	public void debug(String message) {
		writeLog("DEBUG:" + message);
	}

	public void info(String message) {
		writeLog("INFO:" + message);
	}

	public void warn(String message) {
		writeLog("WARN:" + message);
	}

	public void fatal(String message) {
		writeLog("FATAL: " + message);
	}

	public void warn(Throwable e) {
		warn(e.toString());
		writeStackTrace(e);
	}

	public void fatal(Throwable e) {
		fatal(e.toString());
		writeStackTrace(e);
	}
}
