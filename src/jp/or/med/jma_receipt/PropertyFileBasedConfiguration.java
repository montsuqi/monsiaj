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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

class PropertyFileBasedConfiguration extends Configuration {

	static final String CONFIGURATION_FILE = "jma-receipt.properties"; //$NON-NLS-1$
	Properties props;

	PropertyFileBasedConfiguration() {
		props = new Properties();
	}
	void load() {
		try {
			props.load(new FileInputStream(CONFIGURATION_FILE));
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
		return props.getProperty("user"); //$NON-NLS-1$
	}

	String getHost() {
		return props.getProperty("host"); //$NON-NLS-1$
	}

	int getPort() {
		return Integer.parseInt(props.getProperty("port")); //$NON-NLS-1$
	}

	String getStyleFile() {
		return props.getProperty("style"); //$NON-NLS-1$
	}

	String getApplication() {
		return props.getProperty("application"); //$NON-NLS-1$
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
