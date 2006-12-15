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

import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.montsuqi.util.Logger;

/** <p>A class that simulates Gtk+'s Notebook widget.</p>
 * 
 * <p>Since Gtk+'s Notebook uses button for tabs, this component maps
 * a dummy button(NotebookDummyButton) for each tab to simulate its behavior.
 * Dummy buttons are not displayed but can react on action events.</p>
 */
public class Notebook extends JTabbedPane {

	Map buttons;
	protected static final Logger logger = Logger.getLogger(Notebook.class);

	public Notebook() {
		super();

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

	/** <p>Registers a dummy button to a tab.</p>
	 * 
	 * @param button a NotebookDummyButton with an index.
	 */
	public void registerTabButton(NotebookDummyButton button) {
		buttons.put(new Integer(button.getIndex()), button);
	}
}
