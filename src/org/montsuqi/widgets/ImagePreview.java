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

package org.montsuqi.widgets;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

class ImagePreview extends Preview {

	Image image;
	private double scale;

	public ImagePreview() {
		setScale(1.0);
	}

	public void zoomIn() {
		setScale(scale * 1.2);
	}

	public void zoomOut() {
		setScale(scale / 1.2);
	}

	public void load(String fileName) throws IOException {
		image = ImageIO.read(new File(fileName));
		setScale(1.0);
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null) {
			int width = (int)(image.getWidth(this) * scale);
			int height = (int)(image.getHeight(this) * scale);
			setPreferredSize(new Dimension(width, height));
			g.drawImage(image, 0, 0, width, height, this);
		}
	}

	private void setScale(double scale) {
		this.scale = scale;
		repaint();
	}
}
