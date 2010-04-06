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

package org.montsuqi.widgets;

import com.centerkey.utils.BareBonesBrowserLaunch;
import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.montsuqi.util.Logger;
import org.montsuqi.util.SystemEnvironment;

/** <p>A component that holds a timer to fire events periodically.</p>
 * 
 * <p>This class repeatedly fires a TimerEvent once on every repetition
 * of period to its TimerListeners.</p>
 */
public class PandaDownload extends JComponent {

    protected static final Logger logger = Logger.getLogger(PandaDownload.class);
    private Preferences prefs = Preferences.userNodeForPackage(this.getClass());

    /** <p>Constructs a timer component.</p>
     *
     * <p>Initially this component's timer has a duration of 60 seconds.
     * This helps it wait firing events until the component is on view.</p>
     * <p>Correct duration should be set later.</p>
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

    public void showDialog(java.awt.Frame parent,String fileName, File file) throws IOException {
        Object[] options = {Messages.getString("PandaDownload.open"),
            Messages.getString("PandaDownload.save"),
            Messages.getString("PandaDownload.cancel")};
        int n = JOptionPane.showOptionDialog(parent,
                Messages.getString("PandaDownload.question") + "\n\n" +
                Messages.getString("PandaDownload.filename") + fileName + "\n" +
                Messages.getString("PandaDownload.size") + displaySize(file.length()) + "\n",
                Messages.getString("PandaDownload.title"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]);
        if (n == 0) {
            if (SystemEnvironment.isJavaVersionMatch("1.5")) {
                BareBonesBrowserLaunch.openURL("file://" + file.getAbsolutePath());
            } else {
                Desktop d = Desktop.getDesktop();
                if (Desktop.isDesktopSupported() && d.isSupported(Desktop.Action.OPEN)) {
                    try {
                        d.open(file);
                    } catch (IOException ex) {
                        BareBonesBrowserLaunch.openURL("file://" + file.getAbsolutePath());
                    }
                } else {
                    BareBonesBrowserLaunch.openURL("file://" + file.getAbsolutePath());
                }
            }
        } else if (n == 1) {
            String dir = prefs.get(PandaDownload.class.getName(), System.getProperty("user.home"));
            JFileChooser chooser = new JFileChooser(dir);

            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File selected = chooser.getSelectedFile();
            if (selected.exists() && selected.canWrite()) {
                if (JOptionPane.showConfirmDialog(this, Messages.getString("FileEntry.ask_overwrite"), Messages.getString("FileEntry.question"), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) { //$NON-NLS-1$ //$NON-NLS-2$
                    return;
                }
            }
            prefs.put(PandaDownload.class.getName(), selected.getParent());
            FileChannel srcChannel = new FileInputStream(file).getChannel();
            FileChannel destChannel = new FileOutputStream(selected).getChannel();
            try {
                srcChannel.transferTo(0, srcChannel.size(), destChannel);
            } finally {
                srcChannel.close();
                destChannel.close();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("PandaDownload <file>");
            System.exit(1);
        }
        File file = new File(args[0]);
        PandaDownload pd = new PandaDownload();
        pd.showDialog(null,file.getName(),file);
    }
}
