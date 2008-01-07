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
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;


import org.montsuqi.util.Logger;
import org.montsuqi.widgets.Pixmap;

/** <p>A class that simulates Gtk+'s FileEntry.</p>
 * 
 * <p>It can hold a binary data with it.</p>
 */
public class PixmapEntry extends JComponent {
	private JTextField entry;
	private JButton browseButton;
	private Pixmap pixmap;
	private Logger logger;
	byte[] data;

	public PixmapEntry() {
		initComponents();
		layoutComponents();
	}

	private void initComponents() {
		pixmap = new Pixmap();
		entry = new JTextField();
		browseButton = new JButton();
		add(pixmap);
		add(entry);
		add(browseButton);
		browseButton.setAction(new BrowseAction());
	}

	private void layoutComponents() {
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);

		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbl.addLayoutComponent(pixmap, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbl.addLayoutComponent(entry, gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbl.addLayoutComponent(browseButton, gbc);
	}

	public void setFile(String fileName) {
		entry.setText(fileName);
	}

	public void setFile(File file) {
		try {
			setFile(file.getCanonicalPath());
		} catch (IOException e) {
			logger.warn(e);
		}
	}
	
	public File getFile() {
		return new File(entry.getText());
	}

	private class BrowseAction extends AbstractAction {
		BrowseAction() {
			super(Messages.getString("PixmapEntry.browse")); //$NON-NLS-1$
		}

		public void actionPerformed(ActionEvent e) {
			final JFileChooser chooser = new JFileChooser();
		
			final File initialFile = getFile();
			chooser.setSelectedFile(initialFile);

			if (chooser.showOpenDialog(PixmapEntry.this) != JFileChooser.APPROVE_OPTION) {
				return;
			}
			File selected = chooser.getSelectedFile();
			setFile(selected);
			printPreview();
		}
	}
	
	public void clearPreview() {
		pixmap.setIcon(null);
	}

	void printPreview() {
		try {
			byte [] data;
			data = loadData();
			Icon icon = new ImageIcon(data);
			pixmap.setScaled(false);
			pixmap.setText("");
			pixmap.setIcon(icon);
		} catch (Exception e) {
			return;
		}
	}
		
	public byte[] loadData() {
		FileInputStream in;
		byte [] data = new byte[0];
		try {
			int length = (int)getFile().length();
			data = new byte[length];
			in = new FileInputStream(getFile());
			in.read(data, 0, length);
			in.close();
		} catch (IOException e){
			JOptionPane.showMessageDialog(PixmapEntry.this, e.getMessage(), Messages.getString("PixmapEntry.error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
		}
		return data;
	}

	public JTextField getEntry() {
		return entry;
	}

	public AbstractButton getBrowseButton() {
		return browseButton;
    }
}
