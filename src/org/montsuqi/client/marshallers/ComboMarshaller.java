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
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import org.montsuqi.client.Protocol;
import org.montsuqi.client.Type;
import org.montsuqi.monsia.Interface;

/**
 * <p>A class to send/receive Combo data.</p>
 */
class ComboMarshaller extends WidgetMarshaller {

    private final WidgetMarshaller entryMarshaller;

    ComboMarshaller() {
        entryMarshaller = new EntryMarshaller();
    }

    @Override
    public synchronized void receive(WidgetValueManager manager, Component widget) throws IOException {
        Protocol con = manager.getProtocol();
        JComboBox combo = (JComboBox)widget;
        Component sub = null;

        DefaultComboBoxModel<String> model = (DefaultComboBoxModel)combo.getModel();
        Interface xml = con.getInterface();
        con.receiveDataTypeWithCheck(Type.RECORD);
        String selectedItem = null;

        int count = 0;
        for (int i = 0, n = con.receiveInt(); i < n; i++) {
            String name = con.receiveName();
            if (handleCommonAttribute(manager, widget, name)) {
            } else if ("count".equals(name)) { 
                count = con.receiveIntData();
            } else if ("item".equals(name)) { 
                List<String> list = new ArrayList<>();
                list.add(""); 
                con.receiveDataTypeWithCheck(Type.ARRAY);
                for (int j = 0, num = con.receiveInt(); j < num; j++) {
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
                model.removeAllElements();
                Iterator iter = list.iterator();
                while (iter.hasNext()) {
                    model.addElement((String)iter.next());
                }
            } else {
                StringBuffer widgetName = con.getWidgetNameBuffer();
                int offset = widgetName.length();
                widgetName.replace(offset, widgetName.length(), '.' + name);
                sub = xml.getWidgetByLongName(widgetName.toString());
                if (sub != null) {
                    JTextField dummy = (JTextField) sub;
                    entryMarshaller.receive(manager, dummy);
                    selectedItem = dummy.getText();
                } else {
                    throw new WidgetMarshallingException("subwidget not found"); 
                }
            }
        }
        if (selectedItem != null) {
            combo.setSelectedItem(selectedItem);
            widget.dispatchEvent(new KeyEvent(sub, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_UNDEFINED, KeyEvent.CHAR_UNDEFINED));
        }
    }

    public synchronized void send(WidgetValueManager manager, String name, Component widget) throws IOException {
        assert false : "should never be called."; 
    }
}
