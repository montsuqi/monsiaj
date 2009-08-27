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
package org.montsuqi.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.UIManager;
import org.montsuqi.util.Logger;
import org.montsuqi.util.SystemEnvironment;

/** <p>A class to manage configuration settings of the application.</p>
 * <p>Configuration is stored using the Preferences API. On Unix based systems,
 * it is stored in XML files somewhere in user's home directory. On Windows,
 * it is stored in the registry.</p>
 */
public class Configuration {

    private String pass;
    private String clientCertificatePass;
    private Preferences prefs;
    private static final String PORT_KEY = "port"; //$NON-NLS-1$
    private static final String HOST_KEY = "host"; //$NON-NLS-1$
    private static final String USER_KEY = "user"; //$NON-NLS-1$
    private static final String CACHE_KEY = "cache"; //$NON-NLS-1$
    private static final String SAVE_PASSWORD_KEY = "save_pass"; //$NON-NLS-1$
    private static final String PASSWORD_KEY = "password"; //$NON-NLS-1$
    private static final String STYLES_KEY = "styles"; //$NON-NLS-1$
    private static final String APPLICATION_KEY = "application"; //$NON-NLS-1$
    private static final String USE_SSL_KEY = "use_ssl"; //$NON-NLS-1$
    private static final String SAVE_CLIENT_CERTIFICATE_PASSWORD_KEY = "save_client_certificate_pass"; //$NON-NLS-1$
    private static final String CLIENT_CERTIFICATE_KEY = "client_certificate"; //$NON-NLS-1$
    private static final String CLIENT_CERTIFICATE_PASSWORD_KEY = "client_certificate_password"; //$NON-NLS-1$
    private static final String PROTOCOL_VERSION_KEY = "protocol_version"; //$NON-NLS-1$
    private static final String LOOK_AND_FEEL_KEY = "look_and_feel"; //$NON-NLS-1$
    private static final String LAF_THEME_KEY = "laf_theme"; //$NON-NLS-1$
    private static final String EXPAND_SCREEN_KEY = "expand_screen"; //$NON-NLS-1$
    private static final String USE_LOG_VIEWER_KEY = "use_log_viewer"; //$NON-NLS-1$
    private static final String PROPERTIES_KEY = "properties"; //$NON-NLS-1$
    private static final String USE_TIMER_KEY = "use_timer"; //$NON-NLS-1$
    private static final String TIMER_PERIOD_KEY = "timer_period"; //$NON-NLS-1$
    private static final String CURRENT_CONFIG_KEY = "current_config"; //$NON-NLS-1$
    private static final String PANDA_SCHEME = "panda:"; //$NON-NLS-1$
    private static final String CONFIG_NODE_BASE = "config"; //$NON-NLS-1$
    private static final String CONFIG_NODE = CONFIG_NODE_BASE + "/"; //$NON-NLS-1$
    /** <p>Default port: 8000</p> */
    static final int DEFAULT_PORT = 8000;
    /** <p>Default host: localhost</p> */
    static final String DEFAULT_HOST = "localhost"; //$NON-NLS-1$
    /** <p>Default user: value of System property user.name.</p> */
    static final String DEFAULT_USER = System.getProperty("user.name"); //$NON-NLS-1$
    /** <p>Default password: empty string.</p> */
    static final String DEFAULT_PASSWORD = ""; //$NON-NLS-1$
    /** <p>Default client certificate password: empty string.</p> */
    static final String DEFAULT_CLIENT_CERTIFICATE_PASSWORD = ""; //$NON-NLS-1$
    /** <p>Default client certificate path: empty string.</p> */
    static final String DEFAULT_CLIENT_CERTIFICATE = ""; //$NON-NLS-1$
    /** <p>Default value of save password checkbox: false.</p> */
    static final boolean DEFAULT_SAVE_PASSWORD = false;
    /** <p>Default application name: "demo".</p> */
    static final String DEFAULT_APPLICATION = "demo"; //$NON-NLS-1$
    /** <p>Default cache directory: [value of System property user.home]/.monsiaj/cache</p> */
    static final String DEFAULT_CACHE_PATH;


    static {
        String[] pathElements = {
            System.getProperty("user.home"), //$NON-NLS-1$
            ".monsiaj", //$NON-NLS-1$
            "cache" //$NON-NLS-1$
        };
        DEFAULT_CACHE_PATH = SystemEnvironment.createFilePath(pathElements).getAbsolutePath();
    }
    /** <p>Default style definitions: empty string.</p> */
    static final String DEFAULT_STYLES = ""; //$NON-NLS-1$
    /** <p>Default style resource name: /org/montsuqi/client/style.properteis.</p> */
    static final String DEFAULT_STYLE_RESOURCE_NAME = "/org/montsuqi/client/style.properties"; //$NON-NLS-1$
    /** <p>Default value of use SSL checkbox: false.</p> */
    static final boolean DEFAULT_USE_SSL = false;
    /** <p>Default value of save client certificate password checkbox: false.</p> */
    static final boolean DEFAULT_SAVE_CLIENT_CERTIFICATE_PASSWORD = false;
    /** <p>Default protocol version: 1.</p> */
    static final int DEFAULT_PROTOCOL_VERSION = 1;
    /** <p>Default look and feel class name: system look and feel.</p> */
    static final String DEFAULT_LOOK_AND_FEEL_CLASS_NAME = UIManager.getSystemLookAndFeelClassName();
    /** <p>Default look and feel theme filename: "".</p> */
    static final String DEFAULT_LAF_THEME = "";
    /** <p>Default value of do expand screen: false.</p> */
    static final boolean DEFAULT_EXPAND_SCREEN = false;
    /** <p>Default value of use log viewer checkbox: false.</p> */
    static final boolean DEFAULT_USE_LOG_VIEWER = false;
    /** <p>Default value of properties: false.</p> */
    static final String DEFAULT_PROPERTIES = "";
    /** <p>Default value of use timer: false.</p> */
    static final boolean DEFAULT_USE_TIMER = true;
    /** <p>Default value of timer period(ms): 1000.</p> */
    static final long DEFAULT_TIMER_PERIOD = 1000;
    /** <p>Default value of default config name.</p> */
    static final String DEFAULT_CONFIG_NAME = "default";
    protected static final Logger logger = Logger.getLogger(Configuration.class);

    /** <p>Constructs a configuration object.</p>
     *
     * @param clazz class object used to obtain user preference node.
     */
    public Configuration(Class clazz) {
        boolean hasDefault = false;
        prefs = Preferences.userNodeForPackage(clazz);
        hasDefault = nodeExists(DEFAULT_CONFIG_NAME);
        if (!hasDefault) {
            newConfiguration(DEFAULT_CONFIG_NAME);
			copyOldConfig(Preferences.userRoot().node("/jp/or/med/jma_receipt"),
                    Preferences.userNodeForPackage(clazz));
        }
        pass = DEFAULT_PASSWORD;
        clientCertificatePass = DEFAULT_CLIENT_CERTIFICATE_PASSWORD;
    }

    public void listConfiguration() {
        System.out.println(Messages.getString("Configuration.list_title"));
        System.out.println("------------------");
        String[] configNames = getConfigurationNames();
        for (int i = 0; i < configNames.length; i++) {
            System.out.println(configNames[i]);
            System.out.println(Messages.getString("Configuration.list_host") + getHost(configNames[i]));
            System.out.println(Messages.getString("Configuration.list_port") + getPort(configNames[i]));
            System.out.println(Messages.getString("Configuration.list_application") + getApplication(configNames[i]));
            System.out.println(Messages.getString("Configuration.list_user") + getUser(configNames[i]));
        }
    }

    /** <p>Copy old version's configuration.</p>
     *
     */
    private void copyOldConfig(Preferences oldPref, Preferences newPref) {
        try {
            String[] keys;
            String[] children;
            int i;
            keys = oldPref.keys();
            for (i = 0; i < keys.length; i++) {
                newPref.put(keys[i], oldPref.get(keys[i], ""));
            }
            children = oldPref.childrenNames();
            for (i = 0; i < children.length; i++) {
                copyOldConfig(
                    oldPref.node(oldPref.absolutePath()+ "/" + children[i]),
                    newPref.node(newPref.absolutePath()+ "/" + children[i]));
            }
            newPref.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** <p>Ensures the configuration is saved.</p>
     */
    protected void save() {
        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            logger.warn(e);
        }
    }

    /** <p>New configuration.</p>
     *
     * @param configName Configuration Name.
     */
    public boolean newConfiguration(String configName) {
        if (!nodeExists(configName)) {
            prefs.node(CONFIG_NODE + configName);
            return true;
        }
        return false;
    }

    /** <p>Remove configuration.</p>
     *
     * @param configName Configuration Name.
     */
    public void deleteConfiguration(String configName) {
        try {
            Preferences config = prefs.node(CONFIG_NODE + configName);
            config.removeNode();
        } catch (java.util.prefs.BackingStoreException ex) {
            logger.warn(ex);
        }
    }

    /** <p>Get configuration name list.</p>
     *
     * @return configuration name.
     */
    public String[] getConfigurationNames() {
        Preferences configs = prefs.node(CONFIG_NODE_BASE);
        try {
            return configs.childrenNames();
        } catch (java.util.prefs.BackingStoreException ex) {
            logger.warn(ex);
        }
        return null;
    }

    /** <p>rename configuration.</p>
     *
     * @param oldName Old configuration name.
     * @param newName New configuration name.
     * @return result.
     */
    public boolean renameConfiguration(String oldName, String newName) {
        try {
            if (nodeExists(oldName) &&
                    !nodeExists(newName)) {
                Preferences oldConfig = prefs.node(CONFIG_NODE + oldName);
                Preferences newConfig = prefs.node(CONFIG_NODE + newName);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                oldConfig.exportNode(out);
                newConfig.importPreferences(new ByteArrayInputStream(out.toByteArray()));
                newConfig.flush();
                deleteConfiguration(oldName);
                save();
                return true;
            }
        } catch (java.util.prefs.BackingStoreException ex) {
            logger.warn(ex);
        } catch (java.util.prefs.InvalidPreferencesFormatException ex) {
            logger.warn(ex);
        } catch (java.io.IOException ex) {
            logger.warn(ex);
        }
        return false;
    }

    /** <p>Get current configuration.</p>
     *
     * @return Current configuration name.
     */
    public String getConfigurationName() {
        String configName = prefs.get(CURRENT_CONFIG_KEY, DEFAULT_CONFIG_NAME);
        if (!nodeExists(configName)) {
            configName = DEFAULT_CONFIG_NAME;
            setConfigurationName(configName);
        }
        return configName;
    }

    /** <p>Set current configuration.</p>
     *
     * @param configName Current configuration name.
     */
    public void setConfigurationName(String configName) {
        if (nodeExists(configName)) {
            prefs.put(CURRENT_CONFIG_KEY, configName);
        } else {
            prefs.put(CURRENT_CONFIG_KEY, DEFAULT_CONFIG_NAME);
        }
    }

    /** <p>Returns the password.</p>
     * <p>If save password is set to false, it always returns DEFAULT_PASSWORD("").</p>
     * @params configName the configuration name.
     * @return the password or empty string.
     */
    public String getPassword(String configName) {
        if (getBoolean(configName, SAVE_PASSWORD_KEY, false)) {
            return getString(configName, PASSWORD_KEY, DEFAULT_PASSWORD);
        } else {
            return pass;
        }
    }

    /** <p>Sets the password.</p>
     * <p>It stores the new password into the member field.</p>
     * <p>If save password is set to true, it also store the password into the configuration,
     * otherwise it sets the DEFAULT_PASSWORD("") to clear it.</p>
     * @param configName the configuration name.
     * @param pass the new password.
     */
    public void setPassword(String configName, String pass) {
        this.pass = pass;
        if (getBoolean(configName, SAVE_PASSWORD_KEY, false)) {
            setString(configName, PASSWORD_KEY, pass);
        } else {
            setString(configName, PASSWORD_KEY, DEFAULT_PASSWORD);
        }
    }

    /** <p>Returns the value of save password.</p>
     *
     * @param configName the configuration name.
     * @return value of save password.</p>
     */
    public boolean getSavePassword(String configName) {
        return getBoolean(configName, SAVE_PASSWORD_KEY, DEFAULT_SAVE_PASSWORD);
    }

    /** <p>Sets the value of save password.</p>
     *
     * @param configName the configuration name.
     * @param flag new value of save password.
     */
    public void setSavePassword(String configName, boolean flag) {
        setBoolean(configName, SAVE_PASSWORD_KEY, flag);
        if (!flag) {
            setPassword(configName, DEFAULT_PASSWORD);
        }
    }

    /** <p>Returns the client certificate password.</p>
     * <p>If save password is set to false, it always returns DEFAULT_PASSWORD("").</p>
     * @param configName the configuration name.
     * @return the password or empty string.
     */
    public String getClientCertificatePassword(String configName) {
        if (getBoolean(configName, SAVE_CLIENT_CERTIFICATE_PASSWORD_KEY, false)) {
            return getString(configName, CLIENT_CERTIFICATE_PASSWORD_KEY, DEFAULT_CLIENT_CERTIFICATE_PASSWORD);
        } else {
            return clientCertificatePass;
        }
    }

    /** <p>Sets the client certificate password.</p>
     * <p>It stores the new password into the member field.</p>
     * <p>If save client password is set to true, it also store the password into the configuration,
     * otherwise it sets the DEFAULT_PASSWORD("") to clear it.</p>
     * @param configName the configuration name.
     * @param pass the new password.
     */
    public void setClientCertificatePassword(String configName, String pass) {
        this.clientCertificatePass = pass;
        if (getBoolean(configName, SAVE_CLIENT_CERTIFICATE_PASSWORD_KEY, false)) {
            setString(configName, CLIENT_CERTIFICATE_PASSWORD_KEY, pass);
        } else {
            setString(configName, CLIENT_CERTIFICATE_PASSWORD_KEY, DEFAULT_CLIENT_CERTIFICATE_PASSWORD);
        }
    }

    /** <p>Returns the value of save client certificate password.</p>
     *
     * @param configName the configuration name.
     * @return value of save client certificate password.</p>
     */
    public boolean getSaveClientCertificatePassword(String configName) {
        return getBoolean(configName, SAVE_CLIENT_CERTIFICATE_PASSWORD_KEY, DEFAULT_SAVE_CLIENT_CERTIFICATE_PASSWORD);
    }

    /** <p>Sets the value of save client certificate password.</p>
     *
     * @configName the configuration name.
     * @param flag new value of save client certificate password.
     */
    public void setSaveClientCertificatePassword(String configName, boolean flag) {
        setBoolean(configName, SAVE_CLIENT_CERTIFICATE_PASSWORD_KEY, flag);
        if (!flag) {
            setClientCertificatePassword(configName, DEFAULT_CLIENT_CERTIFICATE_PASSWORD);
        }
    }

    /** <p>Returns the port.</p>
     *
     * @param configName the configuration name.
     * @return the port number.
     */
    public int getPort(String configName) {
        return getInt(configName, PORT_KEY, DEFAULT_PORT);
    }

    /** <p>Sets the port.</p>
     *
     * @param configName the configuration name.
     * @param port new value of the port.
     */
    public void setPort(String configName, int port) {
        setInt(configName, PORT_KEY, port);
    }

    /** <p>Returns the host.</p>
     *
     * @param configName the configuration name.
     * @return the host.
     */
    public String getHost(String configName) {
        return getString(configName, HOST_KEY, DEFAULT_HOST);
    }

    /** <p>Sets the host.</p>
     *
     * @param configName the configuration name.
     * @param host new value of the host.
     */
    public void setHost(String configName, String host) {
        setString(configName, HOST_KEY, host);
    }

    /** <p>Returns the user.</p>
     * @param configName the configuration name.
     * @return the user.
     */
    public String getUser(String configName) {
        return getString(configName, USER_KEY, DEFAULT_USER);
    }

    /** <p>Sets the user.</p>
     *
     * @param configName configuration name.
     * @param user new value of the user.
     */
    public void setUser(String configName, String user) {
        setString(configName, USER_KEY, user);
    }

    /** <p>Returns the cache directory.</p>
     *
     * @param configName the configuration name.
     * @return the cache directory.
     */
    public String getCache(String configName) {
        return getString(configName, CACHE_KEY, DEFAULT_CACHE_PATH);
    }

    /** <p>Sets the cache directory.</p>
     *
     * @param configName the configuration name.
     * @param cache new value of the cache directory.
     */
    public void setCache(String configName, String cache) {
        setString(configName, CACHE_KEY, cache);
    }

    /** <p>Returns the style URL.</p>
     * <p>It first tries converting style file name specified in the configuration to URL.
     * If it fails it falls back to default style resource.</p>
     * @param configName the configuration name.
     * @return the style URL.
     */
    public URL getStyleURL(String configName) {
        String styleFileName = getStyleFileName(configName);
        if (styleFileName != null && styleFileName.length() > 0) {
            File file = new File(styleFileName);
            try {
                return file.toURI().toURL();
            } catch (MalformedURLException e) {
                logger.warn(e);
            }
        }
        return Configuration.class.getResource(DEFAULT_STYLE_RESOURCE_NAME);
    }

    /** <p>Returns the style file name.</p>
     *
     * @param configName the configuration name.
     * @return the style file name.
     */
    public String getStyleFileName(String configName) {
        return getString(configName, STYLES_KEY, DEFAULT_STYLES);
    }

    /** <p>Sets the style file name.</p>
     * @param configName the configuration name.
     * @param styles new value of style file name.
     */
    public void setStyleFileName(String configName, String styles) {
        setString(configName, STYLES_KEY, styles);
    }

    /** <p>Returns the name of the application.</p>
     *
     * @param configName the configuration name.
     * @return the name of the application.
     */
    public String getApplication(String configName) {
        return getString(configName, APPLICATION_KEY, DEFAULT_APPLICATION);
    }

    /** <p>Sets the name of the application.</p>
     * @param configName the configuration name.
     * @param app new value of the name of the pplication.
     */
    public void setApplication(String configName, String app) {
        if (app == null || app.length() == 0) {
            app = DEFAULT_APPLICATION;
        } else if (!app.startsWith(PANDA_SCHEME)) {
            app = PANDA_SCHEME + app;
        }
        setString(configName, APPLICATION_KEY, app);
    }

    /** <p>Returns the value of use SSL.</p>
     * @param configName the configuration name.
     * @return true if using SSL. false otherwise.
     */
    public boolean getUseSSL(String configName) {
        return getBoolean(configName, USE_SSL_KEY, DEFAULT_USE_SSL);
    }

    /** <p>Sets the value of use SSL.</p>
     *
     * @param configName the configuration name.
     * @param flag new value of use SSL.
     */
    public void setUseSSL(String configName, boolean flag) {
        setBoolean(configName, USE_SSL_KEY, flag);
    }

    /** <p>Returns the name of client certificate file.</p>
     *
     * @param configName the configuration name.
     * @return the name of client certificate.
     */
    public String getClientCertificateFileName(String configName) {
        return getString(configName, CLIENT_CERTIFICATE_KEY, DEFAULT_CLIENT_CERTIFICATE);
    }

    /** <p>Sets the name of client certificate file.</p>
     *
     * @param configName the configuration name.
     * @param fileName new name of client certificate file.
     */
    public void setClientCertificateFileName(String configName, String fileName) {
        setString(configName, CLIENT_CERTIFICATE_KEY, fileName);
    }

    /** <p>Returns the protocol version.</p>
     *
     * @param configName the configuration name.
     * @return protocol version.
     */
    public int getProtocolVersion(String configName) {
        return getInt(configName, PROTOCOL_VERSION_KEY, DEFAULT_PROTOCOL_VERSION);
    }

    /** <p>Sets the protocol version.</p>
     *
     * @param configName the configuration name.
     * @param version version number(1 or 2).
     */
    public void setProtocolVersion(String configName, int version) {
        if (version != 1 && version != 2) {
            throw new IllegalArgumentException("only protocol version 1 and 2 are acceptable."); //$NON-NLS-1$
        }
        setInt(configName, PROTOCOL_VERSION_KEY, version);
    }

    /** <p>Returns the look and feel class name.</p>
     *
     * @param configName the configuration name,
     * @return look and feel class name.
     */
    public String getLookAndFeelClassName(String configName) {
        return getString(configName, LOOK_AND_FEEL_KEY, DEFAULT_LOOK_AND_FEEL_CLASS_NAME);
    }

    /** <p>Sets the look and feel class name.</p>
     *
     * @param configName the configuration name.
     * @param className look and feel class name.
     */
    public void setLookAndFeelClassName(String configName, String className) {
        setString(configName, LOOK_AND_FEEL_KEY, className);
    }

    /** <p>Returns the laf theme file name.</p>
     *
     * @param configName the configuration name.
     * @return the laf theme file name.
     */
    public String getLAFThemeFileName(String configName) {
        return getString(configName, LAF_THEME_KEY, DEFAULT_LAF_THEME);
    }

    /** <p>Sets the LAF theme file name.</p>
     * @param configName the configuration name.
     * @param styles new value of laf theme file name.
     */
    public void setLAFThemeFileName(String configName, String styles) {
        setString(configName, LAF_THEME_KEY, styles);
    }

    /** <p>Returns the value of do expand screen.</p>
     *
     * @param configName the configuration name.
     * @return the value of do expand screen.
     */
    public boolean getExpandScreen(String configName) {
        return getBoolean(configName, EXPAND_SCREEN_KEY, DEFAULT_EXPAND_SCREEN);
    }

    /** <p>Sets the value of do expand screen.</p>
     *
     * @param configName the configuration name.
     * @param flag new value of do expand screen.
     */
    public void setExpandScreen(String configName, boolean flag) {
        setBoolean(configName, EXPAND_SCREEN_KEY, flag);
    }

    /** <p>Returns the value of use log viewer.</p>
     *
     * @param configName the configuration name.
     * @return the value of use log viewer.
     */
    public boolean getUseLogViewer(String configName) {
        return getBoolean(configName, USE_LOG_VIEWER_KEY, DEFAULT_USE_LOG_VIEWER);
    }

    /** <p>Sets the value of use log viewer.</p>
     *
     * @param configName the configuration name.
     * @param flag new value of use log viewer.
     */
    public void setUseLogViewer(String configName, boolean flag) {
        setBoolean(configName, USE_LOG_VIEWER_KEY, flag);
    }

    /** <p>Returns other properties as string.</p>
     * @param configName the configuration value.
     * @return properties.
     */
    public String getProperties(String configName) {
        final String properties = getString(configName, PROPERTIES_KEY, ""); //$NON-NLS-1$
        return properties;
    }

    /** <p>Sets other properties.</p>
     *
     * @param configName the configuration name.
     * @param properties new line separated property assignments.
     */
    public void setProperties(String configName, String properties) {
        setString(configName, PROPERTIES_KEY, properties);
        updateSystemProperties(properties);
    }

    private void updateSystemProperties(String properties) {
        StringReader sr = new StringReader(properties);
        BufferedReader br = new BufferedReader(sr);
        String line;
        try {
            while ((line = br.readLine()) != null) {
                String[] pair = line.split("\\s*=\\s*"); //$NON-NLS-1$
                if (pair.length == 2) {
                    String key = pair[0].trim();
                    String value = pair[1].trim();
                    System.setProperty(key, value);
                }
            }
        } catch (IOException e) {
            logger.warn(e);
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    /** <p>Returns the timer period.</p>>
     * @params configName the configuration name.
     * @return the timer period.
     */
    public long getTimerPeriod(String configName) {
        return getLong(configName, TIMER_PERIOD_KEY, DEFAULT_TIMER_PERIOD);
    }

    /** <p>Sets the timer period.</p>
     * <p>It stores the timer period.</p>
     * @param configName the configuration name.
     * @param period the timer period.
     */
    public void setTimerPeriod(String configName, long period) {
        setLong(configName, TIMER_PERIOD_KEY, period);
    }

    /** <p>Returns the value of use timer.</p>
     *
     * @param configName the configuration name.
     * @return value of use timer.</p>
     */
    public boolean getUseTimer(String configName) {
        return getBoolean(configName, USE_TIMER_KEY, DEFAULT_USE_TIMER);
    }

    /** <p>Sets the value of use timer.</p>
     *
     * @param configName the configuration name.
     * @param flag the use timer flag.
     */
    public void setUseTimer(String configName, boolean flag) {
        setBoolean(configName, USE_TIMER_KEY, flag);
    }

    /** <p>Returns configuration value(String).</p>
     *
     * @param configName the configuration name.
     * @param key configuration key.
     * @param defaultValue value if the configuration for the given key is missing.
     * @return the configuration value.
     */
    protected String getString(String configName, String key, String defaultValue) {
        String ret = "";
        if (nodeExists(configName)) {
            Preferences config = prefs.node(CONFIG_NODE + configName);
            ret = config.get(key, defaultValue);
        } else {
            logger.warn("configuration:" + configName + " is not exist.");
        }
        return ret;
    }

    /** <p>Returns configuration value(int).</p>
     *
     * @param configName the configuration name.
     * @param key configuration key.
     * @param defaultValue value if the configuration for the given key is missing.
     * @return the configuration value.
     */
    protected int getInt(String configName, String key, int defaultValue) {
        int ret = 0;
        if (nodeExists(configName)) {
            Preferences config = prefs.node(CONFIG_NODE + configName);
            ret = config.getInt(key, defaultValue);
        } else {
            logger.warn("configuration:" + configName + " is not exist.");
        }
        return ret;
    }

    /** <p>Returns configuration value(long).</p>
     *
     * @param configName the configuration name.
     * @param key configuration key.
     * @param defaultValue value if the configuration for the given key is missing.
     * @return the configuration value.
     */
    protected long getLong(String configName, String key, long defaultValue) {
        long ret = 0;
        if (nodeExists(configName)) {
            Preferences config = prefs.node(CONFIG_NODE + configName);
            ret = config.getLong(key, defaultValue);
        } else {
            logger.warn("configuration:" + configName + " is not exist.");
        }
        return ret;
    }

    /** <p>Returns configuration value(boolean).</p>
     *
     * @param configName the configuration name.
     * @param key configuration key.
     * @param defaultValue value if the configuration for the given key is missing.
     * @return the configuration value.
     */
    protected boolean getBoolean(String configName, String key, boolean defaultValue) {
        boolean ret = false;
        if (nodeExists(configName)) {
            Preferences config = prefs.node(CONFIG_NODE + configName);
            ret = config.getBoolean(key, defaultValue);
        } else {
            logger.warn("configuration:" + configName + " is not exist.");
        }
        return ret;
    }

    /** <p>Sets configuration value(String).</p>
     *
     * @param configName configuration key.
     * @param key configuration key.
     * @param value the configuration value.
     */
    protected void setString(String configName, String key, String value) {
        if (nodeExists(configName)) {
            Preferences config = prefs.node(CONFIG_NODE + configName);
            config.put(key, value);
        } else {
            logger.warn("configuration:" + configName + " is not exist.");
        }
    }

    /** <p>Sets configuration value(int).</p>
     *
     * @param configName configuration name.
     * @param key configuration key.
     * @param value the configuration value.
     */
    protected void setInt(String configName, String key, int value) {
        if (nodeExists(configName)) {
            Preferences config = prefs.node(CONFIG_NODE + configName);
            config.putInt(key, value);
        } else {
            logger.warn("configuration:" + configName + " is not exist.");
        }
    }

    /** <p>Sets configuration value(long).</p>
     *
     * @param configName configuration name.
     * @param key configuration key.
     * @param value the configuration value.
     */
    protected void setLong(String configName, String key, long value) {
        if (nodeExists(configName)) {
            Preferences config = prefs.node(CONFIG_NODE + configName);
            config.putLong(key, value);
        } else {
            logger.warn("configuration:" + configName + " is not exist.");
        }
    }

    /** <p>Sets configuration value(boolean).</p>
     *
     * @param configName the configuration name.
     * @param key configuration key.
     * @param value the configuration value.
     */
    protected void setBoolean(String configName, String key, boolean value) {
        if (nodeExists(configName)) {
            Preferences config = prefs.node(CONFIG_NODE + configName);
            config.putBoolean(key, value);
        } else {
            logger.warn("configuration:" + configName + " is not exist.");
        }
    }

    protected boolean nodeExists(String configName) {
        boolean ret = false;
        try {
            ret = prefs.nodeExists(CONFIG_NODE + configName);
        } catch (java.util.prefs.BackingStoreException ex) {
            logger.warn(ex);
        }
        return ret;
    }
}
