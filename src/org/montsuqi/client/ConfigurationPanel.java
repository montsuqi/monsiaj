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
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

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
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.text.JTextComponent;

import org.montsuqi.util.ExtensionFileFilter;
import org.montsuqi.util.Logger;

public class ConfigurationPanel extends JPanel {

	private final class FileSelectionAction extends AbstractAction {

		private JTextComponent entry;
		private String home;
		private String extension;
		private String description;

		private FileSelectionAction(JTextComponent entry, String home, String extension, String description) {
			super(Messages.getString("ConfigurationPanel.browse")); //$NON-NLS-1$
			this.entry = entry;
			this.home = home;
			this.extension = extension;
			this.description = description;
		}

		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser(home); //$NON-NLS-1$
			fileChooser.setFileFilter(new ExtensionFileFilter(extension, description));
			int ret = fileChooser.showOpenDialog(null);
			if (ret == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				entry.setText(file.getAbsolutePath());
			}
		}
	}

	protected static final Logger logger = Logger.getLogger(ConfigurationPanel.class);
	protected Configuration conf;

	// Basic Tab
	protected JTextField userEntry;
	protected JPasswordField passwordEntry;
	protected JCheckBox savePasswordCheckbox;
	protected JTextField hostEntry;
	protected JTextField portEntry;
	protected JCheckBox useSSLCheckbox;
	protected JTextField appEntry;

	// SSL Tab
	protected JTextField serverCertificateEntry;
	protected JTextField clientCertificateEntry;
	protected JPasswordField exportPasswordEntry;
	protected JTextField clientCertificateAliasEntry;

	// Misc Tab
	protected JTextField styleEntry;
	protected JTextField encodingEntry;
	protected JComboBox lookAndFeelCombo;
	protected JRadioButton[] protocolVersionRadios;
	protected JCheckBox useLogViewerCheck;

	LookAndFeelInfo[] lafs;
	JTabbedPane tabbed;
	protected static final int BASIC_TAB = 0;
	protected static final int SSL_TAB = 1;
	protected static final int OTHERS_TAB = 2;

	protected ConfigurationPanel(Configuration conf) {
		this.conf = conf;
		changeLookAndFeel(conf.getLookAndFeelClassName());
		initComponents();
	}

	protected void updateConfiguration() {
		
		// Basic Tab
		conf.setUser(userEntry.getText());
		conf.setPass(new String(passwordEntry.getPassword()));
		conf.setSavePassword(savePasswordCheckbox.isSelected());
		conf.setHost(hostEntry.getText());
		conf.setPort(Integer.parseInt(portEntry.getText()));
		conf.setUseSSL(useSSLCheckbox.isSelected());
		conf.setApplication(appEntry.getText());

		// SSL Tab
		conf.setServerCertificateFileName(serverCertificateEntry.getText());
		conf.setClientCertificateFileName(clientCertificateEntry.getText());
		conf.setClientCertifivatePass(new String(exportPasswordEntry.getPassword()));

		// Others Tab
		conf.setStyleFileName(styleEntry.getText());
		conf.setEncoding(encodingEntry.getText());
		conf.setLookAndFeelClassName(lafs[lookAndFeelCombo.getSelectedIndex()].getClassName());
		for (int i = 0; i < protocolVersionRadios.length; i++) {
			if (protocolVersionRadios[i].isSelected()) {
				conf.setProtocolVersion(Integer.parseInt(protocolVersionRadios[i].getText()));
				break;
			}
		}
		conf.setUseLogViewer(useLogViewerCheck.isSelected());
	}

	protected void initComponents() {
		setLayout(new BorderLayout());

		tabbed = new JTabbedPane();
		tabbed.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbed.addTab(Messages.getString("ConfigurationPanel.basic_tab_label"), createBasicTab()); //$NON-NLS-1$
		tabbed.addTab(Messages.getString("ConfigurationPanel.ssl_tab_label"), createSslTab()); //$NON-NLS-1$
		tabbed.addTab(Messages.getString("ConfigurationPanel.others_tab_label"), createOthersTab()); //$NON-NLS-1$
		add(tabbed, BorderLayout.CENTER);
	}

	private Component createBasicTab() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		int y = 0;
		userEntry = addTextRow(panel, y++, Messages.getString("ConfigurationPanel.user"), conf.getUser()); //$NON-NLS-1$
		passwordEntry = addPasswordRow(panel, y++, Messages.getString("ConfigurationPanel.password")); //$NON-NLS-1$
		final boolean savePassword = conf.getSavePassword();
		if (savePassword) {
			passwordEntry.setText(conf.getPass());
		}
		savePasswordCheckbox = addCheckRow(panel, y++, Messages.getString("ConfigurationPanel.save_passwords"), savePassword); //$NON-NLS-1$
		savePasswordCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (savePasswordCheckbox.isSelected()) {
					int result = JOptionPane.showConfirmDialog(ConfigurationPanel.this, Messages.getString("ConfigurationPanel.save_password_confirm"), Messages.getString("ConfigurationPanel.confirm"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$ //$NON-NLS-2$
					if (result != JOptionPane.YES_OPTION) {
						savePasswordCheckbox.setSelected(false);
					}
				}
			}
		});
		hostEntry = addTextRow(panel, y++, Messages.getString("ConfigurationPanel.host"), conf.getHost()); //$NON-NLS-1$
		portEntry = addIntRow(panel, y++, Messages.getString("ConfigurationPanel.port"), conf.getPort()); //$NON-NLS-1$
		useSSLCheckbox = addCheckRow(panel, y++, Messages.getString("ConfigurationPanel.use_ssl"), conf.getUseSSL()); //$NON-NLS-1$
		useSSLCheckbox.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				if (tabbed.getTabCount() >= SSL_TAB + 1) {
					tabbed.setEnabledAt(SSL_TAB, useSSLCheckbox.isSelected());
				}
			}
		});
		useSSLCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tabbed.setEnabledAt(SSL_TAB, useSSLCheckbox.isSelected());
			}
		});
		appEntry = addTextRow(panel, y++, Messages.getString("ConfigurationPanel.application"), conf.getApplication()); //$NON-NLS-1$
		
		return panel;
	}

	private Component createSslTab() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		final String home = System.getProperty("user.home"); //$NON-NLS-1$
		final String serverCertificateDescription = Messages.getString("ConfigurationPanel.server_certificate_description"); //$NON-NLS-1$
		final String clientCertificateDescription = Messages.getString("ConfigurationPanel.client_certificate_description"); //$NON-NLS-1$
		int y = 0;
		serverCertificateEntry = addTextRow(panel, y++, Messages.getString("ConfigurationPanel.server_certificate"), conf.getServerCertificateFileName()); //$NON-NLS-1$
		addButtonFor(panel, serverCertificateEntry, new FileSelectionAction(serverCertificateEntry, home, ".pem", serverCertificateDescription)); //$NON-NLS-1$

		clientCertificateEntry = addTextRow(panel, y++, Messages.getString("ConfigurationPanel.client_certificate"), conf.getClientCertificateFileName()); //$NON-NLS-1$
		addButtonFor(panel, clientCertificateEntry, new FileSelectionAction(clientCertificateEntry, home, ".p12", clientCertificateDescription)); //$NON-NLS-1$
		clientCertificateAliasEntry = addTextRow(panel, y++, Messages.getString("ConfigurationPanel.alias"), conf.getClientCertificateAlias()); //$NON-NLS-1$
		exportPasswordEntry = addPasswordRow(panel, y++, Messages.getString("ConfigurationPanel.password")); //$NON-NLS-1$

		return panel;
	}

	private Component createOthersTab() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		int y = 0;
		styleEntry = addTextRow(panel, y++, Messages.getString("ConfigurationPanel.style"), conf.getStyleFileName()); //$NON-NLS-1$
		styleEntry.setColumns(20);
		final String home = System.getProperty("user.home"); //$NON-NLS-1$
		final String extension = ".properties"; //$NON-NLS-1$
		final String description = Messages.getString("ConfigurationPanel.filter_pattern"); //$NON-NLS-1$
		addButtonFor(panel, styleEntry, new FileSelectionAction(styleEntry, home, extension, description));

		encodingEntry = addTextRow(panel, y++, Messages.getString("ConfigurationPanel.encoding"), conf.getEncoding()); //$NON-NLS-1$
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
		lookAndFeelCombo = addComboRow(panel, y++, Messages.getString("ConfigurationPanel.look_and_feel"), lafNames, selected); //$NON-NLS-1$
		final JComboBox combo = lookAndFeelCombo;
		lookAndFeelCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeLookAndFeel(lafs[combo.getSelectedIndex()].getClassName());
			}
		});

		String[] versions = { String.valueOf(1), String.valueOf(2) };
		protocolVersionRadios = addRadioGroupRow(panel, y++, Messages.getString("ConfigurationPanel.protocol_version"), versions, String.valueOf(conf.getProtocolVersion())); //$NON-NLS-1$

		useLogViewerCheck = addCheckRow(panel, y, Messages.getString("ConfigurationPanel.use_log_viewer"), conf.getUseLogViewer()); //$NON-NLS-1$

		return panel;
	}

	private void addRow(Container container, int y, String text, JComponent component) {
		GridBagConstraints gbc;
		JLabel label = new JLabel();
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setText(text);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = y;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.insets = new Insets(2, 2, 2, 2);
		container.add(label, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = y;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(2, 2, 2, 2);
		container.add(component, gbc);
	}

	protected JTextField addTextRow(Container container, int y, String text, String value) {
		JTextField entry = new JTextField();
		entry.setText(value);
		addRow(container, y, text, entry);
		return entry;
	}

	protected JTextField addIntRow(Container container, int y, String text, int value) {
		JTextField entry = new JTextField();
		entry.setHorizontalAlignment(SwingConstants.RIGHT);
		entry.setText(String.valueOf(value));
		addRow(container, y, text, entry);
		return entry;
	}

	protected JPasswordField addPasswordRow(Container container, int y, String text) {
		JPasswordField entry = new JPasswordField();
		entry.setText(""); //$NON-NLS-1$
		addRow(container, y, text, entry);
		return entry;
	}

	protected JCheckBox addCheckRow(Container container, int y, String text, boolean flag) {
		JCheckBox check = new JCheckBox();
		check.setSelected(flag);
		addRow(container, y, text, check);
		return check;
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

	protected void addButtonFor(JPanel container, JTextField entry, Action action) {
		GridBagLayout gbl = (GridBagLayout)container.getLayout();

		GridBagConstraints gbc;
		gbc = gbl.getConstraints(entry);
		gbc.gridwidth = 2;
		gbl.setConstraints(entry, gbc);

		JButton button = new JButton();
		button.setAction(action);
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridwidth = 1;
		container.add(button, gbc);
	}

	protected void changeLookAndFeel(String className) {
		try {
			UIManager.setLookAndFeel(className);
		} catch (Exception e) {
			logger.warn(e);
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Component root = SwingUtilities.windowForComponent(ConfigurationPanel.this);
				if (root == null) {
					root = ConfigurationPanel.this;
				}
				try {
					SwingUtilities.updateComponentTreeUI(root);
				} catch (Exception e) {
					logger.warn(e);
				}
			}
		});
	}
}
