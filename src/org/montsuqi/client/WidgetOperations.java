package org.montsuqi.client;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Method;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import java.math.BigDecimal;
import org.montsuqi.util.Logger;
import org.montsuqi.monsia.Interface;
import org.montsuqi.widgets.NumberEntry;

public class WidgetOperations {

	Protocol con;
	Logger logger;
	Interface xml;

	public WidgetOperations(Protocol con) {
		this.con = con;
		logger = Logger.getLogger(Protocol.class);
		xml = con.getInterface();
	}
	
	static Map valueTable;

	void registerValue(String valueName, Object opt) {
		StringBuffer widgetName = con.getWidgetName();
		ValueAttribute va = (ValueAttribute)(valueTable.get(widgetName.toString()));
		if (va == null) {
			va = new ValueAttribute();
			va.name = widgetName.toString();
			va.type = con.getLastDataType();
			valueTable.put(va.name, va);
		} else {
			va.type = con.getLastDataType();
		}
		va.valueName = valueName;
		va.opt = opt;
	}

	ValueAttribute getValue(String name) {
		return (ValueAttribute)(valueTable.get(name));
	}

	public static Handler getHandler(String receiverName, String senderName)
		throws SecurityException, NoSuchMethodException {
		Class[] parameterTypes = null;

		Method receiver = null;
		parameterTypes = new Class[] { Container.class, Protocol.class };
		if (receiverName != null && receiverName.length() > 0) {
			receiver = WidgetOperations.class.getMethod(receiverName, parameterTypes);
		}

		Method sender = null;
		parameterTypes = new Class[] { String.class, Container.class, Protocol.class };
		if (senderName != null && senderName.length() > 0) {
			sender = WidgetOperations.class.getMethod(senderName, parameterTypes);
		}
		
		return new Handler(receiver, sender);
	}

	void setState(Container widget, int state) {
		if (state != 3) { // #define GTK_STATE_INSENSITIVE 3
			widget.setEnabled(true);
		} else {
			widget.setEnabled(false);
		}
	}

	//void setStyle(Container widget, Style style) {
	//	if (widget.getClass() == JLabel.class) {
	//	/*gtk_widget_set_style(widget,style);*/
	//	} else {
	//		Component[] comps = widget.getComponents();
	//		for (int i = 0; i < comps.length; i++) {
	//			setStyle(comps[i], style);
	//		}
	//	}
	//}

	public boolean receiveEntry(Container widget, Protocol con) throws IOException {
		if (con.receiveDataType()  == Type.RECORD) {
			int nItem = con.receiveInt();
			while (nItem-- != 0) {
				String name = con.receiveString();
				if ("state".equals(name)) {
					int state = con.receiveIntData();
					setState(widget, state);
				} else if ("style".equals(name)) {
					String buff = con.receiveStringData();
					/* setStyle(widget, getStyle(buff)); */
				} else {
					String buff = con.receiveStringData();
					registerValue(name, null);
					((JTextField)widget).setText(buff);
				}
			}
		}
		return true;
	}

	public boolean sendEntry(String name, Container widget, Protocol con) throws IOException {
		String p = ((JTextField)widget).getText();
		con.sendPacketClass(PacketClass.ScreenData);
		ValueAttribute v = getValue(name);
		con.sendString(name + "." + v.valueName);
		con.sendStringData(v.type, p);
		return true;
	}

	public boolean receiveNumberEntry(Container widget, Protocol con) throws IOException {
		if (con.receiveDataType() == Type.RECORD) {
			int nItem = con.receiveInt();
			while (nItem-- != 0) {
				String name = con.receiveString();
				if ("state".equals(name)) {
					int state = con.receiveIntData();
					setState(widget, state);
				} else if ("style".equals(name)) {
					String buff = con.receiveStringData();
					/* setStyle(widget,getStyle(buff)); */
				} else {
					String buff = con.getWidgetName().toString() + "." + name;
					ValueAttribute va = getValue(buff);
					if (va != null) {
						BigDecimal val = con.receiveFixedData();
						registerValue(name, val);
						((NumberEntry)widget).setValue(val);
					}
				}
			}
		}
		return true;
	}

	public boolean sendNumberEntry(String name, Container widget, Protocol con) throws IOException {
		BigDecimal value = ((NumberEntry)widget).getValue();
		con.sendPacketClass(PacketClass.ScreenData);
		ValueAttribute va = getValue(name);
		va.opt = value;
		con.sendString(name + "." + va.name);
		con.sendFixedData(va.type, value);
		return true;
	}

	public boolean receiveLabel(Container widget, Protocol con) throws IOException {
		if (con.receiveDataType() == Type.RECORD) {
			int nItem = con.receiveInt();
			while (nItem-- != 0) {
				String name = con.receiveString();
				if ("style".equals(name)) {
					String buff = con.receiveStringData();
					/*gtk_widget_set_style(widget,GetStyle(buff));*/
				} else {
					String buff = con.receiveStringData();
					registerValue(name, null);
					((JLabel)widget).setText(buff);
				}
			}
		}
		return true;
	}

	public boolean sendText(String name, Container widget, Protocol con) throws IOException {
		String p = ((JTextField)widget).getText();
		con.sendPacketClass(PacketClass.ScreenData);
		ValueAttribute va = getValue(name);
		con.sendString(name + "." + va.valueName);
		con.sendStringData(va.type, p);
		return true;
	}

	public boolean receiveText(Container widget, Protocol con) throws IOException {
		if (con.receiveDataType() == Type.RECORD) {
			int nItem = con.receiveInt();
			while (nItem-- != 0) {
				String name= con.receiveString();
				if ("state".equals(name)) {
					int state = con.receiveIntData();
					setState(widget,state);
				} else if ("style".equals(name)) {
					String buff = con.receiveStringData();
					/*gtk_widget_set_style(widget,GetStyle(buff));*/
				} else {
					String buff = con.receiveStringData();
					registerValue(name, null);
					JTextComponent text = (JTextComponent)widget;
					text.setText(buff);
				}
			}
		}
		return true;
	}

	public boolean sendButton(String name, Container widget, Protocol con) throws IOException {
		con.sendPacketClass(PacketClass.ScreenData);
		ValueAttribute va = getValue(name);
		con.sendString(name + "." + va.valueName);
		con.sendBooleanData(va.type, ((JButton)widget).isSelected());
		return true;
	}

	public void setLabel(Container widget, String label, Protocol con) throws IOException {
		if (widget.getClass() == JLabel.class) {
			((JLabel)widget).setText(label);
		} else {
			Component[] comps = widget.getComponents();
			for (int i = 0; i < comps.length; i++) {
				if (comps[i] instanceof Container) {
					setLabel((Container)comps[i], label, con);
				}
			}
		}
	}

	public boolean receiveButton(Container widget, Protocol con) throws IOException {
		if (con.receiveDataType() == Type.RECORD) {
			int nItem = con.receiveInt();
			while (nItem-- != 0) {
				String name = con.receiveString();
				if ("state".equals(name)) {
					int state = con.receiveIntData();
					setState(widget, state);
				} else if ("style".equals(name)) {
					String buff = con.receiveStringData();
					/*setStyle(widget,getStyle(buff));*/
				} else if ("label".equals(name)) {
					String buff = con.receiveStringData();
					setLabel(widget, buff, con);
				} else {
					boolean fActive = con.receiveBooleanData();
					registerValue(name,null);
					((JButton)widget).setSelected(fActive);
				}
			}
		}
		return true;
	}

	public boolean receiveCombo(Container widget, Protocol con) throws IOException {
		con.receiveDataType(Type.RECORD);
		int nItem = con.receiveInt();
		int count = 0;
		while (nItem-- != 0) {
			String name = con.receiveString();
			if ("state".equals(name)) {
				int state = con.receiveIntData();
				setState(widget, state);
			} else if ("style".equals(name)) {
				String buff = con.receiveStringData();
				/* gtk_widget_set_style(widget,GetStyle(buff)); */
			} else if ("count".equals(name)) {
				count = con.receiveIntData();
			} else if ("item".equals(name)) {
				List list = new ArrayList();
				list.add("");
				con.receiveDataType(Type.ARRAY); /*	Type.ARRAY	*/
				int num = con.receiveInt();
				for	(int j = 0; j < num ; j++) {
					String buff = con.receiveStringData();
					if (buff != null && j < count) {
						list.add(buff);
					}
				}
				JComboBox combo = (JComboBox)widget;
				combo.removeAllItems();
				Iterator i = list.iterator();
				while (i.hasNext()) {
					combo.addItem(i.next());
				}
			} else {
				StringBuffer label = con.getWidgetName();
				int offset = label.length();
				label.replace(offset, label.length(), "." + name);
				Container sub =  xml.getWidget(label.toString());
				if (sub != null) {
					receiveEntry(sub, con);
				} else {
					logger.warn("sub widget not found\n");
					/*	fatal error	*/
				}
			}
		}
		return true;
	}

	public boolean receivePandaCombo(Container widget, Protocol con) throws IOException {
		return receiveCombo(widget, con);
	}

	public boolean sendPandaCList(String name, Container widget, Protocol con) throws IOException {
		ValueAttribute va = getValue(name);
		JTable table = (JTable)widget;
		int rows = table.getRowCount();
		
		for	(int i = 0; i < rows; i++) {
			String iname =
				name + "." + va.valueName + "[" + i + ((Integer)va.opt).intValue() + "]";
			con.sendPacketClass(PacketClass.ScreenData);
			con.sendString(iname);
			con.sendDataType(Type.BOOL);
			con.sendBoolean(table.isRowSelected(i));
		}
		return true;
	}

	public boolean receivePandaCList(Container widget, Protocol con) throws IOException {
		con.receiveDataType(Type.RECORD);
		StringBuffer label = con.getWidgetName();
		int offset = label.length();
		int nItem = con.receiveInt();
		int count = -1;
		int from = 0;
		int state;
		
		while (nItem-- != 0) {
			String name = con.receiveString();
			label.replace(offset, label.length(), "." + name);
			Container subWidget = xml.getWidgetByLongName(label.toString());
			if (subWidget != null) {
				con.receiveWidgetData(subWidget);
			} else if ("count".equals(name)) {
				count = con.receiveIntData();
			} else if ("from".equals(name)) {
				from = con.receiveIntData();
			} else if ("state".equals(name)) {
				state = con.receiveIntData();
				setState(widget, state);
			} else if ("style".equals(name)) {
				String buff = con.receiveStringData();
				/* gtk_widget_set_style(widget,GetStyle(buff));*/
			} else if ("row".equals(name)) {
				/* NOP */
			} else if ("column".equals(name)) {
				/* NOP */
			} else if ("item".equals(name)) {
				DefaultTableModel model = new DefaultTableModel();
				con.receiveDataType(Type.ARRAY);
				int num = con.receiveInt();
				if (count < 0) {
					count = num;
				}
				for	(int j = 0; j < num; j++) {
					con.receiveDataType(Type.RECORD);
					int rnum = con.receiveInt();
					String[] rdata = new String[rnum];
					for (int k = 0; k < rnum; k++) {
						String iname = con.receiveString();
						String buff = con.receiveString();
						rdata[k] = buff;
					}
					if ((j >= from) && ((j - from) < count)) {
						model.addRow(rdata);
					}
				}
				((JTable)widget).setModel(model);
			} else {
				con.receiveDataType(Type.ARRAY);
				registerValue(name, new Integer(from));
				int num = con.receiveInt();
				if (count < 0) {
					count = num;
				}
				ListSelectionModel model = ((JTable)widget).getSelectionModel();
				for	(int j = 0; j < num; j++) {
					boolean fActive = con.receiveBooleanData();
					if ((j >= from) && ((j - from) < count)) {
						if (fActive) {
							model.addSelectionInterval(j, j);
						} else {
							model.removeSelectionInterval(j, j);
						}
					}
				}
			}
		}
		return true;
	}

	public boolean sendCList(String name, Container widget, Protocol con) throws IOException {
		ValueAttribute va = getValue(name);
		JTable table = (JTable)widget;
		ListSelectionModel selections = table.getSelectionModel();
		for	(int i = 0, rows = table.getRowCount(); i < rows; i++) {
			con.sendPacketClass(PacketClass.ScreenData);
			con.sendString(name + "." + va.valueName + "[" + i + "]" + va.opt);
			con.sendDataType(Type.BOOL);
			con.sendBoolean(selections.isSelectedIndex(i));
		}
		return true;
	}

	public boolean receiveCList(Container widget, Protocol con) throws IOException {
		JTable table = (JTable)widget;
		con.receiveDataType(Type.RECORD);
		StringBuffer label = con.getWidgetName();
		int offset = label.length();
		int nItem = con.receiveInt();
		int count = -1;
		int from = 0;
		int state;
		while (nItem-- != 0) {
			String name = con.receiveString();
			label.replace(offset, label.length(), "." + name);
			Container subWidget;
			if ((subWidget = xml.getWidget(label.toString())) != null) {
				con.receiveWidgetData(subWidget);
			} else if ("count".equals(name)) {
				count = con.receiveIntData();
			} else if ("from".equals(name)) {
				from = con.receiveIntData();
			} else if ("state".equals(name)) {
				state = con.receiveIntData();
				setState(widget, state);
			} else if ("style".equals(name)) {
				String buff = con.receiveStringData();
				/* gtk_widget_set_style(widget,GetStyle(buff)); */
			} else if ("row".equals(name)) {
				/* NOP */
			} else if ("columns".equals(name)) {
				/* NOP */
			} else if ("item".equals(name)) {
				DefaultTableModel model = new DefaultTableModel();
				con.receiveDataType(Type.ARRAY);
				int num = con.receiveInt();
				if (count < 0) {
					count = num;
				}
				for	(int j = 0; j < num; j++) {
					con.receiveDataType(Type.RECORD);
					int rNum = con.receiveInt();
					Object[] rdata = new String[rNum];
					for	(int k = 0; k < rNum; k++) {
						String iName = con.receiveString();
						rdata[k] = con.receiveString();
					}
					if ((j >= from) && ((j - from) < count)) {
						model.addRow(rdata);
					}
				}
				table.setModel(model);
			} else {
				con.receiveDataType(Type.ARRAY);
				registerValue(name, new Integer(from));
				int num = con.receiveInt();
				if (count < 0) {
					count = num;
				}
				for	(int j = 0; j < num; j++) {
					boolean fActive = con.receiveBooleanData();
					ListSelectionModel model = table.getSelectionModel();
					if (fActive) {
						if ((j >= from) && ((j - from) < count)) {
							if (fActive) {
								model.addSelectionInterval(j - from, j - from);
							} else {
								model.removeSelectionInterval(j - from, j - from);
							}
						}
					}
				}
			}
		}
		return true;
	}

	public boolean sendList(String name, Container widget, Protocol con) throws IOException {
		JList list = (JList)widget;
		ListSelectionModel model = list.getSelectionModel();
		ValueAttribute va = getValue(name);

		for	(int i = 0, rows = list.getModel().getSize(); i < rows; i++) {
			con.sendPacketClass(PacketClass.ScreenData);
			Integer iObject = (Integer)va.opt;
			con.sendString(name + "." + va.valueName + "[" + iObject.intValue() + "]");
			con.sendDataType(Type.BOOL);
			con.sendBoolean(model.isSelectedIndex(i));
		}
		return true;
	}

	public boolean receiveList(Container widget, Protocol con) throws IOException {
		Container item;
		int state;
		
		con.receiveDataType(Type.RECORD);
		StringBuffer label = con.getWidgetName();
		int offset = label.length();
		int nItem = con.receiveInt();
		int count = -1;
		int from = 0;
		while (nItem-- != 0) {
			String name = con.receiveString();
			int num;
			if ("state".equals(name)) {
				setState(widget,con.receiveIntData());
			} else if ("style".equals(name)) {
				String buff = con.receiveStringData();
				/*gtk_widget_set_style(widget,GetStyle(buff));*/
			} else if ("count".equals(name)) {
				count = con.receiveIntData();
			} else if ("from".equals(name)) {
				from = con.receiveIntData();
			} else if ("item".equals(name)) {
				DefaultListModel model = new DefaultListModel();
				con.receiveDataType(Type.ARRAY);
				num = con.receiveInt();
				if (count < 0) {
					count = num;
				}
				for	(int j = 0; j < num; j++) {
					String buff = con.receiveStringData();
					if (buff != null) {
						if ((j >= from) && ((j - from) < count)) {
							model.addElement(buff);
						}
					}
				}
				((JList)widget).setModel(model);
			} else {
				con.receiveDataType(Type.ARRAY);
				registerValue(name, new Integer(from));
				num = con.receiveInt();
				if (count < 0) {
					count = num;
				}
				ListSelectionModel model = ((JList)widget).getSelectionModel();
				for	(int j = 0; j < num; j++) {
					boolean fActive = con.receiveBooleanData();
					if ((j >= from) &&	((j - from) < count)) {
						if (fActive) {
							model.addSelectionInterval(j, j);
						} else {
							model.removeSelectionInterval(j, j);
						}
					}
				}
			}
		}
		return true;
	}

	public boolean sendCalendar(String name, Container calendar, Protocol con) throws IOException {
		String iName;
		int year, month, day;
		Date date = ((org.montsuqi.widgets.Calendar)calendar).getDate();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		con.sendPacketClass(PacketClass.ScreenData);
		con.sendString(name + ".year");
		con.sendDataType(Type.INT);
		con.sendInt(cal.get(java.util.Calendar.YEAR));

		con.sendPacketClass(PacketClass.ScreenData);
		con.sendString(name + ".month");
		con.sendDataType(Type.INT);
		con.sendInt(cal.get(java.util.Calendar.MONTH) + 1);

		con.sendPacketClass(PacketClass.ScreenData);
		con.sendString(name + ".day");
		con.sendDataType(Type.INT);
		con.sendInt(cal.get(java.util.Calendar.DATE));

		return true;
	}

	public boolean receiveCalendar(Container widget, Protocol con) throws IOException {
		con.receiveDataType(Type.RECORD);
		registerValue("", null);
		int nItem = con.receiveInt();
		int year = 0;
		int month = -1;
		int day = 0;
		while (nItem-- != 0) {
			String name = con.receiveString();
			if ("state".equals(name)) {
				setState(widget, con.receiveIntData());
			} else if ("style".equals(name)) {
				String buff = con.receiveStringData();
				/*gtk_widget_set_style(widget,GetStyle(buff));*/
			} else if ("year".equals(name)) {
				year = con.receiveIntData();
			} else if ("month".equals(name)) {
				month = con.receiveIntData();
			} else if ("day".equals(name)) {
				day = con.receiveIntData();
			} else {
				/*	fatal error	*/
			}
		}

		org.montsuqi.widgets.Calendar calendarWidget = (org.montsuqi.widgets.Calendar)widget;
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month - 1);
		calendar.set(Calendar.DATE, day);
		calendarWidget.setDate(calendar.getTime());
		return true;
	}

	public boolean sendNotebook(String name, Container notebook, Protocol con) throws IOException {
		JTabbedPane tabbed = (JTabbedPane)notebook;
		ValueAttribute va = getValue(name);
		con.sendPacketClass(PacketClass.ScreenData);
		con.sendString(name + "." + va.valueName);
		con.sendIntegerData(va.type, tabbed.getSelectedIndex());
		return true;
	}

	public boolean receiveNotebook(Container widget, Protocol con) throws IOException {
		int page = -1;
		int state;

		con.receiveDataType(Type.RECORD);
		int nItem = con.receiveInt();
		StringBuffer widgetName = con.getWidgetName();
		int offset = widgetName.length();
		for	(int i = 0; i < nItem; i++) {
			String name = con.receiveString();
			if ("state".equals(name)) {
				setState(widget, con.receiveIntData());
			} else if ("style".equals(name)) {
				String buff = con.receiveStringData();
				/* gtk_widget_set_style(widget,GetStyle(buff)); */
			} else if ("pageno".equals(name)) {
				page = con.receiveIntData();
				registerValue(name, null);
			} else {
				widgetName.replace(offset, widgetName.length(), "." + name);
				con.receiveValue(offset + name.length());
			}
		}
		if (page == -1) {
			throw new IllegalStateException("page not found");
		}
		JTabbedPane tabbed = (JTabbedPane)widget;
		tabbed.setSelectedIndex(page);
		return true;
	}

	public boolean sendProgressBar(String name, Container widget, Protocol con) throws IOException {
		JProgressBar progress = (JProgressBar)widget;
		ValueAttribute va = getValue(name);
		con.sendPacketClass(PacketClass.ScreenData);
		con.sendString(name + "." + va.valueName);
		con.sendIntegerData(va.type, progress.getValue());
		return true;
	}

	public boolean receiveProgressBar(Container widget, Protocol con) throws IOException {
		con.receiveDataType(Type.RECORD);
		int nItem = con.receiveInt();
		StringBuffer longName = con.getWidgetName();
		int offset = longName.length();

		while (nItem-- != 0) {
			String name = con.receiveString();
			if ("state".equals(name)) {
				setState(widget, con.receiveIntData());
			} else if ("style".equals(name)) {
				String buff = con.receiveStringData();
				/* gtk_widget_set_style(widget,GetStyle(buff)); */
			} else if ("value".equals(name)) {
				registerValue(name, null);
				JProgressBar progress = (JProgressBar)widget;
				progress.setValue(con.receiveIntData());
			}
		}
		return true;
	}

	static {
		valueTable = new HashMap();
	}
}
