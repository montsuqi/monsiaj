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
package org.montsuqi.monsiaj.monsia.builders;

import java.awt.Component;
import java.awt.Container;
import java.util.Map;

import org.montsuqi.monsiaj.monsia.Interface;
import org.montsuqi.monsiaj.monsia.WidgetInfo;
import org.montsuqi.monsiaj.util.ParameterConverter;

/** <p>A builder to create checkbutton widget.</p>
 * <p>When darw_indicator property is set but false, creates a togglebutton instead.</p>
 */
final class CheckButtonBuilder extends WidgetBuilder {

    @Override
    Component buildSelf(final Interface xml, final Container parent, final WidgetInfo info) {
        if (looksLikeToggleButton(info)) {
            info.setClassName("ToggleButton");
        }
        if ("0".equals(info.getProperty("width"))) {
            info.setProperty("width", "20");
        }
        if ("0".equals(info.getProperty("height"))) {
            info.setProperty("height", "20");
        }
        return super.buildSelf(xml, parent, info);
    }

    private boolean looksLikeToggleButton(final WidgetInfo info) {
        final Map props = info.getProperties();
        return props.containsKey("draw_indicator") && !ParameterConverter.toBoolean(info.getProperty("draw_indicator"));
    }
}
