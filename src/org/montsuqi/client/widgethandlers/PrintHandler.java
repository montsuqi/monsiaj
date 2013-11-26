/*      PANDA -- a simple transaction monitor

 Copyright (C) 2010 JMA (Japan Medical Association).

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
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.montsuqi.client.Protocol;

/**
 * <
 * p>
 * A class to send/receive Timer data.</p>
 */
public class PrintHandler extends WidgetHandler {

    public void set(Protocol con, Component widget, JSONObject obj, Map styleMap) throws JSONException {
        if (obj.has("item")) {
            JSONArray array = obj.getJSONArray("item");
            for (int j = 0; j < array.length(); j++) {
                JSONObject itemObj = array.getJSONObject(j);
                String path = "";
                if (itemObj.has("path")) {
                    path = itemObj.getString("path");
                }

                String title = "";
                if (itemObj.has("title")) {
                    title = itemObj.getString("title");
                }

                boolean showDialog = true;
                if (itemObj.has("showdialog")) {
                    int v = itemObj.getInt("showdialog");
                    if (v == 1) {
                        showDialog = true;
                    } else {
                        showDialog = false;
                    }
                }

                int retry = 0;
                if (itemObj.has("retry")) {
                    retry = itemObj.getInt("retry");
                }

                if (!path.isEmpty() && !title.isEmpty()) {
                    con.addPrintRequest(path, title, retry, showDialog);
                }
            }
        }
    }

    public void get(Protocol con, Component widget, JSONObject obj) throws JSONException {
    }

}
