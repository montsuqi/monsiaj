/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.montsuqi.monsiaj.client;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author mihara
 */
public class PrinterConfigPanel extends JPanel {

    private static final int SIZE = 10;

    private final ArrayList<JTextField> nameList;
    private final ArrayList<JComboBox<String>> printerList;

    public PrinterConfigPanel(final List<String> list) {
        super();

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());

        nameList = new ArrayList<>();
        printerList = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            nameList.add(new JTextField());
            JComboBox<String> combo = new JComboBox<>();
            for (String p : list) {
                combo.addItem(p);
            }
            printerList.add(combo);
        }

        int y = 0;
        panel.add(new JLabel("プリンタ名"), createConstraints(0, y, 1, 1, 1.0, 0.0));
        panel.add(new JLabel("割り当てプリンタ"), createConstraints(1, y, 1, 1, 1.0, 0.0));

        for (int i = 0; i < SIZE; i++) {
            y += 1;
            panel.add(nameList.get(i), createConstraints(0, y, 1, 1, 1.0, 0.0));
            panel.add(printerList.get(i), createConstraints(1, y, 1, 1, 1.0, 0.0));
        }

        this.setLayout(new BorderLayout(0, 0));
        this.add(panel, BorderLayout.NORTH);
    }

    public void setPrinterConfigMap(Map<String, String> map) {
        int i;
        for (i = 0; i < SIZE; i++) {
            JTextField name = nameList.get(i);
            name.setText("");
            JComboBox printer = printerList.get(i);
            if (printer.getItemCount() > 1) {
                printer.setSelectedIndex(0);
            }
        }
        i = 0;
        for (Map.Entry<String, String> e : map.entrySet()) {
            if (i < SIZE) {
                JTextField name = nameList.get(i);
                JComboBox printer = printerList.get(i);
                name.setText(e.getKey());
                printer.setSelectedItem(e.getValue());
            }
            i++;
        }
    }

    public TreeMap<String, String> getPrinterConfigMap() {
        TreeMap<String, String> map = new TreeMap<>();
        for (int i = 0; i < SIZE; i++) {
            String name = nameList.get(i).getText();
            String printer = (String) printerList.get(i).getSelectedItem();
            if (!name.isEmpty()) {
                map.put(name, printer);
            }
        }
        return map;
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
}
