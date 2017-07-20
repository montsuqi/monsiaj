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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.montsuqi.monsiaj.util.ExtensionFileFilter;
import org.montsuqi.monsiaj.util.PDFPrint;

/**
 * <
 * p>
 * Preview pane with control buttons and display of current scale.</p>
 */
public class PandaPreview extends JPanel {

    static final Logger logger = LogManager.getLogger(PandaPreview.class);
    private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());

    class HandScrollListener extends MouseInputAdapter {

        private final Cursor defCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        private final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        private final Point pp = new Point();

        @Override
        public void mouseDragged(final MouseEvent e) {
            JViewport vport = scroll.getViewport();
            Point cp = e.getPoint();
            Point vp = vport.getViewPosition();
            vp.translate(pp.x - cp.x, pp.y - cp.y);
            panel.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
            pp.setLocation(cp);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            panel.setCursor(hndCursor);
            pp.setLocation(e.getPoint());
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            panel.setCursor(defCursor);
            panel.repaint();
        }
    }
    private static final double SCALE_FACTOR = 1.2;
    private static final double SCALE_FIT_PAGE = -1.0;
    private static final double SCALE_FIT_PAGE_WIDTH = -2.0;
    private static final String SCALE_FIT_PAGE_WIDTH_STR = Double.toString(SCALE_FIT_PAGE_WIDTH);
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
    private final JToolBar toolbar;
    private NumberEntry pageEntry;
    private final JLabel pageLabel;
    private JComboBox<String> combo;
    private final JScrollPane scroll;
    private double zoom;
    private String fileName;
    private PDFPanel panel;
    private final Action nextAction;
    private final Action prevAction;
    private final Action saveAction;
    private final Action printAction;
    private final Action zoomInAction;
    private final Action zoomOutAction;
    private final Action fitPageAction;
    private final Action fitPageWidthAction;

    private final class NextAction extends AbstractAction {

        NextAction() {
            URL iconURL = getClass().getResource("/images/next.png");
            if (iconURL != null) {
                putValue(Action.SMALL_ICON, new ImageIcon(iconURL));
            }
            putValue(Action.NAME, Messages.getString("PandaPreview.next"));
            putValue(Action.SHORT_DESCRIPTION, Messages.getString("PandaPreview.next_short_description"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int numPages = panel.getNumPages();
            int pageNum = panel.getPageNum();
            if (numPages != 0 && pageNum != numPages) {
                panel.setPage(pageNum + 1);
                pageEntry.setValue(pageNum + 1);
            }
        }
    }

    private final class PrevAction extends AbstractAction {

        PrevAction() {
            URL iconURL = getClass().getResource("/images/prev.png");
            if (iconURL != null) {
                putValue(Action.SMALL_ICON, new ImageIcon(iconURL));
            }
            putValue(Action.NAME, Messages.getString("PandaPreview.prev"));
            putValue(Action.SHORT_DESCRIPTION, Messages.getString("PandaPreview.prev_short_description"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int numPages = panel.getNumPages();
            int pageNum = panel.getPageNum();
            if (numPages != 0 && pageNum != 1) {
                panel.setPage(pageNum - 1);
                pageEntry.setValue(pageNum - 1);
            }
        }
    }

    private final class SaveAction extends AbstractAction {

        SaveAction() {
            URL iconURL = getClass().getResource("/images/save.png");
            if (iconURL != null) {
                putValue(Action.SMALL_ICON, new ImageIcon(iconURL));
            }
            putValue(Action.NAME, Messages.getString("PandaPreview.save"));
            putValue(Action.SHORT_DESCRIPTION, Messages.getString("PandaPreview.save_short_description"));
        }

        @Override
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
                    FileOutputStream out;
                    try (FileInputStream in = new FileInputStream(fileName)) {
                        out = new FileOutputStream(file);
                        b = new byte[in.available()];
                        while (in.read(b) > 0) {
                            out.write(b);
                        }
                    }
                    out.close();
                } catch (IOException ex) {
                    System.out.println(ex);
                }
            }
        }
    }

    private final class PrintAction extends AbstractAction {

        PrintAction() {
            URL iconURL = getClass().getResource("/images/print.png");
            if (iconURL != null) {
                putValue(Action.SMALL_ICON, new ImageIcon(iconURL));
            }
            putValue(Action.NAME, Messages.getString("PandaPreview.print"));
            putValue(Action.SHORT_DESCRIPTION, Messages.getString("PandaPreview.print_short_description"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            PDFPrint printer = new PDFPrint(new File(fileName));
            printer.start();
        }
    }

    private final class ZoomInAction extends AbstractAction {

        @Override
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

        @Override
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

        @Override
        public void actionPerformed(ActionEvent e) {
            zoom = SCALE_FIT_PAGE;
            updateCombo();
            setScale();
        }
    }

    private final class FitPageWidthAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            zoom = SCALE_FIT_PAGE_WIDTH;
            updateCombo();
            setScale();
        }
    }

    public PandaPreview() {
        super();
        setLayout(new BorderLayout());

        nextAction = new NextAction();
        prevAction = new PrevAction();

        pageEntry = new NumberEntry();
        pageEntry.setFormat("------");
        pageEntry.setMinimumSize(new Dimension(55, 1));
        pageEntry.setMaximumSize(new Dimension(55, 40));
        pageEntry.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int pagenum;
                try {
                    pagenum = Integer.parseInt(pageEntry.getText());
                } catch (NumberFormatException ex) {
                    pagenum = panel.getPageNum();
                }
                if (1 <= pagenum && pagenum <= panel.getNumPages()) {
                    panel.setPage(pagenum);
                } else {
                    pageEntry.setValue(panel.getPageNum());
                }
            }
        });
        pageLabel = new JLabel("/");
        pageLabel.setMinimumSize(new Dimension(55, 1));
        pageLabel.setMaximumSize(new Dimension(55, 40));
        saveAction = new SaveAction();
        printAction = new PrintAction();
        zoomInAction = new ZoomInAction();
        zoomOutAction = new ZoomOutAction();
        fitPageAction = new FitPageAction();
        fitPageWidthAction = new FitPageWidthAction();

        combo = new JComboBox<>(SCALE_STRING);
        combo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent anEvent) {
                zoom = SCALE_VALUE[combo.getSelectedIndex()];
                setScale();
            }
        });
        final Dimension preferredSize = combo.getPreferredSize();
        combo.setMaximumSize(preferredSize);

        toolbar = new JToolBar();
        toolbar.setFloatable(false);

        toolbar.add(prevAction);
        toolbar.add(nextAction);
        toolbar.addSeparator();

        toolbar.add(pageEntry);
        toolbar.add(pageLabel);
        toolbar.addSeparator();

        toolbar.add(combo);
        toolbar.addSeparator();

        toolbar.add(saveAction);
        toolbar.add(printAction);

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

        zoom = Double.parseDouble(prefs.get("zoom", SCALE_FIT_PAGE_WIDTH_STR));
        this.updateCombo();

        ActionMap actionMap = getActionMap();
        actionMap.put("prev", prevAction);
        actionMap.put("next", nextAction);
        actionMap.put("save", saveAction);
        actionMap.put("print", printAction);
        actionMap.put("fitPage", fitPageAction);
        actionMap.put("fitPageWidth", fitPageWidthAction);
        actionMap.put("zoomOut", zoomOutAction);
        actionMap.put("zoomIn", zoomInAction);

        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke("ctrl shift P"), "prev");
        inputMap.put(KeyStroke.getKeyStroke("ctrl shift N"), "next");
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

    public void load(String fileName) {
        try {
            this.fileName = fileName;
            panel.clear();
            panel.setVisible(true);
            panel.load(fileName);
            pageLabel.setText("/" + panel.getNumPages());
            pageEntry.setValue(1);
            this.setScale();
        } catch (Exception ex) {
            logger.catching(Level.WARN, ex);
        }
    }

    public void clear() {
        panel.clear();
        panel.setVisible(false);
    }

    private void updateCombo() {
        int index = 1;
        ActionListener[] listeners = combo.getActionListeners();
        for (ActionListener listener : listeners) {
            combo.removeActionListener(listener);
        }
        if (zoom == SCALE_FIT_PAGE) {
            index = 0;
        } else if (zoom == SCALE_FIT_PAGE_WIDTH) {
            index = 1;
        } else {
            for (int i = 2; i < SCALE_VALUE.length; i++) {
                index = i;
                if (zoom <= SCALE_VALUE[i]) {
                    break;
                }
            }
        }
        combo.setSelectedIndex(index);
        for (ActionListener listener : listeners) {
            combo.addActionListener(listener);
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
        if (z > SCALE_VALUE[SCALE_VALUE.length - 1]
                || z <= 0.02) {
            z = 1.0;
        }
        return z;
    }

    private void setScale() {
        prefs.put("zoom", Double.toString(zoom));
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
