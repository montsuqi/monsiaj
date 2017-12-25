package org.montsuqi.monsiaj.util;

import java.awt.*;
import java.awt.print.*;
import java.awt.geom.*;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.io.*;
import java.util.prefs.Preferences;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import com.sun.pdfview.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.montsuqi.monsiaj.util.PDFPrint.PDFPrintPage;
import org.montsuqi.monsiaj.client.PrinterConfig;

public class PDFPrint {

    private static final Logger logger = LogManager.getLogger(PDFPrint.class);
    private static final Preferences prefs = Preferences.userNodeForPackage(PDFPrint.class);

    /* 指定printServiceへ印刷 */
    public static void print(File file, int copies, PrintService printService) {
        logger.debug("print start - " + file);
        try {
            FileInputStream fis = new FileInputStream(file);
            FileChannel fc = fis.getChannel();
            ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            PDFFile pdfFile = new PDFFile(bb);
            MediaSizeName mediaSizeName = MediaSizeName.A;

            PDFPage[] pages = new PDFPage[pdfFile.getNumPages() + 1];
            for (int i = 0; i < pdfFile.getNumPages(); i++) {
                pages[i] = pdfFile.getPage(i + 1);
                if (i == 0) {
                    mediaSizeName = PDFPaperSize.getPDFPaperSize(pages[i]);
                }
            }
            PDFPrintPage printPage = new PDFPrintPage(pages);

            PrinterJob pjob = PrinterJob.getPrinterJob();
            pjob.setJobName(file.getName());
            pjob.setPrintService(printService);

            PrintRequestAttributeSet reqset = new HashPrintRequestAttributeSet();
            if (mediaSizeName != MediaSizeName.A) {
                reqset.add(mediaSizeName);
            }
            reqset.add(new Copies(copies));

            PageFormat pf = pjob.getPageFormat(reqset);
            Paper paper = pf.getPaper();

            paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
            pf.setPaper(paper);
            Book book = new Book();

            book.append(printPage, pf, pdfFile.getNumPages());
            pjob.setPageable(book);

            pjob.print(reqset);
        } catch (PrinterException | IOException ex) {
            logger.catching(Level.WARN, ex);
        }
        logger.debug("print end - " + file);
    }

    public static PrintService loadPrintService() {
        return PrinterConfig.getPrintService(prefs.get("printService", ""));
    }

    public static void savePrintService(PrintService ps) {
        prefs.put("printService", ps.getName());
    }

    public static PrintRequestAttributeSet loadPrintRequestAttributeSet() {
        try {
            byte[] array = prefs.getByteArray("printRequestAttributeSet", new byte[0]);
            ByteArrayInputStream bais = new ByteArrayInputStream(array);
            PrintRequestAttributeSet attr;
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                attr = (PrintRequestAttributeSet) ois.readObject();
            }
            return attr;
        } catch (IOException | ClassNotFoundException ex) {
            logger.warn("can not load printRequestAttributeSet");
            logger.debug(ex, ex);
        }
        return null;
    }

    public static void savePrintRequestAttributeSet(PrintRequestAttributeSet attr) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(attr);
                oos.flush();
            }
            prefs.putByteArray("printRequestAttributeSet", baos.toByteArray());
        } catch (IOException ex) {
            logger.warn(ex, ex);
        }
    }

    /* 印刷ダイアログ表示 */
    public static void print(File file) {
        logger.debug("print start - " + file);
        try {
            FileInputStream fis = new FileInputStream(file);
            FileChannel fc = fis.getChannel();
            ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            PDFFile pdfFile = new PDFFile(bb);

            PDFPage[] pages = new PDFPage[pdfFile.getNumPages() + 1];
            for (int i = 0; i < pdfFile.getNumPages(); i++) {
                pages[i] = pdfFile.getPage(i + 1);
            }
            PDFPrintPage printPage = new PDFPrintPage(pages);

            PrinterJob pjob = PrinterJob.getPrinterJob();
            pjob.setJobName(file.getName());
            PrintService ps = loadPrintService();
            if (ps != null) {
                pjob.setPrintService(ps);
            }
            PrintRequestAttributeSet reqset = loadPrintRequestAttributeSet();
            if (reqset == null) {
                reqset = new HashPrintRequestAttributeSet();
            }

            if (!pjob.printDialog(reqset)) {
                return;
            }
            PageFormat pf = pjob.getPageFormat(reqset);
            Paper paper = pf.getPaper();

            paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
            pf.setPaper(paper);
            Book book = new Book();

            book.append(printPage, pf, pdfFile.getNumPages());
            pjob.setPageable(book);

            pjob.print(reqset);
            savePrintService(pjob.getPrintService());
            savePrintRequestAttributeSet(reqset);
        } catch (PrinterException | IOException ex) {
            logger.catching(Level.WARN, ex);
        }
        logger.debug("print end - " + file);
    }

    public static void main(String args[]) throws Exception {
        DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
        PrintService[] pss = PrintServiceLookup.lookupPrintServices(flavor, null);
        PrintService ps = null;
        System.out.println("---- print service");
        for (PrintService _ps : pss) {
            System.out.println(_ps.getName());
            if (_ps.getName().equals(args[0])) {
                ps = _ps;
            }
        }
        for (int i = 0; i < 1; i++) {
            if (ps == null) {
                PDFPrint.print(new File(args[1]));
            } else {
                PDFPrint.print(new File(args[1]), 1, ps);
            }
            System.out.println(i);
            Thread.sleep(2000);
        }
    }

    public static class PDFPrintPage implements Printable {

        /**
         * The PDFFile to be printed
         */
        private final PDFPage[] pages;

        /**
         * Create a new PDFPrintPage object for a particular PDFFile.
         *
         * @param _pages
         */
        public PDFPrintPage(PDFPage[] _pages) {
            pages = _pages;
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
            double scale;

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
            scale = (format.getImageableWidth() - hmargin * 2) / format.getImageableWidth() * 1.0;
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
            if (index < 0 || index >= pages.length) {
                logger.warn("no such page index:" + index + " pages.length:" + pages.length);
                return NO_SUCH_PAGE;
            }

            // fit the PDFPage into the printing area                
            PDFPage page = pages[index];
            int width = (int) page.getWidth();
            int height = (int) page.getHeight();

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
                logger.catching(Level.WARN, ie);
            }
            return PAGE_EXISTS;
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
