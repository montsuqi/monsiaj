package org.montsuqi.widgets;

import java.awt.LayoutManager;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import org.montsuqi.util.Logger;

public class VBox extends JComponent {

	public VBox() {
		super();
		super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

	public void setLayout(LayoutManager layout) {
		Logger.getLogger(VBox.class).info(Messages.getString("VBox.ignoring_VBox_setLayout")); //$NON-NLS-1$
	}

}
