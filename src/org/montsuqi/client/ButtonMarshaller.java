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
import java.io.IOException;

import javax.swing.AbstractButton;
import javax.swing.JLabel;

class ButtonMarshaller extends WidgetMarshaller {
	
	synchronized boolean receive(WidgetValueManager manager, Component widget) throws IOException {
		Protocol con = manager.getProtocol();
		AbstractButton button = (AbstractButton)widget;
		con.receiveDataTypeWithCheck(Type.RECORD);
		int nItem = con.receiveInt();
		while (nItem-- != 0) {
			String name = con.receiveString();
			if (handleCommon(manager, widget, name)) {
				continue;
			}
			if ("label".equals(name)) { //$NON-NLS-1$
				String buff = con.receiveStringData();
				setLabel(button, buff);
			} else {
				boolean fActive = con.receiveBooleanData();
				manager.registerValue(button, name, null);
				button.setSelected(fActive);
			}
		}
		return true;
	}

	synchronized boolean send(WidgetValueManager manager, String name, Component widget) throws IOException {
		Protocol con = manager.getProtocol();
		AbstractButton button = (AbstractButton)widget;
		con.sendPacketClass(PacketClass.ScreenData);
		ValueAttribute va = manager.getValue(name);
		con.sendString(name + '.' + va.getValueName());
		con.sendBooleanData(va.getType(), button.isSelected());
		return true;
	}

	private void setLabel(Component widget, String value) throws IOException {
		if (widget instanceof JLabel) {
			JLabel label = (JLabel)widget;
			label.setText(value);
		} else if (widget instanceof Container) {
			Container c = (Container)widget;
			Component[] comps = c.getComponents();
			for (int i = 0; i < comps.length; i++) {
				setLabel(comps[i], value);
			}
		}
	}
}

