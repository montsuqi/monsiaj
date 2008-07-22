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

package org.montsuqi.widgets;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/** <p>Preview component that uses images for implementation.</p>
 */
class ImagePreview extends Preview {

	BufferedImage sourceImage;
	BufferedImage image;
	Dimension lastImageSize;

    static final float[][] operator={
       
        { 0.11f, 0.11f, 0.11f,     //operator[0]
          0.11f, 0.12f, 0.11f,
          0.11f, 0.11f, 0.11f},

        {-0.11f, -0.11f, -0.11f,  //operator[1]
	 -0.11f,  1.88f, -0.11f,
         -0.11f, -0.11f, -0.11f},
    };

	/** <p>Constructs an ImagePreview</p>
	 */
	public ImagePreview() {
	    super();
	    sourceImage = null;
	    image = null;
	    lastImageSize = null;
	}

    public BufferedImage makeConvolution(
	 BufferedImage bimg,float[] operator,int type){

	Kernel kernel=new Kernel(3,3,operator);
	ConvolveOp convop;
	if(type==1) 
	    convop=new ConvolveOp(kernel,ConvolveOp.EDGE_ZERO_FILL,null);
	else
	    convop=new ConvolveOp(kernel,ConvolveOp.EDGE_NO_OP,null);
	BufferedImage bimg_dest=convop.filter(bimg,null);
	return bimg_dest;
	
    }

	/** <p>Loads a image from file of given name.</p>
	 * 
	 * <p>If the size of newly loaded image is different from that of the old one,
	 * new image is scaled to the component horizontally.</p>
	 * 
	 * @param fileName name of the image file.
	 */
	public void load(String fileName) throws IOException {
		flushImage(sourceImage);
		sourceImage = ImageIO.read(new File(fileName));
		if (sourceImage != null) {
			final Dimension imageSize =  new Dimension(sourceImage.getWidth(), sourceImage.getHeight());
			if (lastImageSize == null || ! lastImageSize.equals(imageSize)) {
				fitToSizeHorizontally();
				lastImageSize = imageSize;
			}
		}
		updatePreferredSize();
	}

	/** <p>Clears the preview.</p>
	 */
	public void clear() {
		flushImage(sourceImage);
		sourceImage = null;
		flushImage(image);
		image = null;
		lastImageSize = null;
		updatePreferredSize();
	}

	/** <p>Dispose the image resource.</p>
	 * 
	 * @param img image to dispose.
	 */
	private void flushImage(final Image img) {
		if (img != null) {
			img.flush();
		}
	}

	/** <p>Scales the image to component(parent) size.</p>
	 */
	public void fitToSize() {
		if (sourceImage == null) {
			return;
		}
		final Container parent = getParent();
		if (parent == null) {
			return;
		}
		double wScale;
		double hScale;
		if (rotationStep % 2 == 0) {
			wScale = computeScale(sourceImage.getWidth(), parent.getWidth());
			hScale = computeScale(sourceImage.getHeight(), parent.getHeight());
		} else {
			wScale = computeScale(sourceImage.getWidth(), parent.getHeight());
			hScale = computeScale(sourceImage.getHeight(), parent.getWidth());
		}
		final double newScale = Math.min(wScale, hScale);
		setScale(newScale);
	}

	/** <p>Scales the image --only horizontally-- to component(parent) size.</p>
	 */
	public void fitToSizeHorizontally() {
		if (sourceImage == null) {
			return;
		}
		final Container parent = getParent();
		if (parent == null) {
			return;
		}
		double newScale;
		if (rotationStep % 2 == 0) {
			newScale = computeScale(sourceImage.getWidth(), parent.getWidth());
		} else {
			newScale = computeScale(sourceImage.getHeight(), parent.getHeight());
		}
		setScale(newScale);
	}

	private double computeScale(double imageMetric, double parentMetric) {
		return parentMetric / imageMetric;
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
		try {
			if (sourceImage == null) {
				return;
			}
	
			int sw = sourceImage.getWidth();
			int sh = sourceImage.getHeight();
	
			int w = (int)(sw * scale);
			int h = (int)(sh * scale);
			Dimension size = rotationStep % 2 != 0 ? new Dimension(h, w) : new Dimension(w, h);
			setPreferredSize(size);
			flushImage(image);
			image = new BufferedImage(size.width, size.height, sourceImage.getType());
			BufferedImage imagetmp = new BufferedImage(size.width, size.height, sourceImage.getType());
			if ( scale < 0.7 ) {
			    image = makeConvolution(sourceImage,operator[0],0);
			} else {
			    image = sourceImage;
			}
			AffineTransform t = new AffineTransform();
			t.scale(scale, scale);
			int cx = rotationStep != 1 ? sw / 2 : sh / 2;
			int cy = rotationStep != 3 ? sh / 2 : sw / 2;
			double angle = (Math.PI / 2.0) * rotationStep;
			t.rotate(angle, cx, cy);
	
			AffineTransformOp op = new AffineTransformOp(t, AffineTransformOp.TYPE_BILINEAR);

			op.filter(image, imagetmp);
			image = makeConvolution(imagetmp,operator[1],0);
			revalidate();
			repaint();
		} catch (Exception ex) {
			clear();
			return;
		}
	}
}
