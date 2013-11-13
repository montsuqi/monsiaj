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
package org.montsuqi.client.widgethandlers;

import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.montsuqi.client.PacketClass;
import org.montsuqi.client.Protocol;
import org.montsuqi.client.Type;
import org.montsuqi.widgets.PandaTable;

/**
 * <p>
 * A class to send/receive CList data.</p>
 */
class PandaTableHandler extends WidgetHandler {

    private static List widgetList;

    static {
        widgetList = new ArrayList();
    }

    public void set(Protocol con, Component widget, JSONObject obj, Map styleMap) throws JSONException {
        PandaTable table = (PandaTable) widget;

        this.setCommonAttribute(widget, obj, styleMap);

        int trow = 0;
        if (obj.has("trow")) {
            trow = obj.getInt("trow");
            if (trow > 1) {
                trow -= 1;
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
            if (tcolumn > 1) {
                tcolumn -= 1;
            }
        }

        if (obj.has("rowdata")) {
            JSONArray array = obj.getJSONArray("rowdata");
            for (int i = 0; i < array.length(); i++) {
                JSONObject rowObj = array.getJSONObject(i);
                JSONArray keys = rowObj.getJSONArray("__keys__");
                for (int j = 0; j < keys.length(); j++) {
                    JSONObject colObj = rowObj.getJSONObject(keys.getString(j));
                    for (Iterator ite = colObj.keys(); ite.hasNext();) {
                        String key = (String) ite.next();
                        if (key.matches("celldata")) {
                            table.setCell(i, j, colObj.getString(key));
                        } else if (key.matches("fgcolor")) {
                            table.setFGColor(i, j, colObj.getString(key));
                        } else if (key.matches("bgcolor")) {
                            table.setBGColor(i, j, colObj.getString(key));
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
    }

    public void get(Protocol con, Component widget, JSONObject obj) throws JSONException {
        PandaTable table = (PandaTable) widget;
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        
        if (obj.has("trow")) {
            obj.put("trow",table.getChangedRow());
        }
        
        if (obj.has("tcolumn")) {
            obj.put("tcolumn", table.getChangedRow());
        }
        
        if (obj.has("tvalue")) {
            obj.put("tvalue", table.getChangedValue());
        }
        
        if (obj.has("rowdata")) {
            JSONArray array = obj.getJSONArray("rowdata");
            for(int i=0;i<array.length();i++) {
                JSONObject rowObj = array.getJSONObject(i);
                JSONArray keys = rowObj.getJSONArray("__keys__");
                for(int j=0;j<keys.length();j++) {
                    JSONObject colObj = rowObj.getJSONObject(keys.getString(j));
                    if (colObj.has("celldata")) {
                        colObj.put("celldata", (String)tableModel.getValueAt(i, j));
                    }
                }
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
