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

package org.montsuqi.monsia.builders;

import java.awt.Container;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import org.montsuqi.monsia.ChildInfo;
import org.montsuqi.monsia.Interface;
import org.montsuqi.monsia.WidgetInfo;

/** <p>A builder to create MenuBar widget.</p>
 */
class MenuBarBuilder extends ContainerBuilder {
	
	void buildChildren(Interface xml, Container parent, WidgetInfo info) {
		JMenuBar menuBar = (JMenuBar)parent;
		Iterator i = info.getChildren().iterator();
		while (i.hasNext()) {
			// Menu structure differences between Gtk+ and Swing.
			// Gtk: GtkMenuBar       Swing: JMenuBar
			//       +GtkMenuItem            +JMenu
			//         +GtkMenu                +JMenuItem
			//           +GtkMenuItem          +JMenuItem
			//           +GtkMenuItem

			// skip redundant GtkMenuItem part.
			ChildInfo cInfo = (ChildInfo)i.next();
			WidgetInfo wInfo = cInfo.getWidgetInfo();
			Map properties = wInfo.getProperties();
			List children = wInfo.getChildren();
			assert children.size() == 1;
			// get the info of the menu to be created.
			ChildInfo menuItemChildInfo = wInfo.getChild(0);
			WidgetInfo menuInfo = menuItemChildInfo.getWidgetInfo();
			JMenu menu = (JMenu)WidgetBuilder.buildWidget(xml, menuInfo, menuBar);
			// set properties from the redundant GtkMenuItem part above.
			String[] keys = {
				"stock_item", //$NON-NLS-1$
				"label" //$NON-NLS-1$
			};
			for (int k = 0; k < keys.length; k++) {
				if (properties.containsKey(keys[k])) {
					WidgetPropertySetter setter = WidgetPropertySetter.getSetter(JMenu.class, keys[k]);
					setter.set(xml, menuBar, menu, (String)properties.get(keys[k]));
				}
			}
			menuBar.add(menu);
		}
	}
}
