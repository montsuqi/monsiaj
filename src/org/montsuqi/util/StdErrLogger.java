/*      PANDA -- a simple transaction monitor

Copyright (C) 1998-1999 Ogochan.
              2000-2003 Ogochan & JMA (Japan Medical Association).
              2002-2006 OZAWA Sakuro.

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

/** <p>A logger class that logs messages to the standard error output or
 * specified file.</p>
 *
 * <p>By default StdErrLogger logs messages to the standard error output.
 * When the system property "monsia.logging.stderr.file" is set to a file name
 * and it is writable, logs will <em>also</em> be written to that file.</p>
 */
class StdErrLogger extends Logger {

	private static Map loggers;
	private PrintStream log;

	static {
		loggers = new HashMap();
	}

	/** <p>Returns a logger instance mapped to the given name.</p>
	 * 
	 * <p>When a logger already exists for the name, that is returned.
	 * When no logger exists for the name, newly created one is returned.</p>
	 * 
	 * @param name 
	 * @return a logger instance
	 */
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
		super();
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
		e.printStackTrace(System.err);
		if (log != null) {
			e.printStackTrace(log);
		}
	}

	/** <p>Logs message in <em>trace</em> level.</p>
	 * 
	 * @param message a message to log.
	 */
	public void trace(String message) {
		if (level >= TRACE) {
			writeLog("TRACE:" + message); //$NON-NLS-1$
		}
	}

	/** <p>Logs message in <em>debug</em> level.</p>
	 * 
	 * @param message a message to log.
	 */
	public void debug(String message) {
		if (level >= DEBUG) {
			writeLog("DEBUG:" + message); //$NON-NLS-1$
		}
	}

	/** <p>Logs message in <em>info</em> level.</p>
	 * 
	 * @param message a message to log.
	 */
	public void info(String message) {
		if (level >= INFO) {
			writeLog("INFO:" + message); //$NON-NLS-1$
		}
	}

	/** <p>Logs message in <em>warn</em> level.</p>
	 * 
	 * @param message a message to log.
	 */
	public void warn(String message) {
		if (level >= WARNING) {
			writeLog("WARN:" + message); //$NON-NLS-1$
		}
	}

	/** <p>Logs message in <em>fatal</em> level.</p>
	 * 
	 * @param message a message to log.
	 */
	public void fatal(String message) {
		if (level >= FATAL) {
			writeLog("FATAL: " + message); //$NON-NLS-1$
		}
	}

	/** <p>Logs exception in <em>warn</em> level.</p>
	 * 
	 * @param e an exception to log.
	 */
	public void warn(Throwable e) {
		if (level >= WARNING) {
			warn(e.toString());
			writeStackTrace(e);
		}
	}

	/** <p>Logs exception in <em>fatal</em> level.</p>
	 * 
	 * @param e an exception to log.
	 */
	public void fatal(Throwable e) {
		if (level >= FATAL) {
			fatal(e.toString());
			writeStackTrace(e);
		}
	}
}
