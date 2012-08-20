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

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.channels.SocketChannel;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import javax.net.ssl.SSLSocketFactory;
import org.montsuqi.monsia.Style;
import org.montsuqi.util.Logger;
import org.montsuqi.util.OptionParser;

/**
 * <p>The main application class for panda client.</p>
 */
public class Client implements Runnable {

    private Configuration conf;
    Logger logger;
    private Protocol protocol;
    private static final String CLIENT_VERSION = "0.0"; //$NON-NLS-1$

    /**
     * <p>Constructs a client initialized by the given configuration object.</p>
     *
     * @param conf configuration.
     */
    public Client(Configuration conf) {
        this.conf = conf;
        logger = Logger.getLogger(Client.class);
        JarSignersHardLinker.go();
    }

    /**
     * <p>A factory method to construct a Client instance initialized by the
     * command line.</p>
     *
     * @param args command line arguments.
     * @return a client.
     */
    private static Client parseCommandLine(String[] args) {
        OptionParser options = new OptionParser();
        options.add("port", Messages.getString("Client.port_number"), Configuration.DEFAULT_PORT); //$NON-NLS-1$ //$NON-NLS-2$
        options.add("host", Messages.getString("Client.host_name"), Configuration.DEFAULT_HOST); //$NON-NLS-1$ //$NON-NLS-2$
        options.add("cache", Messages.getString("Client.cache_directory"), Configuration.DEFAULT_CACHE_PATH); //$NON-NLS-1$ //$NON-NLS-2$
        options.add("user", Messages.getString("Client.user_name"), Configuration.DEFAULT_USER); //$NON-NLS-1$ //$NON-NLS-2$
        options.add("pass", Messages.getString("Client.password"), ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        options.add("style", Messages.getString("Client.styles"), Configuration.DEFAULT_STYLES); //$NON-NLS-1$ //$NON-NLS-2$
        int protocolVersion = Configuration.DEFAULT_PROTOCOL_VERSION;
        options.add("v1", Messages.getString("Client.use_protocol_version_1"), protocolVersion == 1); //$NON-NLS-1$ //$NON-NLS-2$
        options.add("v2", Messages.getString("Client.use_protocol_version_2"), protocolVersion == 2); //$NON-NLS-1$ //$NON-NLS-2$
        options.add("useSSL", "SSL", Configuration.DEFAULT_USE_SSL); //$NON-NLS-1$ //$NON-NLS-2$

        String[] files = options.parse(Client.class.getName(), args);

        Configuration conf = new Configuration(Client.class);
        String configName = Configuration.DEFAULT_CONFIG_NAME;
        conf.setPort(configName, options.getInt("port")); //$NON-NLS-1$
        conf.setHost(configName, options.getString("host")); //$NON-NLS-1$
        conf.setCache(configName, options.getString("cache")); //$NON-NLS-1$
        conf.setUser(configName, options.getString("user")); //$NON-NLS-1$
        conf.setPassword(configName, options.getString("pass")); //$NON-NLS-1$
        conf.setStyleFileName(configName, options.getString("style")); //$NON-NLS-1$

        boolean v1 = options.getBoolean("v1"); //$NON-NLS-1$
        boolean v2 = options.getBoolean("v2"); //$NON-NLS-1$
        if (!(v1 ^ v2)) {
            throw new IllegalArgumentException("specify -v1 or -v2, not both."); //$NON-NLS-1$
        }
        if (v1) {
            conf.setProtocolVersion(configName, 1);
        } else if (v2) {
            conf.setProtocolVersion(configName, 2);
        } else {
            assert false : "-v1 or -v2 should have been given."; //$NON-NLS-1$
        }

        conf.setUseSSL(configName, options.getBoolean("useSSL")); //$NON-NLS-1$

        conf.setApplication(configName, files.length > 0 ? files[0] : null);

        return new Client(conf);
    }

    /**
     * <p>Connects to the server using protocol, user, password and application
     * name specified in the configuration of this client.</p>
     *
     * @throws IOException on IO errors.
     * @throws GeneralSecurityException on SSL verification/authentication
     * failure.
     */
    void connect() throws IOException, GeneralSecurityException {
        String configName = conf.getConfigurationName();
        Map styles = loadStyles();
        int protocolVersion = conf.getProtocolVersion(configName);
        long timerPeriod = conf.getUseTimer(configName) ? conf.getTimerPeriod(configName) : 0;
        protocol = new Protocol(this, styles, protocolVersion, timerPeriod);

        String user = conf.getUser(configName);
        String password = conf.getPassword(configName);
        String application = conf.getApplication(configName);
        protocol.sendConnect(user, password, application);
    }

    private Map loadStyles() {
        URL url = conf.getStyleURL(conf.getConfigurationName());
        try {
            logger.info("loading styles from URL: {0}", url); //$NON-NLS-1$
            InputStream in = url.openStream();
            return Style.load(in);
        } catch (IOException e) {
            logger.debug(e);
            logger.info("using empty style set"); //$NON-NLS-1$
            return Collections.EMPTY_MAP;
        }
    }

    /**
     * <p>Creates a socket for the connection.</p> <p>When the configuration
     * says useSSL, an SSL Socket is returned instead.</p>
     *
     * @return a socket connected at the TCP layer.
     * @throws IOException on IO error.
     * @throws GeneralSecurityException on SSL verification/authentication
     * failure.
     */
    Socket createSocket() throws IOException, GeneralSecurityException {
        String configName = conf.getConfigurationName();
        String host = conf.getHost(configName);
        int port = conf.getPort(configName);
        SocketAddress address = new InetSocketAddress(host, port);
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(address);
        Socket socket = socketChannel.socket();
        if (!conf.getUseSSL(configName)) {
            return socket;
        } else {
            String fileName = conf.getClientCertificateFileName(configName);
            String password = conf.getClientCertificatePassword(configName);
            SSLSocketBuilder builder = new SSLSocketBuilder(fileName, password);
            return builder.createSSLSocket(socket, host, port);
        }
    }

    /**
     * <p>PrintAgent need SSLSocketFactory for SSL API connection</p>
     * @return
     * @throws IOException
     * @throws GeneralSecurityException 
     */
    SSLSocketFactory createSSLSocketFactory() throws IOException, GeneralSecurityException {
        String configName = conf.getConfigurationName();
        if (!conf.getUseSSL(configName)) {
            return null;
        } else {
            String fileName = conf.getClientCertificateFileName(configName);
            String password = conf.getClientCertificatePassword(configName);
            SSLSocketBuilder builder = new SSLSocketBuilder(fileName, password);
            return builder.getFactory();
        }
    }

    /**
     * <p>Kick the application.</p>
     */
    public void run() {
        try {
            protocol.checkScreens(true);
            protocol.startReceiving();
            protocol.getScreenData();
            protocol.stopReceiving();
            protocol.startPing();
        } catch (IOException e) {
            protocol.exceptionOccured(e);
        }
    }

    /**
     * <p>Terminates the application.</p> <p>Sends end packet to the server and
     * exists.</p>
     */
    void exitSystem() {
        try {
            synchronized (this) {
                if (protocol != null) {
                    protocol.sendPacketClass(PacketClass.END);
                }
            }
        } catch (Exception e) {
            logger.warn(e);
        } finally {
            System.exit(0);
        }
    }

    /**
     * <p>Dispose connection if it exists.</p>
     */
    protected void finalize() {
        if (protocol != null) {
            exitSystem();
        }
    }

    public static void main(String[] args) {
        Object[] params = {CLIENT_VERSION};
        System.out.println(MessageFormat.format(Messages.getString("Client.banner_format"), params)); //$NON-NLS-1$

        Client client = Client.parseCommandLine(args);
        try {
            client.connect();
            Thread t = new Thread(client);
            t.start();
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
