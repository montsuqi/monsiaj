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
		String logName = System.getProperty("monsia.logger.stderr.file"); //$NON-NLS-1$
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
		while (e instanceof ChainedRuntimeException) {
			writeLog("Nested: " + e.toString());
			e = e.getCause();
		}
		e.printStackTrace(System.err);
		if (log != null) {
			e.printStackTrace(log);
		}
	}

	public void trace(String message) {
		writeLog("TRACE:" + message); //$NON-NLS-1$
	}

	public void debug(String message) {
		writeLog("DEBUG:" + message); //$NON-NLS-1$
	}

	public void info(String message) {
		writeLog("INFO:" + message); //$NON-NLS-1$
	}

	public void warn(String message) {
		writeLog("WARN:" + message); //$NON-NLS-1$
	}

	public void fatal(String message) {
		writeLog("FATAL: " + message); //$NON-NLS-1$
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
