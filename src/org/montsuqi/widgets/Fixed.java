package org.montsuqi.widgets;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JComponent;
import org.montsuqi.util.Logger;

public class Fixed extends JComponent {
	public Fixed() {
		super();
		super.setLayout(null);
	}
	public void setLayout(LayoutManager layout) {
		Logger.getLogger(Fixed.class).info(Messages.getString("Fixed.ignoring_Fixed__setLayout")); //$NON-NLS-1$
	}

	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		setPreferredSize(new Dimension(width, height));
	}

	public void setBounds(Rectangle r) {
		super.setBounds(r);
		setPreferredSize(new Dimension(r.width, r.height));
	}

	public void setSize(Dimension d) {
		super.setSize(d);
		setPreferredSize(d);
	}

	public void setSize(int width, int height) {
		super.setSize(width, height);
		setPreferredSize(new Dimension(width, height));
	}
}
