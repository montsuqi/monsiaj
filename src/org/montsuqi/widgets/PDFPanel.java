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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import javax.swing.*;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

/** <p>Preview component that uses images for implementation.</p>
 */
class PDFPanel extends JPanel {

    private PDFFile pdffile;
    private PDFPage page;
    private int pagenum;
    private double scale;
    private Image image;

    /** <p>Constructs an ImagePreview</p>
     */
    public PDFPanel() {
        super();
        pdffile = null;
        page = null;
        image = null;
        pagenum = 0;
        scale = 1.0;
    }

    /** <p>Loads a image from file of given name.</p>
     *
     * <p>If the size of newly loaded image is different from that of the old one,
     * new image is scaled to the component horizontally.</p>
     *
     * @param fileName name of the image file.
     */
    public void load(String fileName) throws IOException {
        //load a pdf from a byte buffer
        page = null;
        pagenum = 0;

        File file = new File(fileName);
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        FileChannel channel = raf.getChannel();
        ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY,
                0, channel.size());
        pdffile = new PDFFile(buf);
        page = pdffile.getPage(pagenum);
        showPage();
    }

    public void setPage(int num) {
        if (pdffile == null) {
            return;
        }
        pagenum = num;
        page = pdffile.getPage(pagenum);
        showPage();
    }

    public void setScale(double s) {
        scale = s;
        showPage();
    }

    public double getPageWidth() {
        if (page == null) {
            return -1;
        } else {
            return page.getWidth();
        }
    }

    public double getPageHeight() {
        if (page == null) {
            return -1;
        } else {
            return page.getHeight();
        }
    }

    private void showPage() {
        try {
            if (page == null) {
                return;
            }

            double sw = page.getWidth();
            double sh = page.getHeight();

            int w = (int) (sw * scale);
            int h = (int) (sh * scale);

            Dimension size = new Dimension(w, h);
            setPreferredSize(size);
            image = page.getImage(w, h, null, this);
            revalidate();
            repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
            // clear();
            return;
        }
    }

    public void paint(Graphics g) {
        Dimension sz = getSize();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        if (image != null) {
            g.drawImage(image, 0, 0, this);

        }
    }
}
