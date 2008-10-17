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

package org.montsuqi.monsia.builders;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Method;

import org.montsuqi.monsia.Interface;
import org.montsuqi.monsia.WidgetInfo;
import org.montsuqi.util.SystemEnvironment;
import org.montsuqi.widgets.Window;

/** <p>A builder to create Window(top level) widget.</p>
 * <p>Since dialogs are variation of windows in Gtk+ while dialogs and windows are 
 * different in Swing. Both treated as Window here.</p>
 */
public class WindowBuilder extends ContainerBuilder {

	Component buildSelf(Interface xml, Container parent, WidgetInfo info) {
		Component c = super.buildSelf(xml, parent, info);
		Window w = (Window)c;
		try {
			w.setTitleString(info.getProperty("title"));
			if (!w.getAllow_Grow()) {
			    if (SystemEnvironment.isJavaVersionMatch("1.4") || SystemEnvironment.isJavaVersionMatch("1.5")){
				w.setResizable(false);
			    } else {
				w.setMaximumSize(w.getSize());
			    }
			}
			if (!w.getAllow_Shrink()) {
			    if (SystemEnvironment.isJavaVersionMatch("1.4") || SystemEnvironment.isJavaVersionMatch("1.5")){
				w.setResizable(false);
			    } else {
				w.setMinimumSize(w.getSize());
			    }
			}
			if ((!w.getAllow_Grow()) && (!w.getAllow_Shrink()) ){
			    w.setResizable(false);
			}
			if (info.getClassName().equals("Dialog")) {
				Method setAlwaysOnTop = Window.class.getMethod("setAlwaysOnTop", new Class[] { Boolean.TYPE });
				setAlwaysOnTop.invoke(w, new Object[] { Boolean.TRUE });
			}
		} catch (NoSuchMethodException e) {
			// ignore
		} catch (Exception e) {
			throw new WidgetBuildingException(e);
		}
		return c;
	}
}
