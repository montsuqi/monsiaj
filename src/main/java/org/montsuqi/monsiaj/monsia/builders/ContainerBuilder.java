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
import javax.swing.JScrollPane;
import javax.swing.RootPaneContainer;
import org.montsuqi.monsiaj.monsia.ChildInfo;
import org.montsuqi.monsiaj.monsia.Interface;
import org.montsuqi.monsiaj.monsia.WidgetInfo;

/** <p>A basic builder for container widgets.</p>
 */
class ContainerBuilder extends WidgetBuilder {
	void buildChildren(Interface xml, Container parent, WidgetInfo info) {
		Iterator i = info.getChildren().iterator();
		if (parent instanceof RootPaneContainer) {
			RootPaneContainer rootPaneContainer = (RootPaneContainer)parent;
			parent = rootPaneContainer.getContentPane();
		}
		while (i.hasNext()) {
			ChildInfo cInfo = (ChildInfo)i.next();
			WidgetInfo wInfo = cInfo.getWidgetInfo();
			Component child = WidgetBuilder.buildWidget(xml, wInfo, parent);
			if (child instanceof JMenuBar) {
				xml.setMenuBar((JMenuBar)child);
			} else {
				parent.add(child);
			}
		}
	}

	/** <p>Insert a scroll pane under the specified child.</p>
	 * @param child a widget that wants to be in a scroll pane.
	 * @return the newly created scroll pane with the given child in it.
	 */
	protected Component underlayScrollPane(Component child, int vpol, int hpol) {
		JScrollPane scroll = new JScrollPane(child, vpol, hpol);
		scroll.setSize(child.getSize());
		return scroll;
	}
}
