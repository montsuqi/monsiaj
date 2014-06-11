package org.montsuqi.client;

import com.sun.pdfview.PDFFile;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.WindowConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.montsuqi.util.GtkStockIcon;
import org.montsuqi.util.PDFPrint;
import org.montsuqi.util.PopupNotify;
import org.montsuqi.widgets.Button;
import org.montsuqi.widgets.PandaDownload;
import org.montsuqi.widgets.PandaPreview;

/*
 * Copyright (C) 2010 JMA (Japan Medical Association)
 */
/**
 *
 * @author mihara
 */
public class PrintAgent extends Thread {

    private static final Logger logger = LogManager.getLogger(PrintAgent.class);
    private final int DELAY = 3000;
    private final ConcurrentLinkedQueue<PrintRequest> printQ;
    private final ConcurrentLinkedQueue<ServerPrintRequest> serverPrintQ;
    private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    private final Protocol con;

    public PrintAgent(Protocol con) {
        printQ = new ConcurrentLinkedQueue<PrintRequest>();
        serverPrintQ = new ConcurrentLinkedQueue<ServerPrintRequest>();
        this.con = con;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(DELAY);
System.out.println(printQ.size());                
                for (PrintRequest request : printQ) {
                    if (processRequest(request)) {
                        synchronized (this) {
                            printQ.remove(request);
                        }
                    }
                }
                for (ServerPrintRequest request : serverPrintQ) {
                    request.action();
                    synchronized (this) {
                        serverPrintQ.remove(request);
                    }
                }
            } catch (InterruptedException e) {
                logger.warn(e);
            }
        }
    }

    boolean processRequest(PrintRequest request) {
        if (request == null) {
            return true;
        }
        if (request.action()) {
        } else {
            switch (request.getRetry()) {
                case 0:
                    return false;
                case 1:
                    request.showRetryError();
                    return true;
                default:
                    request.setRetry(request.getRetry() - 1);
                    return false;
            }
        }
        return true;
    }

    synchronized public void addPrintRequest(String url, String title, int retry, boolean showDialog) {
        for (PrintRequest req : printQ) {
            if (req.path.equals(url)) {
                return;
            }
        }
        printQ.add(new PrintRequest(url, title, retry, showDialog));
    }

    synchronized public void addDLRequest(String url, String filename, String desc, int retry) {
        for (PrintRequest req : printQ) {
            if (req.path.equals(url)) {
                return;
            }
        }
        printQ.add(new DLRequest(url, filename, desc, retry));
    }

    synchronized public void addServerPrintRequest(String printer, String oid) {
        serverPrintQ.add(new ServerPrintRequest(printer, oid));
    }

    public void showDialog(String title, File file) {
        try {
            final JDialog dialog = new JDialog();
            Button closeButton = new Button(new AbstractAction(Messages.getString("PrintAgent.close")) {

                public void actionPerformed(ActionEvent e) {
                    dialog.dispose();
                }
            });
            Container container = dialog.getContentPane();
            container.setLayout(new BorderLayout(5, 5));
            PandaPreview preview = new PandaPreview();
            dialog.setSize(new Dimension(800, 600));
            dialog.setLocationRelativeTo(null);
            dialog.setTitle(Messages.getString("PrintAgent.title") + title);
            container.add(preview, BorderLayout.CENTER);
            container.add(closeButton, BorderLayout.SOUTH);
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            preview.setSize(800, 600);
            preview.load(file.getAbsolutePath());
            preview.setVisible(true);

            dialog.setModal(true);
            dialog.setResizable(false);
            dialog.setVisible(true);
            closeButton.requestFocus();
        } catch (IOException ex) {
            logger.warn(ex);
        }
    }

    int getNumOfPages(File file) {
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            FileChannel channel = raf.getChannel();
            ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY,
                    0, channel.size());
            PDFFile pdf = new PDFFile(buf);
            return pdf.getNumPages();
        } catch (IOException ex) {
            logger.warn(ex);
        }
        return 0;
    }

    public class DLRequest extends PrintRequest {

        private final String description;

        public DLRequest(String url, String filename, String desc, int retry) {
            super(url, "", retry, false);
            this.filename = filename;
            this.description = desc;
        }

        @Override
        public boolean action() {
            try {
                File file = con.apiDownload(path, filename);
                if (file == null) {
                    return false;
                }
                PandaDownload pd = new PandaDownload();
                pd.setName("PrintAgent.PandaDownload");
                pd.showDialog(this.filename, this.description, file);
                return true;
            } catch (IOException ex) {
                if (!ex.getMessage().equals("204")) {
                    logger.warn(ex);
                    this.showOtherError();
                }
                return true;
            }
        }

        @Override
        public void showRetryError() {
            PopupNotify.popup(Messages.getString("PrintAgent.notify_summary"),
                    Messages.getString("PrintAgent.notify_download_fail_retry_over") + "\n\n"
                    + Messages.getString("PrintAgent.filename") + this.filename + "\n"
                    + Messages.getString("PrintAgent.description") + this.description,
                    GtkStockIcon.get("gtk-dialog-error"), 0);
        }

        @Override
        public void showOtherError() {
            PopupNotify.popup(Messages.getString("PrintAgent.notify_summary"),
                    Messages.getString("PrintAgent.notify_download_fail") + "\n\n"
                    + Messages.getString("PrintAgent.filename") + this.filename + "\n"
                    + Messages.getString("PrintAgent.description") + this.description,
                    GtkStockIcon.get("gtk-dialog-error"), 0);
        }
    }

    public class PrintRequest {

        protected String path;
        protected String title;
        protected String filename;
        protected int retry;
        protected final boolean showDialog;

        public PrintRequest(String url, String title, int retry, boolean showdialog) {
            this.path = url;
            this.title = title;
            this.retry = retry;
            this.showDialog = showdialog;
            this.filename = "print.pdf";
        }

        public boolean action() {
            try {
                File file = con.apiDownload(path, filename);
                if (file == null) {
                    return false;
                }
                if (this.isShowdialog()) {
                    showDialog(this.getTitle(), file);
                } else {
                    PopupNotify.popup(Messages.getString("PrintAgent.notify_summary"),
                            Messages.getString("PrintAgent.notify_print_start") + "\n\n"
                            + Messages.getString("PrintAgent.title") + this.getTitle(),
                            GtkStockIcon.get("gtk-print"), 0);
                    PDFPrint printer = new PDFPrint(file, false);
                    printer.start();
                }
                return true;
            } catch (IOException ex) {
                if (!ex.getMessage().equals("204")) {
                    logger.warn(ex);
                    this.showOtherError();
                }
                return true;
            }
        }

        public void showRetryError() {
            PopupNotify.popup(Messages.getString("PrintAgent.notify_summary"),
                    Messages.getString("PrintAgent.notify_print_fail_retry_over") + "\n\n"
                    + Messages.getString("PrintAgent.title") + this.getTitle(),
                    GtkStockIcon.get("gtk-dialog-error"), 0);
        }

        public void showOtherError() {
            PopupNotify.popup(Messages.getString("PrintAgent.notify_summary"),
                    Messages.getString("PrintAgent.notify_print_fail") + "\n\n"
                    + Messages.getString("PrintAgent.title") + this.getTitle(),
                    GtkStockIcon.get("gtk-dialog-error"), 0);
        }

        public void setRetry(int retry) {
            this.retry = retry;
        }

        public int getRetry() {
            return retry;
        }

        public boolean isShowdialog() {
            return showDialog;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String url) {
            this.path = url;
        }
    }

    public class ServerPrintRequest {

        private final String printer;
        private final String oid;

        public ServerPrintRequest(String printer, String oid) {
            this.printer = printer;
            this.oid = oid;
        }

        public void action() {
            try {
                File temp = File.createTempFile("printagent", "pdf");
                temp.deleteOnExit();
                OutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
                con.getBLOB(oid, out);
                PDFPrint pdfPrint = new PDFPrint(temp, this.printer);
                pdfPrint.start();
            } catch (IOException ex) {
                logger.warn(ex);
                PopupNotify.popup(Messages.getString("PrintAgent.notify_summary_server"),
                        Messages.getString("PrintAgent.notify_print_fail") + "\n\n"
                        + Messages.getString("PrintAgent.printer" + this.printer),
                        GtkStockIcon.get("gtk-dialog-error"), 0);
            }
        }
    }

}
