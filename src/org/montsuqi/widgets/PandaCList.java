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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultListSelectionModel;
import javax.swing.InputMap;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class PandaCList extends JTable {

	class MoveAction extends AbstractAction {

		int rowMove;
		int columnMove;

		MoveAction(int rowMove, int columnMove) {
			this.rowMove = rowMove;
			this.columnMove = columnMove;
		}

		public void actionPerformed(final ActionEvent e) {
			JTable table = (JTable)e.getSource();
			moveIndex((DefaultListSelectionModel)table.getSelectionModel(), rowMove, table.getRowCount() - 1);
			moveIndex((DefaultListSelectionModel)table.getColumnModel().getSelectionModel(), columnMove, table.getColumnCount() - 1);
			table.repaint();
		}

		private void moveIndex(DefaultListSelectionModel selections, int move, int max) {
			ListSelectionListener[] listeners = (ListSelectionListener[])selections.getListeners(ListSelectionListener.class);
			for (int i = 0, n = listeners.length; i < n; i++) {
				selections.removeListSelectionListener(listeners[i]);
			}
			int lead = selections.getLeadSelectionIndex() + move;
			lead = lead < 0 ? 0 : max < lead ? max : lead;
			selections.setLeadSelectionIndex(lead);
			selections.setAnchorSelectionIndex(lead);
			for (int i = 0, n = listeners.length; i < n; i++) {
				selections.addListSelectionListener(listeners[i]);
			}
		}
	}

	public class FocusOutNextAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			Component c = (Component)e.getSource();
			c.transferFocus();
		}
	}

	public class FocusOutPreviousAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			Component c = (Component)e.getSource();
			c.transferFocusBackward();
		}
	}

	public PandaCList() {
		ActionMap actions = getActionMap();
		InputMap inputs = getInputMap();

		actions.put("focusOutNext", new FocusOutNextAction()); //$NON-NLS-1$
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "focusOutNext"); //$NON-NLS-1$

		actions.put("focusOutPrevious", new FocusOutPreviousAction()); //$NON-NLS-1$
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), "focusOutPrevious"); //$NON-NLS-1$

		actions.put("moveUp", new MoveAction(-1, 0)); //$NON-NLS-1$
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "moveUp"); //$NON-NLS-1$

		actions.put("moveDown", new MoveAction(1, 0)); //$NON-NLS-1$
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "moveDown"); //$NON-NLS-1$

		actions.put("moveLeft", new MoveAction(0, -1)); //$NON-NLS-1$
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "moveLeft"); //$NON-NLS-1$

		actions.put("moveRight", new MoveAction(0, 1)); //$NON-NLS-1$
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "moveRight"); //$NON-NLS-1$
	}

	public void createDefaultColumnsFromModel() {
		TableColumnModel model = getColumnModel();
		int n = getColumnCount();
		int[] widths = new int[n];
		for (int i = 0; i < n; i++) {
			TableColumn column = model.getColumn(i);
			widths[i] = column.getPreferredWidth();
		}
		super.createDefaultColumnsFromModel();
		for (int i = 0; i < n; i++) {
			TableColumn column = model.getColumn(i);
			column.setPreferredWidth(widths[i]);
			column.setMinWidth(widths[i]);
		}
	}

	public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
		super.changeSelection(rowIndex, columnIndex, true, extend);
	}
}
