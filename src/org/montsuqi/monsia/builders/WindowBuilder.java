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
import java.awt.Dimension;
import java.awt.Toolkit;

import org.montsuqi.monsia.Interface;
import org.montsuqi.monsia.Property;
import org.montsuqi.monsia.WidgetInfo;

public class WindowBuilder extends ContainerBuilder {

	Component buildSelf(Interface xml, Container parent, WidgetInfo info) {
		Component widget = super.buildSelf(xml, parent, info);
		for (int i = 0, n = info.getPropertiesCount(); i < n; i++) {
			Property p = info.getProperty(i);
			if ( ! p.getName().equals("position")) {
				continue;
			}
			String value = p.getValue();
			if (value.startsWith("GTK_")) {
				value = value.substring("GTK_".length());
			}
			if ( ! value.equals("WIN_POS_CENTER")) {
				break;
			}
			Toolkit tk = Toolkit.getDefaultToolkit();
			Dimension screen = tk.getScreenSize();
			Dimension window = widget.getSize();
			int x = (screen.width - window.width) / 2;
			int y = (screen.height - window.height) / 2;
			widget.setLocation(x, y);
		}
		return widget;
	}
}
