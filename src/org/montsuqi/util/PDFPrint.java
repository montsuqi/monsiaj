package org.montsuqi.util;

import java.awt.*;
import java.awt.print.*;
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
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.montsuqi.util.PDFPrint.PDFPrintPage;

public class PDFPrint extends Thread {

    private final File file;
    private String printer;
    private boolean showDialog;

    protected static final Logger logger = LogManager.getLogger(PDFPrint.class);

    public PDFPrint(File file, boolean showDialog) {
        this.file = file;
        this.showDialog = showDialog;
        this.printer = null;
    }

    public PDFPrint(File file, String printer) {
        this.file = file;
        this.showDialog = false;
        this.printer = printer;
    }

    @Override
    public void run() {
        try {
            FileInputStream fis = new FileInputStream(file);
            FileChannel fc = fis.getChannel();
            ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            PDFFile pdfFile = new PDFFile(bb); // Create PDF Print Page
            PrintRequestAttributeSet reqset = new HashPrintRequestAttributeSet();

            PDFPrintPage pages = new PDFPrintPage(pdfFile);
            PrinterJob pjob = PrinterJob.getPrinterJob();
            pjob.setJobName(file.getName());

            if (System.getProperty("monsia.util.PDFPrint.force_use_default_printer") != null) {
                showDialog = false;
            }
            String printer_ = System.getProperty("monsia.util.PDFPrint.printer");
            if (printer_ != null) {
                printer = printer_;
                showDialog = false;
            }            
            if (showDialog) {
                if (!pjob.printDialog(reqset)) {
                    return;
                }
            } else {
                if (printer != null) {
                    PrintService[] pss = PrintServiceLookup.lookupPrintServices(null, null);
                    for (PrintService ps : pss) {
                        if (printer.equals(ps.getName())) {
                            pjob.setPrintService(ps);
                        }
                    }
                }
            }

            // validate the page against the chosen printer to correct
            // paper settings and margins
            PageFormat pf = pjob.getPageFormat(reqset);
            Paper paper = pf.getPaper();

            paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
            pf.setPaper(paper);
            Book book = new Book();

            book.append(pages, pf, pdfFile.getNumPages());
            pjob.setPageable(book);
            
            if (System.getProperty("monsia.util.PDFPrint.debug") != null) {
                PrintService service = pjob.getPrintService();
                System.out.println("PrintService:" + service);
                PrintServiceAttributeSet myAset = service.getAttributes();
                Attribute[] attr = myAset.toArray();
                System.out.println("Attributes set:");
                for (Attribute attr1 : attr) {
                    System.out.println("   " + attr1);
                }
            }
            pjob.print(reqset);
        } catch (PrinterException ex) {
            logger.warn(ex);
        } catch (java.io.IOException ex) {
            logger.warn(ex);
        }
    }

    public static void main(String args[]) throws Exception {
        PDFPrint printer = new PDFPrint(new File(args[0]), args[1]);
        printer.start();
    }

    public static class PDFPrintPage implements Printable {

        /**
         * The PDFFile to be printed
         */
        private PDFFile file;

        /**
         * Create a new PDFPrintPage object for a particular PDFFile.
         *
         * @param file the PDFFile to be printed.
         */
        public PDFPrintPage(PDFFile file) {
            PDFPrintPage.this.file = file;
        }

        // from Printable interface:  prints a single page, given a Graphics
        // to draw into, the page format, and the page number.
        @Override
        public int print(Graphics g, PageFormat format, int index) throws PrinterException {
            Graphics2D g2 = (Graphics2D) g;
            Rectangle2D.Double pageable;
            double hmargin = GetPrintOption("monsia.util.PDFPrint.hmargin");
            double vmargin = GetPrintOption("monsia.util.PDFPrint.vmargin");
            double hoffset = GetPrintOption("monsia.util.PDFPrint.hoffset");
            double voffset = GetPrintOption("monsia.util.PDFPrint.voffset");
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
                g2.setColor(Color.BLACK);
                float dash[] = {1.0f};
                BasicStroke dashStroke = new BasicStroke(1.0f,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        1.0f,
                        dash,
                        0.0f);
                g2.setStroke(dashStroke);
                g2.setStroke(new BasicStroke(0.1f));
                int mm = 10;
                for (int i = 0; i < 100; i++) {
                    int p = (int) Math.floor((i * PDFPrint.MMto72DPI(mm)));
                    g2.drawLine(p, 0, p, 2000);
                    g2.drawLine(0, p, 2000, p);
                }
                System.out.println("pageable:" + pageable);
                g2.setStroke(new BasicStroke(2.0f));
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

                double scale = 1.0;

                if (format.getOrientation() == PageFormat.PORTRAIT) {
                    if (height > width) {
                        g2.transform(new AffineTransform(scale, 0f, 0f, scale, hoffset, voffset));
                    } else {
                        g2.transform(new AffineTransform(0f, -1.0 * scale, scale, 0f, voffset, -hoffset + width));
                    }
                } else if (format.getOrientation() == PageFormat.LANDSCAPE) {
                    if (width > height) {
                        g2.transform(new AffineTransform(scale, 0f, 0f, scale, hoffset, voffset));
                    } else {
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
