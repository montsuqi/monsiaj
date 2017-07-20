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

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.TitledBorder;

/** <p>A class that simulates Gtk+'s Frame(border with title) widget.</p>
 */
public class Frame extends JComponent {

    public Frame() {
        super();
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("")); 
    }

    public void setTitle(String newTitle) {
        TitledBorder border = (TitledBorder) getBorder();
        border.setTitle(newTitle);
    }

    public void setShadow(String shadow) {
        if (shadow == null) {
            return;
        }
        if ("GTK_SHADOW_NONE".equals(shadow)) {
            TitledBorder border = (TitledBorder) getBorder();
            setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), border.getTitle()));
        }
    }
}
