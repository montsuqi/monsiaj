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

import java.io.File;
import java.lang.reflect.Constructor;
import org.montsuqi.util.Logger;

public abstract class Configuration {

	private String pass;

	private static final String PANDA_SCHEME = "panda:"; //$NON-NLS-1$
	static final int DEFAULT_PORT = 8000;
	static final String DEFAULT_HOST = "localhost"; //$NON-NLS-1$
	static final String DEFAULT_USER = System.getProperty("user.anme"); //$NON-NLS-1$
	static final String DEFAULT_APPLICATION = "demo"; //$NON-NLS-1$
	static final String DEFAULT_ENCODING = "EUC-JP"; //$NON-NLS-1$
	static final String DEFAULT_CACHE_PATH = System.getProperty("user.home") + File.separator + "cache";  //$NON-NLS-1$//$NON-NLS-2$
	static final String DEFAULT_STYLES = ""; //$NON-NLS-1$
	static final boolean DEFAULT_USE_SSL = false;
	static final boolean DEFAULT_VERIFY = false;
	static final int DEFAULT_PROTOCOL_VERSION = 1;

	protected static final Logger logger = Logger.getLogger(Configuration.class);

	public static Configuration createConfiguration(Class clazz) {
		try {
			Constructor cntr = getConstructor();
			Object[] params = { clazz };
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
		Class[] params = { Class.class };
		return clazz.getConstructor(params);
	}

	abstract void save();
	abstract String getString(String key, String defaultValue);
	abstract int getInt(String key, int defaultValue);
	abstract boolean getBoolean(String key, boolean defaultValue);

	abstract void setString(String key, String value);
	abstract void setInt(String key, int value);
	abstract void setBoolean(String key, boolean value);

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public int getPort() {
		return getInt("port", DEFAULT_PORT); //$NON-NLS-1$
	}

	public void setPort(int port) {
		setInt("port", port); //$NON-NLS-1$
	}

	public String getHost() {
		return getString("host", DEFAULT_HOST); //$NON-NLS-1$
	}

	public void setHost(String host) {
		setString("host", host); //$NON-NLS-1$
	}

	public String getUser() {
		return getString("user", DEFAULT_USER); //$NON-NLS-1$
	}

	public void setUser(String user) {
		setString("user", user); //$NON-NLS-1$
	}

	public String getCache() {
		return getString("cache", DEFAULT_CACHE_PATH); //$NON-NLS-1$
	}

	public void setCache(String cache) {
		setString("cache", cache); //$NON-NLS-1$
	}

	public String getEncoding() {
		return getString("encoding", DEFAULT_ENCODING); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void setEncoding(String encoding) {
		setString("encoding", encoding); //$NON-NLS-1$
	}

	public String getStyles() {
		return getString("styles", DEFAULT_STYLES); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void setStyles(String styles) {
		setString("styles", styles); //$NON-NLS-1$
	}

	public String getApplication() {
		return getString("application", DEFAULT_APPLICATION); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void setApplication(String app) {
		if (app == null || app.length() == 0) {
			app = DEFAULT_APPLICATION; //$NON-NLS-1$
		} else if ( ! app.startsWith(PANDA_SCHEME)) {
			app = PANDA_SCHEME + app;
		}
		setString("application", app); //$NON-NLS-1$
	}

	public boolean getUseSSL() {
		return getBoolean("use_ssl", DEFAULT_USE_SSL); //$NON-NLS-1$
	}

	public void setUseSSL(boolean flag) {
		setBoolean("use_ssl", flag); //$NON-NLS-1$
	}


	public boolean getVerify() {
		return getBoolean("verify", DEFAULT_VERIFY); //$NON-NLS-1$
	}

	public void setVerify(boolean flag) {
		setBoolean("verify", flag); //$NON-NLS-1$
	}

	public int getProtocolVersion() {
		return getInt("protocol_version", DEFAULT_PROTOCOL_VERSION); //$NON-NLS-1$
	}

	// look and feel
	// logger class

	// if USE_SSL
	//options.add("key", "key file name(pem)", null);
	//options.add("cert", "certification file name(pem)", null);
	//options.add("CApath", "path to CA", null);
	//options.add("CAfile", "CA file", null);

	public void setProtocolVersion(int version) {
		setInt("protocol_version", version); //$NON-NLS-1$
	}
}
