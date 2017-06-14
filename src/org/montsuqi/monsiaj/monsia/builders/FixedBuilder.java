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
import java.awt.Dimension;
import java.awt.event.FocusListener;
import java.awt.Insets;
import java.awt.Rectangle;

import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import org.montsuqi.monsiaj.monsia.ChildInfo;
import org.montsuqi.monsiaj.monsia.Interface;
import org.montsuqi.monsiaj.monsia.WidgetInfo;
import org.montsuqi.monsiaj.util.ParameterConverter;
import org.montsuqi.monsiaj.widgets.CheckBox;
import org.montsuqi.monsiaj.widgets.RadioButton;

/** <p>A builder to create Fixed widgets.</p>
 */
class FixedBuilder extends ContainerBuilder {

    @Override
    void buildChildren(Interface xml, Container parent, WidgetInfo info) {
        for (int i = 0, n = info.getChildren().size(); i < n; i++) {
            ChildInfo cInfo = info.getChild(i);
            WidgetInfo wInfo = cInfo.getWidgetInfo();
            Component child;
            int x = 0;
            int y = 0;
            child = buildWidget(xml, wInfo, parent);
            Map properties = cInfo.getProperties();
            if (properties.containsKey("x")) { 
                x = ParameterConverter.toInteger((String) properties.get("x")); 
            }
            if (properties.containsKey("y")) { 
                y = ParameterConverter.toInteger((String) properties.get("y")); 
            }

            // Since JTable itself does not have capability to scroll,
            // insert a scroll pane as the parent of a table.
            if (child instanceof JTable) {
                child = underlayScrollPane(child,
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            }
            if (child instanceof JTextArea) {
                child = underlayScrollPane(child,
                        JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            }
            if (child instanceof JMenuBar) {
                xml.setMenuBar((JMenuBar) child);
            } else {
                parent.add(child);
                child.setLocation(x,y);
            }
        }

        // resize self so that it has enought size to contain all children.
        Component[] children = parent.getComponents();
        int bottomMost = 0;
        int rightMost = 0;
        for (int i = 0, n = children.length; i < n; i++) {
            Rectangle rect = children[i].getBounds();
            bottomMost = Math.max(bottomMost, rect.y + rect.height);
            rightMost = Math.max(rightMost, rect.x + rect.width);
            children[i].addFocusListener((FocusListener) parent);
            if (children[i] instanceof JLabel) {
                JLabel label = (JLabel) (children[i]);
                Dimension s = label.getSize();
                Dimension ps = label.getPreferredSize();
                s.width = s.width < ps.width ? ps.width : s.width;
                label.setSize(s);
            } else if (children[i] instanceof RadioButton
                    || children[i] instanceof CheckBox) {
                AbstractButton button = (AbstractButton) children[i];
                if (!button.getText().equals("")) {
                    Dimension s = button.getSize();
                    Dimension ps = button.getPreferredSize();
                    s.width = s.width < ps.width ? ps.width : s.width;
                    button.setPreferredSize(s);
                    button.setSize(s);
                }
            }
        }

        Insets insets = parent.getInsets();
        parent.setSize(rightMost + insets.right, bottomMost + insets.bottom);
    }
}
