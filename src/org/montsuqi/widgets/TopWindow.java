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

import java.awt.Component;

import javax.swing.JComponent;

/** <p>A JFrame wrapper.</p>
 */
public class TopWindow extends Window {

    public void showWindow(Window window, boolean setLocation) {
        Component child = window.getChild();
        if (!child.isVisible()) {
            child.setVisible(true);
        }
        this.setName(window.getName());
        this.getContentPane().removeAll();
        this.getContentPane().add(child);
        this.setTitle(window.getTitle());
        this.setResizable(window.getAllow_Grow() && window.getAllow_Shrink());
        this.setSize(window.getSize());
        if (!this.isVisible()) {
            this.setVisible(true);
        }
        ((JComponent) child).revalidate();
        ((JComponent) child).repaint();
        if (setLocation) {
            this.setLocation(window.getLocation());
        }
        this.hideBusyCursor();
// is this necessary?
//        this.toFront();
        this.setEnabled(true);
        this.setChild(child);
        ((JComponent) child).requestFocusInWindow();
    }

    /** <p>Constructs a Window instance.</p>
     */
    public TopWindow() {
        super();
    }
}
