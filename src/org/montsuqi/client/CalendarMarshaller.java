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
import java.util.Calendar;
import java.util.Date;

class CalendarMarshaller extends WidgetMarshaller {

	synchronized 	boolean receive(WidgetValueManager manager, Component widget) throws IOException {
		Protocol con = manager.getProtocol();
		con.receiveDataTypeWithCheck(Type.RECORD);
		manager.registerValue(widget, "", null); //$NON-NLS-1$
		int nItem = con.receiveInt();
		int year = 0;
		int month = -1;
		int day = 0;
		while (nItem-- != 0) {
			String name = con.receiveString();
			if (handleCommon(manager, widget, name)) {
				continue;
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
	
	synchronized boolean send(WidgetValueManager manager, String name, Component calendar) throws IOException {
		Protocol con = manager.getProtocol();
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
}

