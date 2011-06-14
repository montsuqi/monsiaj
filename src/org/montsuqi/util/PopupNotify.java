package org.montsuqi.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author mihara
 */
public class PopupNotify {

    private static List<JDialog> dialogs = new ArrayList<JDialog>();

    public static void popup(final String summary, final String body, final Icon icon, final int timeout) {
        final JDialog dialog = new JDialog();
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                JPanel panel = new JPanel(new BorderLayout(5, 5));
                panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                panel.setBackground((Color) SystemColor.menu);

                JPanel textPanel = new JPanel(new BorderLayout(5, 5));
                textPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                textPanel.setBackground((Color) SystemColor.menu);

                JLabel summaryLabel = new JLabel(summary);
                summaryLabel.setFont(new Font("Seif", Font.BOLD, 16));
                JTextPane bodyText = new JTextPane();
                bodyText.setText(body);
                bodyText.setOpaque(false);
                bodyText.setEditable(false);

                textPanel.add(summaryLabel, BorderLayout.NORTH);
                textPanel.add(bodyText, BorderLayout.CENTER);
                panel.add(new JLabel(icon), BorderLayout.WEST);
                panel.add(textPanel, BorderLayout.CENTER);

                dialog.setUndecorated(true);
                dialog.add(panel);
                dialog.pack();
                Rectangle rect = panel.getBounds();
                int y = 0;
                for (JDialog d : dialogs) {
                    Rectangle r = d.getBounds();
                    if (r.y + r.height > y) {
                        y = r.y + r.height;
                    }
                }
                dialog.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width - rect.width, y + 5);
                dialog.setVisible(true);
                dialog.setFocusable(false);
                dialog.setFocusableWindowState(false);
                dialogs.add(dialog);
            }
        });
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            @Override
            public Void doInBackground() {
                try {
                    final int defaultTimeout = 10;
                    int current = 0;
                    int lengthOfTask;
                    if (timeout > 0) {
                        lengthOfTask = timeout * 10;
                    } else {
                        lengthOfTask = defaultTimeout * 10;
                    }
                    while (current < lengthOfTask && !isCancelled()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                            return null;
                        }
                        setProgress(100 * current++ / lengthOfTask);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            public void done() {
                dialogs.remove(dialog);
                dialog.dispose();
            }
        };
        /*
        worker.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
        
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
        if ("progress".equals(e.getPropertyName())) {
        int v = (Integer)e.getNewValue();
        if (v > 90) {
        AWTUtilities.setWindowOpacity(dialog, 1f - (v-90)*0.1f);                        
        }
        }
        }
        });*/
        worker.execute();
    }

    public static void main(String[] args) {
        PopupNotify.popup("1", "\n\n\n\n\n\n\n\n1111111111111111111",
                GtkStockIcon.get("gtk-dialog-info"), 5);
        
        try {
            Thread.sleep(1000); //3000ミリ秒Sleepする
        } catch (InterruptedException e) {
        };

        PopupNotify.popup("2", "\n\n\2",
                GtkStockIcon.get("gtk-dialog-error"), 5);

        try {
            Thread.sleep(10000); //3000ミリ秒Sleepする
        } catch (InterruptedException e) {
        };

        PopupNotify.popup("3", "333333333\n3333333333333\n\n\n\n",
                GtkStockIcon.get("gtk-dialog-error"), 5);
        
        try {
            Thread.sleep(1000); //3000ミリ秒Sleepする
        } catch (InterruptedException e) {
        };

        PopupNotify.popup("4", "33444443333\n333333433333\n\n\n\n",
                GtkStockIcon.get("gtk-dialog-error"), 5);        
    }
}
