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
package org.montsuqi.monsiaj.widgets;

import org.montsuqi.monsiaj.util.Messages;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.prefs.Preferences;
import javax.swing.*;
import org.montsuqi.monsiaj.util.GtkStockIcon;

/**
 * <p>
 * A class that simulates Gtk+'s FileChooserButton.</p>
 *
 * <p>
 * It can hold a binary data with it.</p>
 */
public class FileChooserButton extends JComponent {

    private Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    private JTextField fileEntry;
    private JButton browseButton;
    private File file;
    byte[] data;

    public FileChooserButton() {
        initComponents();
        layoutComponents();
        data = new byte[0];
        file = null;
    }

    private void initComponents() {
        fileEntry = new JTextField();
        fileEntry.setEditable(false);
        browseButton = new JButton();
        add(fileEntry);
        add(browseButton);
        browseButton.setAction(new BrowseActionForLoad());
        browseButton.setIcon(GtkStockIcon.get("gtk-open"));
    }

    private void layoutComponents() {
        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbl.addLayoutComponent(fileEntry, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        gbl.addLayoutComponent(browseButton, gbc);
    }

    public JButton getBrowseButton() {
        return browseButton;
    }

    private class BrowseActionForLoad extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            final String dir = prefs.get(FileChooserButton.class.getName(), System.getProperty("user.home"));
            final JFileChooser chooser = new JFileChooser(dir);

            if (chooser.showOpenDialog(FileChooserButton.this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            setFile(chooser.getSelectedFile());
            prefs.put(FileChooserButton.class.getName(), file.getParent());
        }
    }

    public byte[] loadData() {
        FileInputStream in;
        byte[] bytes = new byte[0];
        if (file != null) {
            try {
                int length = (int) file.length();
                bytes = new byte[length];
                in = new FileInputStream(file);
                in.read(bytes, 0, length);
                in.close();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(FileChooserButton.this, e.getMessage(), Messages.getString("FileChooserButton.error"), JOptionPane.ERROR_MESSAGE);
            }
        }
        return bytes;
    }

    public void setFile(File file) {
        this.file = file;
        if (file != null) {
            fileEntry.setText(file.getName());
        } else {
            fileEntry.setText(Messages.getString("FileChooserButton.none"));
        }
    }

    public String getFileName() {
        return fileEntry.getText();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("FileChooserButton");
        Container container = frame.getContentPane();
        container.setLayout(new BorderLayout(10, 5));

        final FileChooserButton fcb = new FileChooserButton();
        container.add(fcb, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        container.add(buttonPanel, BorderLayout.SOUTH);

        JButton button3 = new JButton(new AbstractAction("output") {

            public void actionPerformed(ActionEvent ev) {
                System.out.println(fcb.getFileName());
            }
        });

        buttonPanel.add(button3);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setSize(500, 500);
        frame.setVisible(true);
    }
}
