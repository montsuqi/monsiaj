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

import java.awt.Insets;
import java.awt.event.KeyEvent;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;

/** <p>A class that simulates Gtk+'s ToggleButton.</p>
 * 
 * <p>Down arrow key moves focus out to the next component, and
 * up arrow key moves focus out to the previous component respectively.</p>
 */
public class ToggleButton extends JToggleButton {

	public ToggleButton() {
		super();
		setMargin(new Insets(0, -20, 0, -20));
		initActions();
	}

	private void initActions() {
		ActionMap actions = getActionMap();
		actions.put("focusOutNext", new FocusOutNextAction()); 
		actions.put("focusOutPrevious", new FocusOutPreviousAction()); 

		InputMap inputs = getInputMap(JComponent.WHEN_FOCUSED);
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "focusOutNext"); 
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "focusOutPrevious"); 
	}
}
