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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

public abstract class ConfigurationDialog extends JDialog {

	protected Configuration conf;

	protected abstract void updateConfiguration();
	protected abstract JComponent createIcon();
	protected abstract JComponent createControls();
	
	protected ConfigurationDialog(String title, Configuration conf) {
		super();
		setTitle(title);
		this.conf = conf;
		initComponents();
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				closeDialog();
			}
		});
		setModal(true);
	}

	private void initComponents() {
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
				closeDialog();
			}
		});
		buttonBar.add(runButton);
		getRootPane().setDefaultButton(runButton);
	}

	void closeDialog() {
		setVisible(false);
		dispose();
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

}
