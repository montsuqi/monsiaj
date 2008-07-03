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

package org.montsuqi.client;

import com.centerkey.utils.BareBonesBrowserLaunch;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
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
import javax.swing.BorderFactory; 
import javax.swing.BoxLayout;
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

import org.montsuqi.util.ExtensionFileFilter;
import org.montsuqi.util.Logger;
import org.montsuqi.widgets.TablePanel;

public class ConfigurationPanel extends JPanel {

	protected static final Logger logger = Logger.getLogger(ConfigurationPanel.class);
	protected Configuration conf;
	
	// for select server configration
	protected JComboBox configCombo;

	// Basic Tab
	protected JTextField userEntry;
	protected JPasswordField passwordEntry;
	protected JCheckBox savePasswordCheckbox;
	protected JTextField hostEntry;
	protected JTextField portEntry;
	protected JTextField appEntry;

	// SSL Tab
	protected JCheckBox useSSLCheckbox;
	protected JTextField clientCertificateEntry;
	protected JButton browseButton;
	protected JPasswordField exportPasswordEntry;
	protected JCheckBox saveClientCertificatePasswordCheckbox;

	// Others Tab
	protected JTextField styleEntry;
	protected JTextField encodingEntry;
	protected JComboBox lookAndFeelCombo;
	protected JRadioButton[] protocolVersionRadios;
	protected JCheckBox useLogViewerCheck;
	protected JCheckBox useTimerCheck;
	protected JTextField timerPeriodEntry;
	protected JTextArea propertiesText;

	LookAndFeelInfo[] lafs;
	JTabbedPane tabbed;
	protected static final int BASIC_TAB = 0;
	protected static final int SSL_TAB = 1;
	protected static final int OTHERS_TAB = 2;
	
	/** <p>An action to warn vulnerability of saving password.</p>
	 */
	private final class ConfirmSavePasswordAction implements ActionListener {

		final JCheckBox checkbox;
		public ConfirmSavePasswordAction(JCheckBox checkbox) {
			this.checkbox = checkbox;
		}

		public void actionPerformed(ActionEvent e) {
			if (checkbox.isSelected()) {
				int result = JOptionPane.showConfirmDialog(ConfigurationPanel.this, Messages.getString("ConfigurationPanel.save_password_confirm"), Messages.getString("ConfigurationPanel.confirm"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$ //$NON-NLS-2$
				if (result != JOptionPane.YES_OPTION) {
					checkbox.setSelected(false);
				}
			}
		}
	}

	/** <p>An action to pop a fiel selection dialog.</p>
	 * <p>When a file is selected, the path of the selected file is set to specified
	 * text field.</p>
	 */
	private final class FileSelectionAction extends AbstractAction {

		private JTextComponent entry;
		private String home;
		private String extension;
		private String description;

		/** <p>Constructs a FileSelectionAction.</p>
		 * 
		 * @param entry a text field to which the path of the selected file is set.
		 * @param home a directory path from which file selection starts.
		 * @param extension a file name extension passed to an ExtensionFIleFilter.
		 * @param description ditto.
		 */
		FileSelectionAction(JTextComponent entry, String home, String extension, String description) {
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
	
	protected ConfigurationPanel(Configuration conf) {
		this.conf = conf;
		changeLookAndFeel(conf.getLookAndFeelClassName(conf.getConfigurationName()));
		initComponents();
	}

	/** <p>Updates configuration ofject using values set to UI.</p>
	 */
	protected void updateConfiguration() {
		String configName = (String)configCombo.getSelectedItem();
		conf.setConfigurationName(configName);
		
		// Basic Tab
		conf.setUser(configName, userEntry.getText());
		// Save save_pass check field before the password itself,
		// since setPass fetches its value from the preferences internally.
		conf.setSavePassword(configName, savePasswordCheckbox.isSelected());
		conf.setPassword(configName, new String(passwordEntry.getPassword()));
		conf.setHost(configName, hostEntry.getText());
		conf.setPort(configName, Integer.parseInt(portEntry.getText()));
		conf.setApplication(configName, appEntry.getText());

		// SSL Tab
		conf.setUseSSL(configName, useSSLCheckbox.isSelected());
		conf.setClientCertificateFileName(configName, clientCertificateEntry.getText());
		conf.setClientCertificatePassword(configName, new String(exportPasswordEntry.getPassword()));
		conf.setSaveClientCertificatePassword(configName, saveClientCertificatePasswordCheckbox.isSelected());

		// Others Tab
		conf.setStyleFileName(configName, styleEntry.getText());
		conf.setEncoding(configName, encodingEntry.getText());
		conf.setLookAndFeelClassName(configName, lafs[lookAndFeelCombo.getSelectedIndex()].getClassName());
		for (int i = 0; i < protocolVersionRadios.length; i++) {
			if (protocolVersionRadios[i].isSelected()) {
				conf.setProtocolVersion(configName, Integer.parseInt(protocolVersionRadios[i].getText()));
				break;
			}
		}
		conf.setUseLogViewer(configName, useLogViewerCheck.isSelected());
		conf.setUseTimer(configName, useTimerCheck.isSelected());
		conf.setTimerPeriod(configName, Long.parseLong(timerPeriodEntry.getText()));
		conf.setProperties(configName, propertiesText.getText());
	}

	protected void initComponents() {
		this.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints gbc;
		setLayout(layout);
		
		JLabel configLabel = new JLabel(Messages.getString("ConfigurationPanel.config_label"));
		add(configLabel);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.ipadx = 5;
		gbc.insets = new Insets(2,2,2,2);
		layout.addLayoutComponent(configLabel, gbc);		
		
		configCombo = new JComboBox();
		updateConfigCombo();
		configCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				loadConfiguration();
			}
		});
		add(configCombo);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.insets = new Insets(2,2,2,2);
		gbc.fill = GridBagConstraints.BOTH;
		layout.addLayoutComponent(configCombo, gbc);

		tabbed = new JTabbedPane();
		tabbed.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbed.addTab(Messages.getString("ConfigurationPanel.basic_tab_label"), createBasicTab()); //$NON-NLS-1$
		tabbed.addTab(Messages.getString("ConfigurationPanel.ssl_tab_label"), createSslTab()); //$NON-NLS-1$
		tabbed.addTab(Messages.getString("ConfigurationPanel.others_tab_label"), createOthersTab()); //$NON-NLS-1$
		tabbed.addTab(Messages.getString("ConfigurationPanel.info_tab_label"), createInfoTab()); //$NON-NLS-1$
		add(tabbed);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridwidth = 2;
		layout.addLayoutComponent(tabbed, gbc);
		loadConfiguration();
	}
	
	public void updateConfigCombo() {
		ActionListener [] listeners = configCombo.getActionListeners();
		for (int i = 0; i < listeners.length; i++) {
			configCombo.removeActionListener(listeners[i]);
		}
		String [] configNames = conf.getConfigurationNames();
		configCombo.removeAllItems();
		for (int i = 0; i < configNames.length; i++) {
			configCombo.addItem(configNames[i]);
		}
		for (int i = 0; i < listeners.length; i++) {
			configCombo.addActionListener(listeners[i]);
		}
		configCombo.setSelectedItem(conf.getConfigurationName());
	}
	
	protected void loadConfiguration() {
		String configName = (String)configCombo.getSelectedItem();
		conf.setConfigurationName(configName);
		// Basic Tab
		userEntry.setText(conf.getUser(configName));
		// Save save_pass check field before the password itself,
		// since setPass fetches its value from the preferences internally.
		savePasswordCheckbox.setSelected(conf.getSavePassword(configName));
		passwordEntry.setText(conf.getPassword(configName));
		hostEntry.setText(conf.getHost(configName));
		portEntry.setText(String.valueOf(conf.getPort(configName)));
		appEntry.setText(conf.getApplication(configName));

		// SSL Tab
		useSSLCheckbox.setSelected(conf.getUseSSL(configName));
		clientCertificateEntry.setText(conf.getClientCertificateFileName(configName));
		exportPasswordEntry.setText(conf.getClientCertificatePassword(configName));
		saveClientCertificatePasswordCheckbox.setSelected(conf.getSaveClientCertificatePassword(configName));

		// Others Tab
		styleEntry.setText(conf.getStyleFileName(configName));
		encodingEntry.setText(conf.getEncoding(configName));
		for (int i = 0; i < lafs.length; i++) {
			if (conf.getLookAndFeelClassName(configName).equals(lafs[i].getClassName())) {
				lookAndFeelCombo.setSelectedItem(lafs[i].getName());
			}
		}
		protocolVersionRadios[conf.getProtocolVersion(configName) - 1].setSelected(true);
		useLogViewerCheck.setSelected(conf.getUseLogViewer(configName));
		useTimerCheck.setSelected(conf.getUseTimer(configName));
		timerPeriodEntry.setText(String.valueOf(conf.getTimerPeriod(configName)));
		propertiesText.setText(conf.getProperties(configName));
	}

	private Component createBasicTab() {
		TablePanel panel = new TablePanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		String configName = conf.getConfigurationName();

		int y = 0;
		userEntry = panel.addTextFieldRow(y++, Messages.getString("ConfigurationPanel.user"), conf.getUser(configName)); //$NON-NLS-1$
		passwordEntry = panel.addPasswordFieldRow(y++, Messages.getString("ConfigurationPanel.password")); //$NON-NLS-1$
		final boolean savePassword = conf.getSavePassword(configName);
		if (savePassword) {
			passwordEntry.setText(conf.getPassword(configName));
		}
		savePasswordCheckbox = panel.addCheckBoxRow(y++, Messages.getString("ConfigurationPanel.save_password"), savePassword); //$NON-NLS-1$
		savePasswordCheckbox.addActionListener(new ConfirmSavePasswordAction(savePasswordCheckbox));
		hostEntry = panel.addTextFieldRow(y++, Messages.getString("ConfigurationPanel.host"), conf.getHost(configName)); //$NON-NLS-1$
		portEntry = panel.addIntFieldRow(y++, Messages.getString("ConfigurationPanel.port"), conf.getPort(configName)); //$NON-NLS-1$
		appEntry = panel.addTextFieldRow(y++, Messages.getString("ConfigurationPanel.application"), conf.getApplication(configName)); //$NON-NLS-1$
		
		return panel;
	}

	void updateSslTabComponentsEnabled() {
		final boolean useSsl = useSSLCheckbox.isSelected();
		clientCertificateEntry.setEnabled(useSsl);
		exportPasswordEntry.setEnabled(useSsl);
		browseButton.setEnabled(useSsl);
		saveClientCertificatePasswordCheckbox.setEnabled(useSsl);
	}

	private Component createSslTab() {
		TablePanel panel = new TablePanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		String configName = conf.getConfigurationName();

		final String home = System.getProperty("user.home"); //$NON-NLS-1$
		final String clientCertificateDescription = Messages.getString("ConfigurationPanel.client_certificate_description"); //$NON-NLS-1$
		int y = 0;

		useSSLCheckbox = panel.addCheckBoxRow(y++, Messages.getString("ConfigurationPanel.use_ssl"), conf.getUseSSL(configName)); //$NON-NLS-1$
		useSSLCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateSslTabComponentsEnabled();
			}
		});
		clientCertificateEntry = panel.addTextFieldRow(y++, Messages.getString("ConfigurationPanel.client_certificate"), conf.getClientCertificateFileName(configName)); //$NON-NLS-1$
		browseButton = panel.addButtonFor(clientCertificateEntry, new FileSelectionAction(clientCertificateEntry, home, ".p12", clientCertificateDescription)); //$NON-NLS-1$
		exportPasswordEntry = panel.addPasswordFieldRow(y++, Messages.getString("ConfigurationPanel.cert_password")); //$NON-NLS-1$
		final boolean saveClientCertificatePassword = conf.getSaveClientCertificatePassword(configName);
		if (saveClientCertificatePassword) {
			exportPasswordEntry.setText(conf.getClientCertificatePassword(configName));
		}
		saveClientCertificatePasswordCheckbox = panel.addCheckBoxRow(y++, Messages.getString("ConfigurationPanel.save_cert_password"), saveClientCertificatePassword); //$NON-NLS-1$
		saveClientCertificatePasswordCheckbox.addActionListener(new ConfirmSavePasswordAction(saveClientCertificatePasswordCheckbox));

		updateSslTabComponentsEnabled();
		return panel;
	}

	private Component createOthersTab() {
		TablePanel panel = new TablePanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		String configName = conf.getConfigurationName();

		int y = 0;
		styleEntry = panel.addTextFieldRow(y++, Messages.getString("ConfigurationPanel.style"), conf.getStyleFileName(configName)); //$NON-NLS-1$
		styleEntry.setColumns(20);
		final String home = System.getProperty("user.home"); //$NON-NLS-1$
		final String extension = ".properties"; //$NON-NLS-1$
		final String description = Messages.getString("ConfigurationPanel.filter_pattern"); //$NON-NLS-1$
		panel.addButtonFor(styleEntry, new FileSelectionAction(styleEntry, home, extension, description));

		encodingEntry = panel.addTextFieldRow(y++, Messages.getString("ConfigurationPanel.encoding"), conf.getEncoding(configName)); //$NON-NLS-1$
		lafs = UIManager.getInstalledLookAndFeels();
		String selectedLookAndFeelClassName = conf.getLookAndFeelClassName(configName);
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
		lookAndFeelCombo = panel.addComboBoxRow(y++, Messages.getString("ConfigurationPanel.look_and_feel"), lafNames, selected); //$NON-NLS-1$
		final JComboBox combo = lookAndFeelCombo;
		lookAndFeelCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeLookAndFeel(lafs[combo.getSelectedIndex()].getClassName());
			}
		});

		String[] versions = { String.valueOf(1), String.valueOf(2) };
		protocolVersionRadios = panel.addRadioButtonGroupRow(y++, Messages.getString("ConfigurationPanel.protocol_version"), versions, String.valueOf(conf.getProtocolVersion(configName))); //$NON-NLS-1$
		useLogViewerCheck = panel.addCheckBoxRow(y++, Messages.getString("ConfigurationPanel.use_log_viewer"), conf.getUseLogViewer(configName)); //$NON-NLS-1$

		JPanel timerPanel = new JPanel();
		timerPanel.setLayout(new BoxLayout(timerPanel, BoxLayout.X_AXIS));
		useTimerCheck = new JCheckBox();
		useTimerCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timerPeriodEntry.setEnabled(useTimerCheck.isSelected());
			}
		});
		timerPanel.add(useTimerCheck);
		JPanel timerPeriodEntryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		timerPeriodEntryPanel.add(new JLabel(Messages.getString("ConfigurationPanel.timer_period")));
		timerPeriodEntry = new JTextField(5);
		timerPeriodEntryPanel.add(timerPeriodEntry);
		timerPanel.add(timerPeriodEntryPanel);
		panel.addRow(y++,Messages.getString("ConfigurationPanel.use_timer") ,timerPanel);
		
		propertiesText = panel.addTextAreaRow(y++, 3, 30, Messages.getString("ConfigurationPanel.additional_system_properties"), conf.getProperties(configName)); //$NON-NLS-1$
		return panel;
	}

	private Component createInfoTab() {
		TablePanel panel = new TablePanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		int y = 0;
		JLabel name = new JLabel("monsiaj ver." + Messages.getString("application.version"));
		name.setFont(new Font(null, Font.BOLD, 20));
		panel.addRow(y++, name);
		panel.addRow(y++, new JLabel("Copyright (C) 2007 ORCA Project"));
		JButton orcasite = new JButton("<html><a href=\"\">ORCA Project Website</a></html>");
		orcasite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BareBonesBrowserLaunch.openURL(Messages.getString("ConfigurationPanel.info_orca_url"));
			}
		});
		panel.addRow(y++, orcasite);
		JButton montsuqisite = new JButton("<html><a href=\"\">montsuqi.org</a></html>");
		montsuqisite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BareBonesBrowserLaunch.openURL(Messages.getString("ConfigurationPanel.info_montsuqi_url"));
			}
		});
		panel.addRow(y++, montsuqisite);
		return panel;
	}

	protected void changeLookAndFeel(String className) {
		try {
			UIManager.setLookAndFeel(className);
		} catch (Exception e) {
			logger.warn(e);
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Window window = SwingUtilities.windowForComponent(ConfigurationPanel.this);
				Component root;
				if (window != null) {
					root = window;
				} else {
					root = ConfigurationPanel.this;
				}
				try {
					SwingUtilities.updateComponentTreeUI(root);
					if (window != null) {
						window.pack();
					}
					
				} catch (Exception e) {
					logger.warn(e);
				}
			}
		});
	}
}
