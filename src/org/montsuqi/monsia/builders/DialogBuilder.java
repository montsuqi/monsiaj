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

package org.montsuqi.monsia.builders;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;

import org.montsuqi.monsia.Interface;
import org.montsuqi.monsia.WidgetInfo;
import org.montsuqi.util.ParameterConverter;

public class DialogBuilder extends WindowBuilder {

	Component buildSelf(Interface xml, Container parent, WidgetInfo info) {
		Component widget = super.buildSelf(xml, parent, info);
		String modal = info.getProperty("modal"); //$NON-NLS-1$
		if (modal != null) {
			JDialog dialog = (JDialog)widget;
			dialog.setModal(ParameterConverter.toBoolean(modal));
			dialog.setModal(false);
		}
		return widget;
	}

	
	void buildChildren(Interface xml, Container parent, WidgetInfo info) {
		super.buildChildren(xml, parent, info);
		List buttons = new ArrayList();
		lookupButtons(parent, buttons);
		if (buttons.size() == 1) {
			JDialog dialog = (JDialog)parent;
			dialog.getRootPane().setDefaultButton((JButton)buttons.get(0));
		}
	}

	private void lookupButtons(Container parent, List buttons) {
		for (int i = 0, n = parent.getComponentCount(); i < n; i++) {
			Component c = parent.getComponent(i);
			if (c instanceof JButton) {
				buttons.add(c);
			} else if (c instanceof Container) {
				lookupButtons((Container)c, buttons);
			}
		}
	}
}
