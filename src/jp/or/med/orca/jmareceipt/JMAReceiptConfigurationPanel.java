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

package jp.or.med.orca.jmareceipt;

import org.montsuqi.client.Configuration;
import org.montsuqi.client.ConfigurationPanel;

/** <p>Launcher configuration panel specialized for JMA receipt.</p>
 */
public class JMAReceiptConfigurationPanel extends ConfigurationPanel {

	/** <p>Constructs a configuration panel with given configuration.</p>
	 * 
	 * @param conf configuration to read initial values and write result to.
	 */
	JMAReceiptConfigurationPanel(Configuration conf, boolean doPadding, boolean doChangeLookAndFeel) {
		super(conf, doPadding, doChangeLookAndFeel);
		appEntry.setEditable(false);
	}

	/** <p>Updates configuration ofject using values set to UI.</p>
	 * <p>It also ensures some configuration remain preset values.</p>
	 */
    @Override
	protected void saveConfiguration(String configName) {
		super.saveConfiguration(configName);
		conf.setApplication(configName, "orca00"); //$NON-NLS-1$
	}

    @Override
	public void loadConfiguration(String configName,boolean newFlag) {
		super.loadConfiguration(configName,newFlag);
		appEntry.setText("panda:orca00");
	}
}
