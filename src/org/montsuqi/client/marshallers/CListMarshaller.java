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
import java.awt.Container;
import java.io.IOException;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.montsuqi.client.PacketClass;
import org.montsuqi.client.Protocol;
import org.montsuqi.client.Type;
import org.montsuqi.monsia.Interface;

/** <p>A class to send/receive CList data.</p>
 */
class CListMarshaller extends WidgetMarshaller {

    public synchronized void receive(WidgetValueManager manager, Component widget) throws IOException {
        Protocol con = manager.getProtocol();
        JTable table = (JTable) widget;

        widget.setVisible(false);
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        TableColumnModel columnModel = table.getColumnModel();
        ListSelectionModel selections = table.getSelectionModel();
        String[] labels = new String[columnModel.getColumnCount()];
        for (int i = 0, n = columnModel.getColumnCount(); i < n; i++) {
            labels[i] = (String) columnModel.getColumn(i).getHeaderValue();
        }
        con.receiveDataTypeWithCheck(Type.RECORD);
        StringBuffer widgetName = con.getWidgetNameBuffer();
        StringBuffer label = new StringBuffer(widgetName.toString());
        int offset = label.length();
        Interface xml = con.getInterface();
        int row = 1;
        double rowattrw = 0.0;
        int count = -1;
        int from = 0;
        for (int i = 0, n = con.receiveInt(); i < n; i++) {
            String name = con.receiveName();
            label.replace(offset, label.length(), '.' + name);
            Component sub = xml.getWidgetByLongName(label.toString());
            if (sub != null) {
                con.receiveValue(label, offset + 1 + name.length());
            } else if (handleStateStyle(manager, widget, name)) {
                continue;
            } else if ("count".equals(name)) { //$NON-NLS-1$
                count = con.receiveIntData();
            } else if ("from".equals(name)) { //$NON-NLS-1$
                from = con.receiveIntData();
            } else if ("row".equals(name)) { //$NON-NLS-1$
                row = con.receiveIntData();                
            } else if ("rowattr".equals(name)) { //$NON-NLS-1$
                int rowattr = con.receiveIntData();
                switch (rowattr) {
                    case 1: // DOWN
                        rowattrw = 1.0;
                        break;
                    case 2: // MIDDLE
                        rowattrw = 0.5;
                        break;
                    default:
                        rowattrw = 0.0; // [0] TOP
                        break;
                }
            } else if ("column".equals(name)) { //$NON-NLS-1$
				/* int dummy = */ con.receiveIntData();
            } else if ("item".equals(name)) { //$NON-NLS-1$
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
                        /* String dummy = */ con.receiveString();
                        rdata[k] = con.receiveStringData();
                    }
                    if (0 <= j - from && j - from < count) {
                        tableModel.addRow(rdata);
                    }
                }
            } else {
                con.receiveDataTypeWithCheck(Type.ARRAY);
                manager.registerValue(widget, name, new Integer(from));
                int num = con.receiveInt();
                if (count < 0) {
                    count = num;
                }
                for (int j = 0; j < num; j++) {
                    boolean selected = con.receiveBooleanData();
                    if (0 <= j - from && j - from < count) {
                        if (selected) {
                            selections.addSelectionInterval(j - from, j - from);
                        } else {
                            selections.removeSelectionInterval(j - from, j - from);
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
            if ((count - from) > 0) {
                int value = (int) (((row - 1) * 1.0 / (count - from)) * (max - min)) + min;
                if (rowattrw == 1.0) {
                    value += (int) ((1.0 / (count - from)) * (max - min));
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

        // save selection values
        ListSelectionModel savedSelections = new DefaultListSelectionModel();
        int rows = table.getRowCount();
        copySelections(rows, savedSelections, selections);
        // setColumnIdentifiers resets selection values
        //tableModel.setColumnIdentifiers(labels);
        // restore selection values
        copySelections(rows, selections, savedSelections);
        widget.setVisible(true);
    }

    private void copySelections(int rows, ListSelectionModel to, ListSelectionModel from) {
        for (int i = 0; i < rows; i++) {
            if (from.isSelectedIndex(i)) {
                to.addSelectionInterval(i, i);
            } else {
                to.removeSelectionInterval(i, i);
            }
        }
    }

    public synchronized void send(WidgetValueManager manager, String name, Component widget) throws IOException {
        Protocol con = manager.getProtocol();
        JTable table = (JTable) widget;
        ValueAttribute va = manager.getValue(name);
        ListSelectionModel selections = table.getSelectionModel();
        boolean visibleRow = false;
        int rows = table.getRowCount();
        int opt = ((Integer) va.getOpt()).intValue();
        for (int i = 0; i < rows; i++) {
            con.sendPacketClass(PacketClass.ScreenData);
            con.sendName(va.getValueName() + '.' + va.getNameSuffix() + '[' + String.valueOf(i + opt) + ']');
            con.sendBooleanData(Type.BOOL, selections.isSelectedIndex(i));
            if (!visibleRow && isVisibleRow(table, i)) {
                con.sendPacketClass(PacketClass.ScreenData);
                con.sendName(va.getValueName() + ".row"); //$NON-NLS-1$
                con.sendIntegerData(Type.INT, i);
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
