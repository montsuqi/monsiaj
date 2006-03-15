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
	private static final Insets screenInsets;
	private static final Dimension screenSize;
	private static final Dimension screenFreeSize;
	private static final Dimension frameFreeSize;
	private static final Dimension screenInsetsSize;
	private static final Dimension frameInsetsSize;
	private static final Insets frameInsets;
	private static final double frameWidthScale;
	private static final double frameHeightScale;
	private static final double compWidthScale;
	private static final double compHeightScale;

	static {
		final Toolkit tk = Toolkit.getDefaultToolkit();
		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final GraphicsDevice gd = ge.getDefaultScreenDevice();
		final GraphicsConfiguration gc = gd.getDefaultConfiguration();
		screenInsets = tk.getScreenInsets(gc);
		screenSize = tk.getScreenSize();
		screenInsetsSize = new Dimension(screenInsets.left + screenInsets.right, screenInsets.top + screenInsets.bottom);
		screenFreeSize = new Dimension(screenSize);
		screenFreeSize.width -= screenInsetsSize.width;
		screenFreeSize.height -= screenInsetsSize.height;

		final JFrame dummy = new JFrame("DUMMY FRAME FOR METRICS CALCULATION");
		dummy.addNotify();
		dummy.setSize(screenFreeSize);
		frameInsets = dummy.getInsets();
		dummy.dispose();
		frameInsetsSize = new Dimension(frameInsets.left + frameInsets.right, frameInsets.top + frameInsets.bottom);

		frameFreeSize = new Dimension(screenFreeSize);
		frameFreeSize.width -= frameInsetsSize.width;
		frameFreeSize.height -= frameInsetsSize.height;
		frameWidthScale = frameFreeSize.width / (double)screenSize.width;
		frameHeightScale = frameFreeSize.height / (double)screenSize.height;
		compWidthScale = frameFreeSize.width / (double)screenFreeSize.width
			- screenInsetsSize.width / (double)screenSize.width;
		compHeightScale = frameFreeSize.height / (double)screenFreeSize.height
			- screenInsetsSize.height / (double)screenSize.height + 0.025; /* TODO Eliminate this magic number! */
	}

	static Dimension scaleFrame(Dimension size) {
		final int width = (int)(size.width * frameWidthScale) + frameInsetsSize.width;
		final int height = (int)(size.height * frameHeightScale) + frameInsetsSize.height;
		return new Dimension(width, height);
	}

	static Dimension scale(Dimension size) {
		final int width = (int)(size.width * compWidthScale);
		final int height = (int)(size.height * compHeightScale);
		return new Dimension(width, height);
	}

	static Point scaleFrame(Point pos) {
		final int x = (int)(pos.x * frameWidthScale);
		final int y = (int)(pos.y * frameHeightScale);
		return new Point(x, y);
	}

	static Point scale(Point pos) {
		final int x = (int)(pos.x * compWidthScale);
		final int y = (int)(pos.y * compHeightScale);
		return new Point(x, y);
	}

	static Font scale(Font font) {
		final AffineTransform t = AffineTransform.getScaleInstance(compWidthScale, compHeightScale);
		final int style = font.getStyle();
		return font.deriveFont(style, t);
	}

	static void centerWindow(Window window) {
		final int x = (screenFreeSize.width - window.getWidth()) / 2 + screenInsets.left;
		final int y = (screenFreeSize.height - window.getHeight()) / 2 + screenInsets.top;
		window.setLocation(x, y);
	}

	static Dimension getScreenSize() {
		return new Dimension(screenSize);
	}
}
