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

package jp.or.med.jma_receipt;

import org.montsuqi.client.Client;
import org.montsuqi.util.Logger;

abstract class Configuration {

	protected Logger logger;
	private char[] pass;

	public Configuration() {
		logger = Logger.getLogger(Configuration.class);
	}

	abstract void load();
	abstract void save();

	abstract String getString(String key, String defaultValue);
	abstract int getInt(String key, int defaultValue);

	abstract void setString(String key, String value);
	abstract void setInt(String key, int value);

	String getUser() {
		return getString("user", System.getProperty("user.name")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	String getHost() {
		return getString("host", "localhost"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	int getPort() {
		return getInt("port", Client.PORT_GLTERM); //$NON-NLS-1$
	}

	String getStyleFile() {
		return getString("style", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	String getApplication() {
		return getString("application", "orca00"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	char[] getPass() {
		return pass;
	}

	void setUser(String user) {
		setString("user", user); //$NON-NLS-1$
	}

	void setHost(String host) {
		setString("host", host); //$NON-NLS-1$
	}

	void setPort(int port) {
		setInt("port", port); //$NON-NLS-1$
	}

	void setApplication(String app) {
		setString("application", app); //$NON-NLS-1$
	}

	void setStyleFile(String file) {
		setString("style", file); //$NON-NLS-1$
	}

	void setPass(char[] pass) {
		this.pass = new char[pass.length];
		System.arraycopy(pass, 0, this.pass, 0, pass.length);
	}
}
