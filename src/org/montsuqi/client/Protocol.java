package org.montsuqi.client;

import java.awt.Container;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JWindow;

import org.montsuqi.util.Logger;
import org.montsuqi.monsia.Interface;
import org.montsuqi.monsia.Node;
import org.montsuqi.widgets.Calendar;
import org.montsuqi.widgets.NumberEntry;
import org.montsuqi.widgets.PandaEntry;

public class Protocol extends Connection {

	Map classTable;
	Map windowTable;
	Logger logger;
	Client client;
	StringBuffer widgetName;
	Interface xml;
	boolean ignoreEvent;
	boolean inReceive;

	public static final int SCREEN_NULL = 0;
	public static final int SCREEN_CURRENT_WINDOW = 1;
	public static final int SCREEN_NEW_WINDOW = 2;
	public static final int SCREEN_CLOSE_WINDOW = 3;
	public static final int SCREEN_CHANGE_WINDOW = 4;
	public static final int SCREEN_JOIN_WINDOW = 5;
	public static final int SCREEN_FORK_WINDOW = 6;
	public static final int SCREEN_END_SESSION = 7;

	static final String VERSION = "1.2.0";
	
	public String getVersion() {
		return VERSION;
	}
	
	public Protocol(Client client, Socket s) throws IOException {
		super(s);
		this.client = client;
		classTable = new HashMap();
		windowTable = new HashMap();
		inReceive = false;
		ignoreEvent = false;
		logger = Logger.getLogger(Connection.class);
		initWidgetOperations();
	}

	protected void initWidgetOperations() {
		addClass(JTextField.class,       "receiveEntry",       "sendEntry");
		addClass(NumberEntry.class,      "receiveNumberEntry", "sendNumberEntry");
		addClass(JTextArea.class, "receiveText",        "sendText");
//		addClass(PandaCombo.class,       "receivePandaCombo",  null);
//		addClass(PandaCList.class,       "receivePandaCList",  "sendPandaCList");
		addClass(PandaEntry.class,       "receiveEntry",       "sendEntry");
//		addClass(PandaText.class,        "receiveText",        "sendText");
		addClass(JLabel.class,           "receiveLabel",       null);
		addClass(JComboBox.class,        "receiveCombo",       null);
		addClass(JTable.class,           "receiveCList",       "sendCList");
		addClass(JButton.class,          "receiveButton",      null);
		addClass(JToggleButton.class,    "receiveButton",      "sendButton");
		addClass(JCheckBox.class,        "receiveButton",      "sendButton");
		addClass(JRadioButton.class,     "receiveButton",      "sendButton");
		addClass(JList.class,            "receiveList",        "sendList");
		addClass(Calendar.class,       "receiveCalendar",    "sendCalendar");
		addClass(JTabbedPane.class,      "receiveNotebook",    "sendNotebook");
		addClass(JProgressBar.class,      "receiveProgressBar", "sendProgressBar");
	}

	public Interface getInterface() {
		return xml;
	}

	public void sendStringDelim(String s) throws IOException {
		int size = s == null ? 0 : s.length();
		if (size > 0) {
			out.write(s.getBytes());
			out.flush();
		}
	}

	public String receiveStringDelim(int size) throws IOException {
		int  c;
		StringBuffer buf = new StringBuffer();

		while ((c = receiveChar()) != '\n') {
			if (c < 0) {
				break;
			} else {
				buf.append((char)c);
			}
		}
		int i = buf.length();
		while (i-- != 0) {
			if (buf.charAt(i) == '\r' || buf.charAt(i) == '\n') {
				buf.deleteCharAt(i);
			} else {
				break;
			}
		}
		return buf.toString();
	}

	static StringBuffer namebuff;

	public void setValueName(String name) {
		namebuff = new StringBuffer(name);
	}

	public void sendValueString(ValueStruct value, int idx, boolean fName) throws IOException {
		if (idx < 0) {
			idx = 0;
		}
		switch (value.type) {
		case Type.ARRAY:
			for (int i = 0; i < value.body.ArrayData.count; i++) {
				String name = "[" + i + "]";
				namebuff.replace(idx, namebuff.length(), name);
				sendValueString(value.body.ArrayData.item[i], idx + name.length(), fName);
			}
			break;
		case Type.RECORD:
			for (int i = 0; i < value.body.RecordData.count; i++) {
				String name = "." + value.body.RecordData.names[i];
				namebuff.replace(idx, namebuff.length(), name);
				sendValueString(value.body.RecordData.item[i], idx + name.length(), fName);
			}
			break;
		default:
			if (fName) {
				sendStringDelim(namebuff.toString());
				sendStringDelim(": ");
			}
			sendStringDelim(encode(value.toString()));
			sendStringDelim("\n");
			break;
		}
	}

	private static final BitSet ALNUM;
	static {
		ALNUM = new BitSet(256);
		ALNUM.clear();
		
	}
	
	private static final BitSet NOENCODE;
	static {
		NOENCODE = new BitSet(256);
		NOENCODE.clear();
		for (int i = 'a'; i <= 'z'; i++) {
			NOENCODE.set(i);
		}
		for (int i = 'A'; i <= 'Z'; i++) {
			NOENCODE.set(i);
		}
		for (int i = '0'; i <= '9'; i++) {
			NOENCODE.set(i);
		}
	}

	private static String encode(String s) {
		String enc = System.getProperty("file.encoding");
		char[] chars = s.toCharArray();
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (c == ' ') {
				buf.append('+');
			} else if (NOENCODE.get(c)) {
				buf.append(c);
			} else {
				buf.append('%');
				byte[] bytes = null;
				try {
					bytes = String.valueOf(c).getBytes(enc);
				} catch (UnsupportedEncodingException e) {
					throw new InternalError(); // should not happen
				}
				for (int j = 0; j < bytes.length; j++) {
					String hex = Integer.toHexString(bytes[j] & 0xff);
					if (hex.length() == 1) {
						buf.append('0');
					}
					buf.append(hex);
				}
			}
		}
		return buf.toString();
	}

	boolean receiveFile(String name, String fName) throws IOException {
		sendPacketClass(PacketClass.GetScreen);
		sendString(name);

		if (receivePacketClass() == PacketClass.ScreenDefine) {
			OutputStream out = new FileOutputStream(fName);
			int left = receiveLong();
			int size;
			final int SIZE_BUFF = 4096;
			byte[] buff = new byte[SIZE_BUFF];
			do {
				if (left > SIZE_BUFF) {
					size = SIZE_BUFF;
				} else {
					size = left;
				}
				size = in.read(buff, 0, size);
				if (size > 0) {
					out.write(buff);
					left -= size;
				}
			} while (left > 0);
			out.flush();
			out.close();
			return true;
		} else {
			logger.warn("invalid protocol sequence");
			return false;
		}
	}

	public Node showWindow(String wName, int type) {
		String fName = client.getCacheFileName(wName);
		Node node = null;
		if (windowTable.containsKey(wName)) {
			node = (Node)windowTable.get(wName);
		} else {
			/* Create new node */
			switch (type) {
			case SCREEN_NEW_WINDOW:
			case SCREEN_CURRENT_WINDOW:
				Interface xml = Interface.parseFile(fName, this);
				node = new Node(xml, wName);
				windowTable.put(node.name, node);
			}
		}

		if (node != null) {
			switch (type) {
			case SCREEN_NEW_WINDOW:
			case SCREEN_CURRENT_WINDOW:
				node.window.setVisible(true);
				break;
			case SCREEN_CLOSE_WINDOW:
				node.window.setVisible(false);
				/* fall through */
			default:
				node = null;
				break;
			}
		}

		return node;
	}

	private void destroyWindow(String sName) {
		Node node = (Node)windowTable.get(sName);
		if (node != null) {
			windowTable.remove(sName);
		}
	}

	public void checkScreens(boolean init) throws IOException {
		while (receivePacketClass() == PacketClass.QueryScreen) {
			String sName = receiveString();
			int size = receiveLong();
			int mtime = receiveLong();
			String fName = client.getCacheFileName(sName);

			File file = new File(fName);
			if (file.lastModified() < mtime * 1000 ||
				file.length() != size) {
				receiveFile(sName, fName);
				destroyWindow(sName);
			} else {
				sendPacketClass(PacketClass.NOT);
			}
			if (init) {
				showWindow(sName, SCREEN_NEW_WINDOW);
				init = false;
			}
		}
	}

	public boolean receiveWidgetData(Container widget) throws IOException {
		Handler handler = (Handler)(classTable.get(widget.getClass()));
		if (handler != null) {
			return handler.receiveWidget(widget, this);
		}
		return false;
	}

	public boolean sendWidgetData(String name, Container widget) throws IOException {
		Handler handler = (Handler)(classTable.get(widget.getClass()));
		if (handler != null) {
			return handler.sendWidget(name, widget, this);
		}
		return false;
	}

	private void receiveValueSkip() throws IOException {
		String name;
		int count;

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
			count = receiveInt();
			while (count-- != 0) {
				receiveValueSkip();
			}
			break;
		case Type.RECORD:
			count = receiveInt();
			while (count-- != 0) {
				name = receiveString();
				receiveValueSkip();
			}
			break;
		default:
			break;
		}
	}

	public void receiveValue(int offset) throws IOException {
		boolean fTrace;

		Container widget = xml.getWidget(widgetName.toString());
		if (widget != null) {
			fTrace = receiveWidgetData(widget);
		} else {
			fTrace = false; /* fatal error */
			receiveValueSkip();
		}

		if (fTrace) {
			int count;
			switch (receiveDataType()) {
			case Type.RECORD:
				count = receiveInt();
				while (count-- != 0) {
					String name = receiveString();
					int end = widgetName.length();
					widgetName.replace(offset, end, "." + name);
					receiveValue(offset);
				}
				break;
			default:
				break;
			}
		}
	}

	private boolean _grabFocus(Object data) {
		//gtk_widget_grab_focus(data);
		return false;
	}

	private void grabFocus(Container widget) {
		//gtk_idle_add(_GrabFocus, widget);
	}

	private void _resetTimer(Container widget, Object data) {
		//if (GTK_IS_CONTAINER (widget)) {
		//gtk_container_forall (GTK_CONTAINER (widget), _ResetTimer, NULL);
		//} else if (GTK_IS_PANDA_TIMER (widget)) {
		//gtk_panda_timer_reset (GTK_PANDA_TIMER (widget));
		//}
	}

	public void resetTimer(JWindow window) {
		//gtk_container_forall (GTK_CONTAINER (window), _ResetTimer, NULL);
	}

	public boolean getScreenData() throws IOException {
		String window = null;
		Node node;
		Container widget;
		int type;
		
		inReceive = true;
		checkScreens(false);
		sendPacketClass(PacketClass.GetData);
		sendLong((int)0);     /* get all data */ // In Java: int=>32bit, long=>64bit
		boolean fCancel = false;
		int c = receivePacketClass();
		while (c == PacketClass.WindowName) {
			window = receiveString();
			type = receiveInt();
			switch (type) {
			case SCREEN_END_SESSION:
				client.exitSystem();
				fCancel= true;
				break;
			case SCREEN_CLOSE_WINDOW:
			case SCREEN_JOIN_WINDOW:
			case SCREEN_NEW_WINDOW:
			case SCREEN_CHANGE_WINDOW:
				fCancel = true;
				break;
			case SCREEN_CURRENT_WINDOW:
				break;
			default:
				break;
			}
			node = showWindow(window, type);
			if (node != null) {
				xml = node.xml;
			}
			switch (type) {
			case SCREEN_CURRENT_WINDOW:
			case SCREEN_NEW_WINDOW:
			case SCREEN_CHANGE_WINDOW:
				widgetName = new StringBuffer(window);
				c = receivePacketClass();
				if (c == PacketClass.ScreenData) {
					receiveValue(0);
				}
				break;
			default:
				c = receivePacketClass();
				break;
			}
			if (c == PacketClass.NOT) {
				/* no screen data */
			} else {
				/* fatal error */
			}
		}
		if (c == PacketClass.FocusName) {
			window = receiveString();
			String wName = receiveString();
			node = (Node)(windowTable.get(window));
			if (node != null && node.xml != null) {
				widget = xml.getWidget(wName);
				if (widget != null) {
					grabFocus(widget);
				}
			}
			c = receivePacketClass();
		}
		/* reset GtkPandaTimer if exists */
		node = (Node)windowTable.get(window);
		if (node != null) {
			resetTimer(node.window);
		}
		inReceive = false;
		return fCancel;
	}

	public boolean sendConnect(String user, String pass, String apl) throws IOException {
		int pc;
		sendPacketClass(PacketClass.Connect);
		sendString(VERSION);
		sendString(user);
		sendString(pass);
		sendString(apl);

		logger.info("receivePacketClass");
		pc = receivePacketClass();
		logger.info("done");
		if (pc == PacketClass.OK) {
			return true;
		} else {
			switch (pc) {
			case PacketClass.NOT:
				logger.warn("can not connect server");
				break;
			case PacketClass.E_VERSION:
				logger.warn("can not connect server(version not match)");
				break;
			case PacketClass.E_AUTH:
				logger.warn("can not connect server(authentication error)");
				break;
			case PacketClass.E_APPL:
				logger.warn("can not connect server(application name invalid)");
				break;
			default:
				logger.warn("[]\ncan not connect server(other protocol error)",
							new Object[] { new Integer(pc) });
				break;
			}
			return false;
		}
	}

	public void sendEvent(String window, String widget, String event) throws IOException {
		sendPacketClass(PacketClass.Event);
		sendString(window);
		sendString(widget);
		sendString(event);
	}

	void sendWindowData() throws IOException {
		Iterator i = windowTable.keySet().iterator();
		while (i.hasNext()) {
			sendPacketClass(PacketClass.WindowName);
			String wName = (String)(i.next());
			sendString(wName);
			Node node = (Node)windowTable.get(wName);
			Iterator j = node.updateWidget.keySet().iterator();
			while (j.hasNext()) {
				String name = (String)(j.next());
				Container widget = (Container)(node.updateWidget.get(name));
				sendWidgetData(wName, widget);
			}
			sendPacketClass(PacketClass.END);
		}
		sendPacketClass(PacketClass.END);
		clearWindowTable();
	}

	void addClass(Class clazz, String receiverName, String senderName) {
		try {
			Handler handler = WidgetOperations.getHandler(receiverName, senderName);
			classTable.put(clazz, handler);
		} catch (NoSuchMethodException e) {
			logger.fatal(e);
		}
	}

	//
	// callbacks
	//
	public void clearWindowTable() {
		Iterator i = windowTable.values().iterator();
		while (i.hasNext()) {
			Node node = (Node)i.next();
			node.updateWidget = new HashMap();
		}
	}

	// public callbacks
	public boolean select_all(Container widget, Object userData) {
		JTextField field = (JTextField)widget;
		field.selectAll();
		field.setCaretPosition(0);
		return true;
	}

	public boolean unselect_all(Container widget, Object userData) {
		JTextField field = (JTextField)widget;
		field.select(0, 0);
		return true;
	}

	public void send_event(Container widget, Object userData) throws IOException {
		if ( ! inReceive  && !ignoreEvent) {
			Container parent = null;
			while (parent.getParent() != null) {
				parent = widget.getParent();
			}
			sendEvent(parent.getName(), widget.getName(), userData.toString());
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

	public void send_event_on_focus_out(Container widget, Object userData) throws IOException {
		send_event(widget, userData);
	}

	public void clist_send_event(Container widget, Object userData) throws IOException {
		send_event(widget, "SELECT");
	}

	public void activate_widget(Container widget, Object userData) throws IOException {
		send_event(widget, "ACTIVATE");
	}

	public void entry_next_focus(Container widget, Object userData) {
		Container parent = null;
		while (parent.getParent() != null) {
			parent = widget.getParent();
		}
		Node node = (Node)windowTable.get(parent.getName());
		if (node != null) {
			Container nextWidget = node.xml.getWidget(userData.toString());
			if (nextWidget != null) {
				grabFocus(nextWidget);
			}
		}
	}

	private void updateWidget(Container widget, Object userData) {
		String name;
		String windowName;
		if ( ! inReceive) {
			Container parent = null;
			while (parent.getParent() != null) {
				parent = parent.getParent();
			}
			//ResetTimer(GTK_WINDOW (window));
			name = xml.getWidgetLongName(widget);
			String wName = parent.getName();
			Node node = (Node)windowTable.get(wName);
			if (node != null && ! node.updateWidget.containsKey(name)) {
				node.updateWidget.put(name, widget);
			}
		}
	}

	public StringBuffer getWidgetName() {
		return widgetName;
	}

	public void chagned(Container widget, Object userData) {
		updateWidget(widget, null);
	}

	public void entry_changed(Container widget, Object userData) {
		updateWidget(widget, null);
	}
	
	public void text_changed(Container widget, Object userData) {
		updateWidget(widget, userData);
	}

	public void button_toggled(Container widget, Object userData) {
		updateWidget(widget, userData);
	}

	public void selection_changed(Container widget, Object userData) {
		updateWidget(widget, userData);
	}

	public void click_column(Container widget, Object userData) {
		updateWidget(widget, userData);
	}

	public void entry_set_editable(Container widget, Object userData) {
		/* empty??? */
	}

	public void map_event(Container widget, Object userData) {
		clearWindowTable();
	}

	public void set_focus(Container widget, Object userData) {
		String name = widget.getName();
		Node node = (Node)(windowTable.get(name));
		if(node != null) {
			/*FocusedScreen = node;*/ // this variable is referred from nowhere.
		}
	}

/*
  public void day_selected(Container widget, Object userData) {
  updateWidget(cal, userData);
  printf("%d\n",(int)user_data);
  printf("year = %d\n",widget->year);
  printf("month = %d\n",widget->month+1);
  printf("day = %d\n",widget->selected_day);
  }
*/

	public void switch_page(Container widget, Object userData) {
		updateWidget(widget, userData);
	}

	private boolean checkWindow(String name, Node node) {
		return node.window != null;
	}

	public void window_close(Container widget, Object userData) {
		String name = widget.getName();
		Node node = (Node)(windowTable.get(name));
		if (node != null) {
			node.window.setVisible(false);
			if ( ! inReceive) {
				Iterator i = windowTable.keySet().iterator();
				boolean checked = false;
				while (i.hasNext()) {
					String wName = (String)(i.next());
					checked = checkWindow(wName, (Node)node);
				}
				if ( ! checked) {
					System.exit(0);
				}
			}
		}
	}

	public void window_destroy(Container widget, Object userData) {
		inReceive = true;
		System.exit(0);
	}

	public void open_browser(Container widget, Object userData) {
		logger.warn("NOT SUPPORTED");
	}

	/** callback placeholder which has no effect */
	public void gtk_true(Container widget, Object userData) {
		/* DO NOTHING */
	}
}

