package org.montsuqi.widgets;

import java.awt.LayoutManager;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import org.montsuqi.util.Logger;

public class HBox extends JComponent {
	public HBox() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}
	public void setLayout(LayoutManager layout) {
		Logger.getLogger(HBox.class).info("ignoring HBox#setLayout().");
	}
}
