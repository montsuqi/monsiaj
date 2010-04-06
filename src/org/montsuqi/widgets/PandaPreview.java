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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import com.centerkey.utils.BareBonesBrowserLaunch;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.JViewport;
import javax.swing.event.MouseInputAdapter;
import org.montsuqi.util.ExtensionFileFilter;

/** <p>Preview pane with control buttons and display of current scale.</p>
 */
public class PandaPreview extends JPanel {

    class HandScrollListener extends MouseInputAdapter {

        private final Cursor defCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        private final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        private final Point pp = new Point();

        public void mouseDragged(final MouseEvent e) {
            JViewport vport = scroll.getViewport();
            Point cp = e.getPoint();
            Point vp = vport.getViewPosition();
            vp.translate(pp.x - cp.x, pp.y - cp.y);
            panel.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
            pp.setLocation(cp);
        }

        public void mousePressed(MouseEvent e) {
            panel.setCursor(hndCursor);
            pp.setLocation(e.getPoint());
        }

        public void mouseReleased(MouseEvent e) {
            panel.setCursor(defCursor);
            panel.repaint();
        }
    }


    private static final double SCALE_FACTOR = 1.2;
    private static final double SCALE_FIT_PAGE = -1.0;
    private static final double SCALE_FIT_PAGE_WIDTH = -2.0;
    private static final String[] SCALE_STRING = {
        Messages.getString("PandaPreview.fitPage"),
        Messages.getString("PandaPreview.fitPageWidth"),
        "50%",
        "75%",
        "100%",
        "125%",
        "150%",
        "175%",
        "200%",
        "300%"
    };
    private static final double[] SCALE_VALUE = {
        SCALE_FIT_PAGE,
        SCALE_FIT_PAGE_WIDTH,
        0.7071067811,
        0.8408964152,
        1.0,
        1.1892071149,
        1.4142135623,
        1.6817928304,
        2.0,
        2.8284271247
    };
    private JToolBar toolbar;
    private JComboBox combo;
    private JScrollPane scroll;
    private double zoom;
    private String fileName;
    private PDFPanel panel;
    private Action saveAction;
    private Action printAction;
    private Action zoomInAction;
    private Action zoomOutAction;
    private Action fitPageAction;
    private Action fitPageWidthAction;

    private final class SaveAction extends AbstractAction {

        SaveAction() {
            URL iconURL = getClass().getResource("/org/montsuqi/widgets/images/save.png"); //$NON-NLS-1$
            if (iconURL != null) {
                putValue(Action.SMALL_ICON, new ImageIcon(iconURL));
            }
            putValue(Action.NAME, Messages.getString("PandaPreview.save")); //$NON-NLS-1$
            putValue(Action.SHORT_DESCRIPTION, Messages.getString("PandaPreview.save_short_description")); //$NON-NLS-1$
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new ExtensionFileFilter(".pdf", "PDF (*.pdf)"));
            int returnVal = fc.showSaveDialog(PandaPreview.this.getRootPane());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    byte[] b;
                    File file;
                    String outFileName = fc.getSelectedFile().getPath();
                    if (!outFileName.endsWith(".pdf") && !outFileName.endsWith(".PDF")) {
                        outFileName += ".pdf";
                        file = new File(outFileName);
                    } else {
                        file = fc.getSelectedFile();
                    }
                    FileInputStream in = new FileInputStream(fileName);
                    FileOutputStream out = new FileOutputStream(file);
                    b = new byte[in.available()];
                    while (in.read(b) > 0) {
                        out.write(b);
                    }
                    in.close();
                    out.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private final class PrintAction extends AbstractAction {

        PrintAction() {
            URL iconURL = getClass().getResource("/org/montsuqi/widgets/images/print.png"); //$NON-NLS-1$
            if (iconURL != null) {
                putValue(Action.SMALL_ICON, new ImageIcon(iconURL));
            }
            putValue(Action.NAME, Messages.getString("PandaPreview.print")); //$NON-NLS-1$
            putValue(Action.SHORT_DESCRIPTION, Messages.getString("PandaPreview.print_short_description")); //$NON-NLS-1$
        }

        public void actionPerformed(ActionEvent e) {
            BareBonesBrowserLaunch.openURL("file://" + fileName);
        }
    }

    private final class ZoomInAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            zoom = getRealZoom() * SCALE_FACTOR;
            if (zoom > SCALE_VALUE[SCALE_VALUE.length - 1]) {
                zoom = SCALE_VALUE[SCALE_VALUE.length - 1];
            }
            updateCombo();
            setScale();
        }
    }

    private final class ZoomOutAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            zoom = getRealZoom() / SCALE_FACTOR;
            if (zoom < 0.02) {
                zoom = 0.02;
            }
            updateCombo();
            setScale();
        }
    }

    private final class FitPageAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            zoom = SCALE_FIT_PAGE;
            updateCombo();
            setScale();
        }
    }

    private final class FitPageWidthAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            zoom = SCALE_FIT_PAGE_WIDTH;
            updateCombo();
            setScale();
        }
    }

    public PandaPreview() {
        super();
        setLayout(new BorderLayout());

        saveAction = new SaveAction();
        printAction = new PrintAction();
        zoomInAction = new ZoomInAction();
        zoomOutAction = new ZoomOutAction();
        fitPageAction = new FitPageAction();
        fitPageWidthAction = new FitPageWidthAction();

        toolbar = new JToolBar();
        toolbar.setFloatable(false);

        toolbar.add(saveAction);
        toolbar.add(printAction);

        combo = new JComboBox(SCALE_STRING);
        combo.setSelectedIndex(1);
        combo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent anEvent) {
                zoom = SCALE_VALUE[combo.getSelectedIndex()];
                setScale();
            }
        });
        final Dimension preferredSize = combo.getPreferredSize();
        combo.setMaximumSize(preferredSize);
        toolbar.add(combo);
        add(toolbar, BorderLayout.NORTH);

        panel = new PDFPanel();
        fileName = null;
        scroll = new JScrollPane(panel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(32);
        HandScrollListener hsl = new HandScrollListener();
        scroll.getViewport().addMouseMotionListener(hsl);
        scroll.getViewport().addMouseListener(hsl);
        add(scroll, BorderLayout.CENTER);
        zoom = SCALE_FIT_PAGE_WIDTH;

        ActionMap actionMap = getActionMap();
        actionMap.put("save", saveAction);
        actionMap.put("print", printAction);
        actionMap.put("fitPage", fitPageAction);
        actionMap.put("fitPageWidth", fitPageWidthAction);
        actionMap.put("zoomOut", zoomOutAction);
        actionMap.put("zoomIn", zoomInAction);

        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke("ctrl S"), "save");
        inputMap.put(KeyStroke.getKeyStroke("ctrl P"), "print");
        inputMap.put(KeyStroke.getKeyStroke("ctrl G"), "fitPage");
        inputMap.put(KeyStroke.getKeyStroke("shift F5"), "fitPage");
        inputMap.put(KeyStroke.getKeyStroke("ctrl F"), "fitPageWidth");
        inputMap.put(KeyStroke.getKeyStroke("shift F6"), "fitPageWidth");
        inputMap.put(KeyStroke.getKeyStroke("ctrl MINUS"), "zoomOut");
        inputMap.put(KeyStroke.getKeyStroke("shift F7"), "zoomOut");
        inputMap.put(KeyStroke.getKeyStroke("shift ctrl SEMICOLON"), "zoomIn");
        inputMap.put(KeyStroke.getKeyStroke("shift F8"), "zoomIn");
    }

    public void load(String fileName) throws IOException {
        try {
            this.fileName = fileName;
            panel.clear();
            panel.setVisible(true);
            panel.load(fileName);
            this.setScale();
        } catch (Exception ex) {
            if (!ex.getMessage().contains("This may not be a PDF File")) {
                ex.printStackTrace();
            }
        }
    }

    public void clear() {
        panel.clear();
        panel.setVisible(false);
    }

    private void updateCombo() {
        ActionListener[] listeners = combo.getActionListeners();
        for (int i = 0; i < listeners.length; i++) {
            combo.removeActionListener(listeners[i]);
        }
        if (zoom == SCALE_FIT_PAGE) {
            combo.setSelectedIndex(0);
        } else if (zoom == SCALE_FIT_PAGE_WIDTH) {
            combo.setSelectedIndex(1);
        } else {
            for (int i = 2; i < SCALE_VALUE.length; i++) {
                combo.setSelectedIndex(i);
                if (zoom < SCALE_VALUE[i]) {
                    break;
                }
            }
        }
        for (int i = 0; i < listeners.length; i++) {
            combo.addActionListener(listeners[i]);
        }
    }

    private double getRealZoom() {
        double z;

        if (this.zoom == SCALE_FIT_PAGE) {
            double h = panel.getPageHeight();
            if (h > 0) {
                if (scroll.isShowing()) {
                    z = scroll.getSize().height * 1.0 / h;
                } else {
                    z = this.getSize().height * 1.0 / h;
                }
            } else {
                z = 1.0;
            }
        } else if (this.zoom == SCALE_FIT_PAGE_WIDTH) {
            double w = panel.getPageWidth();
            if (w > 0) {
                if (scroll.isShowing()) {
                    z = scroll.getSize().width * 1.0 / w;
                } else {

                    z = this.getSize().width * 1.0 / w;
                }
            } else {
                z = 1.0;
            }
        } else {
            z = this.zoom;
        }
        if (z > SCALE_VALUE[SCALE_VALUE.length - 1] ||
                z <= 0.02) {
            z = 1.0;
        }
        return z;
    }

    private void setScale() {
        panel.setScale(getRealZoom());
    }

    public static void main(String[] args) throws IOException {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        PandaPreview preview = new PandaPreview();
        f.add(preview);
        f.setVisible(true);
        JFileChooser chooser = new JFileChooser();
        chooser.showOpenDialog(preview);
        preview.load(chooser.getSelectedFile().getAbsolutePath());
        preview.revalidate();
        preview.repaint();
        f.pack();
        f.validate();
    }
}
