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
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.montsuqi.client.Protocol;
import org.montsuqi.widgets.FileChooserButton;

/**
 * <p>
 * A class to send/receive FileChooserButton data.</p>
 */
class FileChooserButtonHandler extends WidgetHandler {

    public void set(Protocol con, Component widget, JSONObject obj, Map styleMap) throws JSONException {
        FileChooserButton fcb = (FileChooserButton) widget;

        this.setCommonAttribute(widget, obj, styleMap);
        fcb.setFile(null);
        con._addChangedWidget(widget);
    }

    public void get(Protocol con, Component widget, JSONObject obj) throws JSONException {
        FileChooserButton fcb = (FileChooserButton) widget;
        byte[] binary = fcb.loadData();
        if (binary.length <= 0) {
            return;
        }
        String oid = con.postBLOB(binary);
        obj.put("objectdata", oid);
        obj.put("filename", fcb.getFileName());
    }
}
