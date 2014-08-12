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
import javax.swing.JTextField;
import org.montsuqi.client.PacketClass;
import org.montsuqi.client.Protocol;
import org.montsuqi.client.Type;
import org.montsuqi.monsia.Interface;
import org.montsuqi.widgets.FileEntry;

/**
 * <p>A class to send/receive FileEntry data.</p>
 */
class FileEntryMarshaller extends WidgetMarshaller {

    private WidgetMarshaller entryMarshaller;

    FileEntryMarshaller() {
        entryMarshaller = new EntryMarshaller();
    }

    public synchronized void receive(WidgetValueManager manager, Component widget) throws IOException {
        Protocol con = manager.getProtocol();
        FileEntry fe = (FileEntry) widget;
        Interface xml = con.getInterface();

        byte[] binary = null;
        boolean entryReceived = false;
        con.receiveDataTypeWithCheck(Type.RECORD);
        for (int i = 0, n = con.receiveInt(); i < n; i++) {
            String name = con.receiveName();
            if ("objectdata".equals(name)) { //$NON-NLS-1$
                binary = con.receiveBinaryData();
                manager.registerAttribute(widget, "objectdata", null);
            } else if (handleCommonAttribute(manager, widget, name)) {
                continue;
            } else {
                StringBuffer widgetName = con.getWidgetNameBuffer();
                int offset = widgetName.length();
                widgetName.replace(offset, widgetName.length(), '.' + name);
                Component sub = xml.getWidgetByLongName(widgetName.toString());
                if (sub != null) {
                    JTextField dummy = (JTextField) sub;
                    entryMarshaller.receive(manager, dummy);
                    entryReceived = true;
                } else {
                    throw new WidgetMarshallingException("subwidget not found"); //$NON-NLS-1$
                }
            }
        }
        if (binary != null && binary.length > 0) {
            fe.setSaveAction();
            fe.setData(binary);
            if (entryReceived) {
                fe.getBrowseButton().doClick();
            }
        } else {
            fe.setLoadAction();
        }
    }

    public synchronized void send(WidgetValueManager manager, String name, Component widget) throws IOException {
        Protocol con = manager.getProtocol();
        FileEntry fe = (FileEntry) widget;
        byte[] binary = fe.loadData();
        if (binary.length <= 0) {
            return;
        }
        con.sendPacketClass(PacketClass.ScreenData);
        ValueAttribute va = manager.getAttribute(name);
        con.sendName(va.getValueName() + '.' + va.getNameSuffix());
        con.sendBinaryData(va.getType(), binary);
    }
}
