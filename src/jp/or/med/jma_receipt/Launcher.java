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

import javax.swing.JOptionPane;

import org.montsuqi.client.Client;
import org.montsuqi.util.Logger;

public final class Launcher {

	public static void main(String[] args) {
		Client client = new Client();
		Class clazz = null;
		Logger logger;
		logger = Logger.getLogger(Launcher.class);

		try {
			clazz = Class.forName("jp.or.med.jma_receipt.PreferenceBasedConfiguration"); //$NON-NLS-1$
		} catch (ClassNotFoundException e1) {
			try {
				clazz = Class.forName("jp.or.med.jma_receipt.PropertyFileBasedConfiguration"); //$NON-NLS-1$
			} catch (ClassNotFoundException e2) {
				logger.fatal(e2);
			}
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
				client.setCache(System.getProperty("user.home") + System.getProperty("file.separator") + "cache"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

				try {
					client.connect();
					Thread t = new Thread(client);
					t.start();
				} catch (Exception e2) {
					JOptionPane.showMessageDialog(null, e2.getMessage());
				}
			} else {
				System.exit(0);
			}
		} catch (Exception e) {
			logger.fatal(e);
		}
	}
}
