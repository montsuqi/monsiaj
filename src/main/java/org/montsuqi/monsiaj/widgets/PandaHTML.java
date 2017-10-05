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

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.AttributeSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fit.cssbox.swingbox.BrowserPane;
import org.fit.cssbox.swingbox.util.Anchor;
import org.fit.cssbox.swingbox.util.Constants;

/**
 * <p>
 * A HTML viewer for platforms other than MacOS X.</p>
 * <p>
 * This component uses JEditorPane to render HTML.</p>
 */
public class PandaHTML extends JPanel {

    private static final Logger logger = LogManager.getLogger(PandaHTML.class);
    private final BrowserPane browserPane;
    protected ArrayList<URL> history;

    public PandaHTML() {
        history = new ArrayList<>();

        setLayout(new BorderLayout());
        browserPane = new BrowserPane();
        browserPane.addHyperlinkListener((HyperlinkEvent event) -> {
            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                AttributeSet attrs = event.getSourceElement().getAttributes();
                Anchor anchor = (Anchor) attrs.getAttribute(Constants.ATTRIBUTE_ANCHOR_REFERENCE);
                String target = (String) anchor.getProperties().get(Constants.ELEMENT_A_ATTRIBUTE_TARGET);
                if (target.equals("_blank")) {
                    Desktop d = Desktop.getDesktop();
                    if (Desktop.isDesktopSupported() && d.isSupported(Desktop.Action.BROWSE)) {
                        try {
                            d.browse(event.getURL().toURI());
                        } catch (IOException | URISyntaxException ex) {
                            logger.warn(ex, ex);
                        }
                    }
                } else {
                    setURL(event.getURL());
                }
            }
        });
        browserPane.setComponentPopupMenu(new PandaHTMLPopupMenu());

        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(browserPane);
        add(scroll, BorderLayout.CENTER);
    }

    /**
     * <p>
     * Loads a HTML from the given URL and render it.</p>
     *
     * @param url source URL of the document.
     */
    public void setURL(URL url) {
        try {
            logger.debug("load " + url);
            browserPane.setPage(url);
            history.add(url);
            System.out.println("add " + url);
        } catch (IOException ex) {
            logger.info(ex, ex);
        }
    }

    public void back() {
        try {
            int size = history.size();
            if (size > 1) {
                browserPane.setPage(history.get(size - 2));
                history.remove(size - 1);
            }
        } catch (IOException ex) {
            logger.info(ex, ex);
        }
    }

    public void setText(String text) {
        browserPane.setText(text);
    }

    public static void main(String[] args) throws IOException {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        PandaHTML html = new PandaHTML();
        f.add(html);
        f.setVisible(true);
        html.setURL(new URL(args[0]));
        f.pack();
        f.setSize(600, 800);
        f.validate();
    }

    class PandaHTMLPopupMenu extends JPopupMenu {

        private static final String BACK = "Back";
        private final Action backAction = new BackAction(BACK);

        protected PandaHTMLPopupMenu() {
            super();
            add(backAction);
        }

        class BackAction extends AbstractAction {

            protected BackAction(String label) {
                super(label);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                back();
            }
        }
    }
}
