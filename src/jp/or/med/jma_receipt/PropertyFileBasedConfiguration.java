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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.montsuqi.client.Client;

class PropertyFileBasedConfiguration extends Configuration {

	static final String CONFIGURATION_FILE = "jma-receipt.properties"; //$NON-NLS-1$
	Properties props;

	PropertyFileBasedConfiguration() {
		props = new Properties();
	}
	void load() {
		try {
			props.load(new FileInputStream(CONFIGURATION_FILE));
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	void save() {
		try {
			props.store(new FileOutputStream(CONFIGURATION_FILE), null);
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	String getUser() {
		String user = props.getProperty("user"); //$NON-NLS-1$
		if (user == null) {
			return System.getProperty("user.name"); //$NON-NLS-1$
		} else {
			return user;
		}
	}
	String getHost() {
		String host = props.getProperty("host"); //$NON-NLS-1$
		if (host == null) {
			return "localhost"; //$NON-NLS-1$
		} else {
			return host;
		}
	}

	int getPort() {
		String port = props.getProperty("port"); //$NON-NLS-1$
		if (port == null) {
			return Client.PORT_GLTERM;
		} else {
			return Integer.parseInt(port);
		}
	}

	String getStyleFile() {
		String style = props.getProperty("style"); //$NON-NLS-1$
		if (style == null) {
			return "";
		} else {
			return style;
		}
	}

	String getApplication() {
		String app = props.getProperty("application"); //$NON-NLS-1$
		if (app == null) {
			return "orca00"; //$NON-NLS-1$
		} else {
			return app;
		}
	}

	void setUser(String user) {
		props.setProperty("user", user); //$NON-NLS-1$
	}

	void setHost(String host) {
		props.setProperty("host", host); //$NON-NLS-1$
	}

	void setPort(int port) {
		props.setProperty("port", String.valueOf(port)); //$NON-NLS-1$
	}

	void setApplication(String app) {
		props.setProperty("application", app); //$NON-NLS-1$
	}

	void setStyleFile(String file) {
		props.setProperty("style", file); //$NON-NLS-1$
	}
}
