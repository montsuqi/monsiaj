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

import java.awt.GridBagConstraints;

public class TableConstraints {
	public int leftAttach;
	public int rightAttach;
	public int topAttach;
	public int bottomAttach;

	public int xPadding;
	public int yPadding;
	public boolean xExpand;
	public boolean yExpand;
	public boolean xShrink;
	public boolean yShrink;
	public boolean xFill;
	public boolean yFill;

	public TableConstraints() {
	}

	public TableConstraints(GridBagConstraints gbc) {
		leftAttach = gbc.gridx;
		topAttach = gbc.gridy;
		rightAttach = gbc.gridx + gbc.gridwidth;
		bottomAttach = gbc.gridy + gbc.gridheight;
		xPadding = gbc.ipadx;
		yPadding = gbc.ipady;
		xFill =
			gbc.fill == GridBagConstraints.HORIZONTAL ||
			gbc.fill == GridBagConstraints.BOTH;
		yFill =
			gbc.fill == GridBagConstraints.VERTICAL ||
			gbc.fill == GridBagConstraints.BOTH;
	}

	public GridBagConstraints toGridBagConstraints() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		if (xFill) {
			gbc.fill |= GridBagConstraints.HORIZONTAL;
		}
		if (yFill) {
			gbc.fill |= GridBagConstraints.VERTICAL;
		}
		gbc.gridheight = bottomAttach - topAttach;
		gbc.gridwidth = rightAttach - leftAttach;
		gbc.gridx = leftAttach;
		gbc.gridy = topAttach;
		gbc.ipadx = xPadding;
		gbc.ipady = yPadding;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		return gbc;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("TableChild["); //$NON-NLS-1$
		//		buf.append(comp.toString());
		buf.append("leftAttach=");   buf.append(leftAttach); //$NON-NLS-1$
		buf.append(", rightAttach=");  buf.append(rightAttach); //$NON-NLS-1$
		buf.append(", topAttach=");    buf.append(topAttach); //$NON-NLS-1$
		buf.append(", bottomAttach="); buf.append(bottomAttach); //$NON-NLS-1$
		buf.append("]"); //$NON-NLS-1$
		return buf.toString();
	}
}
