package org.montsuqi.widgets;

import java.awt.LayoutManager;
import javax.swing.JComponent;
import org.montsuqi.util.Logger;

public class Fixed extends JComponent {
	public Fixed() {
		super();
		setLayout(null);
	}
	public void setLayout(LayoutManager layout) {
		Logger.getLogger(Fixed.class).info("ignoring Fixed#setLayout().");
	}
}
