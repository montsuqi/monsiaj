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

package jp.or.med.jma_receipt;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

class ConfigurationDialog extends JDialog {
	private Configuration conf;
	private boolean needRun;

	private JTextField appEntry;
	private JTextField hostEntry;
	private JPasswordField passwordEntry;
	private JTextField portEntry;
	private JTextField styleEntry;
	private JTextField userEntry;

	ConfigurationDialog(Configuration conf) {
		this.conf = conf;
		needRun = false;
		initComponents();
		setModal(true);
	}

	Configuration getConfiguration() {
		return conf;
	}

	boolean needRun() {
		return needRun;
	}

	private void initComponents() {
		GridBagConstraints gridBagConstraints;

		Container root = getContentPane();
		root.setLayout(new GridBagLayout());

		setTitle(Messages.getString("ConfigurationDialog.JMA_Standard_Receipt")); //$NON-NLS-1$
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				closeDialog(evt);
			}
		});

		JLabel logo = new JLabel();
		URL iconURL = getClass().getResource("/orca2.jpg"); //$NON-NLS-1$
		if (iconURL != null) {
			Icon icon = new ImageIcon(iconURL);
			logo.setIcon(icon);
		}
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridheight = 3;
		root.add(logo, gridBagConstraints);

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JLabel userLabel = new JLabel();
		userLabel.setHorizontalAlignment(JLabel.RIGHT);
		userLabel.setText(Messages.getString("ConfigurationDialog.User")); //$NON-NLS-1$
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		gridBagConstraints.weighty = 1.0;
		panel.add(userLabel, gridBagConstraints);

		JLabel passwordLabel = new JLabel();
		passwordLabel.setHorizontalAlignment(JLabel.RIGHT);
		passwordLabel.setText(Messages.getString("ConfigurationDialog.Password")); //$NON-NLS-1$
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		gridBagConstraints.weighty = 1.0;
		panel.add(passwordLabel, gridBagConstraints);

		userEntry = new JTextField();
		userEntry.setText(conf.getUser());
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		panel.add(userEntry, gridBagConstraints);

		passwordEntry = new JPasswordField();
		passwordEntry.setHorizontalAlignment(JTextField.LEFT);
		passwordEntry.setText(""); //$NON-NLS-1$
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		panel.add(passwordEntry, gridBagConstraints);

		JLabel hostLabel = new JLabel();
		hostLabel.setHorizontalAlignment(JLabel.RIGHT);
		hostLabel.setText(Messages.getString("ConfigurationDialog.Host")); //$NON-NLS-1$
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		gridBagConstraints.weighty = 1.0;
		panel.add(hostLabel, gridBagConstraints);

		hostEntry = new JTextField();
		hostEntry.setHorizontalAlignment(JTextField.LEFT);
		hostEntry.setText(conf.getHost());
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		panel.add(hostEntry, gridBagConstraints);

		JLabel portLabel = new JLabel();
		portLabel.setHorizontalAlignment(JLabel.RIGHT);
		portLabel.setText(Messages.getString("ConfigurationDialog.Port")); //$NON-NLS-1$
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		gridBagConstraints.weighty = 1.0;
		panel.add(portLabel, gridBagConstraints);

		portEntry = new JTextField();
		portEntry.setHorizontalAlignment(JTextField.RIGHT);
		portEntry.setText(String.valueOf(conf.getPort()));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		panel.add(portEntry, gridBagConstraints);

		JLabel appLabel = new JLabel();
		appLabel.setHorizontalAlignment(JLabel.RIGHT);
		appLabel.setText(Messages.getString("ConfigurationDialog.Application")); //$NON-NLS-1$
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		gridBagConstraints.weighty = 1.0;
		panel.add(appLabel, gridBagConstraints);

		appEntry = new JTextField();
		appEntry.setHorizontalAlignment(JTextField.LEFT);
		appEntry.setText("orca00"); //$NON-NLS-1$
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		panel.add(appEntry, gridBagConstraints);

		JLabel styleLabel = new JLabel();
		styleLabel.setHorizontalAlignment(JLabel.RIGHT);
		styleLabel.setText(Messages.getString("ConfigurationDialog.Style")); //$NON-NLS-1$
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		gridBagConstraints.weighty = 1.0;
		panel.add(styleLabel, gridBagConstraints);

		styleEntry = new JTextField();
		styleEntry.setHorizontalAlignment(JTextField.LEFT);
		styleEntry.setText(String.valueOf(conf.getStyleFile()));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		panel.add(styleEntry, gridBagConstraints);

		JButton browseButton = new JButton();
		browseButton.setText(Messages.getString("ConfigurationDialog.Browse")); //$NON-NLS-1$
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 5;
		panel.add(browseButton, gridBagConstraints);

		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home")); //$NON-NLS-1$
				fileChooser.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						if (f.getPath().endsWith(".properties")) { //$NON-NLS-1$
							return true;
						}
						if (f.isDirectory()) {
							return true;
						}
						return false;
					}

					public String getDescription() {
						return Messages.getString("ConfigurationDialog.filter.pattern"); //$NON-NLS-1$
					}
				});
				int ret = fileChooser.showOpenDialog(ConfigurationDialog.this);
				if (ret == JFileChooser.APPROVE_OPTION) {
					File styleFile = fileChooser.getSelectedFile();
					styleEntry.setText(styleFile.getAbsolutePath());
				}
			}
		});

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.gridheight = 2;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		root.add(panel, gridBagConstraints);

		JButton runButton = new JButton();
		runButton.setText(Messages.getString("ConfigurationDialog.Run")); //$NON-NLS-1$

		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				needRun = true;
				conf.setUser(userEntry.getText());
				conf.setPass(passwordEntry.getPassword());
				conf.setHost(hostEntry.getText());
				conf.setPort(Integer.parseInt(portEntry.getText()));
				conf.setApplication(appEntry.getText());
				conf.setStyleFile(styleEntry.getText());
				closeDialog(null);
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		root.add(runButton, gridBagConstraints);
		getRootPane().setDefaultButton(runButton);
		setSize(320,240);
	}

	void closeDialog(WindowEvent evt) {
		setVisible(false);
		dispose();
	}
}
