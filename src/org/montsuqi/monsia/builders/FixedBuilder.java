/*      PANDA -- a simple transaction monitor
                                                                                
Copyright (C) 1998-1999 Ogochan.
			  2000-2003 Ogochan & JMA (Japan Medical Association).
                                                                                
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

package org.montsuqi.monsia.builders;

import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JTable;

import org.montsuqi.monsia.ChildInfo;
import org.montsuqi.monsia.Interface;
import org.montsuqi.monsia.Property;
import org.montsuqi.monsia.WidgetInfo;
import org.montsuqi.util.ParameterConverter;

class FixedBuilder extends ContainerBuilder {
	void buildChildren(Interface xml, Container parent, WidgetInfo info) {
		int cCount = info.getChildrenCount();
		for (int i = 0; i < cCount; i++) {
			ChildInfo cInfo = info.getChild(i);
			WidgetInfo wInfo = cInfo.getWidgetInfo();
			Component child = null;
			int x = 0, y = 0;
			child = buildWidget(xml, wInfo, parent);
			int pCount = cInfo.getPropertiesCount();
			for (int j = 0; j < pCount; j++) {
				Property p = cInfo.getProperty(j);
				String name = p.getName();
				String value = p.getValue();
				if ("x".equals(name)) { //$NON-NLS-1$
					x = ParameterConverter.toInteger(value);
				} else if ("y".equals(name)) { //$NON-NLS-1$
					y = ParameterConverter.toInteger(value);
				} else {
					logger.warn(Messages.getString("WidgetBuilder.unknown_child_packing_property_for_Layout"), name); //$NON-NLS-1$
				}
			}
			if (child instanceof JTable) {
				child = underlayScrollPane(child);
			}
			parent.add(child);
			child.setLocation(x, y);
		}
		Component[] children = parent.getComponents();
		int bottomMost = 0;
		int rightMost = 0;
		for (int i = 0, n = children.length; i < n; i++) {
			Rectangle rect = children[i].getBounds();
			bottomMost = Math.max(bottomMost, rect.y + rect.height);
			rightMost = Math.max(rightMost, rect.x + rect.width);
		}
		Insets insets = parent.getInsets();
		parent.setSize(rightMost + insets.right, bottomMost + insets.bottom);
	}
}
