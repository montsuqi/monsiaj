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

import java.awt.Container;
import javax.swing.JButton;

/**
 * <p>A class to help Notebook component to simulate Gtk+'s Notebook widget.</p>
 *
 * <p>NotebookDummyButton is not displayed but can react on actions(by
 * doClick()).</p>
 *
 * <p>Its text label and enabled/disabled status is propagated to corresponding
 * tab.</p>
 */
public class NotebookDummyButton extends JButton {

    private int index;
    private Notebook notebook;

    public NotebookDummyButton(String label, int index, Notebook notebook) {
        super(label);
        this.index = index;
        this.notebook = notebook;
        notebook.registerTabButton(this);
    }

    public int getIndex() {
        return index;
    }

    @Override
    public Container getParent() {
        return notebook;
    }

    @Override
    public void setText(String s) {
        super.setText(s);
        if (isValidTab()) {
            notebook.setTitleAt(index, s);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (isValidTab()) {
            notebook.setEnabledAt(index, enabled);
        }
    }

    private boolean isValidTab() {
        return notebook != null && 0 <= index && index < notebook.getTabCount();
    }
}