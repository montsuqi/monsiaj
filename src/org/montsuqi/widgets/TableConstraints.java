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
		if (xFill) {
			if (yFill) {
				gbc.fill = GridBagConstraints.BOTH;
			} else {
				gbc.fill = GridBagConstraints.HORIZONTAL;
			}
		} else {
			if (yFill) {
				gbc.fill = GridBagConstraints.VERTICAL;
			} else {
				gbc.fill = GridBagConstraints.NONE;
			}
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
