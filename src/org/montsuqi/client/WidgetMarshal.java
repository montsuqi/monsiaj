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
import org.montsuqi.widgets.NumberEntry;

class WidgetMarshal {

	private Protocol con;
	private Logger logger;
	private Map valueTable;

	WidgetMarshal(Protocol con) {
		this.con = con;
		logger = Logger.getLogger(Protocol.class);
		valueTable = new HashMap();
	}
	
	private void registerValue(Container widget, String valueName, Object opt) {
		String longName = con.getInterface().getLongName(widget);
		ValueAttribute va = (ValueAttribute)(valueTable.get(longName));

		if (va == null) {
			va = new ValueAttribute(longName, valueName, con.getWidgetNameBuffer().toString(), con.getLastDataType(), opt);
			valueTable.put(va.getKey(), va);
		} else {
			va.setNameSuffix(valueName);
			va.setOpt(con.getLastDataType(), opt);
		}
	}

	ValueAttribute getValue(String name) {
		return (ValueAttribute)(valueTable.get(name));
	}

	static MarshalHandler getHandler(String receiverName, String senderName)
		throws SecurityException, NoSuchMethodException {
		Class[] parameterTypes = null;

		Method receiver = null;
		parameterTypes = new Class[] { Container.class };
		if (receiverName != null && receiverName.length() > 0) {
			receiver = WidgetMarshal.class.getDeclaredMethod(receiverName, parameterTypes);
		}

		Method sender = null;
		parameterTypes = new Class[] { String.class, Container.class };
		if (senderName != null && senderName.length() > 0) {
			sender = WidgetMarshal.class.getDeclaredMethod(senderName, parameterTypes);
		}

		return new MarshalHandler(receiver, sender);
	}

	private void setState(Container widget, int state) {
		if (state != 3) { // #define GTK_STATE_INSENSITIVE 3
			widget.setEnabled(true);
		} else {
			widget.setEnabled(false);
		}
	}

	boolean receiveEntry(Container widget) throws IOException {
		if (con.receiveDataType()  == Type.RECORD) {
			int nItem = con.receiveInt();
			while (nItem-- != 0) {
				String name = con.receiveString();
				if ("state".equals(name)) { //$NON-NLS-1$
					int state = con.receiveIntData();
					setState(widget, state);
				} else if ("style".equals(name)) { //$NON-NLS-1$
					String buff = con.receiveStringData();
					/* setStyle(widget, getStyle(buff)); */
				} else {
					String buff = con.receiveStringData();
					registerValue(widget, name, null);
					((JTextField)widget).setText(buff);
				}
			}
		}
		return true;
	}

	boolean sendEntry(String name, Container widget) throws IOException {
		String p = ((JTextField)widget).getText();
		con.sendPacketClass(PacketClass.ScreenData);
		ValueAttribute v = getValue(name);
		con.sendString(v.getValueName() + '.' + v.getNameSuffix()); //$NON-NLS-1$
		con.sendStringData(v.getType(), p);
		return true;
	}

	boolean receiveNumberEntry(Container widget) throws IOException {
		if (con.receiveDataType() == Type.RECORD) {
			int nItem = con.receiveInt();
			while (nItem-- != 0) {
				String name = con.receiveString();
				if ("state".equals(name)) { //$NON-NLS-1$
					int state = con.receiveIntData();
					setState(widget, state);
				} else if ("style".equals(name)) { //$NON-NLS-1$
					String buff = con.receiveStringData();
					/* setStyle(widget,getStyle(buff)); */
				} else {
					String buff = con.getWidgetNameBuffer().toString() + '.' + name;
					ValueAttribute va = getValue(buff);
					if (va != null) {
						BigDecimal val = con.receiveFixedData();
						registerValue(widget, name, val);
						((NumberEntry)widget).setValue(val);
					}
				}
			}
		}
		return true;
	}

	boolean sendNumberEntry(String name, Container widget) throws IOException {
		BigDecimal value = ((NumberEntry)widget).getValue();
		con.sendPacketClass(PacketClass.ScreenData);
		ValueAttribute va = getValue(name);
		va.setOpt(value);
		con.sendString(name + '.' + va.getKey());
		con.sendFixedData(va.getType(), value);
		return true;
	}

	boolean receiveLabel(Container widget) throws IOException {
		if (con.receiveDataType() == Type.RECORD) {
			int nItem = con.receiveInt();
			while (nItem-- != 0) {
				String name = con.receiveString();
				if ("style".equals(name)) { //$NON-NLS-1$
					String buff = con.receiveStringData();
					/*gtk_widget_set_style(widget,GetStyle(buff));*/
				} else {
					String buff = con.receiveStringData();
					registerValue(widget, name, null);
					((JLabel)widget).setText(buff);
				}
			}
		}
		return true;
	}

	boolean sendText(String name, Container widget) throws IOException {
		String p = ((JTextField)widget).getText();
		con.sendPacketClass(PacketClass.ScreenData);
		ValueAttribute va = getValue(name);
		con.sendString(name + '.' + va.getValueName());
		con.sendStringData(va.getType(), p);
		return true;
	}

	boolean receiveText(Container widget) throws IOException {
		if (con.receiveDataType() == Type.RECORD) {
			int nItem = con.receiveInt();
			while (nItem-- != 0) {
				String name= con.receiveString();
				if ("state".equals(name)) { //$NON-NLS-1$
					int state = con.receiveIntData();
					setState(widget,state);
				} else if ("style".equals(name)) { //$NON-NLS-1$
					String buff = con.receiveStringData();
					/*gtk_widget_set_style(widget,GetStyle(buff));*/
				} else {
					String buff = con.receiveStringData();
					registerValue(widget, name, null);
					JTextComponent text = (JTextComponent)widget;
					text.setText(buff);
				}
			}
		}
		return true;
	}

	boolean sendButton(String name, Container widget) throws IOException {
		con.sendPacketClass(PacketClass.ScreenData);
		ValueAttribute va = getValue(name);
		con.sendString(name + '.' + va.getValueName());
		con.sendBooleanData(va.getType(), ((JButton)widget).isSelected());
		return true;
	}

	void setLabel(Container widget, String label) throws IOException {
		if (widget.getClass() == JLabel.class) {
			((JLabel)widget).setText(label);
		} else {
			Component[] comps = widget.getComponents();
			for (int i = 0; i < comps.length; i++) {
				if (comps[i] instanceof Container) {
					setLabel((Container)comps[i], label);
				}
			}
		}
	}

	boolean receiveButton(Container widget) throws IOException {
		if (con.receiveDataType() == Type.RECORD) {
			int nItem = con.receiveInt();
			while (nItem-- != 0) {
				String name = con.receiveString();
				if ("state".equals(name)) { //$NON-NLS-1$
					int state = con.receiveIntData();
					setState(widget, state);
				} else if ("style".equals(name)) { //$NON-NLS-1$
					String buff = con.receiveStringData();
					/*setStyle(widget,getStyle(buff));*/
				} else if ("label".equals(name)) { //$NON-NLS-1$
					String buff = con.receiveStringData();
					setLabel(widget, buff);
				} else {
					boolean fActive = con.receiveBooleanData();
					registerValue(widget, name, null);
					((JButton)widget).setSelected(fActive);
				}
			}
		}
		return true;
	}

	boolean receiveCombo(Container widget) throws IOException {
		con.receiveDataType(Type.RECORD);
		int nItem = con.receiveInt();
		int count = 0;
		while (nItem-- != 0) {
			String name = con.receiveString();
			if ("state".equals(name)) { //$NON-NLS-1$
				int state = con.receiveIntData();
				setState(widget, state);
			} else if ("style".equals(name)) { //$NON-NLS-1$
				String buff = con.receiveStringData();
				/* gtk_widget_set_style(widget,GetStyle(buff)); */
			} else if ("count".equals(name)) { //$NON-NLS-1$
				count = con.receiveIntData();
			} else if ("item".equals(name)) { //$NON-NLS-1$
				List list = new ArrayList();
				list.add(""); //$NON-NLS-1$
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
				StringBuffer widgetName = con.getWidgetNameBuffer();
				int offset = widgetName.length();
				widgetName.replace(offset, widgetName.length(), '.' + name);
				Container sub =  con.getInterface().getWidgetByLongName(widgetName.toString());
				if (sub != null) {
					receiveEntry(sub);
				} else {
					logger.warn(Messages.getString("WidgetMarshal.subwidget_not_found")); //$NON-NLS-1$
					/*	fatal error	*/
				}
			}
		}
		return true;
	}

	boolean receivePandaCombo(Container widget) throws IOException {
		return receiveCombo(widget);
	}

	boolean sendPandaCList(String name, Container widget) throws IOException {
		ValueAttribute va = getValue(name);
		JTable table = (JTable)widget;
		
		for (int i = 0, rows = table.getRowCount(); i < rows; i++) {
			String iname = name + '.' + va.getValueName() + '[' + i + (Integer)va.getOpt() + ']';
			con.sendPacketClass(PacketClass.ScreenData);
			con.sendString(iname);
			con.sendDataType(Type.BOOL);
			con.sendBoolean(table.isRowSelected(i));
		}
		return true;
	}

	boolean receivePandaCList(Container widget) throws IOException {
		con.receiveDataType(Type.RECORD);
		StringBuffer widgetName = con.getWidgetNameBuffer();
		int offset = widgetName.length();
		int nItem = con.receiveInt();
		int count = -1;
		int from = 0;
		int state;
		
		while (nItem-- != 0) {
			String name = con.receiveString();
			widgetName.replace(offset, widgetName.length(), '.' + name);
			Container subWidget = con.getInterface().getWidgetByLongName(widgetName.toString());
			if (subWidget != null) {
				con.receiveWidgetData(subWidget);
			} else if ("count".equals(name)) { //$NON-NLS-1$
				count = con.receiveIntData();
			} else if ("from".equals(name)) { //$NON-NLS-1$
				from = con.receiveIntData();
			} else if ("state".equals(name)) { //$NON-NLS-1$
				state = con.receiveIntData();
				setState(widget, state);
			} else if ("style".equals(name)) { //$NON-NLS-1$
				String buff = con.receiveStringData();
				/* gtk_widget_set_style(widget,GetStyle(buff));*/
			} else if ("row".equals(name)) { //$NON-NLS-1$
				/* NOP */
			} else if ("column".equals(name)) { //$NON-NLS-1$
				/* NOP */
			} else if ("item".equals(name)) { //$NON-NLS-1$
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
				registerValue(widget, name, new Integer(from));
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

	boolean sendCList(String name, Container widget) throws IOException {
		ValueAttribute va = getValue(name);
		JTable table = (JTable)widget;
		ListSelectionModel selections = table.getSelectionModel();
		for (int i = 0, rows = table.getRowCount(); i < rows; i++) {
			con.sendPacketClass(PacketClass.ScreenData);
			con.sendString(name + '.' + va.getValueName() + '[' + String.valueOf(i) + ']' + va.getOpt());
			con.sendDataType(Type.BOOL);
			con.sendBoolean(selections.isSelectedIndex(i));
		}
		return true;
	}

	boolean receiveCList(Container widget) throws IOException {
		JTable table = (JTable)widget;
		con.receiveDataType(Type.RECORD);
		StringBuffer widgetName = con.getWidgetNameBuffer();
		int offset = widgetName.length();
		int nItem = con.receiveInt();
		int count = -1;
		int from = 0;
		int state;
		while (nItem-- != 0) {
			String name = con.receiveString();
			widgetName.replace(offset, widgetName.length(), '.' + name);
			Container subWidget;
			if ((subWidget = con.getInterface().getWidgetByLongName(widgetName.toString())) != null) {
				con.receiveWidgetData(subWidget);
			} else if ("count".equals(name)) { //$NON-NLS-1$
				count = con.receiveIntData();
			} else if ("from".equals(name)) { //$NON-NLS-1$
				from = con.receiveIntData();
			} else if ("state".equals(name)) { //$NON-NLS-1$
				state = con.receiveIntData();
				setState(widget, state);
			} else if ("style".equals(name)) { //$NON-NLS-1$
				String buff = con.receiveStringData();
				/* gtk_widget_set_style(widget,GetStyle(buff)); */
			} else if ("row".equals(name)) { //$NON-NLS-1$
				/* NOP */
			} else if ("columns".equals(name)) { //$NON-NLS-1$
				/* NOP */
			} else if ("item".equals(name)) { //$NON-NLS-1$
				DefaultTableModel model = new DefaultTableModel();
				con.receiveDataType(Type.ARRAY);
				int num = con.receiveInt();
				if (count < 0) {
					count = num;
				}
				for (int j = 0; j < num; j++) {
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
				registerValue(widget, name, new Integer(from));
				int num = con.receiveInt();
				if (count < 0) {
					count = num;
				}
				for (int j = 0; j < num; j++) {
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

	boolean sendList(String name, Container widget) throws IOException {
		JList list = (JList)widget;
		ListSelectionModel model = list.getSelectionModel();
		ValueAttribute va = getValue(name);

		for (int i = 0, rows = list.getModel().getSize(); i < rows; i++) {
			con.sendPacketClass(PacketClass.ScreenData);
			con.sendString(name + '.' + va.getValueName() + '[' + (Integer)va.getOpt() + ']');
			con.sendDataType(Type.BOOL);
			con.sendBoolean(model.isSelectedIndex(i));
		}
		return true;
	}

	boolean receiveList(Container widget) throws IOException {
		Container item;
		int state;
		
		con.receiveDataType(Type.RECORD);
		StringBuffer label = con.getWidgetNameBuffer();
		int offset = label.length();
		int nItem = con.receiveInt();
		int count = -1;
		int from = 0;
		while (nItem-- != 0) {
			String name = con.receiveString();
			int num;
			if ("state".equals(name)) { //$NON-NLS-1$
				setState(widget,con.receiveIntData());
			} else if ("style".equals(name)) { //$NON-NLS-1$
				String buff = con.receiveStringData();
				/*gtk_widget_set_style(widget,GetStyle(buff));*/
			} else if ("count".equals(name)) { //$NON-NLS-1$
				count = con.receiveIntData();
			} else if ("from".equals(name)) { //$NON-NLS-1$
				from = con.receiveIntData();
			} else if ("item".equals(name)) { //$NON-NLS-1$
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
				registerValue(widget, name, new Integer(from));
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

	boolean sendCalendar(String name, Container calendar) throws IOException {
		String iName;
		int year, month, day;
		Date date = ((org.montsuqi.widgets.Calendar)calendar).getDate();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		con.sendPacketClass(PacketClass.ScreenData);
		con.sendString(name + ".year"); //$NON-NLS-1$
		con.sendDataType(Type.INT);
		con.sendInt(cal.get(java.util.Calendar.YEAR));

		con.sendPacketClass(PacketClass.ScreenData);
		con.sendString(name + ".month"); //$NON-NLS-1$
		con.sendDataType(Type.INT);
		con.sendInt(cal.get(java.util.Calendar.MONTH) + 1);

		con.sendPacketClass(PacketClass.ScreenData);
		con.sendString(name + ".day"); //$NON-NLS-1$
		con.sendDataType(Type.INT);
		con.sendInt(cal.get(java.util.Calendar.DATE));

		return true;
	}

	boolean receiveCalendar(Container widget) throws IOException {
		con.receiveDataType(Type.RECORD);
		registerValue(widget, "", null); //$NON-NLS-1$
		int nItem = con.receiveInt();
		int year = 0;
		int month = -1;
		int day = 0;
		while (nItem-- != 0) {
			String name = con.receiveString();
			if ("state".equals(name)) { //$NON-NLS-1$
				setState(widget, con.receiveIntData());
			} else if ("style".equals(name)) { //$NON-NLS-1$
				String buff = con.receiveStringData();
				/*gtk_widget_set_style(widget,GetStyle(buff));*/
			} else if ("year".equals(name)) { //$NON-NLS-1$
				year = con.receiveIntData();
			} else if ("month".equals(name)) { //$NON-NLS-1$
				month = con.receiveIntData();
			} else if ("day".equals(name)) { //$NON-NLS-1$
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

	boolean sendNotebook(String name, Container notebook) throws IOException {
		JTabbedPane tabbed = (JTabbedPane)notebook;
		ValueAttribute va = getValue(name);
		con.sendPacketClass(PacketClass.ScreenData);
		con.sendString(name + '.' + va.getValueName());
		con.sendIntegerData(va.getType(), tabbed.getSelectedIndex());
		return true;
	}

	boolean receiveNotebook(Container widget) throws IOException {
		int page = -1;
		int state;

		con.receiveDataType(Type.RECORD);
		int nItem = con.receiveInt();
		StringBuffer widgetName = con.getWidgetNameBuffer();
		int offset = widgetName.length();
		for (int i = 0; i < nItem; i++) {
			String name = con.receiveString();
			if ("state".equals(name)) { //$NON-NLS-1$
				setState(widget, con.receiveIntData());
			} else if ("style".equals(name)) { //$NON-NLS-1$
				String buff = con.receiveStringData();
				/* gtk_widget_set_style(widget,GetStyle(buff)); */
			} else if ("pageno".equals(name)) { //$NON-NLS-1$
				page = con.receiveIntData();
				registerValue(widget, name, null);
			} else {
				widgetName.replace(offset, widgetName.length(), '.' + name);
				con.receiveValue(widgetName, offset + name.length() + 1);
			}
		}
		if (page == -1) {
			throw new IllegalStateException(Messages.getString("WidgetMarshal.page_not_found")); //$NON-NLS-1$
		}
		JTabbedPane tabbed = (JTabbedPane)widget;
		tabbed.setSelectedIndex(page);
		return true;
	}

	boolean sendProgressBar(String name, Container widget) throws IOException {
		JProgressBar progress = (JProgressBar)widget;
		ValueAttribute va = getValue(name);
		con.sendPacketClass(PacketClass.ScreenData);
		con.sendString(name + '.' + va.getValueName());
		con.sendIntegerData(va.getType(), progress.getValue());
		return true;
	}

	boolean receiveProgressBar(Container widget) throws IOException {
		con.receiveDataType(Type.RECORD);
		int nItem = con.receiveInt();
		StringBuffer longName = con.getWidgetNameBuffer();
		int offset = longName.length();

		while (nItem-- != 0) {
			String name = con.receiveString();
			if ("state".equals(name)) { //$NON-NLS-1$
				setState(widget, con.receiveIntData());
			} else if ("style".equals(name)) { //$NON-NLS-1$
				String buff = con.receiveStringData();
				/* gtk_widget_set_style(widget,GetStyle(buff)); */
			} else if ("value".equals(name)) { //$NON-NLS-1$
				registerValue(widget, name, null);
				JProgressBar progress = (JProgressBar)widget;
				progress.setValue(con.receiveIntData());
			}
		}
		return true;
	}
}
