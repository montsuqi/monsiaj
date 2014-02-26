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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.Font;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
 
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.text.JTextComponent;

public class TablePanel extends JPanel {
	
	/** <p>An action to select all text in the component when it is focused.</p>
	 */
	final class FieldSelected extends FocusAdapter {

		public void focusGained(FocusEvent e) {
			Object o = e.getSource();
			if ( ! (o instanceof JTextComponent)) {
				return;
			}
			JTextComponent tc = (JTextComponent)o;
			tc.setCaretPosition(tc.getText().length());
			tc.selectAll();
		}
	}
	
	public TablePanel() {
		super(new GridBagLayout());
	}
	
	public void addRow(int y, JComponent component)
	{
		GridBagConstraints gbc;
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = y;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.CENTER;
		add(component, gbc);
	}

	public void addRow(int y, String text, JComponent component) {
		addRow(y, 1, text, component);
	}

	public void addRow(int y, int h, String text, JComponent component) {
		GridBagConstraints gbc;
		JLabel label = new JLabel();
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setText(text);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = y;
		if (h == 1) {
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.WEST;
		} else {
			gbc.fill = GridBagConstraints.BOTH;
			gbc.anchor = GridBagConstraints.NORTHWEST;
		}
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.gridwidth = 1;
		gbc.gridheight = h;
		gbc.insets = new Insets(2, 2, 2, 2);
		add(label, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = y;
		gbc.gridwidth = 3;
		gbc.gridheight = h;
		if (h == 1) {
			gbc.fill = GridBagConstraints.HORIZONTAL;
		} else {
			gbc.fill = GridBagConstraints.BOTH;
		}
		gbc.weightx = 1.0;
		gbc.weighty = h;
		gbc.insets = new Insets(2, 2, 2, 2);
		add(component, gbc);
	}

	public JTextField addTextFieldRow(int y, String text, String value) {
		JTextField entry = new JTextField();
		entry.setText(value);
		entry.addFocusListener(new FieldSelected());
		addRow(y, text, entry);
		return entry;
	}

	public JTextField addIntFieldRow(int y, String text, int value) {
		JTextField entry = new JTextField();
		entry.setHorizontalAlignment(SwingConstants.LEFT);
		entry.setText(String.valueOf(value));
		entry.addFocusListener(new FieldSelected());
		addRow(y, text, entry);
		return entry;
	}

	public JTextField addLongFieldRow(int y, String text, long value) {
		JTextField entry = new JTextField();
		entry.setHorizontalAlignment(SwingConstants.LEFT);
		entry.setText(String.valueOf(value));
		entry.addFocusListener(new FieldSelected());
		addRow(y, text, entry);
		return entry;
	}
	
	public JPasswordField addPasswordFieldRow(int y, String text) {
		JPasswordField entry = new JPasswordField();
		entry.setText(""); 
		entry.addFocusListener(new FieldSelected());
		addRow(y, text, entry);
		return entry;
	}

	public JTextArea addTextAreaRow(int y, int rows, int cols, String text, String value) {
		JTextArea textArea = new JTextArea(rows, cols);
		textArea.setText(value);
		textArea.addFocusListener(new FieldSelected());
		JScrollPane scroll = new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setMinimumSize(new Dimension(0,100));
		addRow(y, rows, text, scroll);
		return textArea;
	}

	public JCheckBox addCheckBoxRow(int y, String text, boolean flag) {
		JCheckBox check = new JCheckBox();
		check.setSelected(flag);
		addRow(y, text, check);
		return check;
	}

	public JComboBox addComboBoxRow(int y, String text, String[] selections, String selected) {
		JComboBox combo = new JComboBox();
		combo.setEditable(false);
		for (int i = 0; i < selections.length; i++) {
			combo.addItem(selections[i]);
			if (selections[i].equals(selected)) {
				combo.setSelectedIndex(i);
			}
		}
		addRow(y, text, combo);
		return combo;
	}

	public JRadioButton[] addRadioButtonGroupRow(int y, String text, String[] selections, String selected) {
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
		addRow(y, text, panel);
		return radios;
	}

	public JButton addButtonFor(JTextField entry, Action action) {
		GridBagLayout gbl = (GridBagLayout)getLayout();

		GridBagConstraints gbc;
		gbc = gbl.getConstraints(entry);
		gbc.gridwidth = 2;
		gbl.setConstraints(entry, gbc);

		JButton button = new JButton();
		button.setAction(action);
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridwidth = 1;
		add(button, gbc);
		return button;
	}
	
}