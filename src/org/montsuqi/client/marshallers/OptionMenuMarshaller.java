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

package org.montsuqi.client.marshallers;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import org.montsuqi.client.PacketClass;
import org.montsuqi.client.Protocol;
import org.montsuqi.client.Type;
import org.montsuqi.widgets.OptionMenu;

/** <p>A class to send/receive NumberEntry data.</p>
 */
public class OptionMenuMarshaller extends WidgetMarshaller {

	public void receive(WidgetValueManager manager, Component widget) throws IOException {
		Protocol con = manager.getProtocol();
		OptionMenu optionMenu = (OptionMenu)widget;

		con.receiveDataTypeWithCheck(Type.RECORD);

		int count = -1;
		int choice = 0;

		for (int i = 0, n = con.receiveInt(); i < n; i++) {
			String name = con.receiveName();
			if (handleStateStyle(manager, widget, name)) {
				continue;
			} else if ("count".equals(name)) { //$NON-NLS-1$
				count = con.receiveIntData();
			} else if ("select".equals(name)) { //$NON-NLS-1$
				manager.registerValue(widget, name, null);
				choice = con.receiveIntData();
			} else if ("item".equals(name)) { //$NON-NLS-1$
				con.receiveDataTypeWithCheck(Type.ARRAY);
				int num = con.receiveInt();
				if (count < 0) {
					count = num;
				}
				List list = new ArrayList();
				for (int j = 0; j < num ; j++) {
					try {
						String buff = con.receiveStringData();
						if (buff != null) {
							if (j < count) {
								list.add(buff);
							}
						}
					} catch (IllegalArgumentException e) {
						logger.warn(e.getMessage());
					}
				}
				DefaultComboBoxModel model = new DefaultComboBoxModel();
				Iterator iter = list.iterator();
				while (iter.hasNext()) {
					model.addElement(iter.next());
				}
				optionMenu.setModel(model);
			} else {
				StringBuffer widgetName = con.getWidgetNameBuffer();
				int offset = widgetName.length();
				widgetName.replace(offset, widgetName.length(), '.' + name);
				con.receiveValue(widgetName, offset);
			}
		}
		if (0 <= choice && choice < optionMenu.getItemCount()) {
			optionMenu.setSelectedIndex(choice);
		}
	}

	public void send(WidgetValueManager manager, String name, Component widget) throws IOException {
		Protocol con = manager.getProtocol();
		OptionMenu optionMenu = (OptionMenu)widget;

		ValueAttribute va = manager.getValue(name);
		con.sendPacketClass(PacketClass.ScreenData);
		con.sendName(va.getValueName() + '.' + va.getNameSuffix());
		con.sendIntegerData(va.getType(), optionMenu.getSelectedIndex());
	}
}
