package org.montsuqi.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JFrame;

public class TableLayout extends GridBagLayout {
	public void addLayoutComponent(Component comp, Object object) {
		if (object instanceof TableConstraints) {
			setConstraints(comp, (TableConstraints)object);
		} else if (object != null) {
			throw new IllegalArgumentException("Unacceptable constraints type.");
		}
	}
	
	public void setConstraints(Component comp, TableConstraints constraints) {
		if (constraints.leftAttach >= constraints.rightAttach) {
			throw new IllegalArgumentException("Wrong constraints.");
		}
		if (constraints.topAttach >= constraints.bottomAttach) {
			throw new IllegalArgumentException("Wrong constraints.");
		}
		super.setConstraints(comp, constraints.toGridBagConstraints());
	}

	public static void main(String[] args) {
		JFrame f = new JFrame("TableLayoutTest");
		Container container = f.getContentPane();

		TableLayout tl = new TableLayout();
		container.setLayout(tl);
		TableConstraints tc = new TableConstraints();
		JLabel label;

		label = new JLabel("AAA");
		label.setBorder(BorderFactory.createLineBorder(Color.red));
		tc.leftAttach = 0;
		tc.rightAttach = 1;
		tc.topAttach = 0;
		tc.bottomAttach = 1;
		tc.xFill = true;
		tc.yFill = true;
		tl.setConstraints(label, tc);
		container.add(label);


		label = new JLabel("BBB");
		label.setBorder(BorderFactory.createLineBorder(Color.red));
		tc.leftAttach = 1;
		tc.rightAttach = 2;
		tl.setConstraints(label, tc);
		container.add(label);

		label = new JLabel("CCC");
		label.setBorder(BorderFactory.createLineBorder(Color.red));
		tc.leftAttach = 2;
		tc.rightAttach = 3;
		tl.setConstraints(label, tc);
		container.add(label);

		label = new JLabel("DDD");
		label.setBorder(BorderFactory.createLineBorder(Color.red));
		tc.leftAttach = 3;
		tc.rightAttach = 4;
		tl.setConstraints(label, tc);
		container.add(label);

		label = new JLabel("EEE");
		label.setBorder(BorderFactory.createLineBorder(Color.red));
		tc.leftAttach = 0;
		tc.rightAttach = 4;
		tc.topAttach = 1;
		tc.bottomAttach = 2;
		tl.setConstraints(label, tc);
		container.add(label);

		label = new JLabel("FFF");
		label.setBorder(BorderFactory.createLineBorder(Color.red));
		tc.leftAttach = 0;
		tc.rightAttach = 3;
		tc.topAttach = 2;
		tc.bottomAttach = 3;
		tl.setConstraints(label, tc);
		container.add(label);

		label = new JLabel("GGG");
		label.setBorder(BorderFactory.createLineBorder(Color.red));
		tc.leftAttach = 3;
		tc.rightAttach = 4;
		tl.setConstraints(label, tc);
		container.add(label);

		label = new JLabel("HHH");
		label.setBorder(BorderFactory.createLineBorder(Color.red));
		tc.leftAttach = 0;
		tc.rightAttach = 1;
		tc.topAttach = 3;
		tc.bottomAttach = 5;
		tl.setConstraints(label, tc);
		container.add(label);

		label = new JLabel("III");
		label.setBorder(BorderFactory.createLineBorder(Color.red));
		tc.leftAttach = 1;
		tc.rightAttach = 4;
		tc.topAttach = 3;
		tc.bottomAttach = 4;
		tl.setConstraints(label, tc);
		container.add(label);

		label = new JLabel("JJJ");
		label.setBorder(BorderFactory.createLineBorder(Color.red));
		tc.leftAttach = 1;
		tc.rightAttach = 4;
		tc.topAttach = 4;
		tc.bottomAttach = 5;
		tl.setConstraints(label, tc);
		container.add(label);

		f.setSize(200, 200);
		f.setVisible(true);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}
}
