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
package org.montsuqi.monsiaj.client;

import org.montsuqi.monsiaj.util.Messages;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.swing.JOptionPane;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.montsuqi.monsiaj.monsia.Interface;
import org.montsuqi.monsiaj.util.GtkStockIcon;
import org.montsuqi.monsiaj.util.PopupNotify;
import org.montsuqi.monsiaj.widgets.ExceptionDialog;

/**
 * <p>
 * The main application class for panda client.</p>
 */
public class Client {

    private boolean isReceiving;
    private final Config conf;
    private static final Logger logger = LogManager.getLogger(Client.class);
    private Protocol protocol;
    private final UIControl uiControl;
    private static final int PING_TIMER_PERIOD;
    private javax.swing.Timer pingTimer;
    private JSONObject windowStack;
    private String focusedWindow;
    private String focusedWidget;
    private PusherClient pusherClient;

    static {
        if (System.getProperty("monsia.ping_timer_period") != null) {
            int period = Integer.parseInt(System.getProperty("monsia.ping_timer_period")) * 1000;
            if (period < 1000) {
                PING_TIMER_PERIOD = 7 * 1000;
            } else {
                PING_TIMER_PERIOD = period;
            }
        } else {
            PING_TIMER_PERIOD = 7 * 1000;
        }
    }

    public Client(Config conf) throws IOException {
        this.conf = conf;
        int n = conf.getCurrent();
        int delay = conf.getTimerPeriod(n);
        if (!conf.getUseTimer(n)) {
            delay = 0;
        }
        uiControl = new UIControl(this, conf.getStyleURL(n), delay);
        isReceiving = false;
    }

    void connect() throws IOException, GeneralSecurityException, JSONException {
        int num = conf.getCurrent();
        String authURI = conf.getAuthURI(num);
        authURI = authURI.replace("http://", "");
        authURI = authURI.replace("https://", "");
        if (conf.getUseSSL(num)) {
            authURI = "https://" + authURI;
        } else {
            authURI = "http://" + authURI;
        }
        logger.info("try connect " + authURI);
        protocol = new Protocol(authURI, conf.getUser(num), conf.getPassword(num));
        if (conf.getUseSSL(num)) {
            if (conf.getUsePKCS11(num)) {
                protocol.makeSSLSocketFactoryPKCS11(conf.getCACertificateFile(num), conf.getPKCS11Lib(num), conf.getPKCS11Slot(num));
            } else {
                protocol.makeSSLSocketFactoryPKCS12(conf.getCACertificateFile(num), conf.getClientCertificateFile(num), conf.getClientCertificatePassword(num));
            }
        } else {
            protocol.makeSSLSocketFactory(conf.getCACertificateFile(num));
        }
        if (!conf.getSavePassword(num)) {
            conf.setPassword(num, "");
            conf.save();
        }
        if (!conf.getSaveClientCertificatePassword(num)) {
            conf.setClientCertificatePassword(num, "");
            conf.save();
        }
        if (System.getProperty("monsia.config.reset_user") != null) {
            conf.setUser(num, "");
            conf.save();
        }

        protocol.getServerInfo();
        protocol.startSession();
        logger.info("connected session_id:" + protocol.getSessionId());
        startReceiving();
        windowStack = protocol.getWindow();
        updateScreen();
        stopReceiving();
        startPing();

        if (protocol.isUsePushClient()) {
            try {
                pusherClient = new PusherClient(conf, protocol);
                pusherClient.start();
            } catch (URISyntaxException | KeyStoreException | FileNotFoundException | NoSuchAlgorithmException | CertificateException ex) {
                logger.info(ex, ex);
            }
        }
    }

    void disconnect() {
        try {
            protocol.endSession();
            logger.info("disconnect session_id:" + protocol.getSessionId());
        } catch (IOException | JSONException e) {
            logger.warn(e, e);
        } finally {
            System.exit(0);
        }
    }

    public void startPing() {
        pingTimer = new javax.swing.Timer(PING_TIMER_PERIOD, (ActionEvent e) -> {
            sendPing();
        });
        pingTimer.start();
    }

    public void updateScreen() throws JSONException, IOException {
        JSONObject windowData = windowStack.getJSONObject("window_data");
        focusedWindow = windowData.getString("focused_window");
        focusedWidget = windowData.getString("focused_widget");
        JSONArray windows = windowData.getJSONArray("windows");

        for (int i = 0; i < windows.length(); i++) {
            JSONObject w = windows.getJSONObject(i);
            String putType = w.getString("put_type");
            String windowName = w.getString("window");
            Node node = uiControl.getNode(windowName);
            if (node == null) {
                long t1 = System.currentTimeMillis();
                String gladeData = protocol.getScreenDefine(windowName);
                long t2 = System.currentTimeMillis();
                try {
                    node = new Node(Interface.parseInput(new ByteArrayInputStream(gladeData.getBytes("UTF-8")), uiControl), windowName);
                } catch (UnsupportedEncodingException ex) {
                    logger.info(ex, ex);
                    return;
                }
                long t3 = System.currentTimeMillis();
                uiControl.putNode(windowName, node);
                long t4 = System.currentTimeMillis();
                if (System.getProperty("monsia.do_profile") != null) {
                    logger.info("getScreenDefine:" + (t2 - t1) + "ms newNode[" + windowName + "]:" + (t3 - t2) + "ms putNode:" + (t4 - t3) + "ms");
                }
            }
            logger.debug("window[" + windowName + "] put_type[" + putType + "]");
        }

        for (int i = 0; i < windows.length(); i++) {
            JSONObject w = windows.getJSONObject(i);
            String putType = w.getString("put_type");
            String windowName = w.getString("window");
            if (putType.matches("new") || putType.matches("current")) {
            } else {
                uiControl.closeWindow(windowName);
            }
        }
        for (int i = 0; i < windows.length(); i++) {
            JSONObject w = windows.getJSONObject(i);
            JSONObject screenData = w.getJSONObject("screen_data");
            String putType = w.getString("put_type");
            String windowName = w.getString("window");
            JSONObject tmpl = (JSONObject) uiControl.getScreenTemplate(windowName);
            if (tmpl == null) {
                if (screenData.length() > 0) {
                    uiControl.addScreenTemplate(windowName, screenData);
                    tmpl = screenData;
                }
            } else {
                uiControl.updateScreenTemplate(tmpl, screenData);
            }
            if (putType.matches("new") || putType.matches("current")) {
                Node node = uiControl.getNode(windowName);
                long t1 = System.currentTimeMillis();
                uiControl.setWidget(node.getInterface(), node.getInterface().getWidgetByLongName(windowName), tmpl);
                long t2 = System.currentTimeMillis();
                uiControl.showWindow(windowName);
                long t3 = System.currentTimeMillis();
                if (System.getProperty("monsia.do_profile") != null) {
                    logger.info("setWidget:" + (t2 - t1) + "ms showWindow:" + (t3 - t2) + "ms");
                }
            }
        }
        uiControl.setFocus(focusedWindow, focusedWidget);
    }

    public void sendEvent(String windowName, String widgetName, String event) {
        try {
            JSONObject tmpl;
            tmpl = (JSONObject) uiControl.getScreenTemplate(windowName);
            if (tmpl != null) {
                Node node = uiControl.getNode(windowName);
                if (node == null) {
                    throw new IOException("invalid window:" + windowName);
                }
                Interface xml = node.getInterface();
                JSONObject newScreenData = uiControl.updateScreenData(xml, xml.getWidgetByLongName(windowName), tmpl);
                if (newScreenData == null) {
                    newScreenData = new JSONObject();
                }
                uiControl.clearChangedWidget();
                JSONObject eventData = new JSONObject();
                eventData.put("window", windowName);
                eventData.put("widget", widgetName);
                eventData.put("event", event);
                eventData.put("screen_data", newScreenData);
                JSONObject params = new JSONObject();
                params.put("event_data", eventData);

                if (System.getProperty("monsia.do_profile") != null) {
                    logger.info("---- profile ----");
                    logger.info("window:" + windowName + " widget:" + widgetName + " event:" + event);
                }
                long st = System.currentTimeMillis();

                windowStack = protocol.sendEvent(params);
                long t1 = System.currentTimeMillis();
                updateScreen();

                long et = System.currentTimeMillis();
                if (System.getProperty("monsia.do_profile") != null) {
                    logger.info("total:" + (et - st) + "ms protocol.sendEvent:" + (t1 - st) + "ms updateScreen:" + (et - t1) + "ms");
                }
            }
        } catch (JSONException | IOException ex) {
            ExceptionDialog.showExceptionDialog(ex);
            System.exit(1);
        }
    }

    private void listDownloads() throws IOException, JSONException {
        JSONArray array = protocol.listDownloads();
        logger.debug(array);
        for (int j = 0; j < array.length(); j++) {
            JSONObject item = array.getJSONObject(j);

            String type = null;
            if (item.has("type")) {
                type = item.getString("type");
            }
            if (type != null) {
                if (type.equals("report")) {
                    Download.printReport(conf, protocol, item);
                } else {
                    Download.downloadFile(conf, protocol, item);
                }
            }
        }
    }

    private void getMessage() throws IOException, JSONException {
        JSONObject result = protocol.getMessage();
        if (result.has("abort")) {
            String abort = result.getString("abort");
            if (!abort.isEmpty()) {
                protocol.endSession();
                JOptionPane.showMessageDialog(uiControl.getTopWindow(), abort);
                System.exit(0);
            }
        }

        if (result.has("popup")) {
            String popup = result.getString("popup");
            if (!popup.isEmpty()) {
                PopupNotify.popup(Messages.getString("Protocol.message_notify_summary"), popup, GtkStockIcon.get("gtk-dialog-info"), 0);
                return;
            }
        }

        if (result.has("dialog")) {
            String dialog = result.getString("dialog");
            if (!dialog.isEmpty()) {
                JOptionPane.showMessageDialog(uiControl.getTopWindow(), dialog);
            }
        }
    }

    private synchronized void sendPing() {
        try {
            if (!isReceiving()) {
                startReceiving();
                if (!this.getProtocol().isUsePushClient()) {
                    listDownloads();
                }
                getMessage();
                stopReceiving();
            }
        } catch (IOException | JSONException ex) {
            logger.catching(Level.FATAL, ex);
            ExceptionDialog.showExceptionDialog(ex);
            System.exit(1);
        }
    }

    public boolean isReceiving() {
        return isReceiving;
    }

    public void startReceiving() {
        this.isReceiving = true;
    }

    public void stopReceiving() {
        this.isReceiving = false;
    }

    public String getFocusedWindow() {
        return focusedWindow;
    }

    public Protocol getProtocol() {
        return protocol;
    }
}
