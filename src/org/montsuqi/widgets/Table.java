package org.montsuqi.widgets;

import java.awt.LayoutManager;
import javax.swing.JComponent;
import org.montsuqi.util.Logger;

public class Table extends JComponent {
	public Table() {
		super();
		setLayout(new TableLayout());
	}
	public void setLayout(LayoutManager layout) {
		Logger.getLogger(VBox.class).info(Messages.getString("Table.ignoring_Table_setLayout")); //$NON-NLS-1$
	}
}
