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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.montsuqi.client.widgethandlers.WidgetHandler;
import org.montsuqi.monsia.Interface;
import org.montsuqi.util.GtkStockIcon;
import org.montsuqi.util.PopupNotify;
import org.montsuqi.util.SystemEnvironment;
import org.montsuqi.widgets.ExceptionDialog;
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
    private final HashMap nodeTable;
    private String sessionTitle;
    private Color sessionBGColor;
    private StringBuffer widgetName;
    private Interface xml;
    static final Logger logger = LogManager.getLogger(Protocol.class);
    private final TopWindow topWindow;
    private final ArrayList<Component> dialogStack;
    private static final int PingTimerPeriod = 3 * 1000;
    private javax.swing.Timer pingTimer;
    private PrintAgent printAgent;
    private String windowName;
    private Map styleMap;

    // jsonrpc
    private int rpcId;
    private String sessionId;
    private URL rpcURI;
    private String restURIRoot;
    private JSONObject resultJSON;

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
        nodeTable = new HashMap();
        this.styleMap = styleMap;
        this.timerPeriod = timerPeriod;
        sessionTitle = "";
        sessionBGColor = null;
        topWindow = new TopWindow();
        dialogStack = new ArrayList<Component>();
        rpcId = 1;

        int num = conf.getCurrent();
        final String user = this.conf.getUser(num);
        final String password = this.conf.getPassword(num);

        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password.toCharArray());
            }
        });
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

    private JSONObject checkJSONRPCResponse(String jsonStr) throws JSONException {
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
            throw new JSONException("jsonrpc error code:" + obj + " message:" + message);
        }
        if (!obj.has("result")) {
            throw new JSONException("no result object");
        }
        return obj.getJSONObject("result");
    }

    private JSONObject jsonRPC(URL url, String method, JSONObject params) throws JSONException, IOException {
        String reqStr = makeJSONRPCRequest(method, params);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setDoOutput(true);
        con.setInstanceFollowRedirects(false);
        con.setRequestMethod("POST");
        //          ((HttpsURLConnection) con).setFixedLengthStreamingMode(reqStr.length());
        con.setRequestProperty("Content-Type", "application/json");
        OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());

        osw.write(reqStr);
        osw.flush();
        osw.close();

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(bytes);
        BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
        int length;
        while ((length = bis.read()) != -1) {
            bos.write(length);
        }
        bos.close();
        con.disconnect();

        JSONObject result = checkJSONRPCResponse(bytes.toString());
        bytes.close();
        return result;
    }

    public void startSession() {
        try {
            int num = conf.getCurrent();
            URL url = new URL(conf.getAuthURI(num));

            JSONObject params = new JSONObject();
            JSONObject meta = new JSONObject();
            meta.put("client_version", PANDA_CLIENT_VERSION);
            params.put("meta", meta);

            JSONObject result = jsonRPC(url, "start_session", params);
            meta = result.getJSONObject("meta");
            this.rpcURI = new URL(result.getString("app_rpc_endpoint_uri"));
            this.restURIRoot = result.getString("app_rest_api_uri_root");
            this.sessionId = meta.getString("session_id");

            logger.debug("session_id:" + this.sessionId);
            logger.debug("rpcURI:" + this.rpcURI);
            logger.debug("restURIRoot:" + this.restURIRoot);

        } catch (Exception ex) {
            ex.printStackTrace();
            ExceptionDialog.showExceptionDialog(ex);
            System.exit(1);
        }
    }

    public void endSession() {
        try {
            JSONObject params = new JSONObject();
            JSONObject meta = new JSONObject();
            meta.put("client_version", PANDA_CLIENT_VERSION);
            meta.put("session_id", this.sessionId);
            params.put("meta", meta);

            JSONObject result = jsonRPC(this.rpcURI, "end_session", params);
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

            this.resultJSON = jsonRPC(this.rpcURI, "get_window", params);

        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(ex);
            System.exit(1);
        }
    }

    public void sendEvent(JSONObject params) {
        try {
            JSONObject meta = new JSONObject();
            meta.put("client_version", PANDA_CLIENT_VERSION);
            meta.put("session_id", this.sessionId);
            params.put("meta", meta);

            this.resultJSON = jsonRPC(this.rpcURI, "send_event", params);

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

            JSONObject result = jsonRPC(this.rpcURI, "get_message", params);
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
                    return;
                }
            }

        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(ex);
            System.exit(1);
        }
    }

    public int getBLOB(String oid, OutputStream out) throws IOException {
        URL url = new URL(this.restURIRoot + "sessions/" + this.sessionId + "/blob/" + oid);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

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
        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(ex);
            System.exit(1);
        }
        return null;
    }

    private void updateScreenData(Interface xml, String name, Object obj) throws JSONException {
        Component widget = xml.getWidgetByLongName(name);
        if (widget != null) {
            Class clazz = widget.getClass();
            WidgetHandler handler = WidgetHandler.getHandler(clazz);
            if (handler != null) {
                handler.get(this, widget, (JSONObject) obj);
            }
        }
        if (obj instanceof JSONObject) {
            JSONObject j = (JSONObject) obj;
            for (Iterator i = j.keys(); i.hasNext();) {
                String key = (String) i.next();
                String childName = name + "." + key;
                updateScreenData(xml, childName, j.get(key));
            }
        } else if (obj instanceof JSONArray) {
            JSONArray a = (JSONArray) obj;
            for (int i = 0; i < a.length(); i++) {
                String childName = name + "[" + i + "]";
                updateScreenData(xml, childName, a.get(i));
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
                updateScreenData(node.getInterface(), windowName, screenData);

                JSONObject eventData = new JSONObject();
                eventData.put("window", windowName);
                eventData.put("widget", widgetName);
                eventData.put("event", eventName);
                eventData.put("screen_data", screenData);
                JSONObject params = new JSONObject();
                params.put("event_data", eventData);
                this.sendEvent(params);
            }
        } catch (Exception ex) {
            logger.warn(ex);
        }
    }

    private synchronized void updateWidget(Interface xml, String name, Object obj) throws JSONException {
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
                updateWidget(xml, childName, j.get(key));
            }
        } else if (obj instanceof JSONArray) {
            JSONArray a = (JSONArray) obj;
            for (int i = 0; i < a.length(); i++) {
                String childName = name + "[" + i + "]";
                updateWidget(xml, childName, a.get(i));
            }
        }
    }

    private void updateWindow(JSONObject w) throws JSONException {
        String putType = w.getString("put_type");
        String _windowName = w.getString("window");
        logger.debug("window[" + _windowName + "] put_type[" + putType + "]");
        if (_windowName.startsWith("_")) {
            // do nothing for dummy window
            return;
        }
        Node node = getNode(_windowName);
        if (node == null) {
            String gladeData = w.getString("screen_define");
            node = new Node(Interface.parseInput(new ByteArrayInputStream(gladeData.getBytes()), this), _windowName);
            nodeTable.put(_windowName, node);
        }
        if (putType.matches("new") || putType.matches("current")) {
            Object screenData = w.get("screen_data");
            updateWidget(node.getInterface(), _windowName, screenData);
            showWindow(_windowName);
            this.windowName = _windowName;
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
                updateWindow(windows.getJSONObject(i));
            }
            setFocus(focusedWindow, focusedWidget);
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

    void closeWindow(String name) {
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
        // logger.entry(widget);
        if (widget instanceof PandaTimer) {
            ((PandaTimer) widget).stopTimer();
        } else if (widget instanceof Container) {
            Container container = (Container) widget;
            for (int i = 0, n = container.getComponentCount(); i < n; i++) {
                stopTimer(container.getComponent(i));
            }
        }
        // logger.exit();
    }

    private synchronized void resetTimer(Component widget) {
        // logger.entry(widget);
        if (widget instanceof PandaTimer) {
            ((PandaTimer) widget).reset();
        } else if (widget instanceof Container) {
            Container container = (Container) widget;
            for (int i = 0, n = container.getComponentCount(); i < n; i++) {
                resetTimer(container.getComponent(i));
            }
        }
        // logger.exit();
    }

    private synchronized void resetScrollPane(Component widget) {
        if (widget instanceof JScrollPane) {
            JViewport view = ((JScrollPane) widget).getViewport();
            view.setViewPosition(new Point(0, 0));
        }
        if (widget instanceof Container) {
            Component[] children = ((Container) widget).getComponents();
            for (int i = 0, n = children.length; i < n; i++) {
                resetScrollPane(children[i]);
            }
        }
    }

    private synchronized void setFocus(String focusWindowName, String focusWidgetName) {
        if (focusWindowName == null || focusWidgetName == null) {
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

    public void addPrintRequest(String path, String title, int retry, boolean showDialog) {
        printAgent.addPrintRequest(path, title, retry, showDialog);
    }

    public void addDLRequest(String path, String filename, String description, int retry) {
        printAgent.addDLRequest(path, filename, description, retry);
    }

    private synchronized void sendPing() throws IOException {
        if (!isReceiving) {
            this.startReceiving();
            getMessage();
            this.stopReceiving();
        }
    }

    void clearWindowTable() {
        logger.entry();
        Iterator i = nodeTable.values().iterator();
        while (i.hasNext()) {
            Node node = (Node) i.next();
            node.clearChangedWidgets();
        }
        logger.exit();
    }

    synchronized void addChangedWidget(Component widget) {
        logger.entry(widget);
        if (isReceiving) {
            logger.exit();
            return;
        }
        Node node = getNode(widget);
        if (node != null) {
            try {
                node.addChangedWidget(widget.getName(), widget);
            } catch (IllegalArgumentException e) {
                logger.warn(e);
            }
        }
        logger.exit();
    }

    public void _addChangedWidget(Component widget) {
        logger.entry(widget);
        Node node = getNode(widget);
        if (node != null) {
            try {
                node.addChangedWidget(widget.getName(), widget);
            } catch (IllegalArgumentException e) {
                logger.warn(e);
            }
        }
        logger.exit();
    }

    public void addAlwaysSendWidget(Component widget) {
        logger.entry(widget);
        Node node = getNode(widget);
        if (node != null) {
            try {
                node.addAlwaysSendWidget(widget.getName(), widget);
            } catch (IllegalArgumentException e) {
                logger.warn(e);
            }
        }
        logger.exit();
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
