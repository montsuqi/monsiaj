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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.montsuqi.client.widgethandlers.WidgetHandler;
import org.montsuqi.monsia.Interface;
import org.montsuqi.util.GtkStockIcon;
import org.montsuqi.util.PDFPrint;
import org.montsuqi.util.PopupNotify;
import org.montsuqi.util.SystemEnvironment;
import org.montsuqi.widgets.Button;
import org.montsuqi.widgets.ExceptionDialog;
import org.montsuqi.widgets.PandaDownload;
import org.montsuqi.widgets.PandaPreview;
import org.montsuqi.widgets.PandaTimer;
import org.montsuqi.widgets.TopWindow;
import org.montsuqi.widgets.Window;

/**
 * <p>
 * A class that implements high level operations over client/server
 * connection.</p>
 */
public class Protocol {

    private final Config conf;
    private boolean isReceiving;
    private final long timerPeriod;
    private final HashMap<String, Node> nodeTable;
    private String sessionTitle;
    private Color sessionBGColor;
    private StringBuffer widgetName;
    private Interface xml;
    static final Logger logger = LogManager.getLogger(Protocol.class);
    private final TopWindow topWindow;
    private final ArrayList<Component> dialogStack;
    private static final int PingTimerPeriod = 10 * 1000;
    private javax.swing.Timer pingTimer;
    private String windowName;
    private Map styleMap;
    private Map<String, Component> changedWidgetMap;

    // jsonrpc
    private String protocolVersion;
    private String applicationVersion;
    private String serverType;
    private int rpcId;
    private String sessionId;
    private URL rpcUri;
    private String restURIRoot;
    private JSONObject resultJSON;

    private final SSLSocketFactory sslSocketFactory;

    static final String PANDA_CLIENT_VERSION = "1.4.8";

    public String getWindowName() {
        return windowName;
    }

    public String getWindowName(Component widget) {
        String name = widget.getName();
        return name.substring(0, name.indexOf("."));
    }

    public Window getTopWindow() {
        return topWindow;
    }

    Protocol(Config conf, Map styleMap, long timerPeriod) throws IOException, GeneralSecurityException {
        this.conf = conf;
        isReceiving = false;
        nodeTable = new HashMap<>();
        this.styleMap = styleMap;
        this.timerPeriod = timerPeriod;
        sessionTitle = "";
        sessionBGColor = null;
        topWindow = new TopWindow();
        dialogStack = new ArrayList<>();
        rpcId = 1;
        changedWidgetMap = new HashMap<>();

        int num = conf.getCurrent();
        final String user = this.conf.getUser(num);
        final String password = this.conf.getPassword(num);

        if (!this.conf.getSavePassword(num)) {
            this.conf.setPassword(num, "");
            this.conf.save();
        }

        if (System.getProperty("monsia.config.reset_user") != null) {
            conf.setUser(num, "");
            conf.save();
        }

        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password.toCharArray());
            }
        });

        String caCert = conf.getCACertificateFile(num);
        String p12 = conf.getClientCertificateFile(num);
        String p12Password = conf.getClientCertificatePassword(num);
        if (conf.getUseSSL(num)) {
            SSLSocketBuilder builder = new SSLSocketBuilder(caCert, p12, p12Password);
            sslSocketFactory = builder.getFactory();
        } else {
            sslSocketFactory = null;

        }
    }

    public File apiDownload(String path, String filename) throws IOException {
        File temp = File.createTempFile("monsiaj_apidownload_", "__" + filename);
        URL url = new URL(this.restURIRoot + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        String protocol = url.getProtocol();
        if (protocol.equals("https")) {
            if (sslSocketFactory != null) {
                ((HttpsURLConnection) con).setSSLSocketFactory(sslSocketFactory);
                ((HttpsURLConnection) con).setHostnameVerifier(SSLSocketBuilder.CommonNameVerifier);
            }
        } else if (protocol.equals("http")) {
            // do nothing
        } else {
            throw new IOException("bad protocol");
        }

        con.setDoOutput(true);
        con.setInstanceFollowRedirects(false);
        con.setRequestMethod("GET");
        //          ((HttpsURLConnection) con).setFixedLengthStreamingMode(reqStr.length());
        con.setRequestProperty("Content-Type", "application/json");

        int responseCode = con.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_OK) {
            con.disconnect();
            throw new IOException("" + responseCode);
        }
        BufferedInputStream in = new BufferedInputStream(con.getInputStream());
        int length, size = 0;
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
        while ((length = in.read()) != -1) {
            size += length;
            out.write(length);
        }
        out.close();
        con.disconnect();
        if (size == 0) {
            return null;
        }
        return temp;
    }

    private String makeJSONRPCRequest(String method, JSONObject params) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("jsonrpc", "2.0");
        obj.put("id", rpcId);
        obj.put("method", method);
        obj.put("params", params);
        rpcId += 1;
        return obj.toString();
    }

    private Object checkJSONRPCResponse(String jsonStr) throws JSONException {
        JSONObject obj = new JSONObject(jsonStr);
        if (!obj.getString("jsonrpc").matches("2.0")) {
            throw new JSONException("invalid jsonrpc version");
        }
        int id = obj.getInt("id");
        if (id != (this.rpcId - 1)) {
            throw new JSONException("invalid jsonrpc id:" + id + " expected:" + (this.rpcId - 1));
        }
        if (obj.has("error")) {
            JSONObject objError = obj.getJSONObject("error");
            int code = objError.getInt("code");
            String message = objError.getString("message");
            throw new JSONException("jsonrpc error code:" + code + " message:" + message);
        }
        if (!obj.has("result")) {
            throw new JSONException("no result object");
        }
        return obj.get("result");
    }

    private Object jsonRPC(URL url, String method, JSONObject params) throws JSONException, IOException {
        String reqStr = makeJSONRPCRequest(method, params);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        String protocol = url.getProtocol();
        if (protocol.equals("https")) {
            if (sslSocketFactory != null) {
                ((HttpsURLConnection) con).setSSLSocketFactory(sslSocketFactory);
                ((HttpsURLConnection) con).setHostnameVerifier(SSLSocketBuilder.CommonNameVerifier);
            }
        } else if (protocol.equals("http")) {
            // do nothing
        } else {
            throw new IOException("bad protocol");
        }

        con.setDoOutput(true);
        con.setInstanceFollowRedirects(false);
        con.setRequestMethod("POST");
        //          ((HttpsURLConnection) con).setFixedLengthStreamingMode(reqStr.length());
        con.setRequestProperty("Content-Type", "application/json");

        OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(), "UTF-8");

        osw.write(reqStr);
        osw.flush();
        osw.close();

        int responseCode = con.getResponseCode();
        if (responseCode == 401 || responseCode == 403) {
            JOptionPane.showMessageDialog(null, Messages.getString("Protocol.auth_error_message"), Messages.getString("Protocol.auth_error"), JOptionPane.ERROR_MESSAGE);
            logger.info("auth error:" + responseCode);
            System.exit(1);
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(bytes);
        BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
        int length;
        while ((length = bis.read()) != -1) {
            bos.write(length);
        }
        bos.close();
        con.disconnect();
        Object result = checkJSONRPCResponse(bytes.toString("UTF-8"));
        bytes.close();
        return result;
    }

    public void getServerInfo() {
        try {
            int num = conf.getCurrent();
            URL url = new URL(conf.getAuthURI(num));

            JSONObject params = new JSONObject();
            JSONObject result = (JSONObject) jsonRPC(url, "get_server_info", params);
            this.protocolVersion = result.getString("protocol_version");
            this.applicationVersion = result.getString("application_version");
            this.serverType = result.getString("server_type");

            logger.debug("protocol_version:" + this.protocolVersion);
            logger.debug("application_version:" + this.applicationVersion);
            logger.debug("server_type:" + this.serverType);

        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(ex);
            System.exit(1);
        }
    }

    public void startSession() {
        try {
            int num = conf.getCurrent();
            URL url = new URL(conf.getAuthURI(num));

            JSONObject params = new JSONObject();
            JSONObject meta = new JSONObject();
            meta.put("client_version", PANDA_CLIENT_VERSION);
            params.put("meta", meta);

            JSONObject result = (JSONObject) jsonRPC(url, "start_session", params);
            meta = result.getJSONObject("meta");

            this.sessionId = meta.getString("session_id");

            if (this.serverType.startsWith("glserver")) {
                this.rpcUri = url;
                this.restURIRoot = url.toString().replaceFirst("/rpc/", "/rest/");
            } else {
                this.rpcUri = new URL(result.getString("app_rpc_endpoint_uri"));
                this.restURIRoot = result.getString("app_rest_api_uri_root");
            }

            logger.debug("session_id:" + this.sessionId);
            logger.debug("rpcURI:" + this.rpcUri);
            logger.debug("restURIRoot:" + this.restURIRoot);
        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(ex);
            System.exit(1);
        }
    }

    public String getServerType() {
        return serverType;
    }

    public void endSession() {
        try {
            JSONObject params = new JSONObject();
            JSONObject meta = new JSONObject();
            meta.put("client_version", PANDA_CLIENT_VERSION);
            meta.put("session_id", this.sessionId);
            params.put("meta", meta);

            JSONObject result = (JSONObject) jsonRPC(this.rpcUri, "end_session", params);
        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(ex);
            System.exit(1);
        }
    }

    public void getWindow() {
        try {
            JSONObject params = new JSONObject();
            JSONObject meta = new JSONObject();
            meta.put("client_version", PANDA_CLIENT_VERSION);
            meta.put("session_id", this.sessionId);
            params.put("meta", meta);

            this.resultJSON = (JSONObject) jsonRPC(this.rpcUri, "get_window", params);

        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(ex);
            System.exit(1);
        }
    }

    public String getScreenDefine(String wname) {
        try {
            JSONObject params = new JSONObject();
            JSONObject meta = new JSONObject();
            meta.put("client_version", PANDA_CLIENT_VERSION);
            meta.put("session_id", this.sessionId);
            params.put("meta", meta);
            params.put("window", wname);

            JSONObject result = (JSONObject) jsonRPC(this.rpcUri, "get_screen_define", params);
            return result.getString("screen_define");
        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(ex);
            System.exit(1);
        }
        return null;
    }

    public void sendEvent(JSONObject params) {
        try {
            JSONObject meta = new JSONObject();
            meta.put("client_version", PANDA_CLIENT_VERSION);
            meta.put("session_id", this.sessionId);
            params.put("meta", meta);

            this.resultJSON = (JSONObject) jsonRPC(this.rpcUri, "send_event", params);
        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(ex);
            System.exit(1);
        }
    }

    public void getMessage() {
        try {
            JSONObject params = new JSONObject();
            JSONObject meta = new JSONObject();
            meta.put("client_version", PANDA_CLIENT_VERSION);
            meta.put("session_id", this.sessionId);
            params.put("meta", meta);

            JSONObject result = (JSONObject) jsonRPC(this.rpcUri, "get_message", params);
            if (result.has("abort")) {
                String abort = result.getString("abort");
                if (!abort.isEmpty()) {
                    JOptionPane.showMessageDialog(topWindow, abort);
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
                    JOptionPane.showMessageDialog(topWindow, dialog);
                }
            }

        } catch (JSONException ex) {
            ExceptionDialog.showExceptionDialog(ex);
            System.exit(1);
        } catch (IOException ex) {
            ExceptionDialog.showExceptionDialog(ex);
            System.exit(1);
        } catch (HeadlessException ex) {
            ExceptionDialog.showExceptionDialog(ex);
            System.exit(1);
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
                File temp = File.createTempFile("report", "pdf");
                temp.deleteOnExit();
                OutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
                this.getBLOB(oid, out);
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
                logger.warn(ex);
                PopupNotify.popup(Messages.getString("PrintReport.notify_summary"),
                        Messages.getString("PrintReport.notify_print_fail") + "\n\n"
                        + Messages.getString("PrintReport.printer") + printer + "\n"
                        + Messages.getString("PrintReport.title") + title,
                        GtkStockIcon.get("gtk-dialog-error"), 0);
            }
        } catch (JSONException ex) {
            logger.warn(ex);
        }

    }

    private void downloadFile(JSONObject item) {
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
                File temp = File.createTempFile("downloadfile", "dat");
                temp.deleteOnExit();
                OutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
                this.getBLOB(oid, out);
                PandaDownload pd = new PandaDownload();
                pd.showDialog(filename, desc, temp);
            } catch (IOException ex) {
                logger.warn(ex);
                PopupNotify.popup(Messages.getString("DownloadFile.notify_summary"),
                        Messages.getString("DownloadFile.fail") + "\n\n"
                        + Messages.getString("DownloadFile.filename") + filename + "\n"
                        + Messages.getString("DownloadFile.description") + desc,
                        GtkStockIcon.get("gtk-dialog-error"), 0);
            }
        } catch (JSONException ex) {
            logger.warn(ex);
        }
    }

    public void listDownloads() {
        try {
            JSONObject params = new JSONObject();
            JSONObject meta = new JSONObject();
            meta.put("client_version", PANDA_CLIENT_VERSION);
            meta.put("session_id", this.sessionId);
            params.put("meta", meta);

            JSONArray array = (JSONArray) jsonRPC(this.rpcUri, "list_downloads", params);
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
        } catch (JSONException | IOException ex) {
            logger.warn(ex);
        }
    }

    public int getBLOB(String oid, OutputStream out) throws IOException {
        if (oid.equals("0")) {
            // empty object id
            return 404;
        }

        URL url = new URL(this.restURIRoot + "sessions/" + this.sessionId + "/blob/" + oid);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        String protocol = url.getProtocol();
        if (protocol.equals("https")) {
            if (sslSocketFactory != null) {
                ((HttpsURLConnection) con).setSSLSocketFactory(sslSocketFactory);
                ((HttpsURLConnection) con).setHostnameVerifier(SSLSocketBuilder.CommonNameVerifier);
            }
        } else if (protocol.equals("http")) {
            // do nothing
        } else {
            throw new IOException("bad protocol");
        }

        con.setInstanceFollowRedirects(false);
        con.setRequestMethod("GET");

        BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
        int length;
        while ((length = bis.read()) != -1) {
            out.write(length);
        }
        out.close();
        con.disconnect();

        return con.getResponseCode();
    }

    public String postBLOB(byte[] in) {
        try {
            URL url = new URL(this.restURIRoot + "sessions/" + this.sessionId + "/blob/");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            String protocol = url.getProtocol();
            if (protocol.equals("https")) {
                if (sslSocketFactory != null) {
                    ((HttpsURLConnection) con).setSSLSocketFactory(sslSocketFactory);
                    ((HttpsURLConnection) con).setHostnameVerifier(SSLSocketBuilder.CommonNameVerifier);
                }
            } else if (protocol.equals("http")) {
                // do nothing
            } else {
                throw new IOException("bad protocol");
            }

            con.setInstanceFollowRedirects(false);
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            //((HttpsURLConnection) con.setFixedLengthStreamingMode(in.length);
            con.setRequestProperty("Content-Type", "application/octet-stream");
            OutputStream os = con.getOutputStream();
            os.write(in);
            os.flush();
            os.close();
            con.disconnect();
            return con.getHeaderField("x-blob-id");
        } catch (IOException ex) {
            ExceptionDialog.showExceptionDialog(ex);
            System.exit(1);
        }
        return null;
    }

    private void updateScreenData(Interface xml, String name, Object obj) throws JSONException {
        if (obj instanceof JSONObject) {
            JSONObject object = (JSONObject) obj;
            for (Iterator i = object.keys(); i.hasNext();) {
                String key = (String) i.next();
                String childName = name + "." + key;
                Object child = object.get(key);
                if (child instanceof JSONObject || child instanceof JSONArray) {
                    updateScreenData(xml, childName, object.get(key));
                }
            }
            Component widget = xml.getWidgetByLongName(name);
            if (widget != null && changedWidgetMap.containsKey(widget.getName())) {
                Class clazz = widget.getClass();
                WidgetHandler handler = WidgetHandler.getHandler(clazz);
                if (handler != null) {
                    handler.get(this, widget, (JSONObject) obj);
                }
            }
        } else if (obj instanceof JSONArray) {
            JSONArray array = (JSONArray) obj;
            for (int i = 0; i < array.length(); i++) {
                String childName = name + "[" + i + "]";
                Object child = array.get(i);
                if (child instanceof JSONObject || child instanceof JSONArray) {
                    updateScreenData(xml, childName, child);
                }
            }
        }
    }

    public void sendEvent(String windowName, String widgetName, String eventName) {
        try {
            JSONObject screenData = null;
            JSONObject windowData = this.resultJSON.getJSONObject("window_data");
            JSONArray windows = windowData.getJSONArray("windows");
            for (int i = 0; i < windows.length(); i++) {
                JSONObject winObj = windows.getJSONObject(i);
                if (winObj.has("window") && winObj.getString("window").matches(windowName)) {
                    screenData = winObj.getJSONObject("screen_data");
                    break;
                }
            }
            if (screenData != null) {
                Node node = getNode(windowName);
                if (windowName.startsWith("_")) {
                    screenData = new JSONObject();
                } else {
                    updateScreenData(node.getInterface(), windowName, screenData);
                }
                changedWidgetMap.clear();

                JSONObject eventData = new JSONObject();
                eventData.put("window", windowName);
                eventData.put("widget", widgetName);
                eventData.put("event", eventName);
                eventData.put("screen_data", screenData);
                JSONObject params = new JSONObject();
                params.put("event_data", eventData);
                this.sendEvent(params);
            }
        } catch (JSONException ex) {
            logger.warn(ex);
        }
    }

    private synchronized void setWidgetData(Interface xml, String name, Object obj) throws JSONException {
        Component widget = xml.getWidgetByLongName(name);
        if (widget != null) {
            Class clazz = widget.getClass();
            WidgetHandler handler = WidgetHandler.getHandler(clazz);
            if (handler != null) {
                handler.set(this, widget, (JSONObject) obj, styleMap);
            }
        }
        if (obj instanceof JSONObject) {
            JSONObject j = (JSONObject) obj;
            for (Iterator i = j.keys(); i.hasNext();) {
                String key = (String) i.next();
                String childName = name + "." + key;
                setWidgetData(xml, childName, j.get(key));
            }
        } else if (obj instanceof JSONArray) {
            JSONArray a = (JSONArray) obj;
            for (int i = 0; i < a.length(); i++) {
                String childName = name + "[" + i + "]";
                setWidgetData(xml, childName, a.get(i));
            }
        }
    }

    private void setWindowData(JSONObject w) throws JSONException {
        String putType = w.getString("put_type");
        String _windowName = w.getString("window");
        logger.debug("window[" + _windowName + "] put_type[" + putType + "]");

        Node node = getNode(_windowName);
        if (node == null) {
            String gladeData = this.getScreenDefine(_windowName);
            try {
                node = new Node(Interface.parseInput(new ByteArrayInputStream(gladeData.getBytes("UTF-8")), this), _windowName);
            } catch (UnsupportedEncodingException ex) {
                logger.info(ex);
                return;
            }
            nodeTable.put(_windowName, node);
        }
        if (putType.matches("new") || putType.matches("current")) {
            this.windowName = _windowName;
            Object screenData = w.get("screen_data");
            setWidgetData(node.getInterface(), _windowName, screenData);
            if (_windowName.startsWith("_")) {
                // do nothing for dummy window
                return;
            }
            showWindow(_windowName);
        } else {
            closeWindow(_windowName);
        }

    }

    public void updateScreen() {
        try {
            JSONObject windowData = this.resultJSON.getJSONObject("window_data");
            String focusedWindow = windowData.getString("focused_window");
            String focusedWidget = windowData.getString("focused_widget");
            JSONArray windows = windowData.getJSONArray("windows");
            for (int i = 0; i < windows.length(); i++) {
                setWindowData(windows.getJSONObject(i));
            }
            if (!focusedWindow.startsWith("_")) {
                setFocus(focusedWindow, focusedWidget);
            }
        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(ex);
            System.exit(1);
        }
    }

    public long getTimerPeriod() {
        return timerPeriod;
    }

    public Interface getInterface() {
        return xml;
    }

    private void showWindow(String name) {
        logger.entry(name);
        Node node = getNode(name);
        if (node == null) {
            logger.exit();
            return;
        }
        xml = node.getInterface();
        topWindow.setXml(xml);
        topWindow.ReScale();

        Window window = node.getWindow();
        window.setSessionTitle(sessionTitle);

        if (window.isDialog()) {
            Component parent = topWindow;
            JDialog dialog = window.getDialog();

            topWindow.showBusyCursor();
            if (!dialogStack.contains(dialog)) {
                for (Component c : dialogStack) {
                    parent = c;
                    parent.setEnabled(false);
                    stopTimer(parent);
                }
                if (SystemEnvironment.isWindows()) {
                    dialog = window.createDialog(topWindow, topWindow);
                } else {
                    dialog = window.createDialog(parent, topWindow);
                }
                dialogStack.add(dialog);
            } else {
                window.createDialog(parent, topWindow);
            }
            window.getChild().setBackground(this.sessionBGColor);
            dialog.validate();
            resetTimer(dialog);
        } else {
            topWindow.showWindow(window);
            window.getChild().setBackground(this.sessionBGColor);
            resetTimer(window.getChild());
            topWindow.validate();
        }
        logger.exit();
    }

    private void closeWindow(String name) {
        logger.entry(name);
        Node node = getNode(name);
        if (node == null) {
            logger.exit();
            return;
        }
        Window window = node.getWindow();

        if (window.isDialog()) {
            JDialog dialog = window.getDialog();
            if (dialogStack.contains(dialog)) {
                dialogStack.remove(dialog);
            }
            stopTimer(window.getDialog());
            window.destroyDialog();
        } else {
            stopTimer(window.getChild());
            window.getChild().setEnabled(false);
        }
        logger.exit();
    }

    private synchronized void stopTimer(Component widget) {
        if (widget instanceof PandaTimer) {
            ((PandaTimer) widget).stopTimer();
        } else if (widget instanceof Container) {
            Container container = (Container) widget;
            for (int i = 0, n = container.getComponentCount(); i < n; i++) {
                stopTimer(container.getComponent(i));
            }
        }
    }

    private synchronized void resetTimer(Component widget) {
        if (widget instanceof PandaTimer) {
            ((PandaTimer) widget).reset();
        } else if (widget instanceof Container) {
            Container container = (Container) widget;
            for (int i = 0, n = container.getComponentCount(); i < n; i++) {
                resetTimer(container.getComponent(i));
            }
        }
    }

    private synchronized void setFocus(String focusWindowName, String focusWidgetName) {
        if (focusWindowName == null || focusWidgetName == null) {
            return;
        }
        if (focusWindowName.startsWith("_")) {
            return;
        }
        Node node = getNode(focusWindowName);

        if (node != null && node.getInterface() != null && focusWindowName.equals(this.windowName)) {
            Interface thisXML = node.getInterface();
            Component widget = thisXML.getWidget(focusWidgetName);
            if (widget == null || !widget.isFocusable()) {
                widget = thisXML.getAnyWidget();
            }
            final Component focusWidget = widget;
            if (focusWidget != null && focusWidget.isFocusable()) {
                if (SystemEnvironment.isMacOSX()) {
                    EventQueue.invokeLater(new Runnable() {

                        public void run() {
                            focusWidget.requestFocus();

                        }
                    });
                } else {
                    focusWidget.requestFocusInWindow();
                }
            }
        }
    }

    public synchronized void setSessionTitle(String title) {
        sessionTitle = title;
    }

    public synchronized void setSessionBGColor(Color color) {
        sessionBGColor = color;
    }

    public void startPing() {
        pingTimer = new javax.swing.Timer(PingTimerPeriod, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    Protocol.this.sendPing();
                } catch (IOException ioe) {
                    exceptionOccured(ioe);
                }
            }
        });
        pingTimer.start();
    }

    private synchronized void sendPing() throws IOException {
        if (!isReceiving) {
            this.startReceiving();
            listDownloads();
            if (this.getServerType().startsWith("ginbee")) {
            } else {
                getMessage();
            }
            this.stopReceiving();
        }
    }

    synchronized void addChangedWidget(Component widget) {
        if (isReceiving) {
            return;
        }
        _addChangedWidget(widget);
    }

    synchronized public void _addChangedWidget(Component widget) {
        changedWidgetMap.put(widget.getName(), widget);
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

    private Node getNode(String name) {
        return (Node) nodeTable.get(name);
    }

    Node getNode(Component component) {
        return getNode(getWindowName(component));
    }

    synchronized void exit() {
        isReceiving = true;
        this.endSession();
        System.exit(0);
    }

    public StringBuffer getWidgetNameBuffer() {
        return widgetName;
    }

    public void exceptionOccured(IOException e) {
        logger.entry(e);
        ExceptionDialog.showExceptionDialog(e);
        logger.exit();
        System.exit(1);
    }
}
