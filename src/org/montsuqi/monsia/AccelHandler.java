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

package org.montsuqi.monsia;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractButton;

class AccelHandler {
	class Pair {
		Component widget;
		AccelInfo accel;
		Pair(Component widget, AccelInfo accel) {
			this.widget = widget;
			this.accel = accel;
		}
	}

	private List list;

	AccelHandler() {
		list = new ArrayList();
	}

	void addAccels(Component widget, List accels) {
		Iterator i = accels.iterator();
		while (i.hasNext()) {
			AccelInfo accel = (AccelInfo)i.next();
			list.add(new Pair(widget, accel));
		}
	}

	boolean handleAccel(KeyEvent e) {
		if (e.getID() != KeyEvent.KEY_PRESSED) {
			return false;
		}
		Iterator i = list.iterator();
		while (i.hasNext()) {
			Pair pair = (Pair)i.next();
			Component widget = pair.widget;
			if ( ! (widget instanceof AbstractButton)) {
				continue;
			}
			AbstractButton button = (AbstractButton)widget;
			AccelInfo accel = pair.accel;
			if (accel.getKey() == e.getKeyCode()&& accel.getModifiers() == e.getModifiers()) {
				button.doClick();
				return true;
			}
		}
		return false;
	}
}
