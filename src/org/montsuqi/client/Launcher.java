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
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
		JDialog d = createConfigurationDialog();
		d.setLocationRelativeTo(null);
		conf.setConfigured(false);
		d.setVisible(true);
		if (conf.isConfigured()) {
			conf.save();
			if (conf.getUseLogViewer()) {
				createLogFrame();
			}
			try {
				client.connect();
				Thread t = new Thread(client);
				t.start();
			} catch (Exception e) {
				logger.fatal(e);
			}
		} else {
			System.exit(0);
		}
	}

	public JDialog createConfigurationDialog() {
		return new DefaultConfigurationDialog(title, conf);
	}

	private void createLogFrame() {
		final JFrame f = new JFrame(Messages.getString("Launcher.log_title")); //$NON-NLS-1$
		Container container = f.getContentPane();
		container.setLayout(new BorderLayout());

		final ConsolePane console = new ConsolePane();
		System.setOut(console.getOut());
		System.setErr(console.getErr());

		JScrollPane scroll = new JScrollPane(console);
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
	}
}
