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

package org.montsuqi.client.marshallers;

import java.awt.Component;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.montsuqi.client.PacketClass;
import org.montsuqi.client.Protocol;
import org.montsuqi.client.Type;
import org.montsuqi.monsia.Interface;

class CListMarshaller extends WidgetMarshaller {

	private WidgetMarshaller labelMarshaller;

	CListMarshaller() {
		labelMarshaller = new LabelMarshaller();
	}

	public synchronized boolean receive(WidgetValueManager manager, Component widget) throws IOException {
		Protocol con = manager.getProtocol();
		JTable table = (JTable)widget;

		DefaultTableModel tableModel = (DefaultTableModel)table.getModel();
		TableColumnModel columnModel = table.getColumnModel();
		String[] labels = new String[columnModel.getColumnCount()];
		for (int i = 0, n = columnModel.getColumnCount(); i < n; i++) {
			labels[i] = (String)columnModel.getColumn(i).getHeaderValue();
		}

		con.receiveDataTypeWithCheck(Type.RECORD);
		StringBuffer widgetName = con.getWidgetNameBuffer();
		StringBuffer label = new StringBuffer(widgetName.toString());
		int offset = label.length();
		Interface xml = con.getInterface();
		for (int i = 0, n = con.receiveInt(), col = 0, count = -1, from = 0; i < n; i++) {
			String name = con.receiveString();
			label.replace(offset, label.length(), '.' + name);
			Component sub = xml.getWidgetByLongName(label.toString());
			if (sub != null) {
				JLabel dummyLabel = (JLabel)sub;
				labelMarshaller.receive(manager, dummyLabel);
				labels[col++] = dummyLabel.getText();
			} else if (handleStateStyle(manager, widget, name)) {
				continue;
			} else if ("count".equals(name)) { //$NON-NLS-1$
				count = con.receiveIntData();
			} else if ("from".equals(name)) { //$NON-NLS-1$
				from = con.receiveIntData();
			} else if ("row".equals(name)) { //$NON-NLS-1$
				/* NOP */
			} else if ("columns".equals(name)) { //$NON-NLS-1$
				/* NOP */
			} else if ("item".equals(name)) { //$NON-NLS-1$
				while (tableModel.getRowCount() > 0) {
					tableModel.removeRow(0);
				}
				con.receiveDataTypeWithCheck(Type.ARRAY);
				int num = con.receiveInt();
				if (count < 0) {
					count = num;
				}
				for (int j = 0; j < num; j++) {
					con.receiveDataTypeWithCheck(Type.RECORD);
					int rNum = con.receiveInt();
					Object[] rdata = new String[rNum];
					for (int k = 0; k < rNum; k++) {
						String dummy = con.receiveString();
						rdata[k] = con.receiveStringData();
					}
					if (j >= from && j - from < count) {
						tableModel.addRow(rdata);
					}
				}
			} else {
				con.receiveDataTypeWithCheck(Type.ARRAY);
				manager.registerValue(widget, name, new Integer(from));
				int num = con.receiveInt();
				if (count < 0) {
					count = num;
				}
				for (int j = 0; j < num; j++) {
					boolean selected = con.receiveBooleanData();
					if (j >= from && j - from < count) {
						if (selected) {
							table.addRowSelectionInterval(j - from, j - from);
						} else {
							table.removeRowSelectionInterval(j - from, j - from);
						}
					}
				}
			}
		}
		tableModel.setColumnIdentifiers(labels);
		//con.setReceiving(false);
		//con.addChangedWidget(widget, null);
		//con.setReceiving(true);
		return true;
	}

	public synchronized boolean send(WidgetValueManager manager, String name, Component widget) throws IOException {
		Protocol con = manager.getProtocol();
		JTable table = (JTable)widget;
		ValueAttribute va = manager.getValue(name);
		ListSelectionModel selections = table.getSelectionModel();
		for (int i = 0, rows = table.getRowCount(), opt = ((Integer)va.getOpt()).intValue(); i < rows; i++) {
			con.sendPacketClass(PacketClass.ScreenData);
			String iName = name + '.' + va.getVName() + '[' + String.valueOf(i + opt) + ']';
			con.sendString(iName);
			con.sendDataType(Type.BOOL);
			boolean selected = selections.isSelectedIndex(i);
			con.sendBoolean(selected);
		}
		return true;
	}
}

