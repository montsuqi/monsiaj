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

package jp.or.med.jma_receipt;

import java.io.File;

import javax.swing.JOptionPane;

import org.montsuqi.client.Client;
import org.montsuqi.util.Logger;

public final class Launcher {

	static {
		if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")) { //$NON-NLS-1$ //$NON-NLS-2$
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", Messages.getString("application.title")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public static void main(String[] args) {
		Client client = new Client();
		Logger logger;
		logger = Logger.getLogger(Launcher.class);

		String[] classes = {
				"jp.or.med.jma_receipt.PreferenceBasedConfiguration", //$NON-NLS-1$
				"jp.or.med.jma_receipt.PropertyFileBasedConfiguration" //$NON-NLS-1$
		};
		Class clazz = null;
		for (int i = 0; i < classes.length; i++) {
			try {
				clazz = Class.forName(classes[i]);
				break;
			} catch (ClassNotFoundException e) {
				continue;
			}
		}
		if (clazz == null) {
			logger.fatal(Messages.getString("Launcher.no_configuration_class_found")); //$NON-NLS-1$
		}
		Configuration conf;
		try {
			conf = (Configuration)clazz.newInstance();
			conf.load();
			ConfigurationDialog d = new ConfigurationDialog(conf);
			d.setVisible(true);
			if (d.needRun()) {
				conf = d.getConfiguration();
				conf.save();
				client.setUser(conf.getUser());
				String pass = new String(conf.getPass());
				client.setPass(pass);
				client.setHost(conf.getHost());
				client.setPortNumber(conf.getPort());
				client.setCurrentApplication(conf.getApplication());
				client.setEncoding("EUC-JP"); //$NON-NLS-1$
				client.setStyles(conf.getStyleFile());
				client.setCache(System.getProperty("user.home") + File.separator + "cache"); //$NON-NLS-1$ //$NON-NLS-2$
				try {
					client.connect();
					Thread t = new Thread(client);
					t.start();
				} catch (Exception e2) {
					JOptionPane.showMessageDialog(null, e2.getMessage());
					System.exit(0);
				}
			} else {
				System.exit(0);
			}
		} catch (Exception e) {
			logger.fatal(e);
		}
	}
}
