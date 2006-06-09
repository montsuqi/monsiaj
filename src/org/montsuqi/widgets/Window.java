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
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Window extends JFrame {

	public Window() {
		super();
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setGlassPane(new JPanel() {
			{
				setOpaque(false);
				Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
				setCursor(waitCursor);
				addMouseListener(new MouseAdapter() {
					// nothing overridden
				});
				addKeyListener(new KeyAdapter() {
					// nothing overridden
				});
			}
		});
		hideBusyCursor();
	}

	public void showBusyCursor() {
		getGlassPane().setVisible(true);
		getGlassPane().requestFocus();
	}

	public void hideBusyCursor() {
		getGlassPane().setVisible(false);
	}

	public boolean isActive() {
		return ! getGlassPane().isVisible();
	}


	public static void busyAllWindows() {
		Window[] windows = getMontsuqiWindows();
		for (int i = 0; i < windows.length; i++) {
			Window w = windows[i];
			w.showBusyCursor();
		}
	}

	public static Window[] getMontsuqiWindows() {
		Frame[] frames = Frame.getFrames();
		List list = new ArrayList();
		for (int i = 0; i < frames.length; i++) {
			Frame f = frames[i];
			if (f instanceof Window) {
				list.add(f);
			}
		}
		return (Window[])list.toArray(new Window[list.size()]);
	}
}
