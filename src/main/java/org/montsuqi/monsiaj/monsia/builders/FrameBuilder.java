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

import org.montsuqi.monsiaj.monsia.ChildInfo;
import org.montsuqi.monsiaj.monsia.Interface;
import org.montsuqi.monsiaj.monsia.WidgetInfo;
import org.montsuqi.monsiaj.widgets.Frame;

/** <p>A builder to create Frame widget.</p>
 */
class FrameBuilder extends ContainerBuilder {

    @Override
    Component buildSelf(Interface xml, Container parent, WidgetInfo info) {
        final Component widget = super.buildSelf(xml, parent, info);
        Frame frame = (Frame) widget;
        frame.setShadow(info.getProperty("shadow_type"));
        return widget;
    }

    @Override
    void buildChildren(Interface xml, Container parent, WidgetInfo info) {
        if (info.getChildren().size() != 1) {
            throw new WidgetBuildingException("only one child for a Frame"); 
        }
        ChildInfo cInfo = info.getChild(0);
        WidgetInfo wInfo = cInfo.getWidgetInfo();
        Component child = buildWidget(xml, wInfo, parent);
        parent.add(child);
    }
}
