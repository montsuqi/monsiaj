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
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

class CListFixedCellRenderer extends Fixed implements TableCellRenderer {

	Fixed fixed;
	static Pattern expression;

	static {
		expression = Pattern.compile("(?:[ \u3000]+)([0-9\uff10-\uff19]+(?:\u00d7[0-9\uff10-\uff19]+)?)(?:[ \u3000]*)\\z"); //$NON-NLS-1$
	}

	public CListFixedCellRenderer(Fixed fixed) {
		super();
		this.fixed = fixed;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		String s = value.toString();
		setSize(fixed.getSize());
		int nLabels = fixed.getComponentCount();
		removeAll();
		int[] positions = new int[nLabels];
		for (int i = 0; i < nLabels; i++) {
			positions[i] = fixed.getComponent(i).getX();
		}
		Arrays.sort(positions);
		JLabel[] labels = new JLabel[nLabels];
		for (int i = 0; i < nLabels; i++) {
			JLabel label = new JLabel();
			label.setOpaque(false);
			label.setText(""); //$NON-NLS-1$
			label.setLocation(positions[i], 0);
			Rectangle r = table.getCellRect(row, column, false);
			r.x = positions[i];
			r.y = 0;
			r.width = table.getWidth();
			label.setBounds(r);
			labels[i] = label;
			add(label);
		}
		for (int i = nLabels - 1; i >= 1; i--) {
			Matcher match = expression.matcher(s);
			if ( ! match.find(0)) {
				break;
			}
			labels[i].setText(match.group(1));
			s = match.replaceAll(""); //$NON-NLS-1$
		}
		labels[0].setText(s);
		return this;
	}
}
