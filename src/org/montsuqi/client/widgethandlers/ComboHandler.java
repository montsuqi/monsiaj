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
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.ListDataListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.montsuqi.client.UIControl;

/**
 * <p>
 * A class to send/receive Combo data.</p>
 */
class ComboHandler extends WidgetHandler {

    @Override
    @SuppressWarnings("unchecked")
    public void set(UIControl con, Component widget, JSONObject obj, Map styleMap) throws JSONException {
        JComboBox<String> combo = ((JComboBox<String>) widget);
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) combo.getModel();

        this.setCommonAttribute(widget, obj, styleMap);

        int count = 0;
        if (obj.has("count")) {
            count = obj.getInt("count");
        }

        if (obj.has("item")) {
            ListDataListener[] listeners = model.getListDataListeners();
            for (ListDataListener l : listeners) {
                model.removeListDataListener(l);
            }

            JSONArray array = obj.getJSONArray("item");
            List<String> list = new ArrayList<>();
            list.add("");
            for (int j = 0; j < array.length(); j++) {
                if (j < count) {
                    list.add(array.getString(j));
                }
            }
            model.removeAllElements();
            for (String s : list) {
                model.addElement(s);
            }

            for (ListDataListener l : listeners) {
                model.addListDataListener(l);
            }
        }

        Component editor = combo.getEditor().getEditorComponent();
        String entryString = null;
        for (Iterator i = obj.keys(); i.hasNext();) {
            String key = (String) i.next();
            if (this.isCommonAttribute(key)) {
                // do nothing
            } else if (key.matches("count")) {
                // do nothing                
            } else if (key.matches("item")) {
                // do nothing
            } else {
                /*                
                 JSONObject entryObj = obj.getJSONObject(key);
                 EntryHandler entryHandler = new EntryHandler();
                 entryHandler.set(con,editor,entryObj,styleMap);
                 entryString = ((JTextField)editor).getText();
                 */
                entryString = obj.getJSONObject(key).getString("textdata");
            }
        }
        if (entryString != null) {
            combo.setSelectedItem(entryString);
            widget.dispatchEvent(new KeyEvent(editor, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_UNDEFINED, KeyEvent.CHAR_UNDEFINED));
        }
    }

    @Override
    public void get(UIControl con, Component widget, JSONObject obj) throws JSONException {
    }
}
