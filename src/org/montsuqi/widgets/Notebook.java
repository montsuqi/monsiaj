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

import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.montsuqi.util.Logger;

public class Notebook extends JTabbedPane {

	Map buttons;
	Logger logger;

	public Notebook() {
		super();

		logger = Logger.getLogger(Notebook.class);
		buttons = new HashMap();

		addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Integer selected = new Integer(getSelectedIndex());
				if (buttons.containsKey(selected)) {
					AbstractButton dummy = (AbstractButton)buttons.get(selected);
					if (dummy != null) {
						dummy.doClick();
					}
				}
			}
		});
	}

	public void registerTabButton(NotebookDummyButton button) {
		buttons.put(new Integer(button.getIndex()), button);
	}
}
