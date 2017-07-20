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
import org.montsuqi.monsiaj.util.Messages;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.prefs.Preferences;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.montsuqi.monsiaj.util.GtkStockIcon;
import org.montsuqi.monsiaj.util.PopupNotify;

/**
 * <p>
 * A component that holds a timer to fire events periodically.</p>
 *
 * <p>
 * This class repeatedly fires a TimerEvent once on every repetition of period
 * to its TimerListeners.</p>
 */
public class PandaDownload extends JComponent {

    private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    protected static final Logger logger = LogManager.getLogger(PandaDownload.class);

    /**
     * <p>
     * Constructs a timer component.</p>
     *
     * <p>
     * Initially this component's timer has a duration of 60 seconds. This helps
     * it wait firing events until the component is on view.</p>
     * <p>
     * Correct duration should be set later.</p>
     */
    public PandaDownload() {
        super();
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

    public void showDialog(final String fileName, final String description, final File file) throws IOException {
        final JDialog dialog = new JDialog((JFrame) null, Messages.getString("PandaDownload.title"), true);
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        String descLine = "";

        if (description.length() > 0) {
            descLine = Messages.getString("PandaDownload.description") + description + "\n";
        }

        JPanel textPanel = new JPanel(new BorderLayout(5, 5));
        textPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JTextPane bodyText = new JTextPane();
        bodyText.setText(Messages.getString("PandaDownload.question") + "\n\n"
                + Messages.getString("PandaDownload.filename") + fileName + "\n"
                + descLine
                + Messages.getString("PandaDownload.size") + displaySize(file.length()) + "\n");
        bodyText.setOpaque(false);
        bodyText.setEditable(false);

        textPanel.add(bodyText, BorderLayout.CENTER);
        panel.add(new JLabel(GtkStockIcon.get("gtk-dialog-info")), BorderLayout.WEST);
        panel.add(textPanel, BorderLayout.CENTER);

        //buttons
        JPanel bar = new JPanel();
        bar.setLayout(new FlowLayout());
        panel.add(bar, BorderLayout.SOUTH);

        Button openButton = new Button(new AbstractAction(Messages.getString("PandaDownload.open")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                Desktop d = Desktop.getDesktop();
                if (Desktop.isDesktopSupported() && d.isSupported(Desktop.Action.OPEN)) {
                    try {
                        d.open(file);
                    } catch (IOException ex) {
                        System.out.println(ex);
                        JOptionPane.showMessageDialog(null, Messages.getString("PandaDownload.open_failure_message"), Messages.getString("PandaDownload.error"), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        bar.add(openButton);

        Button saveButton = new Button(new AbstractAction(Messages.getString("PandaDownload.save")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                String dir = prefs.get(PandaDownload.class.getName(), System.getProperty("user.home"));
                JFileChooser chooser = new JFileChooser(dir);
                chooser.setSelectedFile(new File(fileName));

                if (chooser.showSaveDialog(dialog) != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                File selected = chooser.getSelectedFile();
                if (selected.exists() && selected.canWrite()) {
                    if (JOptionPane.showConfirmDialog(dialog, Messages.getString("FileEntry.ask_overwrite"), Messages.getString("FileEntry.question"), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {  //$NON-NLS-2$
                        return;
                    }
                }
                prefs.put(PandaDownload.class.getName(), selected.getParent());
                try {
                    FileChannel srcChannel = new FileInputStream(file).getChannel();
                    FileChannel destChannel = new FileOutputStream(selected).getChannel();
                    try {
                        srcChannel.transferTo(0, srcChannel.size(), destChannel);
                    } finally {
                        srcChannel.close();
                        destChannel.close();
                    }
                    PopupNotify.popup(Messages.getString("PandaDownload.save_comp"),
                            selected.getAbsolutePath() + Messages.getString("PandaDownload.save_comp_msg"),
                            GtkStockIcon.get("gtk-dialog-info"), 0);
                } catch (IOException ex) {
                    logger.warn(ex);
                }
            }
        });
        bar.add(saveButton);

        Button closeButton = new Button(new AbstractAction(Messages.getString("PandaDownload.close")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        bar.add(closeButton);

        dialog.add(panel);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo((JFrame) null);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("PandaDownload <file> <description>");
            System.exit(1);
        }
        File file = new File(args[0]);
        PandaDownload pd = new PandaDownload();
        pd.showDialog(file.getName(), args[1], file);
    }
}
