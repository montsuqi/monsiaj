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
import org.montsuqi.widgets.Notebook;
import org.montsuqi.widgets.NotebookDummyButton;

class NotebookBuilder extends ContainerBuilder {
	void buildChildren(Interface xml, Container parent, WidgetInfo info) {
		// create tabs first
		Notebook notebook = (Notebook)parent;
		int cCount = info.getChildren().size();
		if (cCount % 2 != 0) {
			throw new WidgetBuildingException(Messages.getString("WidgetBuilder.odd_number_of_notebook_childrens")); //$NON-NLS-1$
		}
		int tabCount = cCount / 2;
		String[] labels = new String[tabCount];
		Component[] bodies = new Component[tabCount];
		boolean[] enabled = new boolean[tabCount];
		int currentLabel = 0;
		int currentBody = 0;
		for (int i = 0; i < cCount; i++) {
			ChildInfo cInfo = info.getChild(i);
			WidgetInfo wInfo = cInfo.getWidgetInfo();
			Map properties = wInfo.getProperties();
			if (properties.containsKey("child_name")) { //$NON-NLS-1$
				if (properties.containsKey("label")) { //$NON-NLS-1$
					labels[currentLabel] = (String)properties.get("label"); //$NON-NLS-1$
					Component dummy = new NotebookDummyButton(labels[currentLabel], currentLabel, notebook);
					setCommonParameters(xml, dummy, wInfo);
					setSignals(xml, dummy, wInfo);
					enabled[currentLabel] = true;
					if (properties.containsKey("sensitive")) { //$NON-NLS-1$
						enabled[currentLabel] = ParameterConverter.toBoolean((String)properties.get("sensitive")); //$NON-NLS-1$
					}
					currentLabel++;
				} else {
					throw new WidgetBuildingException(Messages.getString("WidgetBuilder.no_label_for_a_tab")); //$NON-NLS-1$
				}
			} else {
				Component body = buildWidget(xml, wInfo, parent);
				bodies[currentBody] = body;
				currentBody++;
			}
		}
		if (currentBody != bodies.length || currentLabel != labels.length) {
			throw new WidgetBuildingException(Messages.getString("WidgetBuilder.tab_label_count_mismatch")); //$NON-NLS-1$
		}
		for (int i = 0; i < tabCount; i++) {
			notebook.add(labels[i], bodies[i]);
			notebook.setEnabledAt(i, enabled[i]);
		}
	}
}
