package org.montsuqi.client;

import com.sun.pdfview.PDFFile;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.prefs.Preferences;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.WindowConstants;
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

    private final int DELAY = 3000;
    private ConcurrentLinkedQueue<PrintRequest> printQ;
    private Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    private String port;
    private SSLSocketFactory sslSocketFactory;

    public PrintAgent(String port, final String user, final String password, SSLSocketFactory sslSocketFactory) {
        printQ = new ConcurrentLinkedQueue<>();
        this.port = port;
        this.sslSocketFactory = sslSocketFactory;
        Authenticator.setDefault(new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password.toCharArray());
            }
        });

    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(DELAY);
                for (PrintRequest request : printQ) {
                    if (processRequest(request)) {
                        printQ.remove(request);
                    }
                }
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }

    synchronized boolean processRequest(PrintRequest request) {
        if (request == null) {
            return true;
        }
        try {
            File file = download(request.path, request.filename);
            if (file != null) {
                request.action(file);
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
        } catch (IOException ex) {
            if (!ex.getMessage().equals("204")) {
                request.showOtherError();
            }
        }
        return true;
    }

    synchronized public void addPrintRequest(String url, String title, int retry, boolean showDialog) {
        printQ.add(new PrintRequest(url, title, retry, showDialog));
    }

    synchronized public void addDLRequest(String url, String filename, String desc, int retry) {
        printQ.add(new DLRequest(url, filename, desc, retry));
    }

    public static void showDialog(String title, File file) {
        try {
            final JDialog dialog = new JDialog();
            Button closeButton = new Button(new AbstractAction(Messages.getString("PrintAgent.close")) {

                @Override
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
            dialog.setVisible(true);
            closeButton.requestFocus();
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    int getNumOfPages(File file) {
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            FileChannel channel = raf.getChannel();
            ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY,
                    0, channel.size());
            PDFFile pdf = new PDFFile(buf);
            if (pdf != null) {
                return pdf.getNumPages();
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return 0;
    }

    public File download(String path, String filename) throws IOException {
        File temp = File.createTempFile("monsiaj_printagent_", "__" + filename);
        temp.deleteOnExit();
        String strURL = (sslSocketFactory == null ? "http" : "https") + "://" + port + "/" + path;

        URL url = new URL(strURL);
        String protocol = url.getProtocol();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setInstanceFollowRedirects(false);
        if (protocol.equals("https")) {
            if (sslSocketFactory != null) {
                ((HttpsURLConnection) con).setSSLSocketFactory(sslSocketFactory);
                ((HttpsURLConnection) con).setHostnameVerifier(SSLSocketBuilder.CommonNameVerifier);
            }
        } else if (protocol.equals("http")) {
            // do nothing
        } else {
            throw new IOException("bad protocol");
        }
        con.setRequestMethod("GET");
        con.connect();
        if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
            con.disconnect();
            throw new IOException("" + con.getResponseCode());
        }
        BufferedInputStream in = new BufferedInputStream(con.getInputStream());
        int length, size = 0;
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
        while ((length = in.read()) != -1) {
            size += length;
            out.write(length);
        }
        out.close();
        con.disconnect();
        if (size == 0) {
            return null;
        }
        return temp;
    }

    static public void main(String[] argv) {

        PrintAgent agent = new PrintAgent(argv[0], "ormaster", "ormaster", null);
        agent.start();

        for (int i = 1; i < argv.length; i++) {
            agent.addPrintRequest(argv[i], argv[i], 100, true);
        }
    }

    public class DLRequest extends PrintRequest {

        private final String description;

        public DLRequest(String url, String filename, String desc, int retry) {
            super(url, "", retry, false);
            this.filename = filename;
            this.description = desc;
        }

        @Override
        public void action(File file) {
            try {
                PandaDownload pd = new PandaDownload();
                pd.setName("PrintAgent.PandaDownload");
                pd.showDialog(this.filename, this.description, file);
            } catch (Exception ex) {
                this.showOtherError();
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

        private String path;
        private String title;
        protected String filename;
        private int retry;
        private final boolean showDialog;

        public PrintRequest(String url, String title, int retry, boolean showdialog) {
            this.path = url;
            this.title = title;
            this.retry = retry;
            this.showDialog = showdialog;
            this.filename = "print.pdf";
        }

        public void action(File file) {
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
}
