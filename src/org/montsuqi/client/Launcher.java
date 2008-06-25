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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.logging.Logger;
import javax.security.auth.login.Configuration;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import org.montsuqi.util.Logger;
import org.montsuqi.util.OptionParser;
import org.montsuqi.util.SystemEnvironment;
import org.montsuqi.widgets.ConsolePane;
import org.montsuqi.widgets.ExceptionDialog;
import org.montsuqi.widgets.PandaCList;
import sun.misc.resources.Messages;

public class Launcher {

	protected static final Logger logger = Logger.getLogger(Launcher.class);
	protected String title;
	protected Configuration conf;

	static {
		if (System.getProperty("monsia.logger.factory") == null) { //$NON-NLS-1$
			System.setProperty("monsia.logger.factory", "org.montsuqi.util.StdErrLogger"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (SystemEnvironment.isMacOSX()) {
			System.setProperty("apple.awt.brushMetalLook", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public static void main(String[] args) {		
		Launcher launcher = new Launcher(Messages.getString("application.title")); //$NON-NLS-1$
		launcher.launch(args);
	}

	public Launcher(String title) {
		this.title = title;
		SystemEnvironment.setMacMenuTitle(title);
		conf = new Configuration(this.getClass());
	}

	public boolean checkCommandLineOption(String [] args) {
		OptionParser options = new OptionParser();
		options.add("config", Messages.getString("Launcher.config_option_message"), ""); //$NON-NLS-1$ //$NON-NLS-2$
		options.add("config-list", Messages.getString("Launcher.config_list_option_message"), false); //$NON-NLS-1$ //$NON-NLS-2$
		String[] files = options.parse(this.getClass().getName(), args);
		
		String configName = options.getString("config");
		boolean listConfigFlag = options.getBoolean("config-list");
		if ( listConfigFlag ) {
			conf.listConfiguration();
			return true;
		}
		if ( ! configName.equals("") ) {
			conf.setConfigurationName(configName);
			
			/* set look and feel */
			
			try {
				UIManager.setLookAndFeel(conf.getLookAndFeelClassName(configName));
			} catch (Exception e) {
				logger.warn(e);
				return true;
			}
			
			/* confirm password when the password not preserved */
			if ( 	! conf.getSavePassword(configName) ) {
				JPasswordField pwd = new JPasswordField();
				Object[] message = { Messages.getString("Launcher.input_password_message"), pwd };
				int resp = JOptionPane.showConfirmDialog(null, message, Messages.getString("Launcher.input_password_message"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(resp == JOptionPane.OK_OPTION) {
				   conf.setPassword(configName, String.valueOf(pwd.getPassword()));
				} else {
					return true;
				}
			}
			/* confirm certificate password when the certificate password not preserved */
			if ( conf.getUseSSL(configName) &&
					! conf.getClientCertificateFileName(configName).equals("") &&
					! conf.getSaveClientCertificatePassword(configName)) {
				JPasswordField pwd = new JPasswordField();
				Object[] message = { Messages.getString("Launcher.input_certificate_password_message"), pwd};
				int resp = JOptionPane.showConfirmDialog(null, message, Messages.getString("Launcher.input_certificate_password_message"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(resp == JOptionPane.OK_OPTION) {
				   conf.setClientCertificatePassword(configName, String.valueOf(pwd.getPassword()));
				} else {
					return true;
				}
			}
			connect();
			return true;
		}
		return false;
	}
	
	public void launch(String [] args) {
		if ( checkCommandLineOption(args) ) {
			return;
		}
		final JFrame f = new JFrame(title);
		Container container = f.getContentPane();
		container.setLayout(new BorderLayout(10,5));
		final ConfigurationPanel panel = createConfigurationPanel();
		final ConfigurationViewer viewer = createConfigurationViewer();
		container.add(panel, BorderLayout.CENTER);
		
		JLabel iconLabel = new JLabel("", createIcon(), JLabel.CENTER);
		container.add(iconLabel, BorderLayout.WEST);

		JPanel bar = new JPanel();
		bar.setLayout(new FlowLayout());
		container.add(bar, BorderLayout.SOUTH);

		JButton run = new JButton(new AbstractAction(Messages.getString("Launcher.run_label")) { //$NON-NLS-1$
			public void actionPerformed(ActionEvent ev) {
				panel.updateConfiguration();
				connect();
				f.dispose();
			}
		});
		bar.add(run);

		JButton cancel = new JButton(new AbstractAction(Messages.getString("Launcher.cancel_label")) { //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		bar.add(cancel);
		
		JButton config = new JButton(new AbstractAction(Messages.getString("Launcher.config_label")) { //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {
				viewer.run(f);
				panel.updateConfigCombo();
			}
		});
		bar.add(config);

		f.setSize(640, 400);
		int state = f.getExtendedState();

		f.setLocationRelativeTo(null);
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.setVisible(true);
	}
	
	private void connect() {
		conf.save();
		Client client = new Client(conf);
		JFrame logFrame = conf.getUseLogViewer(conf.getConfigurationName()) ? createLogFrame(client) : null;
		
		try {
			client.connect();
			client.run();
			//Thread t = new Thread(client);
			//t.start();
			//t.join();
		} catch (Exception e) {
			logger.fatal(e);
			ExceptionDialog.showExceptionDialog(e);
			if (logFrame != null) {
				logFrame.setExtendedState(Frame.NORMAL);
			} else {
				client.exitSystem();
			}
		}
	}

	protected ConfigurationPanel createConfigurationPanel() {
		return new ConfigurationPanel(conf); 
	}

	protected ConfigurationViewer createConfigurationViewer() {
		return new ConfigurationViewer(conf);
	}
	
	protected Icon createIcon() {
		return null;
	}
	
	private JFrame createLogFrame(final Client client) {
		final JFrame f = new JFrame(Messages.getString("Launcher.log_title")); //$NON-NLS-1$
		Container container = f.getContentPane();
		container.setLayout(new BorderLayout());
		final ConsolePane console = new ConsolePane();
		System.setOut(console.getOut());
		System.setErr(console.getErr());

		JScrollPane scroll = new JScrollPane(console);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setPreferredSize(new Dimension(640, 480));
		container.add(scroll, BorderLayout.CENTER);

		JPanel bar = new JPanel();
		bar.setLayout(new FlowLayout());
		container.add(bar, BorderLayout.SOUTH);
		JButton clear = new JButton(new AbstractAction(Messages.getString("Launcher.log_clear")) { //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {
				console.setText(""); //$NON-NLS-1$
			}
		});
		bar.add(clear);

		JButton save = new JButton(new AbstractAction(Messages.getString("Launcher.log_save_log_as")) { //$NON-NLS-1$
			public void actionPerformed(ActionEvent ev) {
				JFileChooser chooser = new JFileChooser();
				int ret = chooser.showSaveDialog(f);
				if (ret == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					try {
						FileWriter fw = new FileWriter(file);
						fw.write(console.getText());
						fw.close();
					} catch (IOException e) {
						logger.warn(e);
					}
				}
			}
		});
		bar.add(save);

		JButton quit = new JButton(new AbstractAction(Messages.getString("Launcher.log_quit")) { //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {
				client.exitSystem();
			}
		});
		bar.add(quit);

		f.setSize(640, 480);
		int state = f.getExtendedState();
		f.setExtendedState(state | Frame.ICONIFIED);
		f.setVisible(true);

		f.setLocationRelativeTo(null);
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				client.exitSystem();
			}
		});
		return f;
	}
}
