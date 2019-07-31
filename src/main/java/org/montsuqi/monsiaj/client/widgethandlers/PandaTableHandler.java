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

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.montsuqi.monsiaj.client.UIControl;
import org.montsuqi.monsiaj.widgets.PandaTable;

/**
 * <p>
 * A class to send/receive CList data.</p>
 */
class PandaTableHandler extends WidgetHandler {

    static final Logger logger = LogManager.getLogger(PandaTableHandler.class);
    private static final List<String> widgetList;

    static {
        widgetList = new ArrayList<>();
    }

    @Override
    public void set(UIControl con, Component widget, JSONObject obj, Map styleMap) throws JSONException {
        PandaTable table = (PandaTable) widget;

        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();

        this.setCommonAttribute(widget, obj, styleMap);

        int trow = 0;
        if (obj.has("trow")) {
            trow = obj.getInt("trow");
            if (trow < 0) {
                logger.warn("" + widget.getName() + "invalid trow:" + trow);
                trow = 0;
            }
            if (trow >= 1) {
                trow -= 1;
            }
            if (trow >= tableModel.getRowCount()) {
                trow = tableModel.getRowCount() - 1;
            }
        }

        double trowattr = 0.0;
        if (obj.has("trowattr")) {
            int val = obj.getInt("trowattr");
            switch (val) {
                case 1:
                    trowattr = 1.0;
                    break;
                case 2:
                    trowattr = 0.5;
                    break;
                case 3:
                    trowattr = 0.25;
                    break;
                case 4:
                    trowattr = 0.75;
                    break;
                default:
                    trowattr = 0.0;
                    break;
            }
        }

        int tcolumn = 0;
        if (obj.has("tcolumn")) {
            tcolumn = obj.getInt("tcolumn");
            if (tcolumn < 0) {
                logger.warn("" + widget.getName() + "invalid tcolumn:" + tcolumn);
                tcolumn = 0;
            }
            if (tcolumn >= 1) {
                tcolumn -= 1;
            }
            if (tcolumn >= tableModel.getColumnCount()) {
                tcolumn = tableModel.getColumnCount() - 1;
            }
        }

        if (obj.has("rowdata")) {
            JSONArray array = obj.getJSONArray("rowdata");
            for (int i = 0; i < array.length(); i++) {
                JSONObject rowObj = array.getJSONObject(i);
                for (int j = 0; j < table.getColumns(); j++) {
                    String key = "column" + (j + 1);
                    if (rowObj.has(key)) {
                        JSONObject colObj = rowObj.getJSONObject(key);
                        if (colObj.has("celldata")) {
                            table.setCell(i, j, colObj.getString("celldata"));
                        }
                        if (colObj.has("fgcolor")) {
                            table.setFGColor(i, j, colObj.getString("fgcolor"));
                        }
                        if (colObj.has("bgcolor")) {
                            table.setBGColor(i, j, colObj.getString("bgcolor"));
                        }
                    }
                }
            }
        }

        widget.validate();
        if (trow >= 0 && tcolumn >= 0) {
            /*
             * Windows7+Java 1.7で初回表示時にセル指定すると微妙にスクロールする問題のため 初回だけ0,0にセル指定する
             */
            if (widgetList.contains(widget.getName())) {
                table.changeSelection(trow, tcolumn, false, false);
            } else {
                widgetList.add(widget.getName());
                table.changeSelection(0, 0, false, false);
            }

            JScrollBar vScroll = getVerticalScrollBar(table);
            if (vScroll != null) {
                BoundedRangeModel model = vScroll.getModel();
                int max = model.getMaximum();
                int min = model.getMinimum();
                int rows = table.getModel().getRowCount();
                int value = (int) (((trow) * 1.0 / (rows)) * (max - min)) + min;
                if (trowattr == 1.0) {
                    value += (int) ((1.0 / (rows)) * (max - min));
                }
                value -= trowattr * model.getExtent();
                if (value < 0) {
                    value = 0;
                }
                model.setValue(value);
            }
        }
        con._addChangedWidget(widget);
    }

    @Override
    public void get(UIControl con, Component widget, JSONObject obj) throws JSONException {
        PandaTable table = (PandaTable) widget;
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        obj.put("trow", table.getChangedRow() + 1);
        obj.put("tcolumn", table.getChangedColumn() + 1);
        obj.put("tvalue", table.getChangedValue());

        int k = 0;
        JSONArray array = new JSONArray();
        obj.put("rowdata", array);
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            for (int j = 0; j < table.getColumns(); j++) {
                String val = (String) tableModel.getValueAt(i, j);
                if (!val.isEmpty()) {
                    k = i + 1;
                }
            }
        }
        for (int i = 0; i < k; i++) {
            JSONObject rowObj = new JSONObject();
            array.put(i, rowObj);
            for (int j = 0; j < table.getColumns(); j++) {
                String key = "column" + (j + 1);
                JSONObject colObj = new JSONObject();
                rowObj.put(key, colObj);
                colObj.put("celldata", (String) tableModel.getValueAt(i, j));
            }
        }
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
