/*      PANDA -- a simple transaction monitor

Copyright (C) 1998-1999 Ogochan.
              2000-2003 Ogochan & JMA (Japan Medical Association).

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
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import org.montsuqi.util.Logger;
import org.montsuqi.util.SystemEnvironment;
import org.montsuqi.widgets.ConsolePane;

public class Launcher {

	protected static final Logger logger = Logger.getLogger(Launcher.class);

	private Client client;
	protected Configuration conf;
	protected String title;

	static {
		if (System.getProperty("monsia.logger.factory") == null) { //$NON-NLS-1$
			System.setProperty("monsia.logger.factory", "org.montsuqi.util.StdErrLogger"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public static void main(String[] args) {
		Launcher launcher = new Launcher(Messages.getString("application.title")); //$NON-NLS-1$
		launcher.launch();
	}

	public Launcher(String title) {
		this.title = title;
		SystemEnvironment.setMacMenuTitle(title);
		conf = new Configuration(this.getClass());
		client = new Client(conf);
	}

	public void launch() {
		ConfigurationPanel panel = createConfigurationPanel();
		Icon icon = createIcon();
		int result = configure(panel, icon);
		if (result != JOptionPane.OK_OPTION) {
			System.exit(0);
		}
		panel.updateConfiguration();
		conf.save();
		JFrame logFrame = conf.getUseLogViewer() ? createLogFrame() : null;
		try {
			client.connect();
			Thread t = new Thread(client);
			t.start();
		} catch (Exception e) {
			logger.fatal(e);
			JOptionPane.showMessageDialog(null, e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			if (logFrame != null) {
				logFrame.setExtendedState(Frame.NORMAL);
			}
		}
	}

	protected ConfigurationPanel createConfigurationPanel() {
		return new ConfigurationPanel(conf); 
	}

	protected Icon createIcon() {
		return null;
	}

	private int configure(ConfigurationPanel panel, Icon icon) {
		Object[] options = { Messages.getString("Launcher.run_label"), Messages.getString("Launcher.cancel_label") }; //$NON-NLS-1$ //$NON-NLS-2$
		Object initial = options[0];
		return JOptionPane.showOptionDialog(null, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, icon, options, initial);
	}

	private JFrame createLogFrame() {
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
		JButton clear = new JButton(Messages.getString("Launcher.log_clear")); //$NON-NLS-1$
		clear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				console.setText(""); //$NON-NLS-1$
			}
		});
		bar.add(clear);

		JButton save = new JButton(Messages.getString("Launcher.log_save_log_as")); //$NON-NLS-1$
		save.addActionListener(new ActionListener() {
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

		f.setSize(640, 480);
		int state = f.getExtendedState();
		f.setExtendedState(state | Frame.ICONIFIED);
		f.setVisible(true);

		f.setLocationRelativeTo(null);
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		return f;
	}
}
