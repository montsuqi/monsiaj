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
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.geom.AffineTransform;

final class ScreenScale {
	private static Insets screenInsets;
	private static double widthScale;
	private static double heightScale;
	private static Insets frameInsets;
	private static double frameWidthScale;
	private static double frameHeightScale;
	private static Point center;
	static {
		Toolkit tk = Toolkit.getDefaultToolkit();
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		screenInsets = tk.getScreenInsets(gc);
		Dimension screenSize = tk.getScreenSize();
		int clientWidth = screenSize.width - screenInsets.right - screenInsets.left;
		int clientHeight = screenSize.height - screenInsets.top - screenInsets.bottom;
		System.out.println("client area width: " + clientWidth + ", height: " + clientHeight);
		widthScale = clientWidth / (double)screenSize.width;
		heightScale = clientHeight / (double)screenSize.height;

		// caption(title) must be taken into account for frame scaling
		Frame f = new Frame();
		f.addNotify();
		frameInsets = f.getInsets();
		System.out.println("FrameInsets:" + frameInsets);
		f.dispose();
		int frameWidth = clientWidth - frameInsets.left - frameInsets.right;
		int frameHeight = clientHeight - frameInsets.top - frameInsets.bottom;
		frameWidthScale = frameWidth / (double)screenSize.width;
		frameHeightScale = frameHeight / (double)screenSize.height;
		center = new Point();
		center.x = (screenSize.width - screenInsets.right + screenInsets.left) / 2;
		center.y = (screenSize.height - screenInsets.bottom + screenInsets.top) / 2;
	}

	static Dimension scale(Dimension size) {
		return new Dimension((int)(size.width * widthScale), (int)(size.height * heightScale));
	}

	static Dimension scaleForFrame(Dimension size) {
		System.out.println("current size =" + size);
		System.out.println("scale = w:" + frameWidthScale + ", h:" + frameHeightScale);
		Dimension newSize = new Dimension((int)(size.width * frameWidthScale), (int)(size.height * frameHeightScale));
		System.out.println("new size = " + newSize);
		return newSize;
	}

	static Point scale(Point pos) {
		return new Point((int)(pos.x * widthScale), (int)(pos.y * heightScale));
	}

	static Font scale(Font font) {
		AffineTransform t = new AffineTransform();
		t.setToScale(widthScale, heightScale);
		return font.deriveFont(font.getStyle(), t);
	}

	static void centerWindow(Window window) {
		window.setLocation(center.x - window.getWidth() / 2, center.y - window.getHeight() / 2);
	}
}
