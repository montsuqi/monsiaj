package org.montsuqi.certman;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

public class Main {

	static {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	class P12Filter extends FileFilter {
		public boolean accept(File f) {
			return f.getName().endsWith(".p12"); 
		}

		public String getDescription() {
			return "PKCS#12"; 
		}
	}

	class BrowseAction extends AbstractAction {
		BrowseAction() {
			super(Messages.getString("Browse")); 
		}

		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			chooser.showSaveDialog(frame);
			File keyStoreFile = chooser.getSelectedFile();
			if (keyStoreFile == null) {
				return;
			}
			ks = loadKeyStore(keyStoreFile, "JKS", Messages.getString("InputPassphrase"));  //$NON-NLS-2$
			importAction.setEnabled(ks != null);
			storeField.setText(keyStoreFile.getPath());
			storeField.setCaretPosition(storeField.getText().length());
			assert ks != null;
		}
	}

	public class ImportAction extends AbstractAction {
		ImportAction() {
			super(Messages.getString("Import")); 
		}
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new P12Filter());
			chooser.showOpenDialog(frame);
			File p12File = chooser.getSelectedFile();
			if (p12File == null) {
				return;
			}
			KeyStore p12Store = loadKeyStore(p12File, "PKCS12", Messages.getString("InputExportPass"));  //$NON-NLS-2$
			try {
				Enumeration aliases = p12Store.aliases();
				while (aliases.hasMoreElements()) {
					String alias = (String)aliases.nextElement();
					Certificate[] chain = p12Store.getCertificateChain(alias);
					if (p12Store.isKeyEntry(alias)) {
						char[] pass = passPhrases.get(p12Store);
						Key key = p12Store.getKey(alias, pass);
						ks.setKeyEntry(alias, key, pass, chain);
						disposePass(p12Store);
					}
				}
				char[] ksPass = (char[])passPhrases.get(ks);
				File ksFile = (File)files.get(ks);
				ks.store(new FileOutputStream(ksFile), ksPass);
				disposePass(ks);
				JOptionPane.showMessageDialog(frame, Messages.getString("KeyStoreSaved")); 
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(frame, ex.getMessage(), Messages.getString("Error"), JOptionPane.ERROR_MESSAGE); 
			}
		}
	}

	public class QuitAction extends AbstractAction {
		QuitAction() {
			super(Messages.getString("Quit")); 
		}
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}

	JFrame frame;
	private JLabel storeLabel;
	JTextField storeField;
	private JButton browseButton;
	private JButton importButton;
	private JButton quitButton;
	private JPanel buttons;

	Map<KeyStore,File> files;
	Map<KeyStore,char[]> passPhrases;
	KeyStore ks;
	ImportAction importAction;

	public static void main(String[] args) {
		new Main();
	}

	Main() {
		files = new HashMap<>();
		passPhrases = new HashMap<>();
		ks = null;
		initComponents();
		layoutComponents();
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private void initComponents() {
		frame = new JFrame(Messages.getString("Application.title")); 
		Container root = frame.getContentPane();

		storeLabel = new JLabel(Messages.getString("KeyStore")); 
		root.add(storeLabel);

		storeField = new JTextField(20);
		storeField.setEditable(false);
		root.add(storeField);

		browseButton = new JButton();
		browseButton.setAction(new BrowseAction());
		root.add(browseButton);

		buttons = new JPanel();
		root.add(buttons);

		importButton = new JButton();
		importAction = new ImportAction();
		importAction.setEnabled(false);
		importButton.setAction(importAction);
		buttons.add(importButton);

		quitButton = new JButton();
		quitButton.setAction(new QuitAction());
		buttons.add(quitButton);
	}

	private void layoutComponents() {
		Container root = frame.getContentPane();
		GridBagLayout gbl = new GridBagLayout();
		root.setLayout(gbl);

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0; gbc.gridy = 0;
		gbc.gridwidth = 1; gbc.gridheight = 1;
		gbc.weightx = 0.0; gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbl.setConstraints(storeLabel, gbc);

		gbc.gridx = 1; gbc.gridy = 0;
		gbc.gridwidth = 4; gbc.gridheight = 1;
		gbc.weightx = 1.0; gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbl.setConstraints(storeField, gbc);

		gbc.gridx = 5; gbc.gridy = 0;
		gbc.gridwidth = 1; gbc.gridheight = 1;
		gbc.weightx = 0.0; gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbl.setConstraints(browseButton, gbc);

		gbc.gridx = 0; gbc.gridy = 1;
		gbc.gridwidth = 6; gbc.gridheight = 1;
		gbc.weightx = 1.0; gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbl.setConstraints(buttons, gbc);

		buttons.setLayout(new FlowLayout(FlowLayout.TRAILING));
	}

	KeyStore loadKeyStore(File keyStoreFile, String type, String message) {
		KeyStore keyStore = null;
		try {
			keyStore = KeyStore.getInstance(type);
			char[] pass = askPass(message);
			keyStore.load(new FileInputStream(keyStoreFile), pass);
			files.put(keyStore, keyStoreFile);
			passPhrases.put(keyStore, pass);
			return keyStore;
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(frame, ex.getMessage(), Messages.getString("Error"), JOptionPane.ERROR_MESSAGE); 
			return null;
		}
	}

	private char[] askPass(String message) {
		JPasswordField passField = new JPasswordField();
		Object[] content = { new JLabel(message), passField };
		JOptionPane.showMessageDialog(frame, content, message, JOptionPane.PLAIN_MESSAGE);
		return passField.getPassword();
	}

	void disposePass(KeyStore keyStore) {
		char[] pass = (char[])passPhrases.get(keyStore);
		for (int i = 0, n = pass.length; i < n; i++) {
			pass[i] = 0;
		}
	}
}
