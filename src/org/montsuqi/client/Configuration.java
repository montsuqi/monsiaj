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

package org.montsuqi.client;

import java.lang.reflect.Constructor;
import org.montsuqi.util.Logger;

public abstract class Configuration {

	protected static Logger logger;
	private String title;
	private char[] pass;

	public Configuration(String title) {
		this.title = title;
		logger = Logger.getLogger(Configuration.class);
	}

	abstract void save();

	public abstract String getString(String key, String defaultValue);
	public abstract int getInt(String key, int defaultValue);

	public abstract void setString(String key, String value);
	public abstract void setInt(String key, int value);

	public String getTitle() {
		return title;
	}

	public char[] getPass() {
		return pass;
	}

	public void setPass(char[] pass) {
		this.pass = new char[pass.length];
		System.arraycopy(pass, 0, this.pass, 0, pass.length);
	}

	public static Configuration createConfiguration(String title, Class clazz) {
		try {
			Constructor cntr = getConstructor();
			Object[] params = { title, clazz };
			return (Configuration)cntr.newInstance(params);
		} catch (Exception e) {
			logger.fatal(e);
			return null;
		}
	}

	private static Constructor getConstructor() throws SecurityException, NoSuchMethodException {
		String[] classes = {
				"org.montsuqi.client.PreferenceBasedConfiguration", //$NON-NLS-1$
				"org.montsuqi.client.PropertyFileBasedConfiguration" //$NON-NLS-1$
		};
		Class clazz = null;
		for (int i = 0; i < classes.length; i++) {
			try {
				clazz = Class.forName(classes[i]);
				break;
			} catch (ClassNotFoundException e) {
				continue;
			}
		}
		assert clazz != null;
		Class[] params = { String.class, Class.class };
		return clazz.getConstructor(params);
	}
}
