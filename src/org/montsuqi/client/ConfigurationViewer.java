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
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.JTextComponent;
import javax.swing.table.DefaultTableModel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;

import org.montsuqi.util.ExtensionFileFilter;
import org.montsuqi.util.Logger;
import org.montsuqi.util.SystemEnvironment;
import org.montsuqi.widgets.TablePanel;
import org.montsuqi.widgets.PandaCList;

public class ConfigurationViewer{

	protected static final Logger logger = Logger.getLogger(Launcher.class);
	protected Configuration conf;
	
	// for edit dialog override
	
	// Basic Tab
	protected JTextField configNameEntry;
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
	protected LookAndFeelInfo[] lafs;
	
	static {
		if (System.getProperty("monsia.logger.factory") == null) { //$NON-NLS-1$
			System.setProperty("monsia.logger.factory", "org.montsuqi.util.StdErrLogger"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (SystemEnvironment.isMacOSX()) {
			System.setProperty("apple.awt.brushMetalLook", "true"); //$NON-NLS-1$ //$NON-NLS-2$
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

	public ConfigurationViewer(Configuration conf) {
		this.conf = conf;
	}
		
	public void run(Frame parent) {
		final JDialog f = new JDialog(parent, Messages.getString("ConfigurationViewer.title"), true); //$NON-NLS-1$		
		Container container = f.getContentPane();
		container.setLayout(new BorderLayout());
		final PandaCList clist = new PandaCList();
		clist.setFocusable(true);
		clist.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		updateConfigurationList(clist);
		clist.setAutoResizeMode(clist.AUTO_RESIZE_LAST_COLUMN);

		JScrollPane scroll = new JScrollPane(clist,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		container.add(scroll, BorderLayout.CENTER);

		JPanel bar = new JPanel();
		bar.setLayout(new FlowLayout());
		container.add(bar, BorderLayout.SOUTH);
		
		JButton newButton = new JButton(new AbstractAction(Messages.getString("ConfigurationViewer.new")) { //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {
				editConfiguration(f, "new", true);
				updateConfigurationList(clist);
			}
		});
		bar.add(newButton);
		
		JButton editButton = new JButton(new AbstractAction(Messages.getString("ConfigurationViewer.edit")) { //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {
				int selectedRow = clist.getSelectedRow();
				if ( selectedRow >= 0 ) {
					String configName = (String)(clist.getValueAt(selectedRow, 0));
					editConfiguration(f, configName, false);
					updateConfigurationList(clist);
				}
			}
		});
		bar.add(editButton);
		
		JButton deleteButton = new JButton(new AbstractAction(Messages.getString("ConfigurationViewer.delete")) { //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {
				int selectedRow = clist.getSelectedRow();
				if ( selectedRow >= 0 ) {
					String configName = (String)(clist.getValueAt(selectedRow, 0));
					DefaultTableModel model = (DefaultTableModel)clist.getModel();
					conf.deleteConfiguration(configName);
					updateConfigurationList(clist);
				}
			}
		});
		bar.add(deleteButton);

		JButton closeButton = new JButton(new AbstractAction(Messages.getString("ConfigurationViewer.close")) { //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {
				f.dispose();
			}
		});
		bar.add(closeButton);
		
		f.setSize(640, 480);
		f.setVisible(true);
		f.setLocationRelativeTo(null);
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
	
	private void updateConfigurationList(PandaCList clist)
	{
		int defaultId = -1;
		final String [] ColumnNames = {
				Messages.getString("ConfigurationViewer.config_name") ,
				Messages.getString("ConfigurationViewer.host") ,
				Messages.getString("ConfigurationViewer.port") ,
				Messages.getString("ConfigurationViewer.application") ,
				Messages.getString("ConfigurationViewer.user")				
		};
		String [] configNames = conf.getConfigurationNames();
		Object [][] tableData = new Object[configNames.length][ColumnNames.length];
		for (int i = 0; i < configNames.length; i++) {
			if ( configNames[i].equals(conf.DEFAULT_CONFIG_NAME) ) {
				defaultId = i;
			}
			tableData[i][0] = configNames[i];
			tableData[i][1] = conf.getHost(configNames[i]);
			tableData[i][2] = String.valueOf(conf.getPort(configNames[i]));
			tableData[i][3] = conf.getApplication(configNames[i]);
			tableData[i][4] = conf.getUser(configNames[i]);
		}
		DefaultTableModel model = new DefaultTableModel(tableData, ColumnNames) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		if ( defaultId >= 0 ) { model.removeRow(defaultId); }
		clist.setModel(model);
	}

	protected TablePanel createEditConfigurationPanel(String configName, boolean newFlag) {
		TablePanel panel = new TablePanel();
		
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
		int protocolVersion = newFlag ? conf.DEFAULT_PROTOCOL_VERSION : conf.getProtocolVersion(configName);
		boolean useLogViewer = newFlag ? conf.DEFAULT_USE_LOG_VIEWER : conf.getUseLogViewer(configName);
		boolean useTimer = newFlag ? conf.DEFAULT_USE_TIMER : conf.getUseTimer(configName);
		long timerPeriod = newFlag ? conf.DEFAULT_TIMER_PERIOD : conf.getTimerPeriod(configName);
		String properties = newFlag ? conf.DEFAULT_PROPERTIES : conf.getProperties(configName);
		
		int y = 0;

		configNameEntry = panel.addTextFieldRow(y++, Messages.getString("ConfigurationPanel.config_name"), configName); //$NON-NLS-1$
		// Basic tab
		userEntry = panel.addTextFieldRow(y++, Messages.getString("ConfigurationPanel.user"), user); //$NON-NLS-1$		
		passwordEntry = panel.addPasswordFieldRow(y++, Messages.getString("ConfigurationPanel.password")); //$NON-NLS-1$
		if (savePassword) {
			passwordEntry.setText(password);
		}
		savePasswordCheckbox = panel.addCheckBoxRow(y++, Messages.getString("ConfigurationPanel.save_password"), savePassword); //$NON-NLS-1$
		hostEntry = panel.addTextFieldRow(y++, Messages.getString("ConfigurationPanel.host"), host); //$NON-NLS-1$
		portEntry = panel.addIntFieldRow(y++, Messages.getString("ConfigurationPanel.port"), port); //$NON-NLS-1$
		appEntry = panel.addTextFieldRow(y++, Messages.getString("ConfigurationPanel.application"), application); //$NON-NLS-1$

		// SSL tab
		final String home = System.getProperty("user.home"); //$NON-NLS-1$
		final String clientCertificateDescription = Messages.getString("ConfigurationPanel.client_certificate_description"); //$NON-NLS-1$
		useSSLCheckbox = panel.addCheckBoxRow(y++, Messages.getString("ConfigurationPanel.use_ssl"), useSSL); //$NON-NLS-1$
		clientCertificateEntry = panel.addTextFieldRow(y++, Messages.getString("ConfigurationPanel.client_certificate"), clientCertificate); //$NON-NLS-1$
		browseButton = panel.addButtonFor(clientCertificateEntry, new FileSelectionAction(clientCertificateEntry, home, ".p12", clientCertificateDescription)); //$NON-NLS-1$
		exportPasswordEntry = panel.addPasswordFieldRow(y++, Messages.getString("ConfigurationPanel.cert_password")); //$NON-NLS-1$
		if (saveClientCertificatePassword) {
			exportPasswordEntry.setText(clientCertificatePassword);
		}
		saveClientCertificatePasswordCheckbox = panel.addCheckBoxRow(y++, Messages.getString("ConfigurationPanel.save_cert_password"), saveClientCertificatePassword); //$NON-NLS-1$
		// Others tab
		styleEntry = panel.addTextFieldRow(y++, Messages.getString("ConfigurationPanel.style"), styleFileName); //$NON-NLS-1$
		styleEntry.setColumns(20);
		final String extension = ".properties"; //$NON-NLS-1$
		final String description = Messages.getString("ConfigurationPanel.filter_pattern"); //$NON-NLS-1$
		panel.addButtonFor(styleEntry, new FileSelectionAction(styleEntry, home, extension, description));

		encodingEntry = panel.addTextFieldRow(y++, Messages.getString("ConfigurationPanel.encoding"), encoding); //$NON-NLS-1$
		lafs = UIManager.getInstalledLookAndFeels();
		String selected = null;
		String[] lafNames = new String[lafs.length];
		for (int i = 0; i < lafNames.length; i++) {
			lafNames[i] = lafs[i].getName();
			if (lookAndFeelClassName.equals(lafs[i].getClassName())) {
				selected = lafNames[i];
			}
		}
		if (selected == null) {
			selected = lafNames[0];
		}
		lookAndFeelCombo = panel.addComboBoxRow(y++, Messages.getString("ConfigurationPanel.look_and_feel"), lafNames, selected); //$NON-NLS-1$
		String[] versions = { String.valueOf(1), String.valueOf(2) };
		protocolVersionRadios = panel.addRadioButtonGroupRow(y++, Messages.getString("ConfigurationPanel.protocol_version"), versions, String.valueOf(protocolVersion)); //$NON-NLS-1$
		useLogViewerCheck = panel.addCheckBoxRow(y++, Messages.getString("ConfigurationPanel.use_log_viewer"), useLogViewer); //$NON-NLS-1$
		useTimerCheck = panel.addCheckBoxRow(y++, Messages.getString("ConfigurationPanel.use_timer"), useTimer); //$NON-NLS-1$
		timerPeriodEntry = panel.addLongFieldRow(y++, Messages.getString("ConfigurationPanel.timer_period"), timerPeriod); //$NON-NLS-1$
		propertiesText = panel.addTextAreaRow(y++, 3, 30, Messages.getString("ConfigurationPanel.additional_system_properties"), properties); //$NON-NLS-1$		
		return panel;
	}
	
	protected void editConfiguration(Dialog parent, String name, boolean flag) {
		final boolean newFlag = flag;
		final String configName = name;
		String title = newFlag ? Messages.getString("ConfigurationViewer.new") : Messages.getString("ConfigurationViewer.edit");
		final JDialog f = new JDialog(parent, title, true); //$NON-NLS-1$
		Container container = f.getContentPane();
		container.setLayout(new BorderLayout());
		
		TablePanel panel = createEditConfigurationPanel(configName, newFlag);
		container.add(panel, BorderLayout.CENTER);
		
		//buttons
		JPanel bar = new JPanel();
		bar.setLayout(new FlowLayout());
		container.add(bar, BorderLayout.SOUTH);
		
		JButton editOKButton = new JButton(new AbstractAction(Messages.getString("ConfigurationViewer.edit_ok")) { //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {
				String entryName = configNameEntry.getText();
				if ( entryName.equals("") ) {
					String message = Messages.getString("ConfigurationViewer.edit_config_empty_config_name_error_message") + entryName;
					logger.warn(message);
					JOptionPane.showMessageDialog(f, message, "" , JOptionPane.ERROR_MESSAGE);
					return;
				}
				if ( newFlag ) {
					// new configuration
					if ( ! conf.newConfiguration(entryName) ) {
						String message = Messages.getString("ConfigurationViewer.edit_config_exist_error_message") + entryName;
						logger.warn(message);
						JOptionPane.showMessageDialog(f, message, "" , JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				if ( ! newFlag && ! configName.equals(entryName) ) {
					// rename configuration
					if ( ! conf.renameConfiguration(configName, entryName) ) {
						String message = Messages.getString("ConfigurationViewer.edit_rename_error_message");
						logger.warn(message);
					    JOptionPane.showMessageDialog(f, message, "" , JOptionPane.ERROR_MESSAGE);
						return;
					}
				}

				// Basic Tab
				conf.setUser(entryName, userEntry.getText());
				// Save save_pass check field before the password itself,
				// since setPass fetches its value from the preferences internally.
				conf.setSavePassword(entryName, savePasswordCheckbox.isSelected());
				conf.setPassword(entryName, new String(passwordEntry.getPassword()));
				conf.setHost(entryName, hostEntry.getText());
				conf.setPort(entryName, Integer.parseInt(portEntry.getText()));
				conf.setApplication(entryName, appEntry.getText());

				// SSL Tab
				conf.setUseSSL(entryName, useSSLCheckbox.isSelected());
				conf.setClientCertificateFileName(entryName, clientCertificateEntry.getText());
				conf.setClientCertificatePassword(entryName, new String(exportPasswordEntry.getPassword()));
				conf.setSaveClientCertificatePassword(entryName, saveClientCertificatePasswordCheckbox.isSelected());

				// Others Tab
				conf.setStyleFileName(entryName, styleEntry.getText());
				conf.setEncoding(entryName, encodingEntry.getText());
				conf.setLookAndFeelClassName(entryName, lafs[lookAndFeelCombo.getSelectedIndex()].getClassName());
				for (int i = 0; i < protocolVersionRadios.length; i++) {
					if (protocolVersionRadios[i].isSelected()) {
						conf.setProtocolVersion(entryName, Integer.parseInt(protocolVersionRadios[i].getText()));
						break;
					}
				}
				conf.setUseLogViewer(entryName, useLogViewerCheck.isSelected());
				conf.setUseTimer(entryName, useTimerCheck.isSelected());
				conf.setTimerPeriod(entryName, Long.parseLong(timerPeriodEntry.getText()));
				conf.setProperties(entryName, propertiesText.getText());
				f.dispose();
			}
		});
		bar.add(editOKButton);

		JButton editCancelButton = new JButton(new AbstractAction(Messages.getString("ConfigurationViewer.edit_cancel")) { //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {
				f.dispose();
			}
		});
		bar.add(editCancelButton);
		
		f.setSize(480, 640);
		f.setVisible(true);

		f.setLocationRelativeTo(null);
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
}
