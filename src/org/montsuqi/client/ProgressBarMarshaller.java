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
import java.io.IOException;

import javax.swing.JProgressBar;


class ProgressBarMarshaller extends WidgetMarshaller {

	synchronized boolean receive(WidgetValueManager manager, Component widget) throws IOException {
		Protocol con = manager.getProtocol();
		JProgressBar progress = (JProgressBar)widget;
		con.receiveDataTypeWithCheck(Type.RECORD);
		int nItem = con.receiveInt();
		StringBuffer longName = con.getWidgetNameBuffer();
		int offset = longName.length();
	
		while (nItem-- != 0) {
			String name = con.receiveString();
			if (handleCommon(manager, widget, name)) {
				continue;
			} else if ("value".equals(name)) { //$NON-NLS-1$
				manager.registerValue(widget, name, null);
				progress.setValue(con.receiveIntData());
			}
		}
		return true;
	}

	synchronized boolean send(WidgetValueManager manager, String name, Component widget) throws IOException {
		Protocol con = manager.getProtocol();
		JProgressBar progress = (JProgressBar)widget;
		ValueAttribute va = manager.getValue(name);
		con.sendPacketClass(PacketClass.ScreenData);
		con.sendString(name + '.' + va.getValueName());
		con.sendIntegerData(va.getType(), progress.getValue());
		return true;
	}
}
