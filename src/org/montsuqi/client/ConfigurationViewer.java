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
import java.awt.Color;
import java.awt.SystemColor;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.GridBagLayout;

import javax.swing.AbstractAction;
import javax.swing.border.Border;  
import javax.swing.BorderFactory;  
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.WindowConstants;

import org.montsuqi.util.Logger;
import org.montsuqi.util.SystemEnvironment;
import org.montsuqi.widgets.PandaCList;
import org.montsuqi.widgets.Button;

public class ConfigurationViewer{

	protected static final Logger logger = Logger.getLogger(Launcher.class);
	protected Configuration conf;
	
	static {
		if (System.getProperty("monsia.logger.factory") == null) { //$NON-NLS-1$
			System.setProperty("monsia.logger.factory", "org.montsuqi.util.StdErrLogger"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (SystemEnvironment.isMacOSX()) {
			System.setProperty("apple.awt.brushMetalLook", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public ConfigurationViewer(Configuration conf) {
		this.conf = conf;
	}
		
	public void run(Frame parent) {
		final JDialog f = new JDialog(parent, Messages.getString("ConfigurationViewer.title"), true); //$NON-NLS-1$		
		Container container = f.getContentPane();
		container.setLayout(new BorderLayout(5,5));
		final PandaCList clist = new PandaCList();
		clist.setFocusable(true);
		clist.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		clist.setShowGrid(true);
		updateConfigurationList(clist);
		clist.setAutoResizeMode(PandaCList.AUTO_RESIZE_LAST_COLUMN);

		JScrollPane scroll = new JScrollPane(clist,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		container.add(scroll, BorderLayout.CENTER);
		scroll.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		JPanel bar = new JPanel();
		bar.setLayout(new FlowLayout());
		container.add(bar, BorderLayout.SOUTH);
		
		Button newButton = new Button(new AbstractAction(Messages.getString("ConfigurationViewer.new")) { //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {
				editConfiguration(f, "new", true);
				updateConfigurationList(clist);
			}
		});
		bar.add(newButton);
		
		Button editButton = new Button(new AbstractAction(Messages.getString("ConfigurationViewer.edit")) { //$NON-NLS-1$
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
		
		Button deleteButton = new Button(new AbstractAction(Messages.getString("ConfigurationViewer.delete")) { //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {
				int selectedRow = clist.getSelectedRow();
				if ( selectedRow >= 0 ) {
					int result = JOptionPane.showConfirmDialog(f, Messages.getString("ConfigurationViewer.delete_confirm_message"), Messages.getString("ConfigurationViewer.delete_confirm"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$ //$NON-NLS-2$
					if (result == JOptionPane.YES_OPTION) {
						String configName = (String)(clist.getValueAt(selectedRow, 0));
						conf.deleteConfiguration(configName);
						updateConfigurationList(clist);
					}
				}
			}
		});
		bar.add(deleteButton);

		Button closeButton = new Button(new AbstractAction(Messages.getString("ConfigurationViewer.close")) { //$NON-NLS-1$
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
			if ( configNames[i].equals(Configuration.DEFAULT_CONFIG_NAME) ) {
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
	
	protected ConfigurationPanel createConfigurationPanel(Configuration conf) {
		return new ConfigurationPanel(conf, false, false);
	}
	
	protected void editConfiguration(Dialog parent, String name, boolean flag) {
		final boolean newFlag = flag;
		final String configName = name;
		String title = newFlag ? Messages.getString("ConfigurationViewer.new") : Messages.getString("ConfigurationViewer.edit");
		final JDialog f = new JDialog(parent, title, true); //$NON-NLS-1$
		Container container = f.getContentPane();
		container.setLayout(new BorderLayout());
		
		final ConfigurationPanel configPanel = createConfigurationPanel(conf);
		if ( newFlag == false ) {
			configPanel.loadConfiguration(name);
		}
				
		JPanel configNamePanel = new JPanel(new GridBagLayout());
		configNamePanel.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
		configNamePanel.add(ConfigurationPanel.createLabel(Messages.getString("ConfigurationPanel.config_name")),
			ConfigurationPanel.createConstraints(0, 0, 1, 1, 0.0, 1.0));
		final JTextField configNameEntry = ConfigurationPanel.createTextField();
		configNameEntry.setText(name);
		configNamePanel.add(configNameEntry,
			ConfigurationPanel.createConstraints(1, 0, 1, 4, 1.0, 0.0));
		
		JPanel basicPanel = configPanel.getBasicPanel();
		JPanel sslPanel = configPanel.getSSLPanel();
		JPanel othersPanel = configPanel.getOthersPanel();
		
		Border border = BorderFactory.createMatteBorder(1,1,1,1, (Color)SystemColor.controlDkShadow);
		basicPanel.setBorder(
			BorderFactory.createTitledBorder(border,
				Messages.getString("ConfigurationPanel.basic_tab_label")));
		sslPanel.setBorder(
			BorderFactory.createTitledBorder(border,
				Messages.getString("ConfigurationPanel.ssl_tab_label")));
		othersPanel.setBorder(
			BorderFactory.createTitledBorder(border,
				Messages.getString("ConfigurationPanel.others_tab_label")));
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(configNamePanel);
		mainPanel.add(basicPanel);
		mainPanel.add(sslPanel);
		mainPanel.add(othersPanel);
		container.add(mainPanel, BorderLayout.CENTER);
		
		//buttons
		JPanel bar = new JPanel();
		bar.setLayout(new FlowLayout());
		container.add(bar, BorderLayout.SOUTH);
		
		Button editOKButton = new Button(new AbstractAction(Messages.getString("ConfigurationViewer.edit_ok")) { //$NON-NLS-1$
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
				configPanel.saveConfiguration(entryName);
				f.dispose();
			}
		});
		bar.add(editOKButton);

		Button editCancelButton = new Button(new AbstractAction(Messages.getString("ConfigurationViewer.edit_cancel")) { //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {
				f.dispose();
			}
		});
		bar.add(editCancelButton);
		
		f.setSize(480, 700);
		f.setVisible(true);

		f.setLocationRelativeTo(null);
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
}
