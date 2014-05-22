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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.json.JSONException;
import org.json.JSONObject;
import org.montsuqi.client.Protocol;
import org.montsuqi.widgets.Pixmap;

/**
 * <
 * p>
 * A class to send/receive Pixmap data.</p>
 */
public class PixmapHandler extends WidgetHandler {

    public void set(Protocol con, Component widget, JSONObject obj, Map styleMap) throws JSONException {
        Pixmap pixmap = (Pixmap) widget;
        this.setCommonAttribute(widget, obj, styleMap);
        if (obj.has("objectdata")) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try {
                int status = con.getBLOB(obj.getString("objectdata"), bytes);
                if (status == 200 && bytes.size() > 0) {
                    Icon icon = new ImageIcon(bytes.toByteArray());
                    pixmap.setText("");
                    pixmap.setIcon(icon);
                    pixmap.validate();
                } else {
                    pixmap.setIcon(null);
                }
            } catch (IOException ex) {
                logger.warn(ex);
            }
        }
    }

    public void get(Protocol con, Component widget, JSONObject obj) throws JSONException {
    }
}
