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

package jp.or.med.jmareceipt;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.montsuqi.client.ConfigurationPanel;
import org.montsuqi.client.ConfigurationViewer;
import org.montsuqi.client.Launcher;

/** <p>Launcher specialized for JMA receipt.</p>
 */
public class JMAReceiptLauncher extends Launcher {

	/** <p>Constructs a launcher.</p>
	 * @param title title of the launcher dialog.
	 */
	protected JMAReceiptLauncher(String title) {
		super(title);
		String [] configNames = conf.getConfigurationNames();
		for (int i = 0; i < configNames.length; i++) {
			conf.setApplication(configNames[i], "orca00"); //$NON-NLS-1$
			conf.setProtocolVersion(configNames[i], 1);
		}
	}
	
		/** <p>Application entry point.</p> */
	public static void main(String[] args) {
		Launcher launcher = new JMAReceiptLauncher(Messages.getString("application.title")); //$NON-NLS-1$
		launcher.launch(args);
	}

	/** <p>Constructs the configuration panel.</p>
	 */
	protected ConfigurationPanel createConfigurationPanel() {
		return new JMAReceiptConfigurationPanel(conf, true, true);
	}
	
	protected ConfigurationViewer createConfigurationViewer() {
		return new JMAReceiptConfigurationViewer(conf);
	}
	
	/** <p>Creates icon to display on the left.</p>
	 */
	protected Icon createIcon() {
		URL iconURL = getClass().getResource("/jp/or/med/jmareceipt/standard60.png"); //$NON-NLS-1$
		if (iconURL != null) {
			return new ImageIcon(iconURL);
		}
		return super.createIcon();
	}	
}
