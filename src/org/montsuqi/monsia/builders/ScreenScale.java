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

/** <p>A class to scale frames and components to fit in screen insets.</p>
 */
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

		// FIXME; now, use magic number
		final Dimension preferredSize = new Dimension(1024, 768);
		
		if (frameFreeSize.width > preferredSize.width) {
			if ("true".equals(System.getProperty("expand_screen"))) {
				frameWidthScale = screenFreeSize.width / (double)preferredSize.width;
				compWidthScale = frameFreeSize.width / (double)preferredSize.width;
			} else {
				compWidthScale = frameWidthScale = 1.0;
			}
		} else {
			frameWidthScale = frameFreeSize.width / (double)screenSize.width;
			compWidthScale = frameFreeSize.width / (double)screenFreeSize.width
				- screenInsetsSize.width / (double)screenSize.width;
		}

		if (frameFreeSize.height > preferredSize.height) {
			if ("true".equals(System.getProperty("expand_screen"))) {
				frameHeightScale = screenFreeSize.height / (double)preferredSize.height;
				compHeightScale = frameFreeSize.height / (double)preferredSize.height;
			} else {
				compHeightScale = frameHeightScale = 1.0;
			}
		} else {
			frameHeightScale = frameFreeSize.height / (double)screenSize.height;
			compHeightScale = frameFreeSize.height / (double)screenFreeSize.height
				- screenInsetsSize.height / (double)screenSize.height + 0.025;
		}
	}

	/** <p>Scale the given size so a frame with that size will fit in the screen insets.</p>
	 * 
	 * @param size arbitrary size (of a frame)
	 * @return scaled size
	 */
	static Dimension scaleFrame(Dimension size) {
		final int width = (int)(size.width * frameWidthScale) + frameInsetsSize.width;
		final int height = (int)(size.height * frameHeightScale) + frameInsetsSize.height;
		return new Dimension(width, height);
	}

	/** <p>Scale the given size so a component with that size will fit in the screen insets.</p>
	 * 
	 * @param size arbitrary size (of a component)
	 * @return scaled size
	 */
	static Dimension scale(Dimension size) {
		final int width = (int)(size.width * compWidthScale);
		final int height = (int)(size.height * compHeightScale);
		return new Dimension(width, height);
	}

	/** <p>Translates the given position so a frame at that position will fit in the screen insets.</p>
	 * 
	 * @param pos arbitrary position (of a frame)
	 * @return translated position
	 */
	static Point scaleFrame(Point pos) {
		return pos;
//		final int x = (int)(pos.x * frameWidthScale);
//		final int y = (int)(pos.y * frameHeightScale);
//		return new Point(x, y);
	}

	/** <p>Translates the given position so a component at that position will fit in the screen insets.</p>
	 * 
	 * @param pos arbitrary position (of a component)
	 * @return translated position
	 */
	static Point scale(Point pos) {
		final int x = (int)(pos.x * compWidthScale);
		final int y = (int)(pos.y * compHeightScale);
		return new Point(x, y);
	}

	/** <p>Scales the size of the given font.</p>
	 * @param font font to scale.
	 * @return scaled font.
	 */
	static Font scale(Font font) {
		final AffineTransform t = AffineTransform.getScaleInstance(compWidthScale, compHeightScale);
		final int style = font.getStyle();
		return font.deriveFont(style, t);
	}

	/** <p>Centers a window in the screen insets.</p>
	 * @param window a window to center.
	 */
	static void centerWindow(Window window) {
		final int x = (screenFreeSize.width - window.getWidth()) / 2 + screenInsets.left;
		final int y = (screenFreeSize.height - window.getHeight()) / 2 + screenInsets.top;
		window.setLocation(x, y);
	}

	/** <p>Gets the size of the screen.</p>
	 */
	static Dimension getScreenSize() {
		return new Dimension(screenSize);
	}
}
