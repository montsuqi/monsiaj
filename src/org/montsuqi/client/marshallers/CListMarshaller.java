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

class CListMarshaller extends WidgetMarshaller {

	public synchronized boolean receive(WidgetValueManager manager, Component widget) throws IOException {
		Protocol con = manager.getProtocol();
		JTable table = (JTable)widget;
		DefaultTableModel tableModel = (DefaultTableModel)table.getModel();
		con.receiveDataTypeWithCheck(Type.RECORD);
		int state;
		TableColumnModel columnModel = table.getColumnModel();
		String[] labels = new String[columnModel.getColumnCount()];
		int labelNumber = 0;
		for (int i = 0, n = columnModel.getColumnCount(); i < n; i++) {
			labels[i] = (String)columnModel.getColumn(i).getHeaderValue();
		}

		for (int i = 0, n = con.receiveInt(), count = -1, from = 0; i < n; i++) {
			String name = con.receiveString();
			StringBuffer widgetName = con.getWidgetNameBuffer();
			int offset = widgetName.length();
			widgetName.replace(offset, widgetName.length(), '.' + name);
			Component sub;
			if ((sub = con.getInterface().getWidgetByLongName(widgetName.toString())) != null) {
				JLabel dummyLabel = (JLabel)sub;
				WidgetMarshaller labelMarshaller = new LabelMarshaller();
				labelMarshaller.receive(manager, dummyLabel);
				labels[labelNumber++] = dummyLabel.getText();
			} else if (handleCommon(manager, widget, name)) {
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
					ListSelectionModel model = table.getSelectionModel();
					boolean selected = con.receiveBooleanData();
					if (j >= from && j - from < count) {
						if (selected) {
							model.addSelectionInterval(j - from, j - from);
						} else {
							model.removeSelectionInterval(j - from, j - from);
						}
					}
				}
			}
		}
		tableModel.setColumnIdentifiers(labels);
		con.setReceiving(false);
		con.addChangedWidget(widget, null);
		con.setReceiving(true);
		return true;
	}

	public synchronized boolean send(WidgetValueManager manager, String name, Component widget) throws IOException {
		Protocol con = manager.getProtocol();
		ValueAttribute va = manager.getValue(name);
		JTable table = (JTable)widget;
		ListSelectionModel selections = table.getSelectionModel();
		int opt = ((Integer)va.getOpt()).intValue();
		for (int i = 0, rows = table.getRowCount(); i < rows; i++) {
			con.sendPacketClass(PacketClass.ScreenData);
			con.sendString(name + '.' + va.getVName() + '[' + String.valueOf(i) + ']' + (i + opt));
			con.sendDataType(Type.BOOL);
			con.sendBoolean(selections.isSelectedIndex(i));
		}
		return true;
	}
}

