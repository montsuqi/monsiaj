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

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;
import org.montsuqi.util.ExtensionFileFilter;

class DefaultConfigurationDialog extends ConfigurationDialog {

	private JTextField hostEntry;
	private JPasswordField passwordEntry;
	private JTextField portEntry;
	private JTextField styleEntry;
	private JTextField userEntry;
	private JTextField appEntry;
	private JTextField encodingEntry;
	private JComboBox lookAndFeelCombo;
	private JRadioButton[] protocolVersionRadios;
	UIManager.LookAndFeelInfo[] lafs;
	private JCheckBox useLogViewerCheck;

	public DefaultConfigurationDialog(String title, Configuration conf) {
		super(title, conf);
		changeLookAndFeel(conf.getLookAndFeelClassName());
	}

	protected void updateConfiguration() {
		// basic settings
		conf.setUser(userEntry.getText()); //$NON-NLS-1$
		conf.setPass(new String(passwordEntry.getPassword()));
		conf.setHost(hostEntry.getText()); //$NON-NLS-1$
		conf.setPort(Integer.parseInt(portEntry.getText())); //$NON-NLS-1$
		conf.setApplication(appEntry.getText()); //$NON-NLS-1$ //$NON-NLS-2$
		conf.setStyles(styleEntry.getText()); //$NON-NLS-1$

		// advanced settings
		conf.setEncoding(encodingEntry.getText()); //$NON-NLS-1$ //$NON-NLS-2$
		conf.setLookAndFeelClassName(lafs[lookAndFeelCombo.getSelectedIndex()].getClassName());

		for (int i = 0; i < protocolVersionRadios.length; i++) {
			if (protocolVersionRadios[i].isSelected()) {
				conf.setProtocolVersion(Integer.parseInt(protocolVersionRadios[i].getText()));
				break;
			}
		}
		conf.setUseSSL(false);
		conf.setVerify(false);
		conf.setUseLogViewer(useLogViewerCheck.isSelected());
	}

	protected JComponent createControls() {
		JTabbedPane tabbed = new JTabbedPane();
		tabbed.addTab(Messages.getString("DefaultConfigurationDialog.basic_tab_label"), createBasicTab()); //$NON-NLS-1$
		tabbed.addTab(Messages.getString("DefaultConfigurationDialog.advanced_tab_label"), createAdvancedTab()); //$NON-NLS-1$
		return tabbed;
	}


	private JComponent createBasicTab() {
		JPanel controls = new JPanel();
		controls.setLayout(new GridBagLayout());

		int y = 0;

		userEntry = addTextRow(controls, y++, Messages.getString("DefaultConfigurationDialog.user"), conf.getUser()); //$NON-NLS-1$
		passwordEntry = addPasswordRow(controls, y++, Messages.getString("DefaultConfigurationDialog.password")); //$NON-NLS-1$
		hostEntry = addTextRow(controls, y++, Messages.getString("DefaultConfigurationDialog.host"), conf.getHost()); //$NON-NLS-1$
		portEntry = addIntRow(controls, y++, Messages.getString("DefaultConfigurationDialog.port"), conf.getPort()); //$NON-NLS-1$
		appEntry = addTextRow(controls, y++, Messages.getString("DefaultConfigurationDialog.application"), conf.getApplication()); //$NON-NLS-1$
		styleEntry = addTextRow(controls, y++, Messages.getString("DefaultConfigurationDialog.style"), conf.getStyles()); //$NON-NLS-1$

		final JTextComponent entry = styleEntry;
		addButtonFor(controls, styleEntry, Messages.getString("DefaultConfigurationDialog.browse"), new ActionListener() { //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home")); //$NON-NLS-1$
				String extension = ".properties"; //$NON-NLS-1$
				String description = Messages.getString("DefaultConfigurationDialog.filter_pattern"); //$NON-NLS-1$
				fileChooser.setFileFilter(new ExtensionFileFilter(extension, description));
				int ret = fileChooser.showOpenDialog(DefaultConfigurationDialog.this);
				if (ret == JFileChooser.APPROVE_OPTION) {
					File styleFile = fileChooser.getSelectedFile();
					entry.setText(styleFile.getAbsolutePath());
				}
			}
		});

		return controls;
	}

	private JComponent createAdvancedTab() {
		JPanel controls = new JPanel();
		controls.setLayout(new GridBagLayout());

		int y = 0;

		encodingEntry = addTextRow(controls, y++, Messages.getString("DefaultConfigurationDialog.encoding"), conf.getEncoding()); //$NON-NLS-1$
		lafs = UIManager.getInstalledLookAndFeels();
		String selectedLookAndFeelClassName = conf.getLookAndFeelClassName();
		String selected = null;
		String[] lafNames = new String[lafs.length];
		for (int i = 0; i < lafNames.length; i++) {
			lafNames[i] = lafs[i].getName();
			if (selectedLookAndFeelClassName.equals(lafs[i].getClassName())) {
				selected = lafNames[i];
			}
		}
		if (selected == null) {
			selected = lafNames[0];
		}
		lookAndFeelCombo = addComboRow(controls, y++, Messages.getString("DefaultConfigurationDialog.look_and_feel"), lafNames, selected); //$NON-NLS-1$
		final JComboBox combo = lookAndFeelCombo;
		lookAndFeelCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				changeLookAndFeel(lafs[combo.getSelectedIndex()].getClassName());
			}
		});

		String[] versions = { String.valueOf(1), String.valueOf(2) };
		protocolVersionRadios = addRadioGroupRow(controls, y++, Messages.getString("DefaultConfigurationDialog.protocol_version"), versions, String.valueOf(conf.getProtocolVersion())); //$NON-NLS-1$

		useLogViewerCheck = addCheckRow(controls, y, Messages.getString("DefaultConfigurationDialog.use_log_viewer"), conf.getUseLogViewer()); //$NON-NLS-1$
		return controls;
	}


	protected void changeLookAndFeel(String className) {
		try {
			UIManager.setLookAndFeel(className);
			SwingUtilities.updateComponentTreeUI(this);
		} catch (Exception e) {
			logger.warn(e);
		}
	}
}
