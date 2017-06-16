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

package org.montsuqi.monsiaj.monsia.builders;

import java.awt.Component;
import java.awt.Container;
import java.util.Map;

import javax.swing.JRadioButton;

import org.montsuqi.monsiaj.monsia.Interface;
import org.montsuqi.monsiaj.monsia.WidgetInfo;

/** <p>A builder to create RadioButton widgets.</p>
 */
class RadioButtonBuilder extends ContainerBuilder {
	Component buildSelf(Interface xml, Container parent, WidgetInfo info) {
		Component widget = super.buildSelf(xml, parent, info);
		JRadioButton radio = (JRadioButton)widget;
		Map properties = info.getProperties();
		// button group is managed in Interface.
		if (properties.containsKey("group")) { 
			xml.setButtonGroup(radio, (String)properties.get("group")); 
		} else {
			logger.warn("RadioButton {0} has no group", properties.get("name"));  //$NON-NLS-2$
		}
		return widget;
	}
}
