/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.montsuqi.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

/**
 *
 * @author mihara
 */
public class PrinterConfigPanel extends JPanel {

    private PrinterConfigTableModel model;

    public PrinterConfigPanel(final List<String> printerList) {
        super();

        this.setLayout(new BorderLayout(10, 5));
        model = new PrinterConfigTableModel();
        final JTable table = new JTable();
        table.setModel(model);
        TableColumn col = table.getColumnModel().getColumn(1);
        col.setCellEditor(new PrinterConfigCellEditor(printerList));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sp = new JScrollPane(table);
        this.add(sp, BorderLayout.CENTER);

        if (System.getProperty("monsia.pandaclist.rowheight") != null) {
            int rowheight = Integer.parseInt(System.getProperty("monsia.pandaclist.rowheight"));
            table.setRowHeight(rowheight);
        } else {
            table.setRowHeight(30);
        }

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        this.add(buttonPanel, BorderLayout.SOUTH);

        JButton button1 = new JButton(new AbstractAction("追加") {
            @Override
            public void actionPerformed(ActionEvent ev) {
                String printer = "";
                for (String p : printerList) {
                    printer = p;
                    break;
                }
                model.addRow(new String[]{"printer", printer});
            }
        });
        buttonPanel.add(button1);

        JButton button2 = new JButton(new AbstractAction("削除") {
            @Override
            public void actionPerformed(ActionEvent ev) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    model.removeRow(table.getSelectedRow());
                }
            }
        });
        buttonPanel.add(button2);
    }

    public void setPrinterConfigMap(Map<String, String> map) {
        model.setRowCount(0);
        for (Map.Entry<String, String> e : map.entrySet()) {
            model.addRow(new String[]{e.getKey(), e.getValue()});
        }
    }

    public TreeMap<String, String> getPrinterConfigMap() {
        TreeMap<String, String> map = new TreeMap<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            map.put((String) model.getValueAt(i, 0), (String) model.getValueAt(i, 1));
        }
        return map;
    }

    private class PrinterConfigCellEditor extends AbstractCellEditor implements TableCellEditor {

        private final JComboBox<String> combo;
        private Object value;

        private Component editor;

        public PrinterConfigCellEditor(List<String> printerList) {
            combo = new JComboBox<>();
            combo.removeAllItems();
            for (String printer : printerList) {
                combo.addItem(printer);
            }
            combo.setBorder(BorderFactory.createEmptyBorder());
        }

        @Override
        public Object getCellEditorValue() {
            return value;
        }

        @Override
        public boolean stopCellEditing() {
            value = "";
            if (editor instanceof JComboBox) {
                JComboBox combo1 = (JComboBox) editor;
                if (combo1.getItemCount() > 0) {
                    value = combo1.getSelectedItem();
                }
            }
            return super.stopCellEditing();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            combo.setSelectedItem(value);
            editor = combo;
            return editor;
        }
    }

    private class PrinterConfigTableModel extends DefaultTableModel {

        public PrinterConfigTableModel() {
            super();
            this.setColumnCount(2);
            this.setRowCount(0);
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }

        @Override
        public String getColumnName(int column) {
            if (column == 0) {
                return "プリンタ名";
            } else {
                return "割り当てプリンタ";
            }
        }
    }
}
