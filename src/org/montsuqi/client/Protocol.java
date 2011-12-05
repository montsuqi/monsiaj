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
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import org.montsuqi.util.Logger;
import org.montsuqi.client.marshallers.WidgetMarshaller;
import org.montsuqi.client.marshallers.WidgetValueManager;
import org.montsuqi.monsia.Interface;
import org.montsuqi.util.GtkColorMap;
import org.montsuqi.util.GtkStockIcon;
import org.montsuqi.util.PopupNotify;
import org.montsuqi.util.SystemEnvironment;
import org.montsuqi.widgets.ExceptionDialog;
import org.montsuqi.widgets.PandaTimer;
import org.montsuqi.widgets.TopWindow;
import org.montsuqi.widgets.Window;

/** <p>A class that implements high level operations over client/server connection.</p>
 */
public class Protocol extends Connection {

    private Client client;
    private boolean protocol1;
    private boolean protocol2;
    private boolean isReceiving;
    private long timerPeriod;
    private HashMap nodeTable;
    private WidgetValueManager valueManager;
    private String sessionTitle;
    private Color sessionBGColor;
    private StringBuffer widgetName;
    private Interface xml;
    static final Logger logger = Logger.getLogger(Protocol.class);
    private static final String VERSION = "version:blob:expand:pdf:negotiation:download:v47:i18n:agent=monsiaj/" + Messages.getString("application.version");
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

    public Window getTopWindow() {
        return topWindow;
    }

    Protocol(Client client, Map styleMap, int protocolVersion, long timerPeriod) throws IOException, GeneralSecurityException {
        super(client.createSocket(), isNetworkByteOrder()); //$NON-NLS-1$
        this.client = client;

        switch (protocolVersion) {
            case 1:
                protocol1 = true;
                protocol2 = false;
                break;
            case 2:
                protocol1 = false;
                protocol2 = true;
                break;
            default:
                throw new IllegalArgumentException("invalid protocol version: " + protocolVersion); //$NON-NLS-1$
        }
        assert protocol1 ^ protocol2;
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
        logger.enter();
        StringTokenizer tokens = new StringTokenizer(VERSION, String.valueOf(':'));
        while (tokens.hasMoreTokens()) {
            if ("no".equals(tokens.nextToken())) { //$NON-NLS-1$
                logger.leave();
                return true;
            }
        }
        logger.leave();
        return false;
    }

    public Interface getInterface() {
        return xml;
    }

    private synchronized boolean receiveFile(String name) throws IOException {
        logger.enter(name);
        sendPacketClass(PacketClass.GetScreen);
        sendString(name);
        byte pc = receivePacketClass();
        if (pc != PacketClass.ScreenDefine) {
            Object[] args = {new Byte(PacketClass.ScreenDefine), new Byte(pc)};
            logger.warn("invalid protocol sequence: expected({0}), but was ({1})", args); //$NON-NLS-1$
            logger.leave();
            return false;
        }
        int size = receiveLength();
        byte[] bytes = new byte[size];
        in.readFully(bytes);
        Node node = new Node(Interface.parseInput(new ByteArrayInputStream(bytes), this), name);
        nodeTable.put(name, node);

        logger.leave();
        return true;
    }

    private void showWindow(String name) {
        logger.enter(name);
        Node node = getNode(name);
        if (node == null) {
            logger.leave();
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
        logger.leave();
    }

    void closeWindow(String name) {
        logger.enter(name);
        Node node = getNode(name);
        if (node == null) {
            logger.leave();
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
        logger.leave();
    }

    void closeWindow(Component widget) {
        logger.enter(widget);
        Node node = getNode(widget);
        if (node == null) {
            logger.leave();
            return;
        }
        node.getWindow().setVisible(false);
        clearWidget(node.getWindow());
        if (isReceiving()) {
            logger.leave();
            return;
        }
        Iterator i = nodeTable.values().iterator();
        while (i.hasNext()) {
            if (((Node) i.next()).getWindow() != null) {
                logger.leave();
                return;
            }
        }
        logger.leave();
        client.exitSystem();
    }

    private void destroyNode(String name) {
        logger.enter(name);
        if (nodeTable.containsKey(name)) {
            Node node = (Node) nodeTable.get(name);
            Window window = node.getWindow();
            if (window != null) {
                window.dispose();
            }
            node.clearChangedWidgets();
            nodeTable.remove(name);
        }
        logger.leave();
    }

    synchronized void checkScreens(boolean init) throws IOException {
        logger.enter(Boolean.valueOf(init));
        Window.busyAllWindows();
        while (receivePacketClass() == PacketClass.QueryScreen) {
            checkScreen1();
        }
        logger.leave();
    }

    private synchronized String checkScreen1() throws IOException {
        logger.enter();
        String name = receiveString();
        receiveLong(); // size
        receiveLong(); // mtime
        receiveLong(); // ctime

        if (getNode(name) == null) {
            receiveFile(name);
        } else {
            sendPacketClass(PacketClass.NOT);
        }
        logger.leave();
        return name;
    }

    synchronized boolean receiveWidgetData(Component widget) throws IOException {
        logger.enter(widget);
        Class clazz = widget.getClass();
        WidgetMarshaller marshaller = WidgetMarshaller.getMarshaller(clazz);
        if (marshaller != null) {
            marshaller.receive(valueManager, widget);
            logger.leave();
            return true;
        }
        logger.leave();
        return false;
    }

    private synchronized boolean sendWidgetData(String name, Component widget) throws IOException {
        logger.enter(name, widget);
        Class clazz = widget.getClass();
        WidgetMarshaller marshaller = WidgetMarshaller.getMarshaller(clazz);
        if (marshaller != null) {
            marshaller.send(valueManager, name, widget);
            logger.leave();
            return true;
        }
        logger.leave();
        return false;
    }

    private synchronized void receiveValueSkip() throws IOException {
        logger.enter();
        int type = Type.NULL;
        if (protocol1) {
            receiveDataType();
            type = getLastDataType();
        } else if (protocol2) {
            type = getLastDataType();
            if (type == Type.NULL) {
                receiveDataType();
                type = getLastDataType();
            }
        } else {
            assert false;
        }
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
        logger.leave();
    }

    public synchronized void receiveValue(StringBuffer longName, int offset) throws IOException {
        logger.enter(longName, new Integer(offset));
        if (!receiveValueNeedTrace(longName)) {
            logger.leave();
            return;
        }
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
        logger.leave();
    }

    private synchronized void receiveRecordValue(StringBuffer longName, int offset) throws IOException {
        logger.enter(longName, new Integer(offset));
        for (int i = 0, n = receiveInt(); i < n; i++) {
            String name = receiveString();
            longName.replace(offset, longName.length(), '.' + name);
            receiveValue(longName, offset + name.length() + 1);
        }
        logger.leave();
    }

    private synchronized void receiveArrayValue(StringBuffer longName, int offset) throws IOException {
        logger.enter(longName, new Integer(offset));
        for (int i = 0, n = receiveInt(); i < n; i++) {
            String name = '[' + String.valueOf(i) + ']';
            longName.replace(offset, longName.length(), name);
            receiveValue(longName, offset + name.length());
        }
        logger.leave();
    }

    private synchronized boolean receiveValueNeedTrace(StringBuffer longName) throws IOException {
        logger.enter(longName);
        boolean done = false;
        boolean needTrace = true;
        if (protocol1) {
            Component widget = xml.getWidgetByLongName(longName.toString());
            if (widget != null) {
                if (receiveWidgetData(widget)) {
                    needTrace = false;
                }
                done = true;
            } else {
                if (!protocol2) {
                    needTrace = false; // fatal error
                    done = true;
                    receiveValueSkip();
                }
            }
        }

        if (protocol2) {
            if (!done) {
                String dataName = longName.toString();
                int dot = dataName.indexOf('.');
                if (dot >= 0) {
                    dataName = dataName.substring(dot + 1);
                }
                Component widget = xml.getWidget(dataName);
                if (widget != null) {
                    if (receiveWidgetData(widget)) {
                        needTrace = false;
                    }
                    done = true;
                }
            }
        }
        if (!done) {
            needTrace = true;
        }

        logger.leave();
        return needTrace;
    }

    public synchronized String receiveName() throws IOException {
        logger.enter();
        final String s = receiveString();
        logger.leave();
        return s;
    }

    public synchronized void sendName(String name) throws IOException {
        logger.enter(name);
        sendString(name);
        logger.leave();
    }

    private synchronized void stopTimer(Component widget) {
        // logger.enter(widget);
        if (widget instanceof PandaTimer) {
            ((PandaTimer) widget).stopTimer();
        } else if (widget instanceof Container) {
            Container container = (Container) widget;
            for (int i = 0, n = container.getComponentCount(); i < n; i++) {
                stopTimer(container.getComponent(i));
            }
        }
        // logger.leave();
    }

    private synchronized void resetTimer(Component widget) {
        // logger.enter(widget);
        if (widget instanceof PandaTimer) {
            ((PandaTimer) widget).reset();
        } else if (widget instanceof Container) {
            Container container = (Container) widget;
            for (int i = 0, n = container.getComponentCount(); i < n; i++) {
                resetTimer(container.getComponent(i));
            }
        }
        // logger.leave();
    }

    private synchronized void clearWidget(Component widget) {
        // logger.enter(widget);
        if (widget instanceof JTextField) {
            JTextField text = (JTextField) widget;
            text.setText(null);
        } else if (widget instanceof Container) {
            Container container = (Container) widget;
            for (int i = 0, n = container.getComponentCount(); i < n; i++) {
                clearWidget(container.getComponent(i));
            }
        }
        // logger.leave();
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
        logger.enter();

        String wName = null;
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
        if (!isDummy) {
            showWindow(this.windowName);
        }
        if (c == PacketClass.FocusName) {
            String focusWindowName = receiveString();
            String focusWidgetName = receiveString();
            if (!isDummy) {
                setFocus(focusWindowName,focusWidgetName);
            }
            c = receivePacketClass();
        }
        logger.leave();
    }

    private synchronized void setFocus(String focusWindowName, String focusWidgetName) {
        Node node = getNode(focusWindowName);

        if (node != null && node.getInterface() != null && focusWindowName.equals(this.windowName)) {
            Interface thisXML = node.getInterface();
            Component widget = thisXML.getWidget(focusWidgetName);
            if (widget == null) {
                widget = thisXML.getAnyWidget();
            }
            final Component focusWidget = widget;
            if (focusWidget != null) {
                if (SystemEnvironment.isMacOSX()) {
                    EventQueue.invokeLater(new Runnable() {

                        public void run() {
                            focusWidget.requestFocus();

                        }
                    });
                } else {
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
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

    synchronized void sendConnect(String user, String pass, String app) throws IOException {
        logger.enter(user, pass, app);
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
        printAgent = new PrintAgent(port, user, pass);
        printAgent.start();
        logger.leave();
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
        printAgent.addRequest(path, title, retry, showDialog);
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
        logger.enter();
        byte[] bytes = VERSION.getBytes();
        sendChar((byte) (bytes.length & 0xff));
        sendChar((byte) 0);
        sendChar((byte) 0);
        sendChar((byte) 0);
        out.write(bytes);
        ((OutputStream) out).flush();
        logger.leave();
    }

    synchronized void sendEvent(String window, String widget, String event) throws IOException {
        logger.enter(window, widget, event);
        sendPacketClass(PacketClass.Event);
        sendString(window);
        sendString(widget);
        sendString(event);
        logger.leave();
    }

    synchronized void sendWindowData() throws IOException {
        logger.enter();
        Iterator i = nodeTable.keySet().iterator();
        while (i.hasNext()) {
            sendWndowData1((String) i.next());
        }
        sendPacketClass(PacketClass.END);
        clearWindowTable();
        logger.leave();
    }

    private synchronized void sendWndowData1(String windowName) throws IOException {
        logger.enter(windowName);
        sendPacketClass(PacketClass.WindowName);
        sendString(windowName);
        Node node = getNode(windowName);
        Iterator i = node.getChangedWidgets().entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            sendWidgetData((String) e.getKey(), (Component) e.getValue());
        }
        sendPacketClass(PacketClass.END);
        logger.leave();
    }

    void clearWindowTable() {
        logger.enter();
        Iterator i = nodeTable.values().iterator();
        while (i.hasNext()) {
            Node node = (Node) i.next();
            node.clearChangedWidgets();
        }
        logger.leave();
    }

    synchronized void addChangedWidget(Component widget) {
        logger.enter(widget);
        if (isReceiving) {
            logger.leave();
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
        logger.leave();
    }

    public void _addChangedWidget(Component widget) {
        logger.enter(widget);
        Node node = getNode(widget);
        if (node != null) {
            try {
                node.addChangedWidget(widget.getName(), widget);
            } catch (IllegalArgumentException e) {
                logger.warn(e);
            }
        }
        logger.leave();
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
        java.awt.Window win;
        if (component != null) {
            win = SwingUtilities.windowForComponent(component);
            if (win != null) {
                return getNode(win.getName());
            }
        }
        return null;
    }

    synchronized void exit() {
        logger.enter();
        isReceiving = true;
        logger.leave();
        client.exitSystem();
    }

    public StringBuffer getWidgetNameBuffer() {
        return widgetName;
    }

    public void exceptionOccured(IOException e) {
        logger.enter(e);
        ExceptionDialog.showExceptionDialog(e);
        logger.leave();
        client.exitSystem();
    }
}
