/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.montsuqi.monsiaj.client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.print.PrintService;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.WindowConstants;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.montsuqi.monsiaj.util.GtkStockIcon;
import org.montsuqi.monsiaj.util.Messages;
import org.montsuqi.monsiaj.util.PDFPrint;
import org.montsuqi.monsiaj.util.PopupNotify;
import org.montsuqi.monsiaj.util.TempFile;
import org.montsuqi.monsiaj.widgets.Button;
import org.montsuqi.monsiaj.widgets.PandaDownload;
import org.montsuqi.monsiaj.widgets.PandaPreview;

/**
 *
 * @author mihara
 */
public class Download {
    private static final Logger logger = LogManager.getLogger(Download.class);
    
    private static void showReportDialog(String title, File file) throws IOException {
        final JDialog dialog = new JDialog();
        Button closeButton = new Button(new AbstractAction(Messages.getString("PrintReport.close")) {

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
        dialog.setTitle(Messages.getString("PrintReport.title") + title);
        container.add(preview, BorderLayout.CENTER);
        container.add(closeButton, BorderLayout.SOUTH);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        preview.setSize(800, 600);
        preview.load(file.getAbsolutePath());
        preview.setFocusable(true);

        closeButton.setFocusable(true);

        dialog.setModal(true);
        dialog.setResizable(true);
        dialog.setVisible(true);
        closeButton.requestFocus();
    }

    public static void printReport(Config conf, Protocol protocol, JSONObject item) {
        try {
            logger.info("printReport:" + item.toString());
            if (!item.has("object_id")) {
                return;
            }
            String oid = item.getString("object_id");

            String printer = null;
            if (item.has("printer")) {
                printer = item.getString("printer");
            }

            String title = "";
            if (item.has("title")) {
                title = item.getString("title");
            }

            boolean showdialog = false;
            if (item.has("showdialog")) {
                showdialog = item.getBoolean("showdialog");
            }
            if (oid == null || oid.equals("0")) {
                return;
            }

            if (System.getProperty("monsia.printreport.showdialog") != null) {
                showdialog = true;
            }

            try {
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                String prefix = "report_" + sdf.format(date) + "_";
                File file = TempFile.createTempFile(prefix, "pdf");
                if (System.getProperty("monsia.save.print_data") != null) {
                } else {
                    file.deleteOnExit();
                }
                OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                protocol.getBLOB(oid, out);
                if (showdialog) {
                    showReportDialog(title, file);
                } else {
                    int cp = 1;
                    PrintService ps = null;
                    if (printer != null) {
                        ps = conf.getPrintService(printer);
                        cp = conf.getCopies(printer);
                    }
                    if (ps != null) {
                        PopupNotify.popup(Messages.getString("PrintReport.notify_summary"),
                                Messages.getString("PrintReport.notify_print_start") + "\n\n"
                                + Messages.getString("PrintReport.printer") + printer + "\n\n"
                                + Messages.getString("PrintReport.title") + title,
                                GtkStockIcon.get("gtk-print"), 0);
                        PDFPrint.print(file,cp,ps);
                    } else {
                        showReportDialog(title, file);
                    }
                }
            } catch (IOException ex) {
                logger.catching(Level.WARN, ex);
                PopupNotify.popup(Messages.getString("PrintReport.notify_summary"),
                        Messages.getString("PrintReport.notify_print_fail") + "\n\n"
                        + Messages.getString("PrintReport.printer") + printer + "\n"
                        + Messages.getString("PrintReport.title") + title,
                        GtkStockIcon.get("gtk-dialog-error"), 0);
            }
        } catch (JSONException ex) {
            logger.catching(Level.WARN, ex);
        }
    }    
    
    public static void downloadFile(Config conf,Protocol protocol, JSONObject item)  {
        try {
            logger.info("downloadFile:" + item.toString());            
            if (!item.has("object_id")) {
                return;
            }
            String oid = item.getString("object_id");

            String filename = "";
            if (item.has("filename")) {
                filename = item.getString("filename");
            }
            String desc = "";
            if (item.has("description")) {
                desc = item.getString("description");
            }
            if (oid == null || oid.equals("0")) {
                return;
            }
            try {
                File temp = TempFile.createTempFile("downloadfile", filename);
                temp.deleteOnExit();
                OutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
                protocol.getBLOB(oid, out);
                PandaDownload pd = new PandaDownload();
                pd.showDialog(filename, desc, temp);
            } catch (IOException ex) {
                logger.catching(Level.WARN, ex);
                PopupNotify.popup(Messages.getString("DownloadFile.notify_summary"),
                        Messages.getString("DownloadFile.fail") + "\n\n"
                        + Messages.getString("DownloadFile.filename") + filename + "\n"
                        + Messages.getString("DownloadFile.description") + desc,
                        GtkStockIcon.get("gtk-dialog-error"), 0);
            }
        } catch (JSONException ex) {
            logger.catching(Level.WARN, ex);
        }
    }    
}
