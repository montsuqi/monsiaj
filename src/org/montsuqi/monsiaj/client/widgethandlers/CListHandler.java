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
package org.montsuqi.monsiaj.client.widgethandlers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.Map;
import javax.swing.BoundedRangeModel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.montsuqi.monsiaj.client.UIControl;
import org.montsuqi.monsiaj.util.SafeColorDecoder;
import org.montsuqi.monsiaj.widgets.PandaCList;

/**
 * <p>
 * A class to send/receive CList data.</p>
 */
class CListHandler extends WidgetHandler {

    @Override
    public void set(UIControl con, Component widget, JSONObject obj, Map styleMap) throws JSONException {
        JTable table = (JTable) widget;
        PandaCList clist = (PandaCList) widget;

        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        TableModelListener[] listeners = tableModel.getTableModelListeners();

        int count = 0;
        if (obj.has("count")) {
            count = obj.getInt("count");
        }

        double rowattr = 0.0;
        if (obj.has("rowattr")) {
            int ra = obj.getInt("rowattr");
            switch (ra) {
                case 1:
                    rowattr = 1.0;
                    break;
                case 2:
                    rowattr = 0.5;
                    break;
                case 3:
                    rowattr = 0.25;
                    break;
                case 4:
                    rowattr = 0.75;
                    break;
                default:
                    rowattr = 0.0;
                    break;
            }
        }

        if (obj.has("item")) {
            JSONArray array = obj.getJSONArray("item");
            int n = array.length();
            n = n > count ? count : n;
            int rows = tableModel.getRowCount();
            int columns = tableModel.getColumnCount();
            for (TableModelListener l : listeners) {
                tableModel.removeTableModelListener(l);
            }

            if (n < rows) {
                for (int i = rows; i > n; i--) {
                    tableModel.removeRow(i - 1);
                }
            } else if (n > rows) {
                Object rowData[] = new String[columns];
                for (int i = rows; i < n; i++) {
                    tableModel.addRow(rowData);
                }
            }

            for (int i = 0; i < n; i++) {
                JSONObject rowObj = array.getJSONObject(i);

                for (int j = 0; j < columns; j++) {
                    String key = "column" + (j + 1);
                    tableModel.setValueAt(rowObj.getString(key), i, j);
                }
            }

            for (TableModelListener l : listeners) {
                tableModel.addTableModelListener(l);
            }
        }

        if (obj.has("bgcolor")) {
            JSONArray array = obj.getJSONArray("bgcolor");
            Color[] bgcolors = new Color[array.length()];
            for (int i = 0; i < array.length(); i++) {
                bgcolors[i] = SafeColorDecoder.decode(array.getString(i));
                if (bgcolors[i] == null) {
                    bgcolors[i] = Color.WHITE;
                }
            }
            clist.setBGColors(bgcolors);
        }

        if (obj.has("fgcolor")) {
            JSONArray array = obj.getJSONArray("fgcolor");
            Color[] fgcolors = new Color[array.length()];
            for (int i = 0; i < array.length(); i++) {
                fgcolors[i] = SafeColorDecoder.decode(array.getString(i));
                if (fgcolors[i] == null) {
                    fgcolors[i] = Color.BLACK;
                }
            }
            clist.setFGColors(fgcolors);
        }

        int row = 0;
        if (obj.has("row")) {
            row = obj.getInt("row");
            int row2;
            if (row <= 0) {
                row2 = 0;
            } else {
                row2 = row - 1;
            }
            if (row2 >= count) {
                row2 = count - 1;
            }
            if (row2 >= 0) {
                clist.changeSelection(row2, 0, false, false);
            }
        }

        if (obj.has("selectdata")) {
            JSONArray array = obj.getJSONArray("selectdata");
            int n = array.length();
            n = n > count ? count : n;
            boolean[] selection = new boolean[n];
            for (int j = 0; j < n; j++) {
                boolean selected = array.getBoolean(j);
                selection[j] = selected;
                if (clist.getMode() == PandaCList.SELECTION_MODE_MULTI && selected) {
                    clist.changeSelection(j, 0, false, false);
                }
            }
            clist.setSelection(selection);
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
                if (rowattr == 1.0) {
                    value += (int) ((1.0 / count) * (max - min));
                }
                value -= rowattr * model.getExtent();
                if (value < 0) {
                    value = 0;
                }
                model.setValue(value);
            } else {
                model.setValue(0);
            }
        }
        this.setCommonAttribute(widget, obj, styleMap);
        widget.setVisible(false);
        widget.setVisible(true);
        clist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    @Override
    public void get(UIControl con, Component widget, JSONObject obj) throws JSONException {
        JTable table = (JTable) widget;
        PandaCList clist = (PandaCList) widget;

        for (int j = 0; j < table.getRowCount(); j++) {
            if (isVisibleRow(table, j)) {
                obj.put("row", j + 1);
                break;
            }
        }
        JSONArray array = new JSONArray();
        obj.put("selectdata", array);
        boolean[] selection = clist.getSelection();
        for (int j = 0; j < selection.length; j++) {
            array.put(j, selection[j]);
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
