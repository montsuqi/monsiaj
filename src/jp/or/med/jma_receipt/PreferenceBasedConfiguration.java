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

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.montsuqi.client.Client;

class PreferenceBasedConfiguration extends Configuration {

	private Preferences prefs;

	PreferenceBasedConfiguration() {
		prefs = Preferences.userNodeForPackage(this.getClass());
	}

	void load() {
		// do nothing
	}

	void save() {
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			logger.warn(e);
		}
	}

	String getUser() {
		return prefs.get("user", System.getProperty("user.name")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	String getHost() {
		return prefs.get("host", "localhost"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	int getPort() {
		return prefs.getInt("port", Client.PORT_GLTERM); //$NON-NLS-1$
	}

	String getStyleFile() {
		return prefs.get("style", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	String getApplication() {
		return prefs.get("application", "orca00"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	void setUser(String user) {
		prefs.put("user", user); //$NON-NLS-1$
	}

	void setHost(String host) {
		prefs.put("host", host); //$NON-NLS-1$
	}

	void setPort(int port) {
		prefs.putInt("port", port); //$NON-NLS-1$
	}

	void setApplication(String app) {
		prefs.put("application", app); //$NON-NLS-1$
	}

	void setStyleFile(String file) {
		prefs.put("style", file); //$NON-NLS-1$
	}
}
