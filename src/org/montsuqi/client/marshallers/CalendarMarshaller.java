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
import java.util.Calendar;
import java.util.Date;

import org.montsuqi.client.PacketClass;
import org.montsuqi.client.Protocol;
import org.montsuqi.client.Type;

class CalendarMarshaller extends WidgetMarshaller {

	public synchronized void receive(WidgetValueManager manager, Component widget) throws IOException {
		Protocol con = manager.getProtocol();
		org.montsuqi.widgets.Calendar calendarWidget = (org.montsuqi.widgets.Calendar)widget;

		con.receiveDataTypeWithCheck(Type.RECORD);
		manager.registerValue(widget, "", null); //$NON-NLS-1$

		Calendar calendar = Calendar.getInstance();
		int year = 0;
		for (int i = 0, n = con.receiveInt(); i < n; i++) {
			String name = con.receiveName();
			if (handleStateStyle(manager, widget, name)) {
				continue;
			} else if ("year".equals(name)) { //$NON-NLS-1$
				year = con.receiveIntData();
				calendar.set(Calendar.YEAR, year);
			} else if ("month".equals(name)) { //$NON-NLS-1$
				int month = con.receiveIntData();
				calendar.set(Calendar.MONTH, month - 1);
			} else if ("day".equals(name)) { //$NON-NLS-1$
				int day = con.receiveIntData();
				calendar.set(Calendar.DATE, day);
			} else {
				throw new WidgetMarshallingException();
			}
		}

		if (year > 0) {
			Date date = calendar.getTime();
			calendarWidget.setDate(date);
		}
	}

	public synchronized void send(WidgetValueManager manager, String name, Component widget) throws IOException {
		Protocol con = manager.getProtocol();
		org.montsuqi.widgets.Calendar calendarWidget = (org.montsuqi.widgets.Calendar)widget;
		Date date = calendarWidget.getDate();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		ValueAttribute va = manager.getValue(name);

		con.sendPacketClass(PacketClass.ScreenData);
		con.sendName(va.getValueName() + ".year"); //$NON-NLS-1$
		con.sendIntegerData(Type.INT, cal.get(java.util.Calendar.YEAR));

		con.sendPacketClass(PacketClass.ScreenData);
		con.sendName(va.getValueName() + ".month"); //$NON-NLS-1$
		con.sendIntegerData(Type.INT, cal.get(java.util.Calendar.MONTH) + 1);

		con.sendPacketClass(PacketClass.ScreenData);
		con.sendName(va.getValueName() + ".day"); //$NON-NLS-1$
		con.sendIntegerData(Type.INT, cal.get(java.util.Calendar.DATE));
	}
}

