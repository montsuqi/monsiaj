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

package org.montsuqi.monsiaj.monsia.builders;

import java.awt.Component;
import java.awt.Container;
import javax.swing.text.JTextComponent;
import org.montsuqi.monsiaj.monsia.Interface;
import org.montsuqi.monsiaj.monsia.WidgetInfo;

/** <p>A builder to create Entry widgets.</p>
 */
public class EntryBuilder extends WidgetBuilder {

        @Override
	Component buildSelf(Interface xml, Container parent, WidgetInfo info) {
		// In Gtk+ entry widgets, can_focus property is set false by default.
		// To simulate Gtk+ behavior, temporally set can_focus to false.
		final String canFocus = info.getProperty("can_focus"); 
		info.setProperty("can_focus", "false");  //$NON-NLS-2$
		final Component c = super.buildSelf(xml, parent, info);
		final JTextComponent text = (JTextComponent)c;
		if (canFocus != null) {
			// set can_focus explicitly here.
			final WidgetPropertySetter setter = WidgetPropertySetter.getSetter(c.getClass(), "can_focus");
			setter.set(xml, parent, c, canFocus);
		}
		return text;
	}
}
