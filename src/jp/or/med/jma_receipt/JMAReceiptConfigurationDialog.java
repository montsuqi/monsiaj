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

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.text.JTextComponent;
import org.montsuqi.client.Configuration;
import org.montsuqi.client.ConfigurationDialog;
import org.montsuqi.client.Messages;
import org.montsuqi.util.ExtensionFileFilter;

public class JMAReceiptConfigurationDialog extends ConfigurationDialog {

	private JTextField hostEntry;
	private JPasswordField passwordEntry;
	private JTextField portEntry;
	private JTextField styleEntry;
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
		String className = UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(className);
		} catch (Exception e) {
			logger.warn(e);
		}
		SwingUtilities.updateComponentTreeUI(this);
		setSize(480, 240);
	}

	protected JComponent createIcon() {
		JLabel icon = new JLabel();
		URL iconURL = getClass().getResource("/jp/or/med/jma_receipt/standard60.png"); //$NON-NLS-1$
		if (iconURL != null) {
			icon.setIcon(new ImageIcon(iconURL));
		}
		icon.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		return icon;
	}

	protected JComponent createControls() {
		JPanel controls = new JPanel();
		controls.setLayout(new GridBagLayout());

		int y = 0;
		userEntry = addTextRow(controls, y++, Messages.getString("DefaultConfigurationDialog.user"), conf.getUser()); //$NON-NLS-1$
		passwordEntry = addPasswordRow(controls, y++, Messages.getString("DefaultConfigurationDialog.password")); //$NON-NLS-1$
		hostEntry = addTextRow(controls, y++, Messages.getString("DefaultConfigurationDialog.host"), conf.getHost()); //$NON-NLS-1$
		portEntry = addIntRow(controls, y++, Messages.getString("DefaultConfigurationDialog.port"), conf.getPort()); //$NON-NLS-1$
		styleEntry = addTextRow(controls, y++, Messages.getString("DefaultConfigurationDialog.style"), conf.getStyles()); //$NON-NLS-1$

		final JTextComponent entry = styleEntry; // to tunnel access control
		addButtonFor(controls, styleEntry, Messages.getString("DefaultConfigurationDialog.browse"), new ActionListener() { //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home")); //$NON-NLS-1$
				String extension = ".properties"; //$NON-NLS-1$
				String description = Messages.getString("DefaultConfigurationDialog.filter_pattern"); //$NON-NLS-1$
				fileChooser.setFileFilter(new ExtensionFileFilter(extension, description));
				int ret = fileChooser.showOpenDialog(JMAReceiptConfigurationDialog.this);
				if (ret == JFileChooser.APPROVE_OPTION) {
					File styleFile = fileChooser.getSelectedFile();
					entry.setText(styleFile.getAbsolutePath());
				}
			}
		});
		return controls;
	}
}
