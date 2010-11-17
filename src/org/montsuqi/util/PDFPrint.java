package org.montsuqi.util;

import java.awt.*;
import java.awt.print.*;
import javax.swing.*;
import java.nio.channels.FileChannel;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.io.File;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.print.attribute.Attribute;
import javax.print.PrintService;
import javax.print.attribute.PrintServiceAttributeSet;

public class PDFPrint extends Thread {

    private File file;

    public PDFPrint(File file) {
        this.file = file;
    }

    @Override
    public void run() {
        try {
            FileInputStream fis = new FileInputStream(file);
            FileChannel fc = fis.getChannel();
            ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            PDFFile pdfFile = new PDFFile(bb); // Create PDF Print Page

            PDFPrintPage pages = new PDFPrintPage(pdfFile);

            PrinterJob pjob = PrinterJob.getPrinterJob();

            pjob.setJobName(file.getName());

            if (System.getProperty("monsia.util.PDFPrint.force_default_printer") == null) {
                if (!pjob.printDialog()) {
                    return;
                }
            }
            // validate the page against the chosen printer to correct
            // paper settings and margins
            PageFormat pfDefault = pjob.validatePage(pjob.defaultPage());
            Paper paper = pfDefault.getPaper();
            paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
            pfDefault.setPaper(paper);
            Book book = new Book();

            book.append(pages, pfDefault, pdfFile.getNumPages());
            pjob.setPageable(book);

            if (System.getProperty("monsia.util.PDFPrint.debug") != null) {
                PrintService service = pjob.getPrintService();
                System.out.println("PrintService:" + service);
                PrintServiceAttributeSet myAset = service.getAttributes();
                Attribute[] attr = myAset.toArray();
                int loop = attr.length;
                System.out.println("Attributes set:");
                for (int i = 0; i < attr.length; i++) {
                    System.out.println("   " + attr[i]);
                }
            }

            try {
                pjob.print();
            } catch (PrinterException exc) {
                System.out.println(exc);
            }
        } catch (java.io.IOException ex) {
            System.out.println(ex);
        }
    }

    public static void main(String args[]) throws Exception {
        JFileChooser chooser = new JFileChooser();
        chooser.showOpenDialog(null);
        PDFPrint printer = new PDFPrint(new File(chooser.getSelectedFile().getAbsolutePath()));
        printer.start();
    }

    public static class PDFPrintPage implements Printable {

        /** The PDFFile to be printed */
        private PDFFile file;

        /**
         * Create a new PDFPrintPage object for a particular PDFFile.
         * @param file the PDFFile to be printed.
         */
        public PDFPrintPage(PDFFile file) {
            PDFPrintPage.this.file = file;
        }

        // from Printable interface:  prints a single page, given a Graphics
        // to draw into, the page format, and the page number.
        public int print(Graphics g, PageFormat format, int index)
                throws PrinterException {
            Graphics2D g2 = (Graphics2D) g;
            Rectangle2D.Double pageable;
            double hmargin = GetPrintOption("monsia.util.PDFPrint.hmargin");
            double vmargin = GetPrintOption("monsia.util.PDFPrint.vmargin");
            double hoffset = GetPrintOption("monsia.util.PDFPrint.hoffset");
            double voffset = GetPrintOption("monsia.util.PDFPrint.voffset");
            double scale = 1.0;
            double hratio = 1.0;
            double vratio = 1.0;

            if (hmargin != 0.0) {
                hratio = hmargin / format.getImageableWidth();
            }
            if (vmargin != 0.0) {
                vratio = vmargin / format.getImageableHeight();
            }
            if (hratio != 1.0 || vratio != 1.0) {
                if (hratio < vratio) {
                    vmargin = hratio * format.getImageableHeight();
                } else {
                    hmargin = vratio * format.getImageableWidth();
                }
            }
            hoffset += hmargin;
            voffset += vmargin;
            pageable = new Rectangle2D.Double(hoffset, voffset,
                    format.getImageableWidth() - hmargin * 2,
                    format.getImageableHeight() - vmargin * 2);

            if (System.getProperty("monsia.util.PDFPrint.debug") != null) {
                System.out.println("PageFormat.orientation(land:" + PageFormat.LANDSCAPE
                        + ",port:" + PageFormat.PORTRAIT + "):" + format.getOrientation());
                System.out.println("PageFormat Imageable:[" + format.getImageableX()
                        + "," + format.getImageableY()
                        + "],[" + format.getImageableWidth()
                        + "," + format.getImageableHeight() + "]");
                g2.setColor(Color.LIGHT_GRAY);
                for (int i = 0; i < 80; i++) {
                    for (int j = 0; j < 80; j++) {
                        g2.drawRect(j * 100, i * 100, 100, 100);
                        g2.drawString("(" + j + "," + i + ")", j * 100, i * 100);
                    }
                }
                System.out.println("pageable:" + pageable);
                g2.setColor(Color.BLACK);
                g2.drawRect((int) pageable.x, (int) pageable.y,
                        (int) pageable.width, (int) pageable.height);
            }
            int pagenum = index + 1;

            // don't bother if the page number is out of range.
            if ((pagenum >= 1) && (pagenum <= file.getNumPages())) {

                // fit the PDFPage into the printing area
                PDFPage page = file.getPage(pagenum);
                int width = (int) page.getWidth();
                int height = (int) page.getHeight();

                if (format.getOrientation() == PageFormat.PORTRAIT) {
                    if (height > width) {
                        scale = pageable.width / (1.0d * width);
                        g2.transform(new AffineTransform(scale, 0f, 0f, scale, hoffset, voffset));
                    } else {
                        scale = pageable.width / (1.0d * height);
                        g2.transform(new AffineTransform(0f, -1.0 * scale, scale, 0f, voffset, -hoffset + width));
                    }
                } else if (format.getOrientation() == PageFormat.LANDSCAPE) {
                    if (width > height) {
                        scale = pageable.width / (1.0d * width);
                        g2.transform(new AffineTransform(scale, 0f, 0f, scale, hoffset, voffset));
                    } else {
                        scale = pageable.width / (1.0d * height);
                        g2.transform(new AffineTransform(0f, scale, -1.0 * scale, 0f, -voffset + height, hoffset));
                    }
                }
                if (System.getProperty("monsia.util.PDFPrint.debug") != null) {
                    System.out.println("scale:" + scale);
                }

                // render the page
                PDFRenderer pgs = new PDFRenderer(page, g2, new Rectangle(0, 0, width, height), null, null);
                try {
                    page.waitForFinish();
                    pgs.run();
                } catch (InterruptedException ie) {
                }
                return PAGE_EXISTS;
            } else {
                return NO_SUCH_PAGE;
            }
        }
    }

    private static double MMto72DPI(double mm) {
        if (mm == 0.0) {
            return 0.0;
        }
        return (mm / 25.4) * 72.0;
    }

    private static double GetPrintOption(String property) {
        double _72dpi = 0.0;
        if (System.getProperty(property) != null) {
            double mm = Double.valueOf(System.getProperty(property));
            _72dpi = PDFPrint.MMto72DPI(mm);
            if (System.getProperty("monsia.util.PDFPrint.debug") != null) {
                System.out.println(property + ":" + mm + "(mm) " + _72dpi + "(72dpi)");
            }
        }
        return _72dpi;
    }
}
