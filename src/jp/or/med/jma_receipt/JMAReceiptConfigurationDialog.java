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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;
import org.montsuqi.client.Configuration;
import org.montsuqi.client.ConfigurationDialog;

public class JMAReceiptConfigurationDialog extends ConfigurationDialog {

	private final class ExtensionFileFilter extends FileFilter {

		private String extension;
		private String description;

		ExtensionFileFilter(String extension, String description) {
			this.extension = extension;
			this.description = description;
		}

		public boolean accept(File f) {
			return f.isDirectory() || f.getPath().endsWith(extension);
		}

		public String getDescription() {
			return description;
		}
	}

	private JTextField hostEntry;
	private JPasswordField passwordEntry;
	private JTextField portEntry;
	JTextField styleEntry;
	private JTextField userEntry;

	protected void updateConfiguration() {
		conf.setUser(userEntry.getText()); //$NON-NLS-1$
		conf.setPass(new String(passwordEntry.getPassword()));
		conf.setHost(hostEntry.getText()); //$NON-NLS-1$
		conf.setPort(Integer.parseInt(portEntry.getText())); //$NON-NLS-1$
		conf.setApplication("orca00"); //$NON-NLS-1$ //$NON-NLS-2$
		conf.setEncoding("EUC-JP"); //$NON-NLS-1$ //$NON-NLS-2$
		conf.setStyles(styleEntry.getText()); //$NON-NLS-1$
		conf.setProtocolVersion(1);
		conf.setUseSSL(false);
		conf.setVerify(false);
	}

	JMAReceiptConfigurationDialog(String title, Configuration conf) {
		super(title, conf);
		setSize(320,240);
	}

	protected JComponent createIcon() {
		JLabel icon = new JLabel();
		URL iconURL = getClass().getResource("/orca2.jpg"); //$NON-NLS-1$
		if (iconURL != null) {
			icon.setIcon(new ImageIcon(iconURL));
		}
		return icon;
	}

	protected JComponent createControls() {
		JPanel controls = new JPanel();
		controls.setLayout(new GridBagLayout());
		GridBagConstraints gbc;

		JLabel userLabel = new JLabel();
		userLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		userLabel.setText(Messages.getString("JMAReceiptConfigurationDialog.user")); //$NON-NLS-1$
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.weighty = 1.0;
		controls.add(userLabel, gbc);

		userEntry = new JTextField();
		userEntry.setText(conf.getUser());
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		controls.add(userEntry, gbc);

		JLabel passwordLabel = new JLabel();
		passwordLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		passwordLabel.setText(Messages.getString("JMAReceiptConfigurationDialog.password")); //$NON-NLS-1$
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.weighty = 1.0;
		controls.add(passwordLabel, gbc);

		passwordEntry = new JPasswordField();
		passwordEntry.setHorizontalAlignment(SwingConstants.LEFT);
		passwordEntry.setText(""); //$NON-NLS-1$
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		controls.add(passwordEntry, gbc);

		JLabel hostLabel = new JLabel();
		hostLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		hostLabel.setText(Messages.getString("JMAReceiptConfigurationDialog.host")); //$NON-NLS-1$
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.weighty = 1.0;
		controls.add(hostLabel, gbc);

		hostEntry = new JTextField();
		hostEntry.setHorizontalAlignment(SwingConstants.LEFT);
		hostEntry.setText(conf.getHost());
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		controls.add(hostEntry, gbc);

		JLabel portLabel = new JLabel();
		portLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		portLabel.setText(Messages.getString("JMAReceiptConfigurationDialog.port")); //$NON-NLS-1$
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.weighty = 1.0;
		controls.add(portLabel, gbc);

		portEntry = new JTextField();
		portEntry.setHorizontalAlignment(SwingConstants.RIGHT);
		portEntry.setText(String.valueOf(conf.getPort()));
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		controls.add(portEntry, gbc);

		JLabel styleLabel = new JLabel();
		styleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		styleLabel.setText(Messages.getString("JMAReceiptConfigurationDialog.style")); //$NON-NLS-1$
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.weighty = 1.0;
		controls.add(styleLabel, gbc);

		styleEntry = new JTextField();
		styleEntry.setHorizontalAlignment(SwingConstants.LEFT);
		styleEntry.setText(conf.getStyles());
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		controls.add(styleEntry, gbc);

		JButton browseButton = new JButton();
		browseButton.setText(Messages.getString("JMAReceiptConfigurationDialog.browse")); //$NON-NLS-1$
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 4;
		controls.add(browseButton, gbc);

		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home")); //$NON-NLS-1$
				String extension = ".properties"; //$NON-NLS-1$
				String description = Messages.getString("JMAReceiptConfigurationDialog.filter_pattern"); //$NON-NLS-1$
				fileChooser.setFileFilter(new ExtensionFileFilter(extension, description));
				int ret = fileChooser.showOpenDialog(JMAReceiptConfigurationDialog.this);
				if (ret == JFileChooser.APPROVE_OPTION) {
					File styleFile = fileChooser.getSelectedFile();
					styleEntry.setText(styleFile.getAbsolutePath());
				}
			}
		});
		return controls;
	}
}
