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

import java.awt.Color;
import java.awt.Component;
import java.io.IOException;

import org.montsuqi.client.Protocol;
import org.montsuqi.client.Type;
import org.montsuqi.util.GtkStockIcon;
import org.montsuqi.util.PopupNotify;
import org.montsuqi.util.SafeColorDecoder;

/** <p>A class to send/receive Window data.</p>
 */
public class WindowMarshaller extends WidgetMarshaller {

    public void receive(WidgetValueManager manager, Component widget) throws IOException {
        Protocol con = manager.getProtocol();
        String summary, body, icon;

        int timeout = 0;
        summary = body = icon = null;
        StringBuffer widgetName = con.getWidgetNameBuffer();
        int offset = widgetName.length();
        con.receiveDataTypeWithCheck(Type.RECORD);
        for (int i = 0, n = con.receiveInt(); i < n; i++) {
            String name = con.receiveName();
            /*
            if (handleCommonAttribute(manager, widget, name)) {
                continue;
            }
             */
            if ("title".equals(name)) { 
                String title = con.receiveStringData();
                con.setSessionTitle(title);
            } else if ("bgcolor".equals(name)) {                
                String bgcolor = con.receiveStringData();
                Color color = SafeColorDecoder.decode(bgcolor);                
                con.setSessionBGColor(color);
            } else if ("popup_summary".equals(name)) {
                summary = con.receiveStringData();
            } else if ("popup_body".equals(name)) {
                body = con.receiveStringData();
            } else if ("popup_icon".equals(name)) {
                icon = con.receiveStringData();
            } else if ("popup_timeout".equals(name)) {
                timeout = con.receiveIntData();
            } else {
                widgetName.replace(offset, widgetName.length(), '.' + name);
                con.receiveValue(widgetName, offset + name.length() + 1);
            }
        }
        if (summary != null && summary.length() > 0
                && body != null && body.length() > 0) {
            PopupNotify.popup(summary, body, GtkStockIcon.get(icon), timeout);
        }
    }

    public void send(WidgetValueManager manager, String name, Component widget) throws IOException {
        // do nothing
    }
}
