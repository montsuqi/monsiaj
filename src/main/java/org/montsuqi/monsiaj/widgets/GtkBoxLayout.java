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

package org.montsuqi.monsiaj.widgets;

import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.BoxLayout;

/** <p>A layout manager that simulates Gtk+'s Box.</p>
 */
public class GtkBoxLayout extends BoxLayout {

	int axis;
	public GtkBoxLayout(Container target, int axis) {
		super(target, axis);
		if (axis != X_AXIS && axis != Y_AXIS) {
			throw new IllegalArgumentException("unknown axis:" + axis); 
		}
		this.axis = axis;
	}


	public void layoutContainer(Container target) {
		super.layoutContainer(target);
		Insets insets = target.getInsets();
		int width = target.getWidth() - insets.left - insets.right;
		int height = target.getHeight() - insets.top - insets.bottom;
		for (int i = 0, n = target.getComponentCount(); i < n; i++) {
			Component c = target.getComponent(i);
			Rectangle r = c.getBounds();
			if (axis == X_AXIS) {
				r.y = 0;
				r.height = height;
			} else if (axis == Y_AXIS) {
				r.x = 0;
				r.width = width;
			} else {
				throw new IllegalStateException("unexpected axis:" + axis); 
			}
			c.setBounds(r);
		}
	}
}
