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

import java.util.HashMap;
import java.util.Map;

class J2SELogger extends Logger {

	private static Map loggers;

	static {
		loggers = new HashMap();
	}

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
		this.logger = logger;
	}

	public void trace(String message) {
		logger.finest(message);
	}
	public void debug(String message) {
		logger.finer(message);
	}
	
	public void info(String message) {
		logger.fine(message);
	}

	public void warn(String message) {
		logger.warning(message);
	}

	public void fatal(String message) {
		logger.severe(message);
	}

	public void info(Throwable e) {
		StackTraceElement stackTop = e.getStackTrace()[0];
		String clazz = stackTop.getClassName();
		String method = stackTop.getMethodName();
		logger.throwing(clazz, method, e);
	}

	public void warn(Throwable e) {
		info(e);
	}

	public void fatal(Throwable e) {
		info(e);
	}

	private final java.util.logging.Logger logger;
}
