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
import java.math.BigDecimal;

import org.montsuqi.widgets.NumberEntry;

class NumberEntryMarshaller extends WidgetMarshaller {

	synchronized boolean receive(WidgetValueManager manager, Component widget) throws IOException {
		Protocol con = manager.getProtocol();
		NumberEntry entry = (NumberEntry)widget;
		con.receiveDataTypeWithCheck(Type.RECORD);
		int nItem = con.receiveInt();
		while (nItem-- != 0) {
			String name = con.receiveString();
			if (handleCommon(manager, widget, name)) {
				continue;
			}
			String buff = con.getWidgetNameBuffer().toString() + '.' + name;
			ValueAttribute va = manager.getValue(buff);
			BigDecimal val = con.receiveFixedData();
			manager.registerValue(entry, name, val);
			entry.setValue(val);
		}
		return true;
	}

	synchronized boolean send(WidgetValueManager manager, String name, Component widget) throws IOException {
		Protocol con = manager.getProtocol();
		NumberEntry entry = (NumberEntry)widget;
		BigDecimal value = entry.getValue();
		con.sendPacketClass(PacketClass.ScreenData);
		ValueAttribute va = manager.getValue(name);
		con.sendString(name + '.' + va.getKey());
		//va.setOpt(value);
		con.sendFixedData(va.getType(), value);
		return true;
	}
}

