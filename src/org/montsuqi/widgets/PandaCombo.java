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

package org.montsuqi.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;

public class PandaCombo extends JComboBox {

	class MoveSelectionAction extends AbstractAction {

		private int move;

		MoveSelectionAction(int move) {
			this.move = move;
		}

		public void actionPerformed(ActionEvent arg0) {
			int selected = getSelectedIndex();
			int newSelected = selected + move;
			int items = getItemCount();
			newSelected = newSelected < 0 ? 0 : items <= newSelected ? items - 1 : newSelected;
			setSelectedIndex(newSelected);
			JTextField editor = (JTextField)getEditor().getEditorComponent();
			String item = getSelectedItem().toString();
			editor.setText(getSelectedItem().toString());
		}
	}

	public PandaCombo() {
		super();
		setEditor(new PandaComboBoxEditor(this));
		initActions();
	}

	private void initActions() {
		ActionMap actionMap = getActionMap();
		InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		KeyStroke upKey = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
		KeyStroke downKey = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
		KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

		for (InputMap parent = inputMap; parent !=  null; parent = parent.getParent()) {
			parent.remove(upKey);
			parent.remove(downKey);
			parent.remove(enterKey);
		}
		actionMap.put("moveSelectionUp", new MoveSelectionAction(-1)); //$NON-NLS-1$
		actionMap.put("moveSelectionDown", new MoveSelectionAction(1)); //$NON-NLS-1$

		inputMap.put(upKey, "moveSelectionUp"); //$NON-NLS-1$
		inputMap.put(downKey, "moveSelectionDown"); //$NON-NLS-1$
		
		addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
				// do nothing
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
				JTextField text = (JTextField)getEditor().getEditorComponent();
				text.postActionEvent();
			}
			public void popupMenuCanceled(PopupMenuEvent arg0) {
				// do nothing
			}
		});
	}
}

class PandaComboBoxEditor extends BasicComboBoxEditor {

	public PandaComboBoxEditor(final PandaCombo combo) {
		editor  = new BorderlessPandaEntry("", 9); //$NON-NLS-1$
		editor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				combo.setSelectedItem(s);
				editor.setText(s);
			}
		});
	}

	static class BorderlessPandaEntry extends PandaEntry {
		public BorderlessPandaEntry(String value, int n) {
			super(value,n);
			setBorder(null);
			InputMap inputMap = getInputMap();
			inputMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
			inputMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
		}

		// workaround for 4530952
		public void setText(String s) {
			if (getText().equals(s)) {
				return;
			}
			super.setText(s);
		}

		public void setBorder(Border b) {
			//
		}
	}
}
