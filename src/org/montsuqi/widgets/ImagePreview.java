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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

class ImagePreview extends Preview {

	Image image;

	public void load(String fileName) throws IOException {
		image = ImageIO.read(new File(fileName));
		resetScale();
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image == null) {
			return;
		}
		Graphics2D g2d = (Graphics2D)g;
		AffineTransform old = g2d.getTransform();
		int w = (int)image.getWidth(this);
		int h = (int)image.getHeight(this);
		int x = rotationStep != 1 ? w / 2 : h / 2;
		int y = rotationStep != 3 ? h / 2 : w / 2;
		double angle = (Math.PI / 2.0) * rotationStep;
		AffineTransform t = new AffineTransform();
		t.scale(scale, scale);
		t.rotate(angle, x, y);
		g2d.setTransform(t);
		g2d.drawImage(image, 0, 0, this);
		g2d.setTransform(old);
	}

	protected void updatePreferredSize(double newScale, int newRotationStep) {
		if (image == null) {
			return;
		}
		int w = (int)(image.getWidth(this) * scale);
		int h = (int)(image.getHeight(this) * scale);
		if (newRotationStep % 2 == 0) {
			setPreferredSize(new Dimension(w, h));
		} else {
			setPreferredSize(new Dimension(h, w));
		}
	}
}
