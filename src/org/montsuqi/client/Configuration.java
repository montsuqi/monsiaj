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
import javax.swing.UIManager;
import org.montsuqi.util.Logger;

public abstract class Configuration {

	private String pass;
	private boolean configured;

	private static final String PORT_KEY = "port"; //$NON-NLS-1$
	private static final String HOST_KEY = "host"; //$NON-NLS-1$
	private static final String USER_KEY = "user"; //$NON-NLS-1$
	private static final String CACHE_KEY = "cache"; //$NON-NLS-1$
	private static final String ENCODING_KEY = "encoding"; //$NON-NLS-1$
	private static final String STYLES_KEY = "styles"; //$NON-NLS-1$
	private static final String APPLICATION_KEY = "application"; //$NON-NLS-1$
	private static final String USE_SSL_KEY = "use_ssl"; //$NON-NLS-1$
	private static final String VERIFY_KEY = "verify"; //$NON-NLS-1$
	private static final String PROTOCOL_VERSION_KEY = "protocol_version"; //$NON-NLS-1$
	private static final String LOOK_AND_FEEL_KEY = "look_and_feel"; //$NON-NLS-1$

	private static final String PANDA_SCHEME = "panda:"; //$NON-NLS-1$
	static final int DEFAULT_PORT = 8000;
	static final String DEFAULT_HOST = "localhost"; //$NON-NLS-1$
	static final String DEFAULT_USER = System.getProperty("user.name"); //$NON-NLS-1$
	static final String DEFAULT_APPLICATION = "demo"; //$NON-NLS-1$
	static final String DEFAULT_ENCODING = "EUC-JP"; //$NON-NLS-1$
	static final String DEFAULT_CACHE_PATH = System.getProperty("user.home") + File.separator + "cache";  //$NON-NLS-1$//$NON-NLS-2$
	static final String DEFAULT_STYLES = ""; //$NON-NLS-1$
	static final boolean DEFAULT_USE_SSL = false;
	static final boolean DEFAULT_VERIFY = false;
	static final int DEFAULT_PROTOCOL_VERSION = 1;
	static final String DEFAULT_LOOK_AND_FEEL_CLASS_NAME = UIManager.getSystemLookAndFeelClassName();

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

	boolean isConfigured() {
		return configured;
	}

	void setConfigured(boolean flag) {
		configured = flag;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public int getPort() {
		return getInt(PORT_KEY, DEFAULT_PORT);
	}

	public void setPort(int port) {
		setInt(PORT_KEY, port);
	}

	public String getHost() {
		return getString(HOST_KEY, DEFAULT_HOST);
	}

	public void setHost(String host) {
		setString(HOST_KEY, host);
	}

	public String getUser() {
		return getString(USER_KEY, DEFAULT_USER);
	}

	public void setUser(String user) {
		setString(USER_KEY, user);
	}

	public String getCache() {
		return getString(CACHE_KEY, DEFAULT_CACHE_PATH);
	}

	public void setCache(String cache) {
		setString(CACHE_KEY, cache);
	}

	public String getEncoding() {
		return getString(ENCODING_KEY, DEFAULT_ENCODING);
	}

	public void setEncoding(String encoding) {
		setString(ENCODING_KEY, encoding);
	}

	public String getStyles() {
		return getString(STYLES_KEY, DEFAULT_STYLES);
	}

	public void setStyles(String styles) {
		setString(STYLES_KEY, styles);
	}

	public String getApplication() {
		return getString(APPLICATION_KEY, DEFAULT_APPLICATION);
	}

	public void setApplication(String app) {
		if (app == null || app.length() == 0) {
			app = DEFAULT_APPLICATION; //$NON-NLS-1$
		} else if ( ! app.startsWith(PANDA_SCHEME)) {
			app = PANDA_SCHEME + app;
		}
		setString(APPLICATION_KEY, app);
	}

	public boolean getUseSSL() {
		return getBoolean(USE_SSL_KEY, DEFAULT_USE_SSL);
	}

	public void setUseSSL(boolean flag) {
		setBoolean(USE_SSL_KEY, flag);
	}


	public boolean getVerify() {
		return getBoolean(VERIFY_KEY, DEFAULT_VERIFY);
	}

	public void setVerify(boolean flag) {
		setBoolean(VERIFY_KEY, flag);
	}

	public int getProtocolVersion() {
		return getInt(PROTOCOL_VERSION_KEY, DEFAULT_PROTOCOL_VERSION);
	}

	public void setProtocolVersion(int version) {
		if (version != 1 && version != 2) {
			throw new IllegalArgumentException("only protocol version 1 and 2 are acceptable."); //$NON-NLS-1$
		}
		setInt(PROTOCOL_VERSION_KEY, version);
	}

	public String getLookAndFeelClassName() {
		return getString(LOOK_AND_FEEL_KEY, DEFAULT_LOOK_AND_FEEL_CLASS_NAME);
	}

	public void setLookAndFeelClassName(String className) {
		setString(LOOK_AND_FEEL_KEY, className);
	}

	// logger class, debug mode
	// html rendering

	// if USE_SSL
	//options.add("key", "key file name(pem)", null);
	//options.add("cert", "certification file name(pem)", null);
	//options.add("CApath", "path to CA", null);
	//options.add("CAfile", "CA file", null);
}
