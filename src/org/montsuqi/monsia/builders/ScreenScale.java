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

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;

final class ScreenScale {
	private static double widthScale;
	private static double heightScale;

	static {
		Toolkit tk = Toolkit.getDefaultToolkit();
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		Insets insets = tk.getScreenInsets(gc);
		Dimension screenSize = tk.getScreenSize();
		widthScale = (screenSize.width - insets.right - insets.left) / (double)screenSize.width;
		heightScale = (screenSize.height - insets.top - insets.bottom) / (double)screenSize.height;
	}

	static Dimension scale(Dimension size) {
		return new Dimension((int)(size.width * widthScale), (int)(size.height * heightScale));
	}
	static Point scale(Point pos) {
		return new Point((int)(pos.x * widthScale), (int)(pos.y * heightScale));
	}
}
