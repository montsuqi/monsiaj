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

package org.montsuqi.client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import org.montsuqi.util.Logger;

public abstract class ConfigurationDialog extends JDialog {

	protected static final Logger logger = Logger.getLogger(ConfigurationDialog.class);

	protected Configuration conf;

	protected ConfigurationDialog(String title, Configuration conf) {
		super();
		setTitle(title);
		this.conf = conf;
		initComponents();
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});

		JRootPane root = getRootPane();
		InputMap inputMap = root.getInputMap();
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close-it"); //$NON-NLS-1$
		ActionMap actionMap = root.getActionMap();
		actionMap.put("close-it", new AbstractAction() { //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		setModal(true);
		setSize(320, 240);
	}

	protected abstract void updateConfiguration();

	protected void initComponents() {
		Container content = getContentPane();
		content.setLayout(new BorderLayout());
		content.add(createConfigurationPanel(), BorderLayout.CENTER);

		JPanel buttonBar = new JPanel();
		buttonBar.setLayout(new FlowLayout(FlowLayout.RIGHT));
		content.add(buttonBar, BorderLayout.SOUTH);

		JButton runButton = new JButton(Messages.getString("ConfigurationDialog.run")); //$NON-NLS-1$
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				conf.setConfigured(true);
				updateConfiguration();
				dispose();
			}
		});
		buttonBar.add(runButton);
		getRootPane().setDefaultButton(runButton);
	}

	private JComponent createConfigurationPanel() {
		JComponent panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc;

		JComponent icon = createIcon();
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		panel.add(icon, gbc);

		JComponent controls = createControls();
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		panel.add(controls, gbc);

		return panel;
	}

	protected JComponent createIcon() {
		JComponent icon;
		icon = new JPanel();
		icon.setBounds(0, 0, 0, 0);
		return icon;
	}

	protected abstract JComponent createControls();

	private void addRow(JPanel container, int y, String text, JComponent component) {
		GridBagConstraints gbc;
		JLabel label = new JLabel();
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setText(text);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = y;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.weighty = 1.0;
		container.add(label, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = y;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		container.add(component, gbc);
	}

	protected JTextField addTextRow(JPanel container, int y, String text, String value) {
		JTextField entry = new JTextField();
		entry.setText(value);
		addRow(container, y, text, entry);
		return entry;
	}

	protected JTextField addIntRow(JPanel container, int y, String text, int value) {
		JTextField entry = new JTextField();
		entry.setHorizontalAlignment(SwingConstants.RIGHT);
		entry.setText(String.valueOf(value));
		addRow(container, y, text, entry);
		return entry;
	}

	protected JPasswordField addPasswordRow(JPanel container, int y, String text) {
		JPasswordField entry = new JPasswordField();
		entry.setText(""); //$NON-NLS-1$
		addRow(container, y, text, entry);
		return entry;
	}

	protected void addButtonFor(JPanel container, JTextField entry, String buttonText, ActionListener listener) {
		GridBagLayout gbl = (GridBagLayout)container.getLayout();

		GridBagConstraints gbc;
		gbc = gbl.getConstraints(entry);
		gbc.gridwidth = 2;
		gbl.setConstraints(entry, gbc);

		int y = gbc.gridy;
		JButton button = new JButton();
		button.setText(buttonText);
		button.addActionListener(listener);
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = y;
		container.add(button, gbc);
	}

	protected JComboBox addComboRow(JPanel container, int y, String text, String[] selections, String selected) {
		JComboBox combo = new JComboBox();
		combo.setEditable(false);
		for (int i = 0; i < selections.length; i++) {
			combo.addItem(selections[i]);
			if (selections[i].equals(selected)) {
				combo.setSelectedIndex(i);
			}
		}
		addRow(container, y, text, combo);
		return combo;
	}

	protected JRadioButton[] addRadioGroupRow(JPanel container, int y, String text, String[] selections, String selected) {
		ButtonGroup group = new ButtonGroup();
		JRadioButton[] radios = new JRadioButton[selections.length];
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, selections.length));
		for (int i = 0; i < selections.length; i++) {
			JRadioButton radio = new JRadioButton(selections[i]);
			radio.setSelected(selections[i].equals(selected));
			group.add(radio);
			panel.add(radio);
			radios[i] = radio;
		}
		addRow(container, y, text, panel);
		return radios;
	}
}
