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

import org.montsuqi.monsia.ChildInfo;
import org.montsuqi.monsia.Interface;
import org.montsuqi.monsia.Property;
import org.montsuqi.monsia.WidgetInfo;
import org.montsuqi.util.ParameterConverter;
import org.montsuqi.widgets.TableConstraints;
import org.montsuqi.widgets.TableLayout;

class TableBuilder extends ContainerBuilder {
	void buildChildren(Interface xml, Container parent, WidgetInfo info) {
		int cCount = info.getChildrenCount();
		TableLayout tl = (TableLayout)parent.getLayout();
		for (int i = 0; i < cCount; i++) {
			ChildInfo cInfo = info.getChild(i);
			WidgetInfo wInfo = cInfo.getWidgetInfo();
			Component child = null;
			TableConstraints tc = new TableConstraints();
			child = buildWidget(xml, wInfo);
			int pCount = wInfo.getPropertiesCount();
			for (int j = 0; j < pCount; j++) {
				Property p = cInfo.getProperty(j);
				String name = p.getName();
				String value = p.getValue();
				if ("left_attach".equals(name)) { //$NON-NLS-1$
					tc.leftAttach = ParameterConverter.toInteger(value);
				} else if ("right_attach".equals(name)) { //$NON-NLS-1$
					tc.rightAttach = ParameterConverter.toInteger(value);
				} else if ("top_attach".equals(name)) { //$NON-NLS-1$
					tc.topAttach = ParameterConverter.toInteger(value);
				} else if ("bottom_attach".equals(name)) { //$NON-NLS-1$
					tc.bottomAttach = ParameterConverter.toInteger(value);
				} else if ("x_options".equals(value)) { //$NON-NLS-1$
					// x_options = ParameterConverter.toInteger(value);
				} else if ("y_options".equals(value)) { //$NON-NLS-1$
					// y_options = ParameterConverter.toInteger(value);
				} else {
					logger.warn(Messages.getString("WidgetBuilder.unknown_child_packing_property_for_Table"), name); //$NON-NLS-1$
				}
			}
			parent.add(child);
			tl.setConstraints(child, tc);
		}
	}
}
