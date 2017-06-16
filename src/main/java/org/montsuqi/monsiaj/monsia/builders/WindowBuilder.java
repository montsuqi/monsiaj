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

import java.util.Iterator;
import javax.swing.JMenuBar;
import javax.swing.RootPaneContainer;
import org.montsuqi.monsiaj.monsia.ChildInfo;
import org.montsuqi.monsiaj.monsia.Interface;
import org.montsuqi.monsiaj.monsia.WidgetInfo;
import org.montsuqi.monsiaj.widgets.Fixed;
import org.montsuqi.monsiaj.widgets.Window;

/** <p>A builder to create Window(top level) widget.</p>
 * <p>Since dialogs are variation of windows in Gtk+ while dialogs and windows are 
 * different in Swing. Both treated as Window here.</p>
 */
public class WindowBuilder extends ContainerBuilder {

    @Override
    Component buildSelf(Interface xml, Container parent, WidgetInfo info) {
        Component c = super.buildSelf(xml, parent, info);
        Window w = (Window) c;
        try {
            w.setTitleString(info.getProperty("title"));
            if (!w.getAllow_Grow()) {
                w.setResizable(false);
            }
            if (!w.getAllow_Shrink()) {
                w.setResizable(false);
            }
            if (info.getClassName().equals("Dialog")) {
                w.setIsDialog(true);
            } else {
                w.setIsDialog(false);
            }
            w.setVisible(false);
        } catch (Exception e) {
            throw new WidgetBuildingException(e);
        }
        return c;
    }

    @Override
    void buildChildren(Interface xml, Container parent, WidgetInfo info) {
        Window w = (Window) parent;
        Iterator i = info.getChildren().iterator();
        if (parent instanceof RootPaneContainer) {
            RootPaneContainer rootPaneContainer = (RootPaneContainer) parent;
            parent = rootPaneContainer.getContentPane();
        }
        while (i.hasNext()) {
            ChildInfo cInfo = (ChildInfo) i.next();
            WidgetInfo wInfo = cInfo.getWidgetInfo();
            Component child = WidgetBuilder.buildWidget(xml, wInfo, parent);
            if (child instanceof JMenuBar) {
                xml.setMenuBar((JMenuBar) child);
            } else {
                w.setChild(child);
            }
            if (child instanceof Fixed) {
                wInfo.setProperty("width", "1024");
                wInfo.setProperty("height", "768");
            }
        }
    }
}
