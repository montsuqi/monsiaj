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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
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

public class ConfigurationPanel extends JPanel {

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

	protected static final Logger logger = Logger.getLogger(ConfigurationPanel.class);
	protected Configuration conf;

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

	// Misc Tab
	protected JTextField styleEntry;
	protected JTextField encodingEntry;
	protected JComboBox lookAndFeelCombo;
	protected JRadioButton[] protocolVersionRadios;
	protected JCheckBox useLogViewerCheck;
	protected JTextArea propertiesText;

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

	/** <p>Updates configuration ofject using values set to UI.</p>
	 */
	protected void updateConfiguration() {

		// Basic Tab
		conf.setUser(userEntry.getText());
		// Save save_pass check field before the password itself,
		// since setPass fetches its value from the preferences internally.
		conf.setSavePassword(savePasswordCheckbox.isSelected());
		conf.setPass(new String(passwordEntry.getPassword()));
		conf.setHost(hostEntry.getText());
		conf.setPort(Integer.parseInt(portEntry.getText()));
		conf.setApplication(appEntry.getText());

		// SSL Tab
		conf.setUseSSL(useSSLCheckbox.isSelected());
		conf.setClientCertificateFileName(clientCertificateEntry.getText());
		conf.setClientCertificatePassword(new String(exportPasswordEntry.getPassword()));
		conf.setSaveClientCertificatePassword(saveClientCertificatePasswordCheckbox.isSelected());

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
		conf.setProperties(propertiesText.getText());
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
		userEntry = addTextFieldRow(panel, y++, Messages.getString("ConfigurationPanel.user"), conf.getUser()); //$NON-NLS-1$
		passwordEntry = addPasswordFieldRow(panel, y++, Messages.getString("ConfigurationPanel.password")); //$NON-NLS-1$
		final boolean savePassword = conf.getSavePassword();
		if (savePassword) {
			passwordEntry.setText(conf.getPass());
		}
		savePasswordCheckbox = addCheckBoxRow(panel, y++, Messages.getString("ConfigurationPanel.save_password"), savePassword); //$NON-NLS-1$
		savePasswordCheckbox.addActionListener(new ConfirmSavePasswordAction(savePasswordCheckbox));
		hostEntry = addTextFieldRow(panel, y++, Messages.getString("ConfigurationPanel.host"), conf.getHost()); //$NON-NLS-1$
		portEntry = addIntFieldRow(panel, y++, Messages.getString("ConfigurationPanel.port"), conf.getPort()); //$NON-NLS-1$
		appEntry = addTextFieldRow(panel, y++, Messages.getString("ConfigurationPanel.application"), conf.getApplication()); //$NON-NLS-1$
		
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
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		final String home = System.getProperty("user.home"); //$NON-NLS-1$
		final String clientCertificateDescription = Messages.getString("ConfigurationPanel.client_certificate_description"); //$NON-NLS-1$
		int y = 0;

		useSSLCheckbox = addCheckBoxRow(panel, y++, Messages.getString("ConfigurationPanel.use_ssl"), conf.getUseSSL()); //$NON-NLS-1$
		useSSLCheckbox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				updateSslTabComponentsEnabled();
			}

		});

		clientCertificateEntry = addTextFieldRow(panel, y++, Messages.getString("ConfigurationPanel.client_certificate"), conf.getClientCertificateFileName()); //$NON-NLS-1$
		browseButton = addButtonFor(panel, clientCertificateEntry, new FileSelectionAction(clientCertificateEntry, home, ".p12", clientCertificateDescription)); //$NON-NLS-1$
		exportPasswordEntry = addPasswordFieldRow(panel, y++, Messages.getString("ConfigurationPanel.cert_password")); //$NON-NLS-1$
		final boolean saveClientCertificatePassword = conf.getSaveClientCertificatePassword();
		if (saveClientCertificatePassword) {
			exportPasswordEntry.setText(conf.getClientCertificatePassword());
		}
		saveClientCertificatePasswordCheckbox = addCheckBoxRow(panel, y++, Messages.getString("ConfigurationPanel.save_cert_password"), saveClientCertificatePassword); //$NON-NLS-1$
		saveClientCertificatePasswordCheckbox.addActionListener(new ConfirmSavePasswordAction(saveClientCertificatePasswordCheckbox));

		updateSslTabComponentsEnabled();
		return panel;
	}

	private Component createOthersTab() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		int y = 0;
		styleEntry = addTextFieldRow(panel, y++, Messages.getString("ConfigurationPanel.style"), conf.getStyleFileName()); //$NON-NLS-1$
		styleEntry.setColumns(20);
		final String home = System.getProperty("user.home"); //$NON-NLS-1$
		final String extension = ".properties"; //$NON-NLS-1$
		final String description = Messages.getString("ConfigurationPanel.filter_pattern"); //$NON-NLS-1$
		addButtonFor(panel, styleEntry, new FileSelectionAction(styleEntry, home, extension, description));

		encodingEntry = addTextFieldRow(panel, y++, Messages.getString("ConfigurationPanel.encoding"), conf.getEncoding()); //$NON-NLS-1$
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
		lookAndFeelCombo = addComboBoxRow(panel, y++, Messages.getString("ConfigurationPanel.look_and_feel"), lafNames, selected); //$NON-NLS-1$
		final JComboBox combo = lookAndFeelCombo;
		lookAndFeelCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeLookAndFeel(lafs[combo.getSelectedIndex()].getClassName());
			}
		});

		String[] versions = { String.valueOf(1), String.valueOf(2) };
		protocolVersionRadios = addRadioButtonGroupRow(panel, y++, Messages.getString("ConfigurationPanel.protocol_version"), versions, String.valueOf(conf.getProtocolVersion())); //$NON-NLS-1$

		useLogViewerCheck = addCheckBoxRow(panel, y++, Messages.getString("ConfigurationPanel.use_log_viewer"), conf.getUseLogViewer()); //$NON-NLS-1$

		propertiesText = addTextAreaRow(panel, y, 3, 30, Messages.getString("ConfigurationPanel.additional_system_properties"), conf.getProperties()); //$NON-NLS-1$
		return panel;
	}

	private void addRow(Container container, int y, String text, JComponent component) {
		addRow(container, y, 1, text, component);
	}

	private void addRow(Container container, int y, int h, String text, JComponent component) {
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
		container.add(label, gbc);

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
		container.add(component, gbc);
	}

	protected JTextField addTextFieldRow(Container container, int y, String text, String value) {
		JTextField entry = new JTextField();
		entry.setText(value);
		entry.addFocusListener(new FieldSelected());
		addRow(container, y, text, entry);
		return entry;
	}

	protected JTextField addIntFieldRow(Container container, int y, String text, int value) {
		JTextField entry = new JTextField();
		entry.setHorizontalAlignment(SwingConstants.RIGHT);
		entry.setText(String.valueOf(value));
		entry.addFocusListener(new FieldSelected());
		addRow(container, y, text, entry);
		return entry;
	}

	protected JPasswordField addPasswordFieldRow(Container container, int y, String text) {
		JPasswordField entry = new JPasswordField();
		entry.setText(""); //$NON-NLS-1$
		entry.addFocusListener(new FieldSelected());
		addRow(container, y, text, entry);
		return entry;
	}

	protected JTextArea addTextAreaRow(Container container, int y, int rows, int cols, String text, String value) {
		JTextArea textArea = new JTextArea(rows, cols);
		textArea.setText(value);
		textArea.addFocusListener(new FieldSelected());
		JScrollPane scroll = new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		addRow(container, y, rows, text, scroll);
		return textArea;
	}

	protected JCheckBox addCheckBoxRow(Container container, int y, String text, boolean flag) {
		JCheckBox check = new JCheckBox();
		check.setSelected(flag);
		addRow(container, y, text, check);
		return check;
	}

	protected JComboBox addComboBoxRow(JPanel container, int y, String text, String[] selections, String selected) {
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

	protected JRadioButton[] addRadioButtonGroupRow(JPanel container, int y, String text, String[] selections, String selected) {
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

	protected JButton addButtonFor(JPanel container, JTextField entry, Action action) {
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
		return button;
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
