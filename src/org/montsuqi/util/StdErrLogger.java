/*      PANDA -- a simple transaction monitor

Copyright (C) 1998-1999 Ogochan.
              2000-2003 Ogochan & JMA (Japan Medical Association).

This module is part of PANDA.

		PANDA is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY.  No author or distributor accepts responsibility
to anyone for the consequences of using it or for whether it serves
any particular purpose or works at all, unless he says so in writing.
Refer to the GNU General Public License for full details.

		Everyone is granted permission to copy, modify and redistribute
PANDA, but only under the conditions described in the GNU General
Public License.  A copy of this license is supposed to have been given
to you along with PANDA so you can know your rights and
responsibilities.  It should be in a file named COPYING.  Among other
things, the copyright notice and this notice must be preserved on all
copies.
*/

package org.montsuqi.util;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

class StdErrLogger extends Logger {

	public static final int FATAL = 0;
	public static final int WARNING = 10;
	public static final int INFO = 20;
	public static final int DEBUG = 30;
	public static final int TRACE = 40;
	private static Map loggers;
	private int level;
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
		String logName = System.getProperty("monsia.logger.stderr.file"); //$NON-NLS-1$
		if (logName != null) {
			try {
				log = new PrintStream(new FileOutputStream(logName));
			} catch (Exception e) {
				// ignore
			}
		}
		String s = System.getProperty("monsia.logger.stderr.level");
		if (s == null) {
			level = WARNING;
		} else if ("FATAL".equalsIgnoreCase(s)){
			level = FATAL;
		} else if ("WARNING".equalsIgnoreCase(s)){
			level = WARNING;
		} else if ("INFO".equalsIgnoreCase(s)){
			level = INFO;
		} else if ("DEBUG".equalsIgnoreCase(s)){
			level = DEBUG;
		} else if ("TRACE".equalsIgnoreCase(s)){
			level = TRACE;
		} else {
			level = WARNING;
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
		if (level >= TRACE) {
			writeLog("TRACE:" + message); //$NON-NLS-1$
		}
	}

	public void debug(String message) {
		if (level >= DEBUG) {
			writeLog("DEBUG:" + message); //$NON-NLS-1$
		}
	}

	public void info(String message) {
		if (level >= INFO) {
			writeLog("INFO:" + message); //$NON-NLS-1$
		}
	}

	public void warn(String message) {
		if (level >= WARNING) {
			writeLog("WARN:" + message); //$NON-NLS-1$
		}
	}

	public void fatal(String message) {
		if (level >= FATAL) {
			writeLog("FATAL: " + message); //$NON-NLS-1$
		}
	}

	public void warn(Throwable e) {
		if (level >= WARNING) {
			warn(e.toString());
			writeStackTrace(e);
		}
	}

	public void fatal(Throwable e) {
		if (level >= FATAL) {
			fatal(e.toString());
			writeStackTrace(e);
		}
	}
}
