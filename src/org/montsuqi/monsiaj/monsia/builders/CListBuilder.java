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
import javax.swing.JComponent;
import javax.swing.table.DefaultTableModel;
import org.montsuqi.monsiaj.monsia.ChildInfo;
import org.montsuqi.monsiaj.monsia.Interface;
import org.montsuqi.monsiaj.monsia.WidgetInfo;
import org.montsuqi.monsiaj.widgets.PandaCList;

/** <p>A builder to create clist widgets.</p>
 * <p>This component assigns a CListHeaderRenderer for each column headers.</p>
 */
class CListBuilder extends ContainerBuilder {

    @Override
    void buildChildren(Interface xml, Container parent, WidgetInfo info) {
        int cCount = info.getChildren().size();

        PandaCList clist = (PandaCList) parent;
        // make all cells uneditable.
        clist.setModel(new DefaultTableModel(0, cCount) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        for (int i = 0; i < cCount; i++) {
            ChildInfo cInfo = info.getChild(i);
            WidgetInfo wInfo = cInfo.getWidgetInfo();
            Component header = WidgetBuilder.buildWidget(xml, wInfo, parent);
            if (header instanceof JComponent) {
                clist.registerHeaderComponent(i, (JComponent) header);
            } else {
                throw new WidgetBuildingException("not-JComponent component for CList header"); 
            }
        }
    }
}
