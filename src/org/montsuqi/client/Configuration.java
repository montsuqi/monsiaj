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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.UIManager;
import org.montsuqi.util.Logger;
import org.montsuqi.util.SystemEnvironment;

public class Configuration {

	private String pass;
	private String clientCertificatePass;
	private Preferences prefs;

	private static final String PORT_KEY = "port"; //$NON-NLS-1$
	private static final String HOST_KEY = "host"; //$NON-NLS-1$
	private static final String USER_KEY = "user"; //$NON-NLS-1$
	private static final String CACHE_KEY = "cache"; //$NON-NLS-1$
	private static final String SAVE_PASSWORD_KEY = "save pass"; //$NON-NLS-1$
	private static final String PASSWORD_KEY = "password"; //$NON-NLS-1$
	private static final String ENCODING_KEY = "encoding"; //$NON-NLS-1$
	private static final String STYLES_KEY = "styles"; //$NON-NLS-1$
	private static final String APPLICATION_KEY = "application"; //$NON-NLS-1$
	private static final String USE_SSL_KEY = "use_ssl"; //$NON-NLS-1$
	private static final String SERVER_CERTIFICATE_KEY = "server_certificate"; //$NON-NLS-1$
	private static final String CLIENT_CERTIFICATE_KEY = "client_certificate"; //$NON-NLS-1$
	private static final String CLIENT_CERTIFICATE_PASSWORD_KEY = "client_certificate_password"; //$NON-NLS-1$
	private static final String CLIENT_CERTIFICATE_ALIAS_KEY = "client_certificate_alias"; //$NON-NLS-1$
	private static final String PROTOCOL_VERSION_KEY = "protocol_version"; //$NON-NLS-1$
	private static final String LOOK_AND_FEEL_KEY = "look_and_feel"; //$NON-NLS-1$
	private static final String USE_LOG_VIEWER_KEY = "use_log_viewer"; //$NON-NLS-1$

	private static final String PANDA_SCHEME = "panda:"; //$NON-NLS-1$
	static final int DEFAULT_PORT = 8000;
	static final String DEFAULT_HOST = "localhost"; //$NON-NLS-1$
	static final String DEFAULT_USER = System.getProperty("user.name"); //$NON-NLS-1$
	static final String DEFAULT_PASSWORD = ""; //$NON-NLS-1$
	static final String DEFAULT_SERVER_CERTIFICATE = ""; //$NON-NLS-1$
	static final String DEFAULT_CLIENT_CERTIFICATE = ""; //$NON-NLS-1$
	static final String DEFAULT_CLIENT_CERTIFICATE_ALIAS = ""; //$NON-NLS-1$
	static final boolean DEFAULT_SAVE_PASSWORD = false;
	static final String DEFAULT_APPLICATION = "demo"; //$NON-NLS-1$
	static final String DEFAULT_ENCODING = "EUC-JP"; //$NON-NLS-1$
	static final String DEFAULT_CACHE_PATH;
	static {
		String[] pathElements = {
			System.getProperty("user.home"), //$NON-NLS-1$
			".monsiaj", //$NON-NLS-1$
			"cache" //$NON-NLS-1$
		};
		DEFAULT_CACHE_PATH = SystemEnvironment.createFilePath(pathElements).getAbsolutePath();
	}
	static final String DEFAULT_STYLES = ""; //$NON-NLS-1$
	static final String DEFAULT_STYLE_RESOURCE_NAME = "/org/montsuqi/client/style.properties"; //$NON-NLS-1$
	static final boolean DEFAULT_USE_SSL = false;
	static final boolean DEFAULT_VERIFY = false;
	static final int DEFAULT_PROTOCOL_VERSION = 1;
	static final String DEFAULT_LOOK_AND_FEEL_CLASS_NAME = UIManager.getSystemLookAndFeelClassName();
	static final boolean DEFAULT_USE_LOG_VIEWER = false;

	protected static final Logger logger = Logger.getLogger(Configuration.class);

	public Configuration(Class clazz) {
		prefs = Preferences.userNodeForPackage(clazz);
	}

	protected void save() {
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			logger.warn(e);
		}
	}

	public String getPass() {
		if (getBoolean(SAVE_PASSWORD_KEY, false)) {
			return getString(PASSWORD_KEY, DEFAULT_PASSWORD);
		} else {
			return pass;
		}
	}

	public void setPass(String pass) {
		this.pass = pass;
		if (getBoolean(SAVE_PASSWORD_KEY, false)) {
			setString(PASSWORD_KEY, pass);
		} else {
			setString(PASSWORD_KEY, DEFAULT_PASSWORD);
		}
	}

	public boolean getSavePassword() {
		return getBoolean(SAVE_PASSWORD_KEY, DEFAULT_SAVE_PASSWORD);
	}

	public void setSavePassword(boolean flag) {
		setBoolean(SAVE_PASSWORD_KEY, flag);
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

	public URL getStyleURL() {
		String styleFileName = getStyleFileName();
		if (styleFileName != null && styleFileName.length() > 0) {
			File file = new File(styleFileName);
			try {
				return file.toURL();
			} catch (MalformedURLException e) {
				logger.warn(e);
			}
		}
		return Configuration.class.getResource(DEFAULT_STYLE_RESOURCE_NAME);
	}

	public String getStyleFileName() {
		return getString(STYLES_KEY, DEFAULT_STYLES);
	}

	public void setStyleFileName(String styles) {
		setString(STYLES_KEY, styles);
	}

	public String getApplication() {
		return getString(APPLICATION_KEY, DEFAULT_APPLICATION);
	}

	public void setApplication(String app) {
		if (app == null || app.length() == 0) {
			app = DEFAULT_APPLICATION;
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


	public String getServerCertificateFileName() {
		return getString(SERVER_CERTIFICATE_KEY, DEFAULT_SERVER_CERTIFICATE);
	}

	public void setServerCertificateFileName(String fileName) {
		setString(SERVER_CERTIFICATE_KEY, fileName);
	}

	public String getClientCertificateFileName() {
		return getString(CLIENT_CERTIFICATE_KEY, DEFAULT_CLIENT_CERTIFICATE);
	}

	public void setClientCertificateFileName(String fileName) {
		setString(CLIENT_CERTIFICATE_KEY, fileName);
	}

	public String getClientCertificatePass() {
		if (getBoolean(SAVE_PASSWORD_KEY, false)) {
			return getString(CLIENT_CERTIFICATE_PASSWORD_KEY, DEFAULT_PASSWORD);
		} else {
			return clientCertificatePass;
		}
	}

	public void setClientCertifivatePass(String pass) {
		this.clientCertificatePass = pass;
		if (getBoolean(SAVE_PASSWORD_KEY, false)) {
			setString(CLIENT_CERTIFICATE_PASSWORD_KEY, pass);
		} else {
			setString(CLIENT_CERTIFICATE_PASSWORD_KEY, DEFAULT_PASSWORD);
		}
	}

	public String getClientCertificateAlias() {
		return getString(CLIENT_CERTIFICATE_ALIAS_KEY, DEFAULT_CLIENT_CERTIFICATE_ALIAS);
	}

	public void setClientCertificateAlias(String alias) {
		setString(CLIENT_CERTIFICATE_ALIAS_KEY, alias);
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

	public boolean getUseLogViewer() {
		return getBoolean(USE_LOG_VIEWER_KEY, DEFAULT_USE_LOG_VIEWER);
	}

	public void setUseLogViewer(boolean flag) {
		setBoolean(USE_LOG_VIEWER_KEY, flag);
	}

	protected String getString(String key, String defaultValue) {
		return prefs.get(key, defaultValue);
	}

	protected int getInt(String key, int defaultValue) {
		return prefs.getInt(key, defaultValue);
	}

	protected boolean getBoolean(String key, boolean defaultValue) {
		return prefs.getBoolean(key, defaultValue);
	}

	protected void setString(String key, String value) {
		prefs.put(key, value);
	}

	protected void setInt(String key, int value) {
		prefs.putInt(key, value);
	}

	protected void setBoolean(String key, boolean value) {
		prefs.putBoolean(key, value);
	}
}
