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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

/** <p>A CellRenderer that is used for CList's cells whose representation is a Fixed widget.</p>
 */
class CListFixedCellRenderer extends Fixed implements TableCellRenderer {

    private static final Border NO_FOCUS_BORDER;
	private static final Pattern EXPRESSION_PATTERN;

	static {
	    NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1); 
		EXPRESSION_PATTERN = Pattern.compile("(?:[ \u3000]+)([0-9\uff10-\uff19]+(?:\u00d7[0-9\uff10-\uff19]+)?)(?:[ \u3000]*)\\z"); //$NON-NLS-1$
	}

	private int[] positions;

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	public CListFixedCellRenderer(Fixed fixed) {
		super();
		setOpaque(true);

		int nLabels = fixed.getComponentCount();
		positions = new int[nLabels];
		for (int i = 0; i < nLabels; i++) {
			positions[i] = fixed.getComponent(i).getX();
		}
		Arrays.sort(positions);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Rectangle cellRect = table.getCellRect(row, column, false);
		setSize(cellRect.width, cellRect.height);

		removeAll();
		JLabel[] labels = new JLabel[positions.length];
		// overlay labels to render components in Fixed.
		// +------------------------------+
		// |+--------+-------+-----------+|
		// ||Label1  |Label2 |Label3     ||
		// |+--------+-------+-----------+|
		// +------------------------------+
		//      |                  |
		//      v                  v
		//  +----------------------------+
		//  |Label1                      |
		//  +----------------------------+
		//           +-------------------+
		//           |Label2             |
		//           +-------------------+
		//                   +-----------+
		//                   |Label3     |
		//                   +-----------+
		for (int i = 0, n = labels.length; i < n; i++) {
			JLabel label = new JLabel(""); //$NON-NLS-1$
			label.setFont(table.getFont());
			label.setOpaque(false);
			label.setLocation(positions[i], 0);
			label.setSize(cellRect.width - positions[i], cellRect.height);
			labels[i] = label;
			add(labels[i]);
		}

		String s = value.toString();
		for (int i = labels.length - 1; i >= 1; i--) {
			Matcher match = EXPRESSION_PATTERN.matcher(s);
			if ( ! match.find(0)) {
				break;
			}
			labels[i].setText(match.group(1));
			s = match.replaceAll(""); //$NON-NLS-1$
		}
		labels[0].setText(s);

		setOpaque(true);
		if (isSelected) {
			setForeground(table.getSelectionForeground());
			setBackground(table.getSelectionBackground());
		} else {
			setForeground(table.getForeground());
			setBackground(table.getBackground());
		}
		if (hasFocus) {
			setBorder(UIManager.getBorder("Table.focusCellHighlightBorder")); //$NON-NLS-1$
			if ( ! isSelected && table.isCellEditable(row, column)) {
				Color col = UIManager.getColor("Table.focusCellForeground"); //$NON-NLS-1$
				if (col != null) {
					setForeground(col);
				}
				col = UIManager.getColor("Table.focusCellBackground"); //$NON-NLS-1$
				if (col != null) {
					setBackground(col);
				}
			}
		} else {
			setBorder(NO_FOCUS_BORDER);
		}

		return this;
	}
}
