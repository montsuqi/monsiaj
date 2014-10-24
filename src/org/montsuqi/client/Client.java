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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.montsuqi.monsia.Interface;
import org.montsuqi.util.GtkStockIcon;
import org.montsuqi.util.PDFPrint;
import org.montsuqi.util.PopupNotify;
import org.montsuqi.util.TempFile;
import org.montsuqi.widgets.Button;
import org.montsuqi.widgets.ExceptionDialog;
import org.montsuqi.widgets.PandaDownload;
import org.montsuqi.widgets.PandaPreview;

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
    private static final int PingTimerPeriod = 10 * 1000;
    private javax.swing.Timer pingTimer;
    private JSONObject windowStack;
    private String focusedWindow;
    private String focusedWidget;

    public Client(Config conf) throws IOException {
        this.conf = conf;
        int n = conf.getCurrent();
        uiControl = new UIControl(this, conf.getStyleURL(n), conf.getTimerPeriod(n));
        isReceiving = false;
    }

    void connect() throws IOException, GeneralSecurityException, JSONException {
        int num = conf.getCurrent();
        String authURI = conf.getAuthURI(num);

        if (conf.getUseSSL(num)) {
            if (conf.getUsePKCS11(num)) {
                protocol = new Protocol(authURI, conf.getCACertificateFile(num), conf.getPKCS11Lib(num), conf.getPKCS11Slot(num), "dummy");
            } else {
                protocol = new Protocol(authURI, conf.getCACertificateFile(num), conf.getClientCertificateFile(num), conf.getClientCertificatePassword(num), "dummy");
            }
        } else {
            protocol = new Protocol(authURI, conf.getUser(num), conf.getPassword(num));
            if (!this.conf.getSavePassword(num)) {
                this.conf.setPassword(num, "");
                this.conf.save();
            }

            if (System.getProperty("monsia.config.reset_user") != null) {
                conf.setUser(num, "");
                conf.save();
            }
        }

        protocol.getServerInfo();
        protocol.startSession();
        startReceiving();
        windowStack = protocol.getWindow();
        updateScreen();
        stopReceiving();
    }

    void disconnect() {
        try {
            protocol.endSession();
        } catch (IOException | JSONException e) {
            logger.warn(e, e);
        } finally {
            System.exit(0);
        }
    }

    private void showReportDialog(String title, File file) throws IOException {
        final JDialog dialog = new JDialog();
        Button closeButton = new Button(new AbstractAction(Messages.getString("PrintReport.close")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        Container container = dialog.getContentPane();
        container.setLayout(new BorderLayout(5, 5));
        PandaPreview preview = new PandaPreview();
        dialog.setSize(new Dimension(800, 600));
        dialog.setLocationRelativeTo(null);
        dialog.setTitle(Messages.getString("PrintReport.title") + title);
        container.add(preview, BorderLayout.CENTER);
        container.add(closeButton, BorderLayout.SOUTH);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        preview.setSize(800, 600);
        preview.load(file.getAbsolutePath());
        preview.setVisible(true);

        dialog.setModal(true);
        dialog.setResizable(false);
        dialog.setVisible(true);
        closeButton.requestFocus();
    }

    private void printReport(JSONObject item) {
        try {
            if (!item.has("object_id")) {
                return;
            }
            String oid = item.getString("object_id");

            String printer = null;
            if (item.has("printer")) {
                printer = item.getString("printer");
            }

            String title = "";
            if (item.has("title")) {
                printer = item.getString("title");
            }

            boolean showdialog = false;
            if (item.has("showdialog")) {
                showdialog = item.getBoolean("showdialog");
            }
            if (oid == null || oid.equals("0")) {
                return;
            }

            try {
                File temp = TempFile.createTempFile("report", "pdf");
                temp.deleteOnExit();
                OutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
                protocol.getBLOB(oid, out);
                if (showdialog) {
                    showReportDialog(title, temp);
                } else {
                    PopupNotify.popup(Messages.getString("PrintReport.notify_summary"),
                            Messages.getString("PrintReport.notify_print_start") + "\n\n"
                            + Messages.getString("PrintReport.printer") + printer + "\n\n"
                            + Messages.getString("PrintReport.title") + title,
                            GtkStockIcon.get("gtk-print"), 0);
                    if (printer != null) {
                        PDFPrint print = new PDFPrint(temp, printer);
                        print.start();
                    } else {
                        PDFPrint print = new PDFPrint(temp, false);
                        print.start();
                    }
                }
            } catch (IOException ex) {
                logger.warn(ex, ex);
                PopupNotify.popup(Messages.getString("PrintReport.notify_summary"),
                        Messages.getString("PrintReport.notify_print_fail") + "\n\n"
                        + Messages.getString("PrintReport.printer") + printer + "\n"
                        + Messages.getString("PrintReport.title") + title,
                        GtkStockIcon.get("gtk-dialog-error"), 0);
            }
        } catch (JSONException ex) {
            logger.warn(ex, ex);
        }
    }

    private void downloadFile(JSONObject item) throws JSONException, IOException {
        try {
            if (!item.has("object_id")) {
                return;
            }
            String oid = item.getString("object_id");

            String filename = "";
            if (item.has("filename")) {
                filename = item.getString("filename");
            }
            String desc = "";
            if (item.has("description")) {
                desc = item.getString("description");
            }
            if (oid == null || oid.equals("0")) {
                return;
            }
            try {
                File temp = TempFile.createTempFile("downloadfile", "dat");
                temp.deleteOnExit();
                OutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
                protocol.getBLOB(oid, out);
                PandaDownload pd = new PandaDownload();
                pd.showDialog(filename, desc, temp);
            } catch (IOException ex) {
                logger.warn(ex, ex);
                PopupNotify.popup(Messages.getString("DownloadFile.notify_summary"),
                        Messages.getString("DownloadFile.fail") + "\n\n"
                        + Messages.getString("DownloadFile.filename") + filename + "\n"
                        + Messages.getString("DownloadFile.description") + desc,
                        GtkStockIcon.get("gtk-dialog-error"), 0);
            }
        } catch (JSONException ex) {
            logger.warn(ex, ex);
        }
    }

    public void startPing() {
        pingTimer = new javax.swing.Timer(PingTimerPeriod, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendPing();
            }
        });
        pingTimer.start();
    }

    private void listDownloads() throws IOException, JSONException {
        JSONArray array = protocol.listDownloads();
        for (int j = 0; j < array.length(); j++) {
            JSONObject item = array.getJSONObject(j);

            String type = null;
            if (item.has("type")) {
                type = item.getString("type");
            }
            if (type != null) {
                if (type.equals("report")) {
                    printReport(item);
                } else {
                    downloadFile(item);
                }
            }
        }
    }

    private void getMessage() throws IOException, JSONException {
        JSONObject result = protocol.getMessage();
        if (result.has("abort")) {
            String abort = result.getString("abort");
            if (!abort.isEmpty()) {
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
                listDownloads();
                getMessage();
                stopReceiving();
            }
        } catch (IOException | JSONException ex) {
            logger.catching(Level.FATAL, ex);
            ExceptionDialog.showExceptionDialog(ex);
            System.exit(1);
        }
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
                String gladeData = protocol.getScreenDefine(windowName);
                try {
                    node = new Node(Interface.parseInput(new ByteArrayInputStream(gladeData.getBytes("UTF-8")), uiControl), windowName);
                } catch (UnsupportedEncodingException ex) {
                    logger.info(ex, ex);
                    return;
                }
                uiControl.putNode(windowName, node);
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
            if (putType.matches("new") || putType.matches("current")) {
                Node node = uiControl.getNode(windowName);
                uiControl.setWidget(node.getInterface(), windowName, screenData);
                uiControl.showWindow(windowName);
            }
        }
        uiControl.setFocus(focusedWindow, focusedWidget);
    }

    public void sendEvent(String windowName, String widgetName, String event) {
        try {
            JSONObject screenData = null;
            JSONObject windowData = windowStack.getJSONObject("window_data");
            JSONArray windows = windowData.getJSONArray("windows");
            for (int i = 0; i < windows.length(); i++) {
                JSONObject winObj = windows.getJSONObject(i);
                if (winObj.has("window") && winObj.getString("window").matches(windowName)) {
                    screenData = winObj.getJSONObject("screen_data");
                    break;
                }
            }
            if (screenData != null) {
                uiControl.updateScreenData(null, windowName, screenData);
                uiControl.clearChangedWidget();
                JSONObject eventData = new JSONObject();
                eventData.put("window", windowName);
                eventData.put("widget", widgetName);
                eventData.put("event", event);
                eventData.put("screen_data", screenData);
                JSONObject params = new JSONObject();
                params.put("event_data", eventData);
                windowStack = protocol.sendEvent(params);
                updateScreen();
            }
        } catch (JSONException | IOException ex) {
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
