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

package org.montsuqi.widgets;

import java.awt.Cursor;
import javax.swing.JComponent;
import javax.swing.JFrame;

public class Window extends JFrame {
	public Window() {
		super();
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setGlassPane(new JComponent() {
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			}
		});
		getGlassPane().setVisible(false);
	}

	public void showBusyCursor() {
		getGlassPane().setVisible(true);
	}

	public void hideBusyCursor() {
		getGlassPane().setVisible(false);
	}
}
