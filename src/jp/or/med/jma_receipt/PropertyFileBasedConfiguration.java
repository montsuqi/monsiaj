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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

class PropertyFileBasedConfiguration extends Configuration {

	static final String CONFIGURATION_FILE = System.getProperty("user.home") + File.separator + "jma-receipt.properties"; //$NON-NLS-1$ //$NON-NLS-2$
	Properties props;

	PropertyFileBasedConfiguration() {
		props = new Properties();
	}

	void load() {
		try {
			props.load(new FileInputStream(CONFIGURATION_FILE));
		} catch (FileNotFoundException e) {
			Object[] args = { CONFIGURATION_FILE };
			logger.warn("configuration file not found: {0}", args); //$NON-NLS-1$
		} catch (IOException e) {
			logger.warn(e);
		}
	}

	void save() {
		try {
			props.store(new FileOutputStream(CONFIGURATION_FILE), null);
		} catch (IOException e) {
			logger.warn(e);
		}
	}

	String getString(String key, String defaultValue) {
		String value = props.getProperty(key);
		return value != null ? value : defaultValue;
	}

	int getInt(String key, int defaultValue) {
		String value = props.getProperty(key);
		return value != null ? Integer.parseInt(value) : defaultValue;
	}

	void setString(String key, String value) {
		props.put(key, value);
	}

	void setInt(String key, int value) {
		props.put(key, String.valueOf(value));
	}
}
