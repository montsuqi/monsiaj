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

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;

/** <p>A Logger subclass which wraps Log4j logging system.</p>
 * 
 *  <p>Log4j system can be initialized by specifying a property file's name to
 *  "monsia.logger.log4j.properties" system property.</p>
 */
class Log4JLogger extends Logger {

	private static Map loggers;

	// initialize Log4j logging system.
	static {
		loggers = new HashMap();
		String propertyFile = System.getProperty("monsia.logger.log4j.properties"); //$NON-NLS-1$
		if (propertyFile != null) {
			PropertyConfigurator.configure(propertyFile);
		} else {
			BasicConfigurator.configure();
		}
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
		super();
		this.logger = logger;
		if (level == FATAL) {
			this.logger.setLevel(Level.FATAL);
		} else if (level == WARNING) {
			this.logger.setLevel(Level.WARN);
		} else if (level == INFO) {
			this.logger.setLevel(Level.INFO);
		} else if (level == DEBUG) {
			this.logger.setLevel(Level.DEBUG);
		} else if (level == TRACE) {
			this.logger.setLevel(Level.ALL);
		} else {
			this.logger.setLevel(Level.WARN);
		}
	}

	/** <p>Logs message in <em>trace</em> level.</p>
	 * 
	 * @param message a message to log.
	 */
	public void trace(String message) {
		logger.debug(message);
	}

	/** <p>Logs message in <em>debug</em> level.</p>
	 * 
	 * @param message a message to log.
	 */
	public void debug(String message) {
		logger.debug(message);
	}

	/** <p>Logs message in <em>info</em> level.</p>
	 * 
	 * @param message a message to log.
	 */
	public void info(String message) {
		logger.info(message);
	}

	/** <p>Logs message in <em>warn</em> level.</p>
	 * 
	 * @param message a message to log.
	 */
	public void warn(String message) {
		logger.warn(message);
	}

	/** <p>Logs message in <em>fatal</em> level.</p>
	 * 
	 * @param message a message to log.
	 */
	public void fatal(String message) {
		logger.fatal(message);
	}

	private final org.apache.log4j.Logger logger;
}
