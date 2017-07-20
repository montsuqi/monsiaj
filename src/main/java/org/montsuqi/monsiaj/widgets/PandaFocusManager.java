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
import java.awt.DefaultKeyboardFocusManager;
import java.awt.event.KeyEvent;

import javax.swing.SwingUtilities;

import org.montsuqi.monsiaj.monsia.Interface;

/** <p>A focus manager which delegates actions to Interface object first.</p>
 * 
 * <p>It ignores key events if the window of the component is not active(is busy).</p>
 */
public class PandaFocusManager extends DefaultKeyboardFocusManager {

	public void processKeyEvent(Component focusedComponent, KeyEvent e) {
		java.awt.Window w = SwingUtilities.windowForComponent(focusedComponent);
		// Busy windows should not accept key events.
		if (w instanceof Window && ! ((Window)w).isActive()) {
			return;
		}
		// if the event is handled by the Interface, do nothing further.
		if (Interface.handleAccels(e)) {
			e.consume();
		} else {
			super.processKeyEvent(focusedComponent, e);
		}
	}
}
