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
import java.awt.Container;
import java.io.IOException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.montsuqi.client.PacketClass;
import org.montsuqi.client.Protocol;
import org.montsuqi.client.Type;
import org.montsuqi.monsia.Interface;
import org.montsuqi.util.SafeColorDecoder;
import org.montsuqi.widgets.PandaCList;

/**
 * <p>A class to send/receive CList data.</p>
 */
class CListMarshaller extends WidgetMarshaller {

    public synchronized void receive(WidgetValueManager manager, Component widget) throws IOException {
        Protocol con = manager.getProtocol();
        JTable table = (JTable) widget;
        PandaCList clist = (PandaCList) widget;

        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();

        con.receiveDataTypeWithCheck(Type.RECORD);
        StringBuffer widgetName = con.getWidgetNameBuffer();
        StringBuffer label = new StringBuffer(widgetName.toString());
        int offset = label.length();
        Interface xml = con.getInterface();
        int row = 1;
        double rowattrw = 0.0;
        int count = -1;
        for (int i = 0, n = con.receiveInt(); i < n; i++) {
            String name = con.receiveName();
            label.replace(offset, label.length(), '.' + name);
            Component sub = xml.getWidgetByLongName(label.toString());
            if (sub != null) {
                con.receiveValue(label, offset + 1 + name.length());
            } else if (handleCommonAttribute(manager, widget, name)) {
                //
            } else if ("count".equals(name)) { 
                count = con.receiveIntData();
            } else if ("row".equals(name)) { 
                row = con.receiveIntData();
                int row2 = row > 1 ? row - 1 : 0;
                clist.changeSelection(row2, 0, false, false);
            } else if ("rowattr".equals(name)) { 
                int rowattr = con.receiveIntData();
                switch (rowattr) {
                    case 1: // DOWN
                        rowattrw = 1.0;
                        break;
                    case 2: // MIDDLE
                        rowattrw = 0.5;
                        break;
                    case 3: // QUATER
                        rowattrw = 0.25;
                        break;
                    case 4: // THREE QUATER
                        rowattrw = 0.75;
                        break;
                    default:
                        rowattrw = 0.0; // [0] TOP
                        break;
                }
            } else if ("column".equals(name)) { 
				/*
                 * int dummy =
                 */ con.receiveIntData();
            } else if ("item".equals(name)) { 
                while (tableModel.getRowCount() > 0) {
                    tableModel.removeRow(0);
                }
                con.receiveDataTypeWithCheck(Type.ARRAY);
                int num = con.receiveInt();
                if (count < 0) {
                    count = num;
                }
                for (int j = 0; j < num; j++) {
                    con.receiveDataTypeWithCheck(Type.RECORD);
                    int rows = con.receiveInt();
                    Object[] rdata = new String[rows];
                    for (int k = 0; k < rows; k++) {
                        /*
                         * String dummy =
                         */ con.receiveString();
                        rdata[k] = con.receiveStringData();
                    }
                    if (j < count) {
                        tableModel.addRow(rdata);
                    }
                }
            } else if ("bgcolor".equals(name)) {
                Color[] bgcolors;
                con.receiveDataTypeWithCheck(Type.ARRAY);
                int num = con.receiveInt();
                bgcolors = new Color[num];
                for (int j = 0; j < num; j++) {
                    Color color = SafeColorDecoder.decode(con.receiveStringData());
                    bgcolors[j] = color != null ? color : Color.WHITE;
                }
                clist.setBGColors(bgcolors);
            } else if ("fgcolor".equals(name)) {
                Color[] fgcolors;
                con.receiveDataTypeWithCheck(Type.ARRAY);
                int num = con.receiveInt();
                fgcolors = new Color[num];
                for (int j = 0; j < num; j++) {
                    Color color = SafeColorDecoder.decode(con.receiveStringData());
                    fgcolors[j] = color != null ? color : Color.BLACK;
                }
                clist.setFGColors(fgcolors);
            } else {
                con.receiveDataTypeWithCheck(Type.ARRAY);
                manager.registerAttribute(widget, name, null);
                int num = con.receiveInt();
                if (count < 0) {
                    count = num;
                }
                for (int j = 0; j < num; j++) {
                    boolean selected = con.receiveBooleanData();
                    if (j < count) {
                        clist.setSelection(j,selected);
                        if (clist.getMode() == PandaCList.SELECTION_MODE_MULTI && selected) {
                            clist.changeSelection(j, 0, false, false);
                        }
                    }
                }
            }
        }

        JScrollBar vScroll = getVerticalScrollBar(table);
        if (vScroll != null) {
            BoundedRangeModel model = vScroll.getModel();
            int max = model.getMaximum();
            int min = model.getMinimum();
            if (row <= 0) {
                row = 1;
            }
            if (count > 0) {
                int value = (int) (((row - 1) * 1.0 / count) * (max - min)) + min;
                if (rowattrw == 1.0) {
                    value += (int) ((1.0 / count) * (max - min));
                }
                value -= rowattrw * model.getExtent();
                if (value < 0) {
                    value = 0;
                }
                model.setValue(value);
            } else {
                model.setValue(0);
            }
        }
        widget.setVisible(true);
    }

    public synchronized void send(WidgetValueManager manager, String name, Component widget) throws IOException {
        Protocol con = manager.getProtocol();
        JTable table = (JTable) widget;
        PandaCList clist = (PandaCList)widget;
        ValueAttribute va = manager.getAttribute(name);
        boolean visibleRow = false;
        int rows = table.getRowCount();
        for (int i = 0; i < rows; i++) {
            con.sendPacketClass(PacketClass.ScreenData);
            con.sendName(va.getValueName() + '.' + va.getNameSuffix() + '[' + String.valueOf(i) + ']');
            con.sendBooleanData(Type.BOOL,clist.getSelection(i));
            if (!visibleRow && isVisibleRow(table, i)) {
                con.sendPacketClass(PacketClass.ScreenData);
                con.sendName(va.getValueName() + ".row"); 
                con.sendIntegerData(Type.INT, i + 1);
                visibleRow = true;
            }
        }
    }

    private boolean isVisibleRow(JTable table, int row) {
        JScrollBar vScroll = getVerticalScrollBar(table);
        if (vScroll == null) {
            return true;
        }
        BoundedRangeModel model = vScroll.getModel();
        int rowHeight = table.getRowHeight() * row - model.getValue();
        return 0 <= rowHeight && rowHeight < model.getExtent();
    }

    private JScrollBar getVerticalScrollBar(JTable table) {
        Container parent = table.getParent();
        if (parent instanceof JViewport) {
            parent = parent.getParent();
            if (parent instanceof JScrollPane) {
                JScrollPane scroll = (JScrollPane) parent;
                return scroll.getVerticalScrollBar();
            }
        }
        return null;
    }
}
