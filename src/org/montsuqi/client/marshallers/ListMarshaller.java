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

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import org.montsuqi.client.PacketClass;
import org.montsuqi.client.Protocol;
import org.montsuqi.client.Type;

class ListMarshaller extends WidgetMarshaller {
	
	public synchronized void receive(WidgetValueManager manager, Component widget) throws IOException {
		Protocol con = manager.getProtocol();
		JList list = (JList)widget;

		DefaultListModel listModel = (DefaultListModel)list.getModel();

		StringBuffer label = con.getWidgetNameBuffer();
		int offset = label.length();

		con.receiveDataTypeWithCheck(Type.RECORD);

		for (int i = 0, n = con.receiveInt(), count = -1, from = 0; i < n; i++) {
			String name = con.receiveName();
			if (handleStateStyle(manager, widget, name)) {
				continue;
			}
			if ("count".equals(name)) { //$NON-NLS-1$
				count = con.receiveIntData();
			} else if ("from".equals(name)) { //$NON-NLS-1$
				from = con.receiveIntData();
			} else if ("item".equals(name)) { //$NON-NLS-1$
				if (listModel.getSize() > 0) {
					listModel.clear();
				}
				con.receiveDataTypeWithCheck(Type.ARRAY);
				int num = con.receiveInt();
				if (count < 0) {
					count = num;
				}
				for (int j = 0; j < num; j++) {
					String buff = con.receiveStringData();
					if (buff != null) {
						if (j >= from && j - from < count) {
							listModel.addElement(buff);
						}
					}
				}
			} else {
				con.receiveDataTypeWithCheck(Type.ARRAY);
				manager.registerValue(widget, name, new Integer(from));
				int num = con.receiveInt();
				if (count < 0) {
					count = num;
				}
				ListSelectionModel model = list.getSelectionModel();
				for (int j = 0; j < num; j++) {
					boolean selected = con.receiveBooleanData();
					if (j >= from && j - from < count) {
						if (selected) {
							model.addSelectionInterval(j, j);
						} else {
							model.removeSelectionInterval(j, j);
						}
					}
				}
			}
		}
	}

	public synchronized void send(WidgetValueManager manager, String name, Component widget) throws IOException {
		Protocol con = manager.getProtocol();
		JList list = (JList)widget;

		ListSelectionModel model = list.getSelectionModel();
		ValueAttribute va = manager.getValue(name);
		int opt = ((Integer)va.getOpt()).intValue();
		for (int i = 0, n = list.getModel().getSize(); i < n; i++) {
			con.sendPacketClass(PacketClass.ScreenData);
			con.sendName(va.getValueName() + '.' + va.getNameSuffix() + '[' + (i + opt) + ']');
			con.sendBooleanData(Type.BOOL, model.isSelectedIndex(i));
		}
	}
}

