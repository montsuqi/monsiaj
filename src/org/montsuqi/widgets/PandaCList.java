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

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class PandaCList extends JTable {

	public PandaCList() {
		super();
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
