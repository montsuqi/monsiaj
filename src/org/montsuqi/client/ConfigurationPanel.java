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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.Font;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory; 
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.text.JTextComponent;

import org.montsuqi.util.ExtensionFileFilter;
import org.montsuqi.util.Logger;
import org.montsuqi.util.SystemEnvironment;

import com.nilo.plaf.nimrod.*;

public class ConfigurationPanel extends JPanel {

	protected static final Logger logger = Logger.getLogger(ConfigurationPanel.class);
	protected Configuration conf;
	
	protected JPanel basicPanel;
	protected JPanel sslPanel;
	protected JPanel othersPanel;
	protected JPanel infoPanel;
	
	// Basic Tab
	protected JTextField userEntry;
	protected JPasswordField passwordEntry;
	protected JCheckBox savePasswordCheckbox;
	protected JTextField hostEntry;
	protected JTextField portEntry;
	protected JTextField appEntry;

	// SSL Tab
	protected JCheckBox useSSLCheckbox;
	protected JButton clientCertificateButton;
	protected JTextField clientCertificateEntry;
	protected JPasswordField exportPasswordEntry;
	protected JCheckBox saveClientCertificatePasswordCheckbox;

	// Others Tab
	protected JTextField styleEntry;
	protected JTextField encodingEntry;
	protected JComboBox lookAndFeelCombo;
	protected JTextField lafThemeEntry;
	protected JButton lafThemeButton;
	protected JRadioButton[] protocolVersionRadios;
	protected JCheckBox useLogViewerCheck;
	protected JCheckBox useTimerCheck;
	protected JTextField timerPeriodEntry;
	protected JTextArea propertiesText;

	protected LookAndFeelInfo[] lafs;
	
	private static final int MAX_PANEL_ROWS = 12;
	private static final int MAX_PANEL_COLUMNS = 4;
	
	private boolean doPadding;
	private boolean doChangeLookAndFeel;
	private MetalTheme systemMetalTheme;
	
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
	
	private final class ThemeSelectionAction extends AbstractAction {

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
		ThemeSelectionAction(JTextComponent entry, String home, String extension, String description) {
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
				changeLookAndFeel();
			}
		}
	}
	
	final class TextAreaSelected extends FocusAdapter {

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
	
	protected void changeLookAndFeel() {
		changeLookAndFeel(lafs[lookAndFeelCombo.getSelectedIndex()].getClassName());
	}
	
	protected void changeLookAndFeel(String className) {
		if (doChangeLookAndFeel) {
			MetalLookAndFeel.setCurrentTheme(systemMetalTheme);
			try {
				if ( !SystemEnvironment.isJavaVersionMatch("1.4") ) {
					if ( className.startsWith("com.nilo.plaf.nimrod")) {
						System.setProperty("nimrodlf.themeFile", lafThemeEntry.getText());
						UIManager.setLookAndFeel(new NimRODLookAndFeel());
					} else {
						UIManager.setLookAndFeel(className);
					}
				} else {
					UIManager.setLookAndFeel(className);
				}
			} catch (Exception e) {
				logger.warn(e);
			}
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Component root = SwingUtilities.getRoot(basicPanel);
					try {
						if (root != null) {
							SwingUtilities.updateComponentTreeUI(root);
						}

					} catch (Exception e) {
						logger.warn(e);
					}
				}
			});
		}
	}
	
	protected ConfigurationPanel(Configuration conf, boolean doPadding, boolean doChangeLookAndFeel) {
		this.conf = conf;
		this.doPadding = doPadding;
		this.doChangeLookAndFeel =  doChangeLookAndFeel;
		this.systemMetalTheme = MetalLookAndFeel.getCurrentTheme();
		initComponents();
	}
	
	protected void initComponents() {
		basicPanel = createBasicPanel();
		sslPanel = createSSLPanel();
		othersPanel = createOthersPanel();
		infoPanel = createInfoPanel();
		loadConfiguration("");
	}
		
	protected void loadConfiguration(String configName) {
		boolean newFlag = configName.equals("");
		
		// Basic tab
		String user = newFlag ? conf.DEFAULT_USER : conf.getUser(configName);
		String password = newFlag ? conf.DEFAULT_PASSWORD : conf.getPassword(configName);
		boolean savePassword = newFlag ? conf.DEFAULT_SAVE_PASSWORD : conf.getSavePassword(configName);
		String host = newFlag ? conf.DEFAULT_HOST : conf.getHost(configName);
		int port = newFlag ? conf.DEFAULT_PORT : conf.getPort(configName);
		String application  = newFlag ? conf.DEFAULT_APPLICATION : conf.getApplication(configName);
		
		// SSL tab
		boolean useSSL = newFlag ? conf.DEFAULT_USE_SSL : conf.getUseSSL(configName);
		String clientCertificate = newFlag ? conf.DEFAULT_CLIENT_CERTIFICATE : conf.getClientCertificateFileName(configName);
		boolean saveClientCertificatePassword = newFlag ? conf.DEFAULT_SAVE_CLIENT_CERTIFICATE_PASSWORD : conf.getSaveClientCertificatePassword(configName);
		String clientCertificatePassword = newFlag ? conf.DEFAULT_CLIENT_CERTIFICATE_PASSWORD : conf.getClientCertificatePassword(configName);
		
		// Others tab
		String styleFileName = newFlag ? conf.DEFAULT_STYLES : conf.getStyleFileName(configName);
		String encoding = newFlag ? conf.DEFAULT_ENCODING : conf.getEncoding(configName);
		String lookAndFeelClassName = newFlag ? conf.DEFAULT_LOOK_AND_FEEL_CLASS_NAME : conf.getLookAndFeelClassName(configName);
		String lafThemeFileName = newFlag ? conf.DEFAULT_LAF_THEME : conf.getLAFThemeFileName(configName);
		int protocolVersion = newFlag ? conf.DEFAULT_PROTOCOL_VERSION : conf.getProtocolVersion(configName);
		boolean useLogViewer = newFlag ? conf.DEFAULT_USE_LOG_VIEWER : conf.getUseLogViewer(configName);
		boolean useTimer = newFlag ? conf.DEFAULT_USE_TIMER : conf.getUseTimer(configName);
		long timerPeriod = newFlag ? conf.DEFAULT_TIMER_PERIOD : conf.getTimerPeriod(configName);
		String properties = newFlag ? conf.DEFAULT_PROPERTIES : conf.getProperties(configName);
		
		// Basic Tab
		userEntry.setText(user);
		// Save save_pass check field before the password itself,
		// since setPass fetches its value from the preferences internally.
		savePasswordCheckbox.setSelected(savePassword);
		passwordEntry.setText(password);
		hostEntry.setText(host);
		portEntry.setText(String.valueOf(port));
		appEntry.setText(application);

		// SSL Tab
		useSSLCheckbox.setSelected(useSSL);
		clientCertificateEntry.setText(clientCertificate);
		exportPasswordEntry.setText(clientCertificatePassword);
		saveClientCertificatePasswordCheckbox.setSelected(saveClientCertificatePassword);

		// Others Tab
		styleEntry.setText(styleFileName);
		encodingEntry.setText(encoding);
		lafThemeEntry.setText(lafThemeFileName);
		for (int i = 0; i < lafs.length; i++) {
			if (lookAndFeelClassName.equals(lafs[i].getClassName())) {
				lookAndFeelCombo.setSelectedItem(lafs[i].getName());
			}
		}
		protocolVersionRadios[protocolVersion - 1].setSelected(true);
		useLogViewerCheck.setSelected(useLogViewer);
		useTimerCheck.setSelected(useTimer);
		timerPeriodEntry.setText(String.valueOf(timerPeriod));
		propertiesText.setText(properties);
		updateSSLPanelComponentsEnabled();
	}

	protected void saveConfiguration(String configName) {
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
		conf.setLAFThemeFileName(configName, lafThemeEntry.getText());
		conf.setUseLogViewer(configName, useLogViewerCheck.isSelected());
		conf.setUseTimer(configName, useTimerCheck.isSelected());
		conf.setTimerPeriod(configName, Long.parseLong(timerPeriodEntry.getText()));
		conf.setProperties(configName, propertiesText.getText());
	}

	public static GridBagConstraints createConstraints(int x, int y, int width, int height, double weightx, double weighty) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		return gbc;
	}
	
	public static JLabel createLabel(String str) {
		JLabel label = new JLabel(str);
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setPreferredSize(new Dimension(170,20));
		label.setMinimumSize(new Dimension(170,20));
		label.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
		return label;
	}
	
	public static JTextField createTextField() {
		JTextField tf = new JTextField();
		tf.setHorizontalAlignment(SwingConstants.LEFT);
		tf.setText("");
		return tf;
	}
	
	public static JPasswordField createPasswordField() {
		JPasswordField pf = new JPasswordField();
		pf.setHorizontalAlignment(SwingConstants.LEFT);
		pf.setText("");
		return pf;
	}
	
	protected JPanel createBasicPanel() {
		int y;
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		hostEntry = createTextField();
		portEntry = createTextField();
		appEntry = createTextField();
		String[] versions = { String.valueOf(1), String.valueOf(2) };
		protocolVersionRadios = new JRadioButton[versions.length];
		ButtonGroup group = new ButtonGroup();
		JPanel protocolVersionPanel = new JPanel();
		protocolVersionPanel.setLayout(new GridLayout(1, versions.length));
		for (int i = 0; i < versions.length; i++) {
			JRadioButton radio = new JRadioButton(versions[i]);
			radio.setSelected(versions[i].equals(versions[0]));
			group.add(radio);
			protocolVersionPanel.add(radio);
			protocolVersionRadios[i] = radio;
		}
		userEntry = createTextField();
		passwordEntry = createPasswordField();
		savePasswordCheckbox = new JCheckBox();
		savePasswordCheckbox.addActionListener(new ConfirmSavePasswordAction(savePasswordCheckbox));
		
		y = 0;
		panel.add(createLabel(Messages.getString("ConfigurationPanel.host")), 
			createConstraints(0, y, 1, 1, 0.0, 1.0));
		panel.add(hostEntry, 
			createConstraints(1, y, 2, 1, 1.0, 0.0));
		panel.add(portEntry, 
			createConstraints(3, y, 1, 1, 0.0, 0.0));
		y++;	
		
		panel.add(createLabel(Messages.getString("ConfigurationPanel.application")), 
			createConstraints(0, y, 1, 1, 0.0, 1.0));
		panel.add(appEntry, 
			createConstraints(1, y, 3, 1, 1.0, 0.0));
		y++;

		panel.add(createLabel(Messages.getString("ConfigurationPanel.protocol_version")), 
			createConstraints(0, y, 1, 1, 0.0, 1.0));
		panel.add(protocolVersionPanel, 
			createConstraints(1, y, 3, 1, 1.0, 0.0));
		y++;
		
		panel.add(createLabel(Messages.getString("ConfigurationPanel.user")), 
			createConstraints(0, y, 1, 1, 0.0, 1.0));
		panel.add(userEntry, 
			createConstraints(1, y, 3, 1, 1.0, 0.0));
		y++;
		
		panel.add(createLabel(Messages.getString("ConfigurationPanel.password")), 
			createConstraints(0, y, 1, 1, 0.0, 1.0));
		panel.add(passwordEntry, 
			createConstraints(1, y, 3, 1, 1.0, 0.0));
		y++;

		panel.add(createLabel(Messages.getString("ConfigurationPanel.save_password")), 
			createConstraints(0, y, 1, 1, 0.0, 1.0));
		panel.add(savePasswordCheckbox, 
			createConstraints(1, y, 3, 1, 1.0, 0.0));
		y++;

		if (doPadding) {
			for(int i = y ; i < MAX_PANEL_ROWS; i++) {
				panel.add(new JLabel(" "), 
					createConstraints(0,i,MAX_PANEL_COLUMNS, 1, 1.0, 1.0));
			}
		}
		return panel;
	}

	private void updateSSLPanelComponentsEnabled() {
		final boolean useSsl = useSSLCheckbox.isSelected();
		clientCertificateEntry.setEnabled(useSsl);
		exportPasswordEntry.setEnabled(useSsl);
		clientCertificateButton.setEnabled(useSsl);
		saveClientCertificatePasswordCheckbox.setEnabled(useSsl);
	}

	protected JPanel createSSLPanel() {
		int y;
		final String home = System.getProperty("user.home"); //$NON-NLS-1$
		final String clientCertificateDescription = Messages.getString("ConfigurationPanel.client_certificate_description"); //$NON-NLS-1$

		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		useSSLCheckbox = new JCheckBox();
		useSSLCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateSSLPanelComponentsEnabled();
			}
		});
		clientCertificateEntry = createTextField();
		clientCertificateButton = new JButton();
		clientCertificateButton.setAction( new FileSelectionAction(clientCertificateEntry, home, ".p12", clientCertificateDescription));
		exportPasswordEntry = createPasswordField();
		saveClientCertificatePasswordCheckbox = new JCheckBox();
		saveClientCertificatePasswordCheckbox.addActionListener(new ConfirmSavePasswordAction(saveClientCertificatePasswordCheckbox));
		
		y = 0;
		panel.add(createLabel(Messages.getString("ConfigurationPanel.use_ssl")), 
			createConstraints(0, y, 1, 1, 0.0, 1.0));
		panel.add(useSSLCheckbox, 
			createConstraints(1, y, 3, 1, 1.0, 0.0));
		y++;
		
		panel.add(createLabel(Messages.getString("ConfigurationPanel.client_certificate")), 
			createConstraints(0, y, 1, 1, 0.0, 1.0));
		panel.add(clientCertificateEntry, 
			createConstraints(1, y, 2, 1, 1.0, 0.0));
		panel.add(clientCertificateButton, 
			createConstraints(3, y, 1, 1, 0.0, 0.0));
		y++;

		panel.add(createLabel(Messages.getString("ConfigurationPanel.cert_password")), 
			createConstraints(0, y, 1, 1, 0.0, 1.0));
		panel.add(exportPasswordEntry, 
			createConstraints(1, y, 3, 1, 1.0, 0.0));
		y++;
		
		panel.add(createLabel(Messages.getString("ConfigurationPanel.save_cert_password")), 
			createConstraints(0, y, 1, 1, 0.0, 1.0));
		panel.add(saveClientCertificatePasswordCheckbox, 
			createConstraints(1, y, 3, 1, 0.0, 0.0));
		y++;

		if (doPadding) {
			for(int i = y ; i < MAX_PANEL_ROWS; i++) {
				panel.add(new JLabel(" "), 
					createConstraints(0,i,MAX_PANEL_COLUMNS, 1, 1.0, 1.0));
			}
		}

		return panel;
	}

	private void updateLAFThemeEnabled() {
		String laf = (String)lookAndFeelCombo.getSelectedItem();
		boolean isNimrod = laf.equals("Nimrod");
		lafThemeEntry.setEnabled(isNimrod);
		lafThemeButton.setEnabled(isNimrod);
	}	
	
	protected JPanel createOthersPanel() {		
		int y;
		final String home = System.getProperty("user.home"); //$NON-NLS-1$
		
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		styleEntry = createTextField();
		JButton styleButton = new JButton();
		styleButton.setAction(
			new FileSelectionAction(styleEntry, home, ".properties", 
				Messages.getString("ConfigurationPanel.style_filter_pattern")));
		encodingEntry = createTextField();
		lafs = UIManager.getInstalledLookAndFeels();
		String[] lafNames = new String[lafs.length];
		for (int i = 0; i < lafNames.length; i++) {
			lafNames[i] = lafs[i].getName();
		}
		lookAndFeelCombo = new JComboBox();
		lookAndFeelCombo.setEditable(false);
		for (int i = 0; i < lafNames.length; i++) {
			lookAndFeelCombo.addItem(lafNames[i]);
		}
		lookAndFeelCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateLAFThemeEnabled();
				changeLookAndFeel(lafs[lookAndFeelCombo.getSelectedIndex()].getClassName());
			}
		});
		lafThemeEntry = createTextField();
		lafThemeButton = new JButton();
		lafThemeButton.setAction(
			new ThemeSelectionAction(lafThemeEntry, lafThemeEntry.getText(),".theme",
				Messages.getString("ConfigurationPanel.laf_theme_filter_pattern")));
		useLogViewerCheck = new JCheckBox();
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

		propertiesText = new JTextArea(4, 30);
		propertiesText.addFocusListener(new TextAreaSelected());
		JScrollPane propertiesScroll = new JScrollPane(propertiesText, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		propertiesScroll.setMinimumSize(new Dimension(0,100));
		
		y = 0;
		panel.add(createLabel(Messages.getString("ConfigurationPanel.style")), 
			createConstraints(0, y, 1, 1, 0.0, 1.0));
		panel.add(styleEntry, 
			createConstraints(1, y, 2, 1, 1.0, 0.0));
		panel.add(styleButton, 
			createConstraints(3, y, 1, 1, 0.0, 0.0));
		y++;
		
		panel.add(createLabel(Messages.getString("ConfigurationPanel.encoding")), 
			createConstraints(0, y, 1, 1, 0.0, 1.0));
		panel.add(encodingEntry, 
			createConstraints(1, y, 3, 1, 1.0, 0.0));
		y++;
		
		panel.add(createLabel(Messages.getString("ConfigurationPanel.look_and_feel")), 
			createConstraints(0, y, 1, 1, 0.0, 1.0));
		panel.add(lookAndFeelCombo, 
			createConstraints(1, y, 3, 1, 1.0, 0.0));
		y++;
		
		panel.add(createLabel(Messages.getString("ConfigurationPanel.laf_theme")), 
			createConstraints(0, y, 1, 1, 0.0, 1.0));
		panel.add(lafThemeEntry, 
			createConstraints(1, y, 2, 1, 1.0, 0.0));
		panel.add(lafThemeButton, 
			createConstraints(3, y, 1, 1, 0.0, 0.0));
		y++;

		panel.add(createLabel(Messages.getString("ConfigurationPanel.use_log_viewer")), 
			createConstraints(0, y, 1, 1, 0.0, 1.0));
		panel.add(useLogViewerCheck, 
			createConstraints(1, y, 3, 1, 1.0, 0.0));
		y++;
		
		panel.add(createLabel(Messages.getString("ConfigurationPanel.use_timer")), 
			createConstraints(0, y, 1, 1, 0.0, 1.0));
		panel.add(timerPanel, 
			createConstraints(1, y, 3, 1, 1.0, 0.0));
		y++;
		
		panel.add(createLabel(Messages.getString("ConfigurationPanel.additional_system_properties")), 
			createConstraints(0, y, 1, 4, 0.0, 1.0));
		panel.add(propertiesScroll, 
			createConstraints(1, y, 3, 4, 1.0, 1.0));
		y += 4;
		if (doPadding) {
			for(int i = y ; i < MAX_PANEL_ROWS; i++) {
				panel.add(new JLabel(" "), 
				createConstraints(0,i,MAX_PANEL_COLUMNS, 1, 1.0, 1.0));
			}	
		}
		return panel;
	}

	protected JPanel createInfoPanel() {
		int y;
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		JLabel version = new JLabel("monsiaj ver." + Messages.getString("application.version"));
		version.setHorizontalAlignment(SwingConstants.CENTER);
		version.setFont(new Font(null, Font.BOLD, 20));
		JLabel copy = new JLabel("Copyright (C) 2007 ORCA Project");
		copy.setHorizontalAlignment(SwingConstants.CENTER);
		JButton orcaButton = new JButton("<html><a href=\"\">ORCA Project Website</a></html>");
		orcaButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BareBonesBrowserLaunch.openURL(Messages.getString("ConfigurationPanel.info_orca_url"));
			}
		});
		JButton montsuqiButton = new JButton("<html><a href=\"\">montsuqi.org</a></html>");
		montsuqiButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BareBonesBrowserLaunch.openURL(Messages.getString("ConfigurationPanel.info_montsuqi_url"));
			}
		});		

		y = 0;
		panel.add(version, 
			createConstraints(0, y, 4, 3, 1.0, 1.0));
		y += 3;
		
		panel.add(copy, 
			createConstraints(0, y, 4, 1, 1.0, 1.0));
		y++;

		panel.add(orcaButton, 
			createConstraints(0, y, 4, 1, 0.0, 1.0));
		y++;

		panel.add(montsuqiButton, 
			createConstraints(0, y, 4, 1, 0.0, 1.0));
		y++;

		if (doPadding) {
			for(int i = y ; i < MAX_PANEL_ROWS; i++) {
				panel.add(new JLabel(" "), 
				createConstraints(0,i,MAX_PANEL_COLUMNS, 1, 1.0, 1.0));
			}		
		}
		return panel;
	}

	public JPanel getBasicPanel() {
		return basicPanel;
	}
	
	public JPanel getSSLPanel() {
		return sslPanel;
	}
	
	public JPanel getOthersPanel() {
		return othersPanel;
	}
	
	public JPanel getInfoPanel() {
		return infoPanel;
	}
}
