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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.montsuqi.monsiaj.client.UIControl;
import org.montsuqi.monsiaj.util.TempFile;
import org.montsuqi.monsiaj.widgets.PandaPreview;

/**
 * <
 * p>
 * A class to send/receive Preview data.</p>
 */
class PreviewHandler extends WidgetHandler {

    private static final String TEMP_PREFIX = "pandapreview";
    private static final String TEMP_SUFFIX = ".pdf";

    static final Logger logger = LogManager.getLogger(PreviewHandler.class);

    @Override
    public void set(UIControl con, Component widget, JSONObject obj, Map styleMap) throws JSONException {
        PandaPreview preview = (PandaPreview) widget;
        preview.clear();
        this.setCommonAttribute(widget, obj, styleMap);
        if (obj.has("objectdata")) {
            try {
                String oid = obj.getString("objectdata");
                if (oid.isEmpty() || oid.equals("0")) {
                } else {
                    File temp = TempFile.createTempFile(TEMP_PREFIX, TEMP_SUFFIX);
                    temp.deleteOnExit();
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
                    con.getClient().getProtocol().getBLOB(oid, out);
                    preview.load(temp.getAbsolutePath());
                }
            } catch (IOException | JSONException ex) {
                logger.warn(ex);
            }
        }
    }

    @Override
    public void get(UIControl con, Component widget, JSONObject obj) throws JSONException {
    }
}
