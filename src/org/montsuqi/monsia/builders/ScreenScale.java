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
	private static final Dimension frameFreeSize;
	private static final double widthScale;
	private static final double heightScale;
	private static final Insets frameInsets;
	static {
		final Toolkit tk = Toolkit.getDefaultToolkit();
		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final GraphicsDevice gd = ge.getDefaultScreenDevice();
		final GraphicsConfiguration gc = gd.getDefaultConfiguration();
		final Insets insets = tk.getScreenInsets(gc);

		screenSize = tk.getScreenSize();
		screenFreeSize = new Dimension(screenSize);
		screenFreeSize.width -= insets.left + insets.right;
		screenFreeSize.height -= insets.top + insets.bottom;

		final JFrame dummy = new JFrame("DUMMY FRAME FOR METRICS CALCULATION");
		dummy.addNotify();
		dummy.setSize(screenFreeSize);
		frameInsets = dummy.getInsets();
		dummy.dispose();

		frameFreeSize = new Dimension(screenFreeSize);
		frameFreeSize.width -= frameInsets.left + frameInsets.right;
		frameFreeSize.height -= frameInsets.top + frameInsets.bottom;
		widthScale = frameFreeSize.width / (double)screenSize.width;
		heightScale = frameFreeSize.height / (double)screenSize.height;

	}

	static Dimension scaleFrame(Dimension size) {
		size.width += frameInsets.right + frameInsets.left;
		size.height += frameInsets.top + frameInsets.bottom;
		final int width;
		final int height;
		if (size.width > screenFreeSize.width) {
			width = (int)(size.width * widthScale);
		} else {
			width = size.width;
		}
		if (size.height > screenFreeSize.height) {
			height = (int)(size.height * heightScale);
		} else {
			height = size.height;
		}
		return new Dimension(width, height);
	}

	static Dimension scale(Dimension size) {
		final int width = (int)(size.width * widthScale);
		final int height = (int)(size.height * heightScale);
		return new Dimension(width, height);
	}

	static Point scaleFrame(Point pos) {
		final int x = (int)(pos.x * widthScale);
		final int y = (int)(pos.y * heightScale);
		return new Point(x, y);
	}

	static Point scale(Point pos) {
		final int x = (int)(pos.x * widthScale);
		final int y = (int)(pos.y * heightScale);
		return new Point(x, y);
	}

	static Font scale(Font font) {
		final AffineTransform t = AffineTransform.getScaleInstance(widthScale, heightScale);
		final int style = font.getStyle();
		return font.deriveFont(style, t);
	}

	static void centerWindow(Window window) {
		final int x = (screenFreeSize.width - window.getWidth()) / 2;
		final int y = (screenFreeSize.height - window.getHeight()) / 2;
		window.setLocation(x, y);
	}

	static Dimension getScreenSize() {
		return new Dimension(screenSize);
	}
}
