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
import java.util.Map;

import org.montsuqi.monsia.ChildInfo;
import org.montsuqi.monsia.Interface;
import org.montsuqi.monsia.WidgetInfo;
import org.montsuqi.util.ParameterConverter;
import org.montsuqi.widgets.TableConstraints;
import org.montsuqi.widgets.TableLayout;

class TableBuilder extends ContainerBuilder {
	void buildChildren(Interface xml, Container parent, WidgetInfo info) {
		TableLayout tl = (TableLayout)parent.getLayout();
		for (int i = 0, n = info.getChildren().size(); i < n; i++) {
			ChildInfo cInfo = info.getChild(i);
			WidgetInfo wInfo = cInfo.getWidgetInfo();
			Component child = null;
			TableConstraints tc = new TableConstraints();
			child = buildWidget(xml, wInfo, parent);
			Map properties = wInfo.getProperties();
			if (properties.containsKey("left_attach")) { //$NON-NLS-1$
				tc.leftAttach = ParameterConverter.toInteger((String)properties.get("left_attach")); //$NON-NLS-1$
			}
			if (properties.containsKey("right_attach")) { //$NON-NLS-1$
				tc.rightAttach = ParameterConverter.toInteger((String)properties.get("right_attach")); //$NON-NLS-1$
			}
			if (properties.containsKey("top_attach")) { //$NON-NLS-1$
				tc.topAttach = ParameterConverter.toInteger((String)properties.get("top_attach")); //$NON-NLS-1$
			}
			if (properties.containsKey("botom_attach")) { //$NON-NLS-1$
				tc.bottomAttach = ParameterConverter.toInteger((String)properties.get("bottom_attach")); //$NON-NLS-1$
			}
			if (properties.containsKey("x")) { //$NON-NLS-1$
				// x_options = ParameterConverter.toInteger(value);
			}
			if (properties.containsKey("y")) { //$NON-NLS-1$
				// y_options = ParameterConverter.toInteger(value);
			}
			parent.add(child);
			tl.setConstraints(child, tc);
		}
	}
}
