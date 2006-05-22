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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

class ImagePreview extends Preview {

	BufferedImage sourceImage;
	BufferedImage image;

	public void load(String fileName) throws IOException {
		sourceImage = ImageIO.read(new File(fileName));
		updatePreferredSize();
	}

	public void clear() {
		sourceImage = null;
		image = null;
		updatePreferredSize();
	}

	public void fitToSize() {
		if (image == null) {
			return;
		}
		final Container parent = getParent();
		if (parent == null) {
			return;
		}
		int iw = image.getWidth();
		int ih = image.getHeight();
		int pw = parent.getWidth();
		int ph = parent.getHeight();
		double wScale = (double)pw / iw;
		double hScale = (double)ph / ih;
		double scale = Math.min(wScale, hScale);
		setScale(scale);
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null) {
			g.drawImage(image, 0, 0, this);
		} else if (sourceImage != null) {
			g.drawImage(sourceImage, 0, 0, this);
		}
	}

	protected void setScale(double newScale) {
		super.setScale(newScale);
		updatePreferredSize();
	}

	protected void setRotationStep(int newRotationStep) {
		super.setRotationStep(newRotationStep);
		updatePreferredSize();
	}

	private void updatePreferredSize() {
		if (sourceImage == null) {
			return;
		}

		int sw = sourceImage.getWidth();
		int sh = sourceImage.getHeight();

		int w = (int)(sw * scale);
		int h = (int)(sh * scale);
		Dimension size = rotationStep % 2 != 0 ? new Dimension(h, w) : new Dimension(w, h);
		setPreferredSize(size);
		image = new BufferedImage(size.width, size.height, sourceImage.getType());

		AffineTransform t = new AffineTransform();
		t.scale(scale, scale);
		int cx = rotationStep != 1 ? sw / 2 : sh / 2;
		int cy = rotationStep != 3 ? sh / 2 : sw / 2;
		double angle = (Math.PI / 2.0) * rotationStep;
		t.rotate(angle, cx, cy);

		AffineTransformOp op = new AffineTransformOp(t, AffineTransformOp.TYPE_BILINEAR);
		op.filter(sourceImage, image);

		revalidate();
		repaint();
	}
}
