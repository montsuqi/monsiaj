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
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;
import javax.net.ssl.SSLSocketFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.montsuqi.monsia.Style;

/**
 * <p>
 * The main application class for panda client.</p>
 */
public class Client implements Runnable {

    private Config conf;
    Logger logger;
    private Protocol protocol;

    /**
     * <p>
     * Constructs a client initialized by the given configuration object.</p>
     *
     * @param conf configuration.
     */
    public Client(Config conf) {
        this.conf = conf;
        logger = LogManager.getLogger(Client.class);
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
        protocol = new Protocol(conf, styles, timerPeriod);
        logger.debug("user : {}", conf.getUser(num));
        protocol.startSession();
    }

    private Map loadStyles() {
        URL url = conf.getStyleURL(conf.getCurrent());
        try {
            logger.debug("loading styles from URL: {0}", url); //$NON-NLS-1$
            InputStream in = url.openStream();
            return Style.load(in);
        } catch (IOException e) {
            logger.debug(e);
            logger.debug("using empty style set"); //$NON-NLS-1$
            return Collections.EMPTY_MAP;
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
            String fileName = conf.getClientCertificateFile(num);
            String password = conf.getClientCertificatePassword(num);
            SSLSocketBuilder builder = new SSLSocketBuilder(fileName, password);
            return builder.getFactory();
        }
    }

    /**
     * <p>
     * Kick the application.</p>
     */
    public void run() {
        protocol.startReceiving();
        protocol.getWindow();
        protocol.updateScreen();
        protocol.stopReceiving();
        protocol.startPing();
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
                protocol.endSession();
            }
        } catch (Exception e) {
            logger.warn(e);
        } finally {
            System.exit(0);
        }
    }

    /**
     * <p>
     * Dispose connection if it exists.</p>
     */
    protected void finalize() {
    }
}
