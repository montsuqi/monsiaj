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

package org.montsuqi.monsiaj.widgets;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/** <p>A image label components that simulates Gtk+'s Pixmap widget.</p> 
 */
public class Pixmap extends JLabel {

	/** <p>An ImageIcon subclass which scales the image to fit within the component
	 * size.</p>
	 */
	final class ScalableImageIcon extends ImageIcon {
		ScalableImageIcon(Image image) {
			super(image);
		}

		public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
			final Image image = getImage();
			if (image != null) {
				ImageObserver imageObserver = getImageObserver();
				if (imageObserver == null) {
					imageObserver = c;
				}
				int imageWidth = image.getWidth(imageObserver);
				int imageHeight = image.getHeight(imageObserver);
				if (imageWidth > 0 && imageHeight > 0) {
					final int widgetWidth = getWidth();
					final double hScale = (double)widgetWidth / (double)imageWidth;
					final int widgetHeight = getHeight();
					final double vScale = (double)widgetHeight / (double)imageHeight;
					final double scale = Math.min(hScale, vScale);
					final int width = (int)(imageWidth * scale);
					final int height = (int)(imageHeight * scale);
					final int imageX = (widgetWidth - width) / 2;
					final int imageY = (widgetHeight - height) / 2;
					g.drawImage(image, imageX, imageY, width, height, imageObserver);
				}
			} else {
				super.paintIcon(c, g, x, y);
			}
		}
	}

	private int scaledWidth;
	private int scaledHeight;
	private boolean scaled;

	public Pixmap() {
		super();
		setIcon(null);
		setIconTextGap(0);
	}

	public void setIcon(Icon icon) {
		if (icon == null || ! (icon instanceof ImageIcon)) {
			super.setIcon(icon);
		} else {
			final ImageIcon imageIcon = (ImageIcon)icon;
			final Image iconImage = imageIcon.getImage();
			final Icon scalableIcon = new ScalableImageIcon(iconImage);
			super.setIcon(scalableIcon);
		}
	}

	public void setScaled(boolean scaled) {
		this.scaled = scaled;
	}

	boolean isScaled() {
		return scaled;
	}

	public void setScaledWidth(int scaledWidth) {
		this.scaledWidth = scaledWidth;
	}

	int getScaledWidth() {
		return scaledWidth;
	}

	public void setScaledHeight(int scaledHeight) {
		this.scaledHeight = scaledHeight;
	}

	int getScaledHeight() {
		return scaledHeight;
	}
}
