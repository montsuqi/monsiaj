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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import javax.swing.*;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import org.apache.logging.log4j.LogManager;

/**
 * <
 * p>
 * Preview component that uses images for implementation.</p>
 */
class PDFPanel extends JPanel {

    static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(PDFPanel.class);    
    
    private PDFFile pdffile;
    private PDFPage page;
    private int pagenum;
    private double scale;
    private Image image;

    /**
     * <
     * p>
     * Constructs an ImagePreview</p>
     */
    public PDFPanel() {
        super();
        pdffile = null;
        page = null;
        image = null;
        pagenum = 0;
        scale = 1.0;
    }

    public void load(String fileName) {
        load(new File(fileName));
    }

    public void load(File file) {
        try {
            page = null;
            pagenum = 1;
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            FileChannel channel = raf.getChannel();
            ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            pdffile = new PDFFile(buf);
            page = pdffile.getPage(pagenum);
            page.waitForFinish();
        } catch (Exception ex) {
            /* catch illegalArgumentException */
            logger.catching(org.apache.logging.log4j.Level.WARN, ex);
        }
        showPage();
    }

    public void setPage(int num) {
        if (pdffile == null) {
            return;
        }
        if (num < 1 || num > (pdffile.getNumPages())) {
            num = 1;
        }
        pagenum = num;
        page = pdffile.getPage(pagenum);
        try {
            page.waitForFinish();
        } catch (Exception ex) {
            logger.catching(org.apache.logging.log4j.Level.WARN, ex);            
        }
        showPage();
    }

    public int getPageNum() {
        return pagenum;
    }

    public int getNumPages() {
        if (pdffile == null) {
            return 0;
        }
        return pdffile.getNumPages();
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

    public void clear() {
        pdffile = null;
        page = null;
        image = null;
        setPreferredSize(new Dimension(0, 0));
        revalidate();
        repaint();
    }

    private void showPage() {
        if (pdffile == null || page == null) {
            return;
        }

        double sw = page.getWidth();
        double sh = page.getHeight();

        int w = (int) (sw * scale);
        int h = (int) (sh * scale);
        setPreferredSize(new Dimension(w, h));
        image = page.getImage(w, h, null, this);
        revalidate();
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        if (image != null) {
            int cx = (int) ((this.getWidth() / 2.0) - (image.getWidth(this) / 2.0));
            int cy = (int) ((this.getHeight() / 2.0) - (image.getHeight(this) / 2.0));
            g.drawImage(image, cx, cy, this);
        }
    }
}
