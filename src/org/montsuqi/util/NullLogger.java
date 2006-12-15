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

/** <p>A logger that logs nothing.</p>
 * 
 *  <p>This is helpful for supressing all logging.</p> */
class NullLogger extends Logger {

	private static Logger instance  = new NullLogger();

	/** <p>Returns a singleton null logger.</p>
	 *
	 * @param name ignored.
	 * @return the singleton null logger.
	 */
	public static synchronized Logger getLogger(String name) {
		return instance;
	}

	private NullLogger() {
		// inhibit instantiation
	}

	/** <p>Do nothing.</p> */
	public void trace(String message) {
		// do nothing 
	}

	/** <p>Do nothing.</p> */
	public void debug(String message) {
		// do nothing 
	}

	/** <p>Do nothing.</p> */
	public void info(String message) {
		// do nothing 
	}

	/** <p>Do nothing.</p> */
	public void warn(String message) {
		// do nothing
	}

	/** <p>Do nothing.</p> */
	public void fatal(String message) {
		// do nothing
	}
}

