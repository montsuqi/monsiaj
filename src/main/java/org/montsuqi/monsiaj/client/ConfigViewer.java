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
package org.montsuqi.monsiaj.client;

import org.montsuqi.monsiaj.util.Messages;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.montsuqi.monsiaj.widgets.Button;

public class ConfigViewer {

    protected static final Logger logger = LogManager.getLogger(Launcher.class);
    protected Config conf;

    public ConfigViewer(Config conf) {
        this.conf = conf;
    }

    public void run(Frame parent) {
        final JDialog f = new JDialog(parent, Messages.getString("ConfigurationViewer.title"), true);
        Container container = f.getContentPane();
        container.setLayout(new BorderLayout(5, 5));
        final JTable table = new JTable();
        table.setRowSelectionAllowed(true);

        DefaultCellEditor ce = (DefaultCellEditor) table.getDefaultEditor(Object.class);
        ce.addCellEditorListener(new CellEditorListener() {
            @Override
            public void editingStopped(ChangeEvent e) {
                int row = table.getSelectedRow();
                int col = table.getSelectedColumn();
                if (row != -1 && col == 0) {
                    int num = (int) table.getValueAt(row, 3);
                    String configName = (String) (table.getValueAt(row, col));
                    conf.setDescription(num, configName);
                }
            }

            @Override
            public void editingCanceled(ChangeEvent e) {
            }
        });

        java.util.List<Integer> list = conf.getList();
        final String[] ColumnNames = {
            Messages.getString("ConfigurationViewer.config_name"),
            Messages.getString("ConfigurationViewer.authuri"),
            Messages.getString("ConfigurationViewer.user"),
            Messages.getString("ConfigurationViewer.num")
        };
        Object[][] tableData = new Object[list.size()][4];
        int j = 0;
        for (int i : list) {
            tableData[j][0] = conf.getDescription(i);
            tableData[j][1] = conf.getAuthURI(i);
            tableData[j][2] = conf.getUser(i);
            tableData[j][3] = i;
            j++;
        }
        DefaultTableModel model = new DefaultTableModel(tableData, ColumnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };
        table.setModel(model);

        JScrollPane scroll = new JScrollPane(table,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        container.add(scroll, BorderLayout.CENTER);
        scroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel bar = new JPanel();
        bar.setLayout(new FlowLayout());
        container.add(bar, BorderLayout.SOUTH);

        Button newButton = new Button(new AbstractAction(Messages.getString("ConfigurationViewer.new")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                int num = conf.getNext();
                conf.setDescription(num, "server" + num);
                conf.save();
                updateConfigList(table);
                int selectedRow = 0;
                for (int i = 0; i < table.getRowCount(); i++) {
                    if (num == (int) table.getValueAt(i, 3)) {
                        selectedRow = i;
                    }
                }
                table.changeSelection(selectedRow, 0, false, false);
            }
        });
        bar.add(newButton);

        Button copyButton = new Button(new AbstractAction(Messages.getString("ConfigurationViewer.copy")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    int num = conf.copyConfig((int) table.getValueAt(row, 3));
                    conf.save();
                    updateConfigList(table);
                    int selectedRow = 0;
                    for (int i = 0; i < table.getRowCount(); i++) {
                        if (num == (int) table.getValueAt(i, 3)) {
                            selectedRow = i;
                        }
                    }
                    table.changeSelection(selectedRow, 0, false, false);
                }
            }
        });
        bar.add(copyButton);

        Button deleteButton = new Button(new AbstractAction(Messages.getString("ConfigurationViewer.delete")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    int result = JOptionPane.showConfirmDialog(f, Messages.getString("ConfigurationViewer.delete_confirm_message"), Messages.getString("ConfigurationViewer.delete_confirm"), JOptionPane.YES_NO_OPTION);  //$NON-NLS-2$
                    if (result == JOptionPane.YES_OPTION) {
                        int num = (int) table.getValueAt(row, 3);
                        String name = (String) table.getValueAt(row, 0);
                        conf.deleteConfig(num);
                        updateConfigList(table);
                        logger.info("server name:" + name + " num:" + num + " deleted");
                    }
                }
            }
        });
        bar.add(deleteButton);

        Button closeButton = new Button(new AbstractAction(Messages.getString("ConfigurationViewer.close")) {

            @Override
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

    private void updateConfigList(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        java.util.List<Integer> list = conf.getList();
        Object[] tableData = new Object[4];
        for (int i : list) {
            tableData[0] = conf.getDescription(i);
            tableData[1] = conf.getAuthURI(i);
            tableData[2] = conf.getUser(i);
            tableData[3] = i;
            model.addRow(tableData);
        }
    }

    protected ConfigPanel createConfigPanel(Config conf) {
        return new ConfigPanel(conf, false, false);
    }

    protected void editConfig(Dialog parent, final int num, final boolean newFlag) {
        String title = newFlag ? Messages.getString("ConfigurationViewer.new") : Messages.getString("ConfigurationViewer.edit");
        final JDialog f = new JDialog(parent, title, true);
        Container container = f.getContentPane();
        container.setLayout(new BorderLayout());

        final ConfigPanel configPanel = createConfigPanel(conf);

        configPanel.loadConfig(num);

        JPanel configNamePanel = new JPanel(new GridBagLayout());
        configNamePanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        configNamePanel.add(ConfigPanel.createLabel(Messages.getString("ConfigurationPanel.config_name")),
                ConfigPanel.createConstraints(0, 0, 1, 1, 0.0, 1.0));
        final JTextField configNameEntry = ConfigPanel.createTextField();
        configNameEntry.setText(conf.getDescription(num));
        configNamePanel.add(configNameEntry,
                ConfigPanel.createConstraints(1, 0, 1, 4, 1.0, 0.0));

        JPanel basicPanel = configPanel.getBasicPanel();
        JPanel sslPanel = configPanel.getSSLPanel();
        PrinterConfigPanel printerConfigPanel = configPanel.getPrinterConfigPanel();
        JPanel othersPanel = configPanel.getOthersPanel();

        Border border = BorderFactory.createMatteBorder(1, 1, 1, 1, (Color) SystemColor.controlDkShadow);
        basicPanel.setBorder(
                BorderFactory.createTitledBorder(border,
                        Messages.getString("ConfigurationPanel.basic_tab_label")));
        sslPanel.setBorder(
                BorderFactory.createTitledBorder(border,
                        Messages.getString("ConfigurationPanel.ssl_tab_label")));
        printerConfigPanel.setBorder(
                BorderFactory.createTitledBorder(border,
                        Messages.getString("ConfigurationPanel.printer_config_tab_label")));
        othersPanel.setBorder(
                BorderFactory.createTitledBorder(border,
                        Messages.getString("ConfigurationPanel.others_tab_label")));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(configNamePanel);
        mainPanel.add(basicPanel);
        mainPanel.add(sslPanel);
        mainPanel.add(printerConfigPanel);
        mainPanel.add(othersPanel);
        container.add(mainPanel, BorderLayout.CENTER);

        //buttons
        JPanel bar = new JPanel();
        bar.setLayout(new FlowLayout());
        container.add(bar, BorderLayout.SOUTH);

        Button editOKButton = new Button(new AbstractAction(Messages.getString("ConfigurationViewer.edit_ok")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                String entryName = configNameEntry.getText();
                configPanel.saveConfig(num, entryName);
                logger.info("server config:" + entryName + " edited");
                f.dispose();
            }
        });
        bar.add(editOKButton);

        Button editCancelButton = new Button(new AbstractAction(Messages.getString("ConfigurationViewer.edit_cancel")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                f.dispose();
            }
        });
        bar.add(editCancelButton);

        f.setSize(480, 900);
        f.setVisible(true);

        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }
}
