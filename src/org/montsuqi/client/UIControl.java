/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.montsuqi.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.montsuqi.client.widgethandlers.WidgetHandler;
import org.montsuqi.monsia.Interface;
import org.montsuqi.monsia.Style;
import org.montsuqi.util.SystemEnvironment;
import org.montsuqi.widgets.ExceptionDialog;
import org.montsuqi.widgets.PandaTimer;
import org.montsuqi.widgets.TopWindow;
import org.montsuqi.widgets.Window;

/**
 *
 * @author mihara
 */
public class UIControl {

    protected static final Logger logger = LogManager.getLogger(UIControl.class);
    private final HashMap<String, Node> nodeTable;
    private String sessionTitle;
    private Color sessionBGColor;
    private Interface xml;
    private final TopWindow topWindow;
    private final ArrayList<Component> dialogStack;
    private final Map styleMap;
    private final Map<String, Component> changedWidgetMap;
    private final Client client;
    private final long timerPeriod;

    public UIControl(Client client, URL styleURL, long timerPeriod) throws IOException {
        nodeTable = new HashMap<>();
        sessionTitle = "";
        sessionBGColor = null;
        topWindow = new TopWindow();
        dialogStack = new ArrayList<>();
        changedWidgetMap = new HashMap<>();
        this.client = client;
        styleMap = loadStyles(styleURL);
        this.timerPeriod = timerPeriod;
    }

    public long getTimerPeriod() {
        return timerPeriod;
    }

    public Client getClient() {
        return client;
    }

    private Map loadStyles(URL url) throws IOException {
        logger.debug("loading styles from URL: {0}", url);
        InputStream in = url.openStream();
        return Style.load(in);
    }

    public String getWindowName(Component widget) {
        String name = widget.getName();
        return name.substring(0, name.indexOf("."));
    }

    public Window getTopWindow() {
        return topWindow;
    }

    public Node getNode(String name) {
        return (Node) nodeTable.get(name);
    }

    public Node getNode(Component component) {
        return getNode(getWindowName(component));
    }

    public void putNode(String wName, Node node) {
        nodeTable.put(wName, node);
    }

    public void setWidget(Interface xml, Component widget, Object obj) throws JSONException {
        if (widget == null) {
            return;
        }
        Class clazz = widget.getClass();
        WidgetHandler handler = WidgetHandler.getHandler(clazz);
        if (handler != null) {
            long t1 = System.currentTimeMillis();
            handler.set(this, widget, (JSONObject) obj, styleMap);
            long t2 = System.currentTimeMillis();
            if (System.getProperty("monsia.do_profile") != null) {
                //logger.info("" + (t2-t1) + "ms " + clazz.getName()+ " " + name);
            }
        }
        if (obj instanceof JSONObject) {
            JSONObject j = (JSONObject) obj;
            for (Iterator i = j.keys(); i.hasNext();) {
                String key = (String) i.next();
                Component child = xml.getWidgetByLongName(widget.getName() + "." + key);
                if (child != null) {
                    setWidget(xml, child, j.get(key));
                }
            }
        }
    }

    public void showWindow(String name) {
        Node node = getNode(name);
        if (node == null) {
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
    }

    public void closeWindow(String name) {
        Node node = getNode(name);
        if (node == null) {
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
    }

    public void setFocus(String focusWindowName, String focusWidgetName) {
        if (focusWindowName == null || focusWidgetName == null) {
            return;
        }
        if (focusWindowName.startsWith("_")) {
            return;
        }
        Node node = getNode(focusWindowName);

        if (node != null && node.getInterface() != null && focusWindowName.equals(client.getFocusedWindow())) {
            Interface thisXML = node.getInterface();
            Component widget = thisXML.getWidget(focusWidgetName);
            if (widget == null || !widget.isFocusable()) {
                widget = thisXML.getAnyWidget();
            }
            final Component focusWidget = widget;
            if (focusWidget != null && focusWidget.isFocusable()) {
                if (SystemEnvironment.isMacOSX()) {
                    EventQueue.invokeLater(new Runnable() {

                        @Override
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

    public void exceptionOccured(IOException e) {
        ExceptionDialog.showExceptionDialog(e);
        System.exit(1);
    }

    private void stopTimer(Component widget) {
        if (widget instanceof PandaTimer) {
            ((PandaTimer) widget).stopTimer();
        } else if (widget instanceof Container) {
            Container container = (Container) widget;
            for (int i = 0, n = container.getComponentCount(); i < n; i++) {
                stopTimer(container.getComponent(i));
            }
        }
    }

    private void resetTimer(Component widget) {
        if (widget instanceof PandaTimer) {
            ((PandaTimer) widget).reset();
        } else if (widget instanceof Container) {
            Container container = (Container) widget;
            for (int i = 0, n = container.getComponentCount(); i < n; i++) {
                resetTimer(container.getComponent(i));
            }
        }
    }

    private String getTitle(java.awt.Window window) {
        String title = "";
        if (window instanceof JFrame) {
            title = ((JFrame) window).getTitle();
        } else if (window instanceof JDialog) {
            title = ((JDialog) window).getTitle();
        }
        return title;
    }

    private void setTitle(java.awt.Window window, String title) {
        if (window instanceof JFrame) {
            ((JFrame) window).setTitle(title);
        } else if (window instanceof JDialog) {
            ((JDialog) window).setTitle(title);
        }
    }

    static private String getWidgetName(String str) {
        int index;

        index = str.lastIndexOf('.');
        if (index == -1) {
            return str;
        } else {
            return str.substring(index + 1);
        }
    }

    public void sendEvent(Component widget, Object userData) {
        if (client.isReceiving()) {
            return;
        }
        try {
            client.startReceiving();

            java.awt.Window window;
            if (widget instanceof JMenuItem) {
                JComponent c = (JComponent) widget;
                window = (java.awt.Window) c.getClientProperty("window");
            } else {
                window = SwingUtilities.windowForComponent(widget);
            }
            if (window == null || widget == null) {
                return;
            }
            if (!window.getName().equals(client.getFocusedWindow())) {
                return;
            }

            String oldTitle = getTitle(window);
            setTitle(window, Messages.getString("Client.loading"));

            String _windowName = getWidgetName(window.getName());
            String _widgetName = getWidgetName(widget.getName());
            String event;
            if (userData == null) {
                event = _widgetName;
            } else {
                event = userData.toString();
                if (event.length() == 0) {
                    event = _widgetName;
                }
            }
            org.montsuqi.widgets.Window.busyAllWindows();
            client.sendEvent(_windowName, _widgetName, event);

            if (Messages.getString("Client.loading").equals(getTitle(window))) {
                setTitle(window, oldTitle);
            }
        } finally {
            client.stopReceiving();
        }
    }

    public void addChangedWidget(Component widget) {
        if (client.isReceiving()) {
            return;
        }
        _addChangedWidget(widget);
    }

    public void _addChangedWidget(Component widget) {
        changedWidgetMap.put(widget.getName(), widget);
    }

    public void clearChangedWidget() {
        changedWidgetMap.clear();
    }

    public Interface getInterface() {
        return xml;
    }

    public boolean updateScreenData(Interface xml, Component widget, Object obj) throws JSONException {
        boolean changed = false;
        ArrayList<String> removeList = new ArrayList();
        if (obj instanceof JSONObject) {
            JSONObject object = (JSONObject) obj;
            for (Iterator i = object.keys(); i.hasNext();) {
                String key = (String) i.next();
                Component child = xml.getWidgetByLongName(widget.getName() + "." + key);
                Object childObj = object.get(key);
                if (child != null) {
                    if (childObj instanceof JSONObject || childObj instanceof JSONArray) {
                        boolean _changed = updateScreenData(xml, child, childObj);
                        if (_changed) {
                            changed = true;
                        } else {
                            removeList.add(key);
                        }
                    }
                } else {
                    if (!changedWidgetMap.containsKey(widget.getName())) {
                        removeList.add(key);
                    }
                }
            }
            for (String key : removeList) {
                object.remove(key);
            }
            if (changedWidgetMap.containsKey(widget.getName())) {
                Class clazz = widget.getClass();
                WidgetHandler handler = WidgetHandler.getHandler(clazz);
                if (handler != null) {
                    handler.get(this, widget, (JSONObject) obj);
                    changed = true;
                }
            }

        }
        return changed;
    }

    public synchronized void setSessionTitle(String title) {
        sessionTitle = title;
    }

    public synchronized void setSessionBGColor(Color color) {
        sessionBGColor = color;
    }
}
