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

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class PandaCList extends JTable {

	class MoveAnchorAction extends AbstractAction {
		int dx;
		int dy;
		MoveAnchorAction(int dx, int dy) {
			this.dx = dx;
			this.dy = dy;
		}

		int withinRange(int value, int min, int max) {
			if (value < min) {
				return min;
			} else if (max < value) {
				return max;
			} else {
				return value;
			}
		}
		public void actionPerformed(final ActionEvent e) {
			JTable table = (JTable)e.getSource();
			ListSelectionModel rowSelections = table.getSelectionModel();
			ListSelectionModel columnSelections = table.getColumnModel().getSelectionModel();
			int leadRow = rowSelections.getLeadSelectionIndex();
			int leadCol = columnSelections.getLeadSelectionIndex();
			int anchorRow = rowSelections.getAnchorSelectionIndex();
			int anchorCol = columnSelections.getAnchorSelectionIndex();
			leadRow = withinRange(leadRow + dx, 0, table.getRowCount() - 1);
			leadCol = withinRange(leadCol + dy, 0, table.getColumnCount() - 1);
			anchorRow = withinRange(anchorRow + dx, 0, table.getRowCount() - 1);
			anchorCol = withinRange(anchorCol + dy, 0, table.getColumnCount() - 1);
			table.repaint();
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
		inputs.put(KeyStroke.getKeyStroke("TAB"), "focusOutNext");  //$NON-NLS-1$//$NON-NLS-2$

		actions.put("focusOutPrevious", new FocusOutPreviousAction()); //$NON-NLS-1$
		inputs.put(KeyStroke.getKeyStroke("shift TAB"), "focusOutPrevious"); //$NON-NLS-1$ //$NON-NLS-2$

//		actions.put("moveAnchorUp", new MoveAnchorAction(-1, 0)); //$NON-NLS-1$
//		inputs.put(KeyStroke.getKeyStroke("UP"), "moveAnchorUp");  //$NON-NLS-1$//$NON-NLS-2$
//
//		actions.put("moveAnchorDown", new MoveAnchorAction(1, 0)); //$NON-NLS-1$
//		inputs.put(KeyStroke.getKeyStroke("DOWN"), "moveAnchorDown"); //$NON-NLS-1$ //$NON-NLS-2$
//
//		actions.put("moveAnchorLeft", new MoveAnchorAction(0, -1)); //$NON-NLS-1$
//		inputs.put(KeyStroke.getKeyStroke("LEFT"), "moveAnchorLeft"); //$NON-NLS-1$ //$NON-NLS-2$
//
//		actions.put("moveAnchorRight", new MoveAnchorAction(0, 1)); //$NON-NLS-1$
//		inputs.put(KeyStroke.getKeyStroke("RIGHT"), "moveAnchorRight");  //$NON-NLS-1$//$NON-NLS-2$
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
