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

package org.montsuqi.widgets;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JComponent;
import org.montsuqi.util.Logger;

/** <p>A container that lays out components in fixed position and size.</p>
 */
public class Fixed extends JComponent {

	public Fixed() {
		super();
		super.setLayout(null);
	}

	public void setLayout(LayoutManager layout) {
		Logger.getLogger(Fixed.class).info("ignoring Fixed#setLayout()"); //$NON-NLS-1$
	}

	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		setPreferredSize(new Dimension(width, height));
	}

	public void setBounds(Rectangle r) {
		super.setBounds(r);
		setPreferredSize(new Dimension(r.width, r.height));
	}

	public void setSize(Dimension d) {
		super.setSize(d);
		setPreferredSize(d);
	}

	public void setSize(int width, int height) {
		super.setSize(width, height);
		setPreferredSize(new Dimension(width, height));
	}
}
