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
import java.util.Collections;
import java.util.Map;
import javax.net.ssl.SSLSocketFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.montsuqi.monsia.Style;
import org.montsuqi.util.TempFile;

/**
 * <p>
 * The main application class for panda client.</p>
 */
public class Client implements Runnable {

    private final Config conf;
    private Protocol protocol;
    protected static final Logger logger = LogManager.getLogger(Launcher.class);
    private String user;
    private String host;

    public Config getConf() {
        return conf;
    }

    public String getUser() {
        return user;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
    private int port;

    /**
     * <p>
     * Constructs a client initialized by the given configuration object.</p>
     *
     * @param conf configuration.
     */
    public Client(Config conf) {
        this.conf = conf;
    }

    /**
     * <p>
     * Connects to the server using protocol, user, password and application
     * name specified in the configuration of this client.</p>
     *
     * @throws IOException on IO errors.
     * @throws GeneralSecurityException on SSL verification/authentication
     * failure.
     */
    void connect() throws IOException, GeneralSecurityException {
        int num = conf.getCurrent();
        Map styles = loadStyles();
        long timerPeriod = conf.getUseTimer(num) ? conf.getTimerPeriod(num) : 0;
        protocol = new Protocol(this, styles, timerPeriod);

        user = conf.getUser(num);
        host = conf.getHost(num);
        port = conf.getPort(num);

        if (System.getProperty("monsia.config.reset_user") != null) {
            conf.setUser(num, "");
            conf.save();
        }
        String password = conf.getPassword(num);
        String application = conf.getApplication(num);
        
        if (!conf.getSavePassword(num)) {
            conf.setPassword(num, "");
            conf.save();
        }

        logger.info("connect {}@{}:{} {}", user, host, port, this);
        protocol.sendConnect(user, password, application);
    }

    private Map loadStyles() {
        URL url = conf.getStyleURL(conf.getCurrent());
        try {
            logger.debug("loading styles from URL: {0}", url);
            InputStream in = url.openStream();
            return Style.load(in);
        } catch (IOException e) {
            logger.debug(e);
            logger.debug("using empty style set");
            return Collections.EMPTY_MAP;
        }
    }

    /**
     * <p>
     * Creates a socket for the connection.</p>
     * <p>
     * When the configuration says useSSL, an SSL Socket is returned
     * instead.</p>
     *
     * @return a socket connected at the TCP layer.
     * @throws IOException on IO error.
     * @throws GeneralSecurityException on SSL verification/authentication
     * failure.
     */
    Socket createSocket() throws IOException, GeneralSecurityException {
        int num = conf.getCurrent();
        String hostName = conf.getHost(num);
        int portNum = conf.getPort(num);
        logger.debug("host : {}:{}", hostName, portNum);
        SocketAddress address = new InetSocketAddress(hostName, portNum);
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(address);
        Socket socket = socketChannel.socket();
        if (!conf.getUseSSL(num)) {
            return socket;
        } else {
            String fileName = conf.getClientCertificateFile(num);
            String caFileName = conf.getCACertificateFile(num);            
            String password = conf.getClientCertificatePassword(num);
            SSLSocketBuilder builder = new SSLSocketBuilder(caFileName,fileName, password);
            return builder.createSSLSocket(socket, hostName, portNum);
        }
    }

    /**
     * <p>
     * PrintAgent need SSLSocketFactory for SSL API connection</p>
     *
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     */
    SSLSocketFactory createSSLSocketFactory() throws IOException, GeneralSecurityException {
        int num = conf.getCurrent();
        if (!conf.getUseSSL(num)) {
            return null;
        } else {
            String caFileName = conf.getCACertificateFile(num);                        
            String fileName = conf.getClientCertificateFile(num);
            String password = conf.getClientCertificatePassword(num);
            SSLSocketBuilder builder = new SSLSocketBuilder(caFileName,fileName, password);
            return builder.getFactory();
        }
    }

    /**
     * <p>
     * Kick the application.</p>
     */
    @Override
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
     * <p>
     * Terminates the application.</p>
     * <p>
     * Sends end packet to the server and exists.</p>
     */
    void exitSystem() {
        try {
            synchronized (this) {
                if (protocol != null) {
                    TempFile.cleanTempDir();
                }
            }
        } catch (Exception e) {
            logger.catching(Level.WARN,e);
        } finally {
            logger.info("disconnect {}@{}:{} {}", user, host, port, this);
            System.exit(0);
        }
    }
}
