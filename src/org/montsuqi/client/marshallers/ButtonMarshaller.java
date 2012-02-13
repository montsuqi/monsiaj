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
import javax.swing.AbstractButton;
import org.montsuqi.client.PacketClass;
import org.montsuqi.client.Protocol;
import org.montsuqi.client.Type;

/**
 * <p>A class to send/receive Button data.</p>
 */
class ButtonMarshaller extends WidgetMarshaller {

    public synchronized void receive(WidgetValueManager manager, Component widget) throws IOException {
        Protocol con = manager.getProtocol();
        AbstractButton button = (AbstractButton) widget;
        con.receiveDataTypeWithCheck(Type.RECORD);
        for (int i = 0, n = con.receiveInt(); i < n; i++) {
            String name = con.receiveName();
            if (handleCommonAttribute(manager, widget, name)) {
                continue;
            }
            if ("label".equals(name)) { //$NON-NLS-1$
                String label = con.receiveStringData();
                button.setText(label);
            } else {
                boolean selected = con.receiveBooleanData();
                manager.registerValue(button, name, null);
                button.setSelected(selected);
            }
        }
    }

    public synchronized void send(WidgetValueManager manager, String name, Component widget) throws IOException {
        Protocol con = manager.getProtocol();
        AbstractButton button = (AbstractButton) widget;

        con.sendPacketClass(PacketClass.ScreenData);
        ValueAttribute va = manager.getValue(name);
        con.sendName(va.getValueName() + '.' + va.getNameSuffix());
        boolean selected = button.isSelected();
        con.sendBooleanData(va.getType(), selected);
    }
}
