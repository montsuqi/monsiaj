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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JTabbedPane;


public class Notebook extends JTabbedPane {
	List buttons;

	public Notebook() {
		super();
		initButtonSensor();
	}

	public Notebook(int tabPlacement) {
		super(tabPlacement);
		initButtonSensor();
	}

	public Notebook(int tabPlacement, int tabLayoutPolicy) {
		super(tabPlacement, tabLayoutPolicy);
		initButtonSensor();
	}

	private void initButtonSensor() {
		buttons = new ArrayList();
		MouseListener listener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				Point p = e.getPoint();
				for (int i = 0, n = getTabCount(); i < n; i++) {
					Rectangle r = getBoundsAt(i);
					if (r == null) {
						continue;
					}
					if (r.contains(p)) {
						JButton b = (JButton)buttons.get(i);
						if (b != null) {
							b.doClick();
							break;
						}
					}
				}
			}
		};
		addMouseListener(listener);
	}

	public void addButton(JButton button) {
		button.putClientProperty("index", new Integer(buttons.size())); //$NON-NLS-1$
		buttons.add(button);
	}
}

