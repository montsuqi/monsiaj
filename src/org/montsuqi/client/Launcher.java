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

import javax.swing.JOptionPane;

public abstract class Launcher {

	protected Client target;
	protected Configuration conf;
	protected String title;

	static {
		if (System.getProperty("monsia.logger.factory") == null) { //$NON-NLS-1$
			System.setProperty("monsia.logger.factory", "org.montsuqi.util.NullLogger"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public Launcher(String title) {
		this.title = title;
		if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")) { //$NON-NLS-1$ //$NON-NLS-2$
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", title); //$NON-NLS-1$
		}
		conf = Configuration.createConfiguration(this.getClass());
		target = new Client(conf);
	}

	public void launch() {
		ConfigurationDialog d = createDialog(conf);
		d.setLocationRelativeTo(null);
		d.setVisible(true);
		if (d.needRun()) {
			conf.save();
			try {
				target.connect();
				Thread t = new Thread(target);
				t.start();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, e.getMessage());
				System.exit(0);
			}
		} else {
			System.exit(0);
		}
	}

	public abstract ConfigurationDialog createDialog(Configuration newConf);
}
