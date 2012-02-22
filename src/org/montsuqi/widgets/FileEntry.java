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
package org.montsuqi.widgets;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.prefs.Preferences;
import javax.swing.*;
import org.montsuqi.util.Logger;

/** <p>A class that simulates Gtk+'s FileEntry.</p>
 * 
 * <p>It can hold a binary data with it.</p>
 */
public class FileEntry extends JComponent {

    private Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    private JTextField fileEntry;
    private JButton browseButton;
    private Logger logger;
    byte[] data;

    public FileEntry() {
        initComponents();
        layoutComponents();
        data = new byte[0];
    }

    private void initComponents() {
        fileEntry = new JTextField();
        browseButton = new JButton();
        add(fileEntry);
        add(browseButton);
        browseButton.setAction(new BrowseActionForSave());
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

    public void setFile(String fileName) {
        fileEntry.setText(fileName);
    }

    public void setFile(File file) {
        try {
            setFile(file.getCanonicalPath());
        } catch (IOException e) {
           System.out.println(e);
        }
    }

    public File getFile() {
        return new File(fileEntry.getText());
    }

    public void setSaveAction() {
        browseButton.setAction(new BrowseActionForSave());
    }

    public void setLoadAction() {
        browseButton.setAction(new BrowseActionForLoad());
    }

    private class BrowseActionForSave extends AbstractAction {

        BrowseActionForSave() {
            super(Messages.getString("FileEntry.browse")); //$NON-NLS-1$
        }

        public void actionPerformed(ActionEvent e) {
            final String dir = prefs.get(FileEntry.class.getName(), System.getProperty("user.home"));
            final JFileChooser chooser = new JFileChooser(dir);
            chooser.setSelectedFile(getFile());

            if (chooser.showSaveDialog(FileEntry.this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File selected = chooser.getSelectedFile();
            setFile(selected);
            assert data != null;
            if (data.length <= 0) {
                return;
            }
            if (selected.exists() && selected.canWrite()) {
                if (JOptionPane.showConfirmDialog(FileEntry.this, Messages.getString("FileEntry.ask_overwrite"), Messages.getString("FileEntry.question"), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) { //$NON-NLS-1$ //$NON-NLS-2$
                    return;
                }
            }
            prefs.put(FileEntry.class.getName(), selected.getParent());
            saveData();
        }
    }

    private class BrowseActionForLoad extends AbstractAction {

        BrowseActionForLoad() {
            super(Messages.getString("FileEntry.browse")); //$NON-NLS-1$
        }

        public void actionPerformed(ActionEvent e) {
            final String dir = prefs.get(FileEntry.class.getName(), System.getProperty("user.home"));
            final JFileChooser chooser = new JFileChooser(dir);
            chooser.setSelectedFile(getFile());

            if (chooser.showOpenDialog(FileEntry.this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File selected = chooser.getSelectedFile();
            setFile(selected);
            prefs.put(FileEntry.class.getName(), selected.getParent());
            fileEntry.postActionEvent();
        }
    }

    public void setData(byte[] data) {
        this.data = new byte[data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);
    }

    public byte[] getData() {
        byte[] result = new byte[data.length];
        System.arraycopy(result, 0, data, 0, data.length);
        return result;
    }

    void saveData() {
        FileOutputStream out;
        try {
            out = new FileOutputStream(getFile());
            out.write(data);
            JOptionPane.showMessageDialog(FileEntry.this, Messages.getString("FileEntry.saved")); //$NON-NLS-1$
            out.flush();
            out.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(FileEntry.this, e.getMessage(), Messages.getString("FileEntry.error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
        }
    }

    public byte[] loadData() {
        FileInputStream in;
        byte[] bytes = new byte[0];
        try {
            int length = (int) getFile().length();
            bytes = new byte[length];
            in = new FileInputStream(getFile());
            in.read(bytes, 0, length);
            in.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(FileEntry.this, e.getMessage(), Messages.getString("FileEntry.error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
        }
        return bytes;
    }

    public JTextField getEntry() {
        return fileEntry;
    }

    public AbstractButton getBrowseButton() {
        return browseButton;
    }
}
