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

import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

/** <p>A class to simulate Gtk+'s Entry widget.</p>
 * 
 * <p>Down arrow key moves focus out to the next component, and
 * up arrow key moves focus out to the previous component respectively.</p>
 * 
 * <p>This component uses LengthLimitableDocument by default and length of
 * the text can be set.</p>
 */
public class Entry extends JTextField {

	public Entry() {
		super();
		setDocument(new LengthLimitableDocument());
		initActions();
	}

	public Entry(String text, int n) {
		super();
		setDocument(new LengthLimitableDocument());
		setText(text);
		setColumns(n);
		initActions();
	}

	private void initActions() {
		ActionMap actions = getActionMap();
		InputMap inputs = getInputMap();

		actions.put("focusOutNext", new FocusOutNextAction()); 
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "focusOutNext"); 

		actions.put("focusOutPrevious", new FocusOutPreviousAction()); 
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "focusOutPrevious"); 
		addFocusListener(new EntryFocusListener());
	}

	public void setLimit(int limit) {
		LengthLimitableDocument doc = (LengthLimitableDocument)getDocument();
                if (limit <= 0) {
                    limit = Integer.MAX_VALUE;
                }
		doc.setLimit(limit);
	}

	public int getLimit() {
		LengthLimitableDocument doc = (LengthLimitableDocument)getDocument();
		return doc.getLimit();
	}
	
    private static class EntryFocusListener implements FocusListener {
       public void focusGained(final FocusEvent e) {
    	   JTextField tf = (JTextField)e.getSource();
            tf.setCaretPosition(Math.max(0, tf.getText().length()));
        }
       public void focusLost(final FocusEvent e) {
    	   JTextField tf = (JTextField)e.getSource();
            tf.setCaretPosition(0);
       }        
    }
}
