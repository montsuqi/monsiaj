/*      PANDA -- a simple transaction monitor

Copyright (C) 1998-1999 Ogochan.
              2000-2003 Ogochan & JMA (Japan Medical Association).

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

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.swing.SwingUtilities;

import org.montsuqi.util.Logger;
import org.montsuqi.client.marshallers.WidgetMarshaller;
import org.montsuqi.client.marshallers.WidgetValueManager;
import org.montsuqi.monsia.Interface;
import org.montsuqi.monsia.InterfaceBuildingException;
import org.montsuqi.monsia.Style;
import org.montsuqi.widgets.PandaTimer;

public class Protocol extends Connection {

	private WidgetValueManager valueManager;
	private Map windowTable;
	private Logger logger;
	private Client client;
	private StringBuffer widgetName;
	private Interface xml;
	private boolean isReceiving;
	private boolean protocol1;
	private boolean protocol2;

	private static final String VERSION = "symbolic:expand"; //$NON-NLS-1$

	Protocol(Client client, Socket s, int protocolVersion) throws IOException {
		super(s, client.getEncoding(), isNetworkByteOrder()); //$NON-NLS-1$
		this.client = client;
		windowTable = new HashMap();
		isReceiving = false;
		switch (protocolVersion) {
		case 1:
			protocol1 = true;
			protocol2 = false;
			break;
		case 2:
			protocol1 = false;
			protocol2 = true;
		default:
			throw new IllegalArgumentException("invalid protocol version: " + protocolVersion); //$NON-NLS-1$
		}
		logger = Logger.getLogger(Connection.class);
		valueManager = new WidgetValueManager(this, Style.load(client.getStyles()));
	}

	private static boolean isNetworkByteOrder() {
		StringTokenizer tokens = new StringTokenizer(VERSION, String.valueOf(':'));
		while (tokens.hasMoreTokens()) {
			if ("no".equals(tokens.nextToken())) { //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}

	public Interface getInterface() {
		return xml;
	}

	private Window activeWindow;

	private boolean receiveFile(String name, String fName) throws IOException {
		sendPacketClass(PacketClass.GetScreen);
		sendString(name);
		byte pc = receivePacketClass();
		if (pc != PacketClass.ScreenDefine) {
			logger.warn(Messages.getString("Protocol.invalid_protocol_sequence"), //$NON-NLS-1$
				new Object[] { new Byte(PacketClass.ScreenDefine), new Byte(pc) }); 
			return false;
		}

		OutputStream file = new FileOutputStream(fName);
		int size = receiveLength();
		byte[] bytes = new byte[size];
		in.readFully(bytes);
		file.write(bytes);
		file.flush();
		file.close();
		return true;
	}

	private Node showWindow(String name, int type) {
		Node node = getNode(name);
		if (node == null && (type == ScreenType.NEW_WINDOW || type == ScreenType.CURRENT_WINDOW)) {
			node = createNode(name);
		}
		if (node == null) {
			return null;
		}
		Window w = node.getWindow();
		if (type == ScreenType.NEW_WINDOW || type == ScreenType.CURRENT_WINDOW) {
			activeWindow = w;
			w.pack();
			w.setVisible(true);
			return node;
		}
		if (type == ScreenType.CLOSE_WINDOW){
			w.setVisible(false);
		}
		return null;
	}

	private Node createNode(String name) {
		try {
			InputStream input = new FileInputStream(client.getCacheFileName(name));
			Node node = new Node(Interface.parseInput(input, this), name);
			input.close();
			windowTable.put(name, node);
			return node;
		} catch (IOException e) {
			throw new InterfaceBuildingException(e);
		}
	}

	private void destroyWindow(String name) {
		if (windowTable.containsKey(name)) {
			windowTable.remove(name);
		}
	}

	void checkScreens(boolean init) throws IOException {
		while (receivePacketClass() == PacketClass.QueryScreen) {
			String name = checkScreen1();
			if (init) {
				showWindow(name, ScreenType.NEW_WINDOW);
				init = false;
			}
		}
	}

	private String checkScreen1() throws IOException {
		String name = receiveString();
		int size = receiveLong();
		int mtime = receiveLong();
		/* int ctime = */ receiveLong();
		String cachFileName = client.getCacheFileName(name);

		File file = new File(cachFileName);
		File parent = file.getParentFile();
		parent.mkdirs();
		file.createNewFile();
		if (file.lastModified() < mtime * 1000 || file.length() != size) {
			receiveFile(name, cachFileName);
			destroyWindow(name);
		} else {
			sendPacketClass(PacketClass.NOT);
		}
		return name;
	}

	boolean receiveWidgetData(Component widget) throws IOException {
		Class clazz = widget.getClass();
		WidgetMarshaller marshaller = WidgetMarshaller.getMarshaller(clazz);
		if (marshaller != null) {
			marshaller.receive(valueManager, widget);
			return true;
		}
		return false;
	}

	private boolean sendWidgetData(String name, Component widget) throws IOException {
		Class clazz = widget.getClass();
		WidgetMarshaller marshaller = WidgetMarshaller.getMarshaller(clazz);
		if (marshaller != null) {
			marshaller.send(valueManager, name, widget);
			return true;
		}
		return false;
	}

	private void receiveValueSkip() throws IOException {
		switch (receiveDataType()) {
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
	}

	public void receiveValue(StringBuffer longName, int offset) throws IOException {
		if ( ! receiveValueNeedTrace(longName)) {
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
	}

	private void receiveRecordValue(StringBuffer longName, int offset) throws IOException {
		for (int i = 0, n = receiveInt(); i < n; i++) {
			String name = receiveString();
			longName.replace(offset, longName.length(), '.' + name);
			receiveValue(longName, offset + name.length() + 1);
		}
	}

	private void receiveArrayValue(StringBuffer longName, int offset) throws IOException {
		for (int i = 0, n = receiveInt(); i < n; i++) {
			String name = '[' + String.valueOf(i) + ']';
			longName.replace(offset, longName.length(), name);
			receiveValue(longName, offset + name.length());
		}
	}

	private boolean receiveValueNeedTrace(StringBuffer longName) throws IOException {
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
				if ( ! protocol2) {
					needTrace = false;	// fatal error
					done = true;
					receiveValueSkip();
				}
			}
		}

		if (protocol2) {
			if ( ! done) {
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
		if ( ! done) {
			needTrace = true;
		}

		return needTrace;
	}

	public String receiveName() throws IOException {
		return receiveString();
	}

	public void sendName(String name) throws IOException {
		sendString(name);
	}

	void resetTimer(Component widget) {
		if (widget instanceof PandaTimer) {
			((PandaTimer)widget).reset();
		} else if (widget instanceof Container) {
			Container container = (Container)widget;
			for (int i = 0, n = container.getComponentCount(); i < n; i++) {
				resetTimer(container.getComponent(i));
			}
		}
	}

	synchronized boolean getScreenData() throws IOException {
		String window = null;
		Node node;
		boolean fCancel = false;

		try {
			isReceiving = true;
			checkScreens(false);
			sendPacketClass(PacketClass.GetData);
			sendLong(0); // get all data // In Java: int=>32bit, long=>64bit
			byte c;
			while ((c = receivePacketClass()) == PacketClass.WindowName) {
				window = receiveString();
				int type = receiveInt();
				switch (type) {
				case ScreenType.END_SESSION:
					client.exitSystem();
					fCancel= true;
					break;
				case ScreenType.CLOSE_WINDOW:
				case ScreenType.JOIN_WINDOW:
				case ScreenType.NEW_WINDOW:
				case ScreenType.CHANGE_WINDOW:
					fCancel = true;
					break;
				case ScreenType.CURRENT_WINDOW:
					break;
				default:
					break;
				}
				node = showWindow(window, type);
				if (node != null) {
					xml = node.getInterface();
				}
				switch (type) {
				case ScreenType.CURRENT_WINDOW:
				case ScreenType.NEW_WINDOW:
				case ScreenType.CHANGE_WINDOW:
					widgetName = new StringBuffer(window);
					c = receivePacketClass();
					if (c == PacketClass.ScreenData) {
						receiveValue(widgetName, widgetName.length());
					}
					break;
				default:
					c = receivePacketClass();
					break;
				}
				if (c == PacketClass.NOT) {
					// no screen data
				} else {
					// fatal error
				}
			}
			if (c == PacketClass.FocusName) {
				window = receiveString();
				String wName = receiveString();
				node = getNode(window);
				if (node != null && node.getInterface() != null) {
					Component widget = xml.getWidget(wName);
					if (widget != null) {
						widget.requestFocus();
					}
				}
				c = receivePacketClass();
			}
			// reset GtkPandaTimer if exists
			node = getNode(window);
			if (node != null) {
				resetTimer(node.getWindow());
			}
		} finally {
			isReceiving = false;
		}
		return fCancel;
	}

	void sendConnect(String user, String pass, String app) throws IOException {
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
		case PacketClass.NOT:
			throw new ConnectException("cannot connect server"); //$NON-NLS-1$
		case PacketClass.E_VERSION:
			throw new ConnectException("cannot connect to server(version mismatch)"); //$NON-NLS-1$
		case PacketClass.E_AUTH:
			throw new ConnectException("cannot connect to server(authentication error)"); //$NON-NLS-1$
		case PacketClass.E_APPL:
			throw new ConnectException("cannot connect to server(application name invalid)"); //$NON-NLS-1$
		default:
			String message = "cannot connect to server(other protocol error {0})"; //$NON-NLS-1$
			message = MessageFormat.format(message, new Object[] { Integer.toHexString(pc) });
			throw new ConnectException(message);
		}
	}

	private void sendVersionString() throws IOException {
		byte[] bytes = VERSION.getBytes();
		sendChar((byte)(bytes.length & 0xff));
		sendChar((byte)0);
		sendChar((byte)0);
		sendChar((byte)0);
		out.write(bytes);
		((OutputStream)out).flush();
	}

	void sendEvent(String window, String widget, String event) throws IOException {
		sendPacketClass(PacketClass.Event);
		sendString(window);
		sendString(widget);
		sendString(event);
	}

	void sendWindowData() throws IOException {
		Iterator i = windowTable.keySet().iterator();
		while (i.hasNext()) {
			sendWndowData1((String)i.next());
		}
		sendPacketClass(PacketClass.END);
		clearWindowTable();
	}

	private void sendWndowData1(String windowName) throws IOException {
		sendPacketClass(PacketClass.WindowName);
		sendString(windowName);
		Node node = getNode(windowName);
		Iterator i = node.getChangedWidgets().entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry)i.next();
			sendWidgetData((String)e.getKey(), (Component)e.getValue());
		}
		sendPacketClass(PacketClass.END);
	}

	void clearWindowTable() {
		Iterator i = windowTable.values().iterator();
		while (i.hasNext()) {
			Node node = (Node)i.next();
			node.clearChangedWidgets();
		}
	}

	synchronized void addChangedWidget(Component widget) {
		if (isReceiving) {
			return;
		}
		Node node = getNode(widget);
		if (node != null) {
			node.addChangedWidget(xml.getLongName(widget), widget);
		}
	}

	public boolean isReceiving() {
		return isReceiving;
	}

	Node getNode(String name) {
		return (Node)windowTable.get(name);
	}

	Node getNode(Component component) {
		return getNode(SwingUtilities.windowForComponent(component).getName());
	}

	void closeWindow(Component widget) {
		Node node = getNode(widget);
		if (node == null) {
			return;
		}
		node.getWindow().setVisible(false);
		if (isReceiving()) {
			return;
		}
		Iterator i = windowTable.values().iterator();
		while (i.hasNext()) {
			if (((Node)i.next()).getWindow() != null) {
				return;
			}
		}
		client.exitSystem();
	}

	synchronized void exit() {
		isReceiving = true;
		client.exitSystem();
	}

	public StringBuffer getWidgetNameBuffer() {
		return widgetName;
	}

	public Window getActiveWindow() {
		return activeWindow;
	}
}
