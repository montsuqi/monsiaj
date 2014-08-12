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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.montsuqi.client.marshallers.WidgetMarshaller;
import org.montsuqi.client.marshallers.WidgetValueManager;
import org.montsuqi.monsia.Interface;
import org.montsuqi.util.GtkStockIcon;
import org.montsuqi.util.PopupNotify;
import org.montsuqi.util.SystemEnvironment;
import org.montsuqi.widgets.ExceptionDialog;
import org.montsuqi.widgets.PandaTimer;
import org.montsuqi.widgets.TopWindow;
import org.montsuqi.widgets.Window;

/**
 * <p>A class that implements high level operations over client/server
 * connection.</p>
 */
public class Protocol extends Connection {

    private Client client;
    private boolean isReceiving;
    private long timerPeriod;
    private HashMap nodeTable;
    private WidgetValueManager valueManager;
    private String sessionTitle;
    private Color sessionBGColor;
    private StringBuffer widgetName;
    private Interface xml;
    static final Logger logger = LogManager.getLogger(Protocol.class);
    private static final String VERSION = "version:blob:expand:pdf:negotiation:download:v47:v48:i18n:agent=monsiaj/" + Messages.getString("application.version");
    private TopWindow topWindow;
    private ArrayList<Component> dialogStack;
    private boolean enablePing;
    private static final int PingTimerPeriod = 3 * 1000;
    private javax.swing.Timer pingTimer;
    private PrintAgent printAgent;
    private String windowName;
    private int serverVersion;

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

    Protocol(Client client, Map styleMap, long timerPeriod) throws IOException, GeneralSecurityException {
        super(client.createSocket(), isNetworkByteOrder()); //$NON-NLS-1$
        this.client = client;
        isReceiving = false;
        nodeTable = new HashMap();
        valueManager = new WidgetValueManager(this, styleMap);
        this.timerPeriod = timerPeriod;
        sessionTitle = "";
        sessionBGColor = null;
        topWindow = new TopWindow();
        dialogStack = new ArrayList<Component>();
        enablePing = false;
    }

    public long getTimerPeriod() {
        return timerPeriod;
    }

    private static boolean isNetworkByteOrder() {
        logger.entry();
        StringTokenizer tokens = new StringTokenizer(VERSION, String.valueOf(':'));
        while (tokens.hasMoreTokens()) {
            if ("no".equals(tokens.nextToken())) { //$NON-NLS-1$
                logger.exit();
                return true;
            }
        }
        logger.exit();
        return false;
    }

    public Interface getInterface() {
        return xml;
    }

    private synchronized boolean receiveFile(String name) throws IOException {
        logger.entry(name);
        sendPacketClass(PacketClass.GetScreen);
        sendString(name);
        byte pc = receivePacketClass();
        if (pc != PacketClass.ScreenDefine) {
            Object[] args = {new Byte(PacketClass.ScreenDefine), new Byte(pc)};
            logger.warn("invalid protocol sequence: expected({0}), but was ({1})", args); //$NON-NLS-1$
            logger.exit();
            return false;
        }
        int size = receiveLength();
        byte[] bytes = new byte[size];
        in.readFully(bytes);
        Node node = new Node(Interface.parseInput(new ByteArrayInputStream(bytes), this), name);
        nodeTable.put(name, node);

        logger.exit();
        return true;
    }

    private void showWindow(String name, String focusWindowName, String focusWidgetName) {
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
            setFocus(focusWindowName, focusWidgetName);
        } else {
            topWindow.showWindow(window);
            window.getChild().setBackground(this.sessionBGColor);
            resetTimer(window.getChild());
            setFocus(focusWindowName, focusWidgetName);
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

    void closeWindow(Component widget) {
        logger.entry(widget);
        Node node = getNode(widget);
        if (node == null) {
            logger.exit();
            return;
        }
        node.getWindow().setVisible(false);
        clearWidget(node.getWindow());
        if (isReceiving()) {
            logger.exit();
            return;
        }
        Iterator i = nodeTable.values().iterator();
        while (i.hasNext()) {
            if (((Node) i.next()).getWindow() != null) {
                logger.exit();
                return;
            }
        }
        logger.exit();
        client.exitSystem();
    }

    private void destroyNode(String name) {
        logger.entry(name);
        if (nodeTable.containsKey(name)) {
            Node node = (Node) nodeTable.get(name);
            Window window = node.getWindow();
            if (window != null) {
                window.dispose();
            }
            node.clearChangedWidgets();
            nodeTable.remove(name);
        }
        logger.exit();
    }

    synchronized void checkScreens(boolean init) throws IOException {
        logger.entry(Boolean.valueOf(init));
        Window.busyAllWindows();
        while (receivePacketClass() == PacketClass.QueryScreen) {
            checkScreen1();
        }
        logger.exit();
    }

    private synchronized String checkScreen1() throws IOException {
        logger.entry();
        String name = receiveString();
        receiveLong(); // size
        receiveLong(); // mtime
        receiveLong(); // ctime

        if (getNode(name) == null) {
            receiveFile(name);
        } else {
            sendPacketClass(PacketClass.NOT);
        }
        logger.exit();
        return name;
    }

    synchronized boolean receiveWidgetData(Component widget) throws IOException {
        logger.entry(widget);
        Class clazz = widget.getClass();
        WidgetMarshaller marshaller = WidgetMarshaller.getMarshaller(clazz);
        if (marshaller != null) {
            marshaller.receive(valueManager, widget);
            logger.exit();
            return true;
        }
        logger.exit();
        return false;
    }

    private synchronized boolean sendWidgetData(String name, Component widget) throws IOException {
        logger.entry(name, widget);
        Class clazz = widget.getClass();
        WidgetMarshaller marshaller = WidgetMarshaller.getMarshaller(clazz);
        if (marshaller != null) {
            marshaller.send(valueManager, name, widget);
            logger.exit();
            return true;
        }
        logger.exit();
        return false;
    }

    private synchronized void receiveValueSkip() throws IOException {
        logger.entry();
        int type = Type.NULL;
        receiveDataType();
        type = getLastDataType();
        switch (type) {
            case Type.INT:
                receiveInt();
                break;
            case Type.BOOL:
                receiveBoolean();
                break;
            case Type.CHAR:
            case Type.VARCHAR:
            case Type.DBCODE:
            case Type.TEXT:
            case Type.NUMBER:
                receiveString();
                break;
            case Type.ARRAY:
                for (int i = 0, n = receiveInt(); i < n; i++) {
                    receiveValueSkip();
                }
                break;
            case Type.RECORD:
                for (int i = 0, n = receiveInt(); i < n; i++) {
                    receiveString();
                    receiveValueSkip();
                }
                break;
            default:
                break;
        }
        logger.exit();
    }

    public synchronized void receiveNodeValue(StringBuffer longName, int offset) throws IOException {
        logger.entry(longName, new Integer(offset));
        switch (receiveDataType()) {
            case Type.RECORD:
                receiveRecordValue(longName, offset);
                break;
            case Type.ARRAY:
                receiveArrayValue(longName, offset);
                break;
            default:
                receiveValueSkip();
                break;
        }
        logger.exit();
    }

    public synchronized void receiveValue(StringBuffer longName, int offset) throws IOException {
        logger.entry(longName, new Integer(offset));
        Component widget = xml.getWidgetByLongName(longName.toString());
        if (widget != null) {
            if (receiveWidgetData(widget)) {
            } else {
                receiveNodeValue(longName, offset);
            }
        } else {
            receiveValueSkip();
        }
        logger.exit();
    }

    private synchronized void receiveRecordValue(StringBuffer longName, int offset) throws IOException {
        logger.entry(longName, new Integer(offset));
        for (int i = 0, n = receiveInt(); i < n; i++) {
            String name = receiveString();
            longName.replace(offset, longName.length(), '.' + name);
            receiveValue(longName, offset + name.length() + 1);
        }
        logger.exit();
    }

    private synchronized void receiveArrayValue(StringBuffer longName, int offset) throws IOException {
        logger.entry(longName, new Integer(offset));
        for (int i = 0, n = receiveInt(); i < n; i++) {
            String name = '[' + String.valueOf(i) + ']';
            longName.replace(offset, longName.length(), name);
            receiveValue(longName, offset + name.length());
        }
        logger.exit();
    }

    public synchronized String receiveName() throws IOException {
        logger.entry();
        final String s = receiveString();
        logger.exit();
        return s;
    }

    public synchronized void sendName(String name) throws IOException {
        logger.entry(name);
        sendString(name);
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

    private synchronized void clearWidget(Component widget) {
        // logger.entry(widget);
        if (widget instanceof JTextField) {
            JTextField text = (JTextField) widget;
            text.setText(null);
        } else if (widget instanceof Container) {
            Container container = (Container) widget;
            for (int i = 0, n = container.getComponentCount(); i < n; i++) {
                clearWidget(container.getComponent(i));
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

    synchronized void getScreenData() throws IOException {
        logger.entry();

        String focusWindowName = null;
        String focusWidgetName = null;
        String wName;
        Node node;
        checkScreens(false);
        sendPacketClass(PacketClass.GetData);
        sendLong(0); // get all data // In Java: int=>32bit, long=>64bit
        byte c;
        while ((c = receivePacketClass()) == PacketClass.WindowName) {
            wName = receiveString();
            logger.debug("window: {0}", wName);
            int type = receiveInt();

            node = getNode(wName);
            if (node != null) {
                xml = node.getInterface();
            }
            switch (type) {
                case ScreenType.END_SESSION:
                    client.exitSystem();
                    break;
                case ScreenType.CURRENT_WINDOW:
                case ScreenType.NEW_WINDOW:
                case ScreenType.CHANGE_WINDOW:
                    this.windowName = wName;
                    widgetName = new StringBuffer(wName);
                    c = receivePacketClass();
                    if (c == PacketClass.ScreenData) {
                        receiveValue(widgetName, widgetName.length());
                    }
                    if (type != ScreenType.CURRENT_WINDOW && xml != null) {
                        Component widget = xml.getWidget(wName);
                        if (widget != null) {
                            resetScrollPane(widget);
                        }
                    }
                    break;
                case ScreenType.JOIN_WINDOW:
                case ScreenType.CLOSE_WINDOW:
                default:
                    closeWindow(wName);
                    c = receivePacketClass();
                    break;
            }
            if (c == PacketClass.NOT) {
                // no screen data
            } else {
                // fatal error
            }
        }
        boolean isDummy = this.widgetName.toString().startsWith("_");
        if (c == PacketClass.FocusName) {
            focusWindowName = receiveString();
            focusWidgetName = receiveString();
            receivePacketClass();
        }
        if (!isDummy) {
            showWindow(this.windowName, focusWindowName, focusWidgetName);
        }

        logger.exit();
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

    synchronized void sendConnect(String user, String pass, String app) throws IOException, GeneralSecurityException {
        logger.entry(user, pass, app);
        sendPacketClass(PacketClass.Connect);
        sendVersionString();
        sendString(user);
        sendString(pass);
        sendString(app);
        byte pc = receivePacketClass();
        switch (pc) {
            case PacketClass.OK:
                // throw nothing
                break;
            case PacketClass.ServerVersion:
                serverVersion = Integer.parseInt(receiveString().replaceAll("\\.", ""));
                if (serverVersion > 14400) {
                    enablePing = true;
                    this.setEncoding("UTF-8");
                }
                break;
            case PacketClass.NOT:
                throw new ConnectException(Messages.getString("Client.cannot_connect_to_server")); //$NON-NLS-1$
            case PacketClass.E_VERSION:
                throw new ConnectException(Messages.getString("Client.version_mismatch")); //$NON-NLS-1$
            case PacketClass.E_AUTH:
                throw new ConnectException(Messages.getString("Client.authentication_error")); //$NON-NLS-1$
            case PacketClass.E_APPL:
                throw new ConnectException(Messages.getString("Client.application_name_invalid")); //$NON-NLS-1$
            default:
                Object[] args = {Integer.toHexString(pc)};
                throw new ConnectException(MessageFormat.format("cannot connect to server(other protocol error {0})", args)); //$NON-NLS-1$
        }

        String port = this.socket.getInetAddress().getHostName() + ":" + this.socket.getPort();
        printAgent = new PrintAgent(port, user, pass, this.client.createSSLSocketFactory());
        printAgent.start();
        logger.exit();
    }

    public void startPing() {
        if (enablePing) {
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
    }

    public int getServerVersion() {
        return serverVersion;
    }

    public void addPrintRequest(String path, String title, int retry, boolean showDialog) {
        printAgent.addPrintRequest(path, title, retry, showDialog);
    }

    public void addDLRequest(String path, String filename, String description, int retry) {
        printAgent.addDLRequest(path, filename,description, retry);
    }

    private synchronized void sendPing() throws IOException {
        if (!isReceiving) {
            // for orca 4.7
            if (serverVersion >= 14700) {
                this.startReceiving();
                this.sendPacketClass(PacketClass.Ping);
                switch (this.receivePacketClass()) {
                    case PacketClass.PongPopup:
                        this.stopReceiving();
                        PopupNotify.popup(Messages.getString("Protocol.message_notify_summary"), this.receiveString(), GtkStockIcon.get("gtk-dialog-info"), 0);
                        break;
                    case PacketClass.PongDialog:
                        this.stopReceiving();
                        JOptionPane.showMessageDialog(topWindow, this.receiveString());
                        break;
                    case PacketClass.PongAbort:
                        JOptionPane.showMessageDialog(topWindow, this.receiveString());
                        client.exitSystem();
                        break;
                    default:
                        this.stopReceiving();
                        break;
                }
            } else {
                // for orca 4.6, 4.5
                this.startReceiving();
                this.sendPacketClass(PacketClass.Ping);
                this.receivePacketClass();
                switch (this.receivePacketClass()) {
                    case PacketClass.CONTINUE:
                        this.stopReceiving();
                        JOptionPane.showMessageDialog(topWindow, this.receiveString());
                        break;
                    case PacketClass.STOP:
                        JOptionPane.showMessageDialog(topWindow, this.receiveString());
                        client.exitSystem();
                    default:
                        this.stopReceiving();
                        break;
                }
            }
        }
    }

    private synchronized void sendVersionString() throws IOException {
        logger.entry();
        byte[] bytes = VERSION.getBytes();
        sendChar((byte) (bytes.length & 0xff));
        sendChar((byte) 0);
        sendChar((byte) 0);
        sendChar((byte) 0);
        out.write(bytes);
        ((OutputStream) out).flush();
        logger.exit();
    }

    synchronized void sendEvent(String window, String widget, String event) throws IOException {
        logger.entry(window, widget, event);
        sendPacketClass(PacketClass.Event);
        sendString(window);
        sendString(widget);
        sendString(event);
        logger.exit();
    }

    synchronized void sendWindowData() throws IOException {
        logger.entry();
        Iterator i = nodeTable.keySet().iterator();
        while (i.hasNext()) {
            _sendWndowData((String) i.next());
        }
        sendPacketClass(PacketClass.END);
        clearWindowTable();
        logger.exit();
    }

    private synchronized void _sendWndowData(String windowName) throws IOException {
        logger.entry(windowName);
        sendPacketClass(PacketClass.WindowName);
        sendString(windowName);
        Map<String, Component> changedMap = getNode(windowName).getChangedWidgets();
        for (Map.Entry<String, Component> e : changedMap.entrySet()) {
            sendWidgetData(e.getKey(), e.getValue());
        }
        Map<String, Component> alwaysSendMap = getNode(windowName).getAlwaysSendWidgets();
        for (Map.Entry<String, Component> e : alwaysSendMap.entrySet()) {
            sendWidgetData(e.getKey(), e.getValue());
        }
        sendPacketClass(PacketClass.END);
        logger.exit();
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
        logger.entry();
        isReceiving = true;
        logger.exit();
        client.exitSystem();
    }

    public StringBuffer getWidgetNameBuffer() {
        return widgetName;
    }

    public void exceptionOccured(IOException e) {
        logger.entry(e);
        ExceptionDialog.showExceptionDialog(e);
        logger.exit();
        client.exitSystem();
    }
}
