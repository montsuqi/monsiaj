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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/** <p>A Logger subclass which wraps J2SE's logging system.</p> */
class J2SELogger extends Logger {

	private static Map loggers;

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
			synchronized (J2SELogger.class) {
				if ( ! loggers.containsKey(name)) {
					logger = new J2SELogger(java.util.logging.Logger.getLogger(name));
					loggers.put(name, logger);
				}
			}
		}
		return (Logger)loggers.get(name);
	}

	private J2SELogger(java.util.logging.Logger logger) {
		super();
		this.logger = logger;
		if (level == FATAL) {
			this.logger.setLevel(Level.SEVERE);
		} else if (level == WARNING) {
			this.logger.setLevel(Level.WARNING);
		} else if (level == INFO) {
			this.logger.setLevel(Level.INFO);
		} else if (level == DEBUG) {
			this.logger.setLevel(Level.FINER);
		} else if (level == TRACE) {
			this.logger.setLevel(Level.FINEST);
		} else {
			this.logger.setLevel(Level.WARNING);
		}
	}

	/** <p>Logs message in <em>trace</em> level.</p>
	 * 
	 * @param message a message to log.
	 */
	public void trace(String message) {
		logger.finest(message);
	}

	/** <p>Logs message in <em>debug</em> level.</p>
	 * 
	 * @param message a message to log.
	 */
	public void debug(String message) {
		logger.finer(message);
	}

	/** <p>Logs message in <em>info</em> level.</p>
	 * 
	 * @param message a message to log.
	 */
	public void info(String message) {
		logger.fine(message);
	}

	/** <p>Logs message in <em>warn</em> level.</p>
	 * 
	 * @param message a message to log.
	 */
	public void warn(String message) {
		logger.warning(message);
	}

	/** <p>Logs message in <em>fatal</em> level.</p>
	 * 
	 * @param message a message to log.
	 */
	public void fatal(String message) {
		logger.severe(message);
	}

	/** <p>Logs exception in <em>info</em> level.</p>
	 * 
	 * @param e an exception to log.
	 */
	public void info(Throwable e) {
		StackTraceElement stackTop = e.getStackTrace()[0];
		String clazz = stackTop.getClassName();
		String method = stackTop.getMethodName();
		logger.throwing(clazz, method, e);
	}

	/** <p>Logs exception in <em>warn</em> level.</p>
	 * 
	 * @param e an exception to log.
	 */
	public void warn(Throwable e) {
		info(e);
	}

	/** <p>Logs exception in <em>fatal</em> level.</p>
	 * 
	 * @param e an exception to log.
	 */
	public void fatal(Throwable e) {
		info(e);
	}

	// delegates logging to this.
	private final java.util.logging.Logger logger;
}
