package org.montsuqi.client;

import com.sun.pdfview.PDFFile;
import java.awt.BorderLayout;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import org.montsuqi.util.PDFPrint;
import org.montsuqi.widgets.PandaPreview;
import org.montsuqi.widgets.Button;

/*
 * Copyright (C) 2010 JMA (Japan Medical Association)
 */
/**
 *
 * @author mihara
 */
public class PrintAgent extends Thread {

    private final int DELAY = 1000;
    private ConcurrentLinkedQueue<PrintRequest> queue;
    private Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    private String port;

    public PrintAgent(String port,final String user, final String password) {
        queue = new ConcurrentLinkedQueue<PrintRequest>();
        this.port = port;
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
                processRequest();
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }

    synchronized public void processRequest() {
        PrintRequest request = queue.poll();
        if (request != null) {
            try {
                File file = download(request);
                if (file != null) {
                    showDialog(request.getTitle(), file);
                } else {
                    queue.add(request);
                }
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
    }

    synchronized public void addRequest(String url, String title) {
        queue.add(new PrintRequest(url, title));
    }

    private String displaySize(long size) {
        String displaySize;
        final long ONE_KB = 1024;
        final long ONE_MB = ONE_KB * ONE_KB;
        final long ONE_GB = ONE_MB * ONE_MB;
        if (size / ONE_GB > 0) {
            displaySize = String.valueOf(size / ONE_GB) + " GB";
        } else if (size / ONE_MB > 0) {
            displaySize = String.valueOf(size / ONE_MB) + " MB";
        } else if (size / ONE_KB > 0) {
            displaySize = String.valueOf(size / ONE_KB) + " KB";
        } else {
            displaySize = String.valueOf(size) + " bytes";
        }
        return displaySize;
    }

    public void showDialog(String title, File file) {
        Object[] options = {Messages.getString("PrintAgent.preview_button"),
            Messages.getString("PrintAgent.save_button"),
            Messages.getString("PrintAgent.print_button"),
            Messages.getString("PrintAgent.cancel_button")
        };
        int n = JOptionPane.showOptionDialog(null,
                Messages.getString("PrintAgent.question") + "\n\n"
                + Messages.getString("PrintAgent.title") + title + "\n"
                + Messages.getString("PrintAgnet.num_of_pages") + getNumOfPages(file) + "\n"
                + Messages.getString("PrintAgent.size") + displaySize(file.length()) + "\n",
                Messages.getString("PrintAgent.dialog_title"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[3]);
        try {
            if (n == 0) {
                final JDialog dialog = new JDialog();
                Button closeButton = new Button(new AbstractAction(Messages.getString("PrintAgent.close")) { //$NON-NLS-1$

                    public void actionPerformed(ActionEvent e) {
                        dialog.dispose();
                    }
                });
                Container container = dialog.getContentPane();
                container.setLayout(new BorderLayout(5, 5));
                PandaPreview preview = new PandaPreview();
                dialog.setSize(new Dimension(800, 600));
                container.add(preview, BorderLayout.CENTER);
                container.add(closeButton, BorderLayout.SOUTH);
                dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                dialog.setVisible(true);
                closeButton.requestFocus();
                preview.load(file.getAbsolutePath());
            } else if (n == 1) {
                String dir = prefs.get(PrintAgent.class.getName(), System.getProperty("user.home"));
                JFileChooser chooser = new JFileChooser(dir);

                if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                File selected = chooser.getSelectedFile();
                if (selected.exists() && selected.canWrite()) {
                    if (JOptionPane.showConfirmDialog(null, Messages.getString("FileEntry.ask_overwrite"), Messages.getString("FileEntry.question"), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) { //$NON-NLS-1$ //$NON-NLS-2$
                        return;
                    }
                }
                prefs.put(PrintAgent.class.getName(), selected.getParent());
                FileChannel srcChannel = new FileInputStream(file).getChannel();
                FileChannel destChannel = new FileOutputStream(selected).getChannel();
                try {
                    srcChannel.transferTo(0, srcChannel.size(), destChannel);
                } finally {
                    srcChannel.close();
                    destChannel.close();
                }
            } else if (n == 2) {
                PDFPrint printer = new PDFPrint(file);
                printer.start();
            }
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

    public File download(PrintRequest request) throws IOException {
        File temp = File.createTempFile("monsiaj_printagent", ".pdf");
        temp.deleteOnExit();
        URL url = new URL("http://" + port + "/" + request.getPath());
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("GET");
        http.connect();
        BufferedInputStream bis = new BufferedInputStream(http.getInputStream());
        int data, outsize = 0;
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(temp));
        while ((data = bis.read()) != -1) {
            outsize += data;
            bos.write(data);
        }
        bos.close();
        if (outsize == 0) {
            return null;
        }
        return temp;
    }

    static public void main(String[] argv) {

        PrintAgent agent = new PrintAgent(argv[0],"ormaster", "ormaster");
        agent.start();

        for (int i = 1; i < argv.length; i++) {
            agent.addRequest(argv[i], argv[i]);
        }
    }

    public class PrintRequest {

        private String path;
        private String title;

        public PrintRequest(String url, String title) {
            this.path = url;
            this.title = title;
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
