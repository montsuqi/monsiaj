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

package org.montsuqi.monsia.builders;

import java.awt.Component;
import java.awt.Container;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;

import org.montsuqi.monsia.Interface;
import org.montsuqi.monsia.Property;
import org.montsuqi.monsia.WidgetInfo;

class RadioButtonBuilder extends ContainerBuilder {
	Component buildSelf(Interface xml, Container parent, WidgetInfo info) {
		Component widget = super.buildSelf(xml, parent, info);
		AbstractButton button = (AbstractButton)widget;
		ButtonGroup group = null;
		for (int i = 0, n = info.getPropertiesCount(); i < n; i++) {
			Property p = info.getProperty(i);
			if ("group".equals(p.getName())) { //$NON-NLS-1$
				group = xml.getButtonGroup(p.getValue());
				break;
			}
		}
		if (group == null) {
			logger.warn(Messages.getString("WidgetBuilder.radio_button_has_no_group"), widget.getName()); //$NON-NLS-1$
		} else {
			group.add(button);
		}
		return widget;
	}
}
