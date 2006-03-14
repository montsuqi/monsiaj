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
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.geom.AffineTransform;
import javax.swing.JFrame;

final class ScreenScale {
	private static final Dimension screenSize;
	private static final Dimension screenFreeSize;
	private static final double frameWidthScale;
	private static final double frameHeightScale;
	private static final double compWidthScale;
	private static final double compHeightScale;
	static {
		Toolkit tk = Toolkit.getDefaultToolkit();
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		Insets insets = tk.getScreenInsets(gc);
		screenSize = tk.getScreenSize();
		screenFreeSize = new Dimension(screenSize.width - insets.right - insets.left,
				screenSize.height - insets.top - insets.bottom);
		frameWidthScale = screenFreeSize.width / (double)screenSize.width;
		frameHeightScale = screenFreeSize.height / (double)screenSize.height;
		JFrame dummy = new JFrame("DUMMY FRAME FOR METRICS CALCULATION");
		dummy.addNotify();
		dummy.setSize(screenFreeSize);
		Insets frameInsets = dummy.getInsets();
		dummy.dispose();
		Dimension frameFreeSize = new Dimension(screenFreeSize.width - frameInsets.right - frameInsets.left,
				screenFreeSize.height - frameInsets.top - frameInsets.bottom);
		compWidthScale = frameFreeSize.width / (double)screenFreeSize.width;
		compHeightScale = frameFreeSize.height / (double)screenFreeSize.height;
	}

	static Dimension scaleFrame(Dimension size) {
		return new Dimension((int)(size.width * frameWidthScale), (int)(size.height * frameHeightScale));
	}

	static Dimension scale(Dimension size) {
		return new Dimension((int)(size.width * compWidthScale), (int)(size.height * compHeightScale));
	}

	static Point scaleFrame(Point pos) {
		return new Point((int)(pos.x * frameWidthScale), (int)(pos.y * frameHeightScale));
	}

	static Point scale(Point pos) {
		return new Point((int)(pos.x * compWidthScale), (int)(pos.y * compHeightScale));
	}

	static Font scale(Font font) {
		AffineTransform t = new AffineTransform();
		t.setToScale(compWidthScale, compHeightScale);
		return font.deriveFont(font.getStyle(), t);
	}

	static void centerWindow(Window window) {
		window.setLocation(screenFreeSize.width / 2 - window.getWidth() / 2, screenFreeSize.height / 2 - window.getHeight() / 2);
	}

	static Dimension getScreenSize() {
		return new Dimension(screenSize);
	}
}
