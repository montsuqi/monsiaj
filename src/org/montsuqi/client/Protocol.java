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
import java.net.URL;
import java.text.MessageFormat;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JTextPane;
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
	private boolean ignoreEvent;
	private boolean receiving;
	private boolean protocol1;
	private boolean protocol2;

	private static final String VERSION = "symbolic:expand"; //$NON-NLS-1$
	
	Protocol(Client client, Socket s) throws IOException {
		super(s, client.getEncoding(), isNetworkByteOrder()); //$NON-NLS-1$
		this.client = client;
		windowTable = new HashMap();
		setReceiving(false);
		ignoreEvent = false;
		protocol1 = true;
		protocol2 = false;
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

	private static final BitSet NOENCODE;
	static {
		NOENCODE = new BitSet(256);
		for (int i = 0, n = NOENCODE.length(); i < n; i++) {
			NOENCODE.clear(i);
		}
		for (int i = 'a'; i < 'z'; i++) {
			NOENCODE.set(i);
		}
		for (int i = 'A'; i < 'Z'; i++) {
			NOENCODE.set(i);
		}
		for (int i = '0'; i < '9'; i++) {
			NOENCODE.set(i);
		}
		// 1.4 feature
		//NOENCODE.set(0, NOENCODE.length(), false);
		//NOENCODE.set('a', 'z', true);
		//NOENCODE.set('A', 'Z', true);
		//NOENCODE.set('0', '9', true);
	}

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
		Node node = null;
		if (windowTable.containsKey(name)) {
			node = (Node)windowTable.get(name);
		} else {
			switch (type) {
			case ScreenType.NEW_WINDOW:
			case ScreenType.CURRENT_WINDOW:
				try {
					InputStream input = new FileInputStream(client.getCacheFileName(name));
					node = new Node(Interface.parseInput(input, this), name);
					input.close();
					windowTable.put(name, node);
				} catch (IOException e) {
					throw new InterfaceBuildingException(e);
				}
			}
		}
		if (node != null) {
			JFrame w = node.getWindow();
			switch (type) {
			case ScreenType.NEW_WINDOW:
			case ScreenType.CURRENT_WINDOW:
				w.pack();
				w.setVisible(true);
				break;
			case ScreenType.CLOSE_WINDOW:
				w.setVisible(false);
				// fall through
			default:
				node = null;
				break;
			}
		}
		return node;
	}

	private void destroyWindow(String name) {
		if (windowTable.containsKey(name)) {
			windowTable.remove(name);
		}
	}

	void checkScreens(boolean init) throws IOException {
		while (receivePacketClass() == PacketClass.QueryScreen) {
			String sName = receiveString();
			int size = receiveLong();
			int mtime = receiveLong();
			int ctime = receiveLong();
			String fName = client.getCacheFileName(sName);

			File file = new File(fName);
			File parent = file.getParentFile();
			parent.mkdirs();
			file.createNewFile();
			if (file.lastModified() < mtime * 1000 ||
				file.length() != size) {
				receiveFile(sName, fName);
				destroyWindow(sName);
			} else {
				sendPacketClass(PacketClass.NOT);
			}
			if (init) {
				showWindow(sName, ScreenType.NEW_WINDOW);
				init = false;
			}
		}
	}

	boolean receiveWidgetData(Component widget) throws IOException {
		Class clazz = widget.getClass();
		WidgetMarshaller marshaller = WidgetMarshaller.getMarshaller(clazz);
		if (marshaller != null) {
			marshaller.receive(valueManager, widget);
			return true;
		} else {
			return false;
		}
	}

	private boolean sendWidgetData(String name, Component widget) throws IOException {
		Class clazz = widget.getClass();
		WidgetMarshaller marshaller = WidgetMarshaller.getMarshaller(clazz);
		if (marshaller != null) {
			marshaller.send(valueManager, name, widget);
			return true;
		} else {
			return false;
		}
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
		if (receiveValueStep1(longName)) {
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

	private boolean receiveValueStep1(StringBuffer longName) throws IOException {
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
				} else {
					dataName = null;
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

	boolean getScreenData() throws IOException {
		String window = null;
		Node node;
		Component widget;
		int type;

		setReceiving(true);
		checkScreens(false);
		sendPacketClass(PacketClass.GetData);
		sendLong(0); // get all data // In Java: int=>32bit, long=>64bit
		boolean fCancel = false;
		byte c;
		while ((c = receivePacketClass()) == PacketClass.WindowName) {
			window = receiveString();
			type = receiveInt();
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
			node = (Node)(windowTable.get(window));
			if (node != null && node.getInterface() != null) {
				widget = xml.getWidget(wName);
				if (widget != null) {
					widget.requestFocus();
				}
			}
			c = receivePacketClass();
		}
		// reset GtkPandaTimer if exists
		node = (Node)windowTable.get(window);
		if (node != null) {
			resetTimer(node.getWindow());
		}
		setReceiving(false);
		return fCancel;
	}

	public boolean setReceiving(boolean receiving) {
		return this.receiving = receiving;
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
			throw new ConnectException(Messages.getString("Protocol.cannot_connect_to_server")); //$NON-NLS-1$
		case PacketClass.E_VERSION:
			throw new ConnectException(Messages.getString("Protocol.cannot_connect_to_server_version_mismatch")); //$NON-NLS-1$
		case PacketClass.E_AUTH:
			throw new ConnectException(Messages.getString("Protocol.cannot_connect_to_server_authentication_error")); //$NON-NLS-1$
		case PacketClass.E_APPL:
			throw new ConnectException(Messages.getString("Protocol.cannot_connect_to_server_invalid_application_name")); //$NON-NLS-1$
		default:
			String message = Messages.getString("Protocol.cannot_connect_to_server_other_protocol_error"); //$NON-NLS-1$
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

	private void sendEvent(String window, String widget, String event) throws IOException {
		sendPacketClass(PacketClass.Event);
		sendString(window);
		sendString(widget);
		sendString(event);
	}

	private void sendWindowData() throws IOException {
		Iterator i = windowTable.keySet().iterator();
		while (i.hasNext()) {
			sendPacketClass(PacketClass.WindowName);
			String wName = (String)(i.next());
			sendString(wName);
			Node node = (Node)windowTable.get(wName);
			Iterator j = node.getChangedWidgets().entrySet().iterator();
			while (j.hasNext()) {
				Map.Entry e = (Map.Entry)j.next();
				String name = (String)e.getKey();
				Component widget = (Component)e.getValue();
				sendWidgetData(name, widget);
			}
			sendPacketClass(PacketClass.END);
		}
		sendPacketClass(PacketClass.END);
		clearWindowTable();
	}

	private void clearWindowTable() {
		Iterator i = windowTable.values().iterator();
		while (i.hasNext()) {
			Node node = (Node)i.next();
			node.clearChangedWidgets();
		}
	}

	// public callbacks
	public boolean select_all(Component widget, Object userData) {
		JTextField field = (JTextField)widget;
		field.selectAll();
		field.setCaretPosition(0);
		return true;
	}

	public boolean unselect_all(Component widget, Object userData) {
		JTextField field = (JTextField)widget;
		field.select(0, 0);
		return true;
	}

	public void send_event(Component widget, Object userData) throws IOException {
		if ( ! isReceiving()  && !ignoreEvent) {
			sendEvent(SwingUtilities.windowForComponent(widget).getName(), widget.getName(), userData == null ? "" : userData.toString()); //$NON-NLS-1$
			sendWindowData();
//			blockChangedHanders();
			if (getScreenData()) {
				ignoreEvent = true;
//				while (gtk_events_pending()) {
//					gtk_main_iteration();
//				}
				ignoreEvent = false;
			}
//			unblockChangedHanders();
		}
	}

	public void send_event_when_idle(Component widget, Object userData) throws IOException {
		send_event(widget, userData);
	}

	public void send_event_on_focus_out(Component widget, Object userData) throws IOException {
		send_event(widget, userData);
	}

	public void clist_send_event(Component widget, Object userData) throws IOException {
		addChangedWidget(widget, userData);
		send_event(widget, "SELECT"); //$NON-NLS-1$
	}

	public void activate_widget(Component widget, Object userData) throws IOException {
		send_event(widget, "ACTIVATE"); //$NON-NLS-1$
	}

	public void entry_next_focus(Component widget, Object userData) {
		Node node = (Node)windowTable.get(SwingUtilities.windowForComponent(widget).getName());
		if (node != null) {
			Component nextWidget = node.getInterface().getWidget(userData.toString());
			if (nextWidget != null) {
				nextWidget.requestFocus();
			}
		}
	}

	public void addChangedWidget(Component widget, Object userData) {
		if (isReceiving()) {
			return;
		}
		Window window = SwingUtilities.windowForComponent(widget);
		String name = xml.getLongName(widget);
		String windowName = window.getName();
		Node node = (Node)windowTable.get(windowName);
		if (node != null) {
			node.addChangedWidget(name, widget);
		}
	}

	public boolean isReceiving() {
		return receiving;
	}

	public void changed(Component widget, Object userData) {
		addChangedWidget(widget, null);
	}

	public void entry_changed(Component widget, Object userData) {
		addChangedWidget(widget, null);
	}
	
	public void text_changed(Component widget, Object userData) {
		addChangedWidget(widget, userData);
	}

	public void button_toggled(Component widget, Object userData) {
		addChangedWidget(widget, userData);
	}

	public void selection_changed(Component widget, Object userData) {
		addChangedWidget(widget, userData);
	}

	public void click_column(Component widget, Object userData) {
		addChangedWidget(widget, userData);
	}

	public void entry_set_editable(Component widget, Object userData) {
		// empty???
	}

	public void map_event(Component widget, Object userData) {
		clearWindowTable();
	}

	public void set_focus(Component widget, Object userData) {
		if(windowTable.containsKey(widget.getName())) {
			// FocusedScreen = node; // this variable is referred from nowhere.
		}
	}

	public void day_selected(Component widget, Object userData) {
		addChangedWidget(widget, userData);
	}

	public void switch_page(Component widget, Object userData) {
		addChangedWidget(widget, userData);
	}

	private boolean checkWindow(String name, Node node) {
		return node.getWindow() != null;
	}

	public void window_close(Component widget, Object userData) {
		Node node = (Node)windowTable.get(widget.getName());
		if (node != null) {
			node.getWindow().setVisible(false);
			if ( ! isReceiving()) {
				Iterator i = windowTable.keySet().iterator();
				boolean checked = false;
				while (i.hasNext()) {
					String wName = (String)(i.next());
					checked = checkWindow(wName, node);
				}
				if ( ! checked) {
					client.exitSystem();
				}
			}
		}
	}

	public void window_destroy(Component widget, Object userData) {
		setReceiving(true);
		client.exitSystem();
	}

	public void open_browser(Component widget, Object userData) {
		if ( ! (widget instanceof JTextPane)) {
			logger.warn(Messages.getString("Protocol.not_a_JTextPane_widget")); //$NON-NLS-1$
		}
		JTextPane pane = (JTextPane)widget;
		URL uri;
		try {
			uri = new URL((String)userData);
			pane.setPage(uri);
		} catch (Exception e) {
			logger.warn(e);
		}
	}

	public void keypress_filter(Component widget, Object userData) {
		Component next = xml.getWidget((String)userData);
		next.requestFocus();
	}

	public void press_filter(Component widget, Object userData) {
		logger.warn(Messages.getString("Protocol.press_filter_is_not_impremented_yet")); //$NON-NLS-1$
	}

	public void gtk_true(Component widget, Object userData) {
		// callback placeholder which has no effect
	}

	public StringBuffer getWidgetNameBuffer() {
		return widgetName;
	}
}

