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

import java.awt.Component;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.net.URL;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.LayoutFocusTraversalPolicy;
import org.montsuqi.monsiaj.monsia.Interface;

/**
 * <p>
 * A JFrame wrapper.</p>
 */
public class TopWindow extends Window implements ComponentListener {

    private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    private final int DEFAULT_WIDTH = 1024;
    private final int DEFAULT_HEIGHT = 768;
    private final int FOOTER = 24;
    private double hScale = 1.0;
    private double vScale = 1.0;
    private Interface xml;

    public double getHScale() {
        return hScale;
    }

    public double getVScale() {
        return vScale;
    }

    public void showWindow(Window window) {
        Component child = window.getChild();
        if (!child.isVisible()) {
            child.setVisible(true);
        }
        this.setName(window.getName());
        this.getContentPane().removeAll();
        this.getContentPane().add(child);
        this.setTitle(window.getTitle());
        if (!this.isResizable()) {
            this.setResizable(true);
        }

        if (!this.isVisible()) {
            this.setVisible(true);
        }
        ((JComponent) child).revalidate();
        ((JComponent) child).repaint();

        this.hideBusyCursor();
        if (!this.isEnabled()) {
            this.setEnabled(true);
        }
        this.setChild(child);
        ((JComponent) child).requestFocusInWindow();
    }

    /**
     * <p>
     * Constructs a Window instance.</p>
     */
    public TopWindow() {
        super();

        URL iconURL = getClass().getResource("/images/orcamo.png");
        setIconImage(Toolkit.getDefaultToolkit().createImage(iconURL));

        int x, y, width, height;
        x = prefs.getInt(this.getClass().getName() + ".x", 0);
        y = prefs.getInt(this.getClass().getName() + ".y", 0);
        width = prefs.getInt(this.getClass().getName() + ".width", DEFAULT_WIDTH);
        height = prefs.getInt(this.getClass().getName() + ".height", DEFAULT_HEIGHT - FOOTER);

        if (System.getProperty("monsia.topwindow.width") != null) {
            width = Integer.parseInt(System.getProperty("monsia.topwindow.width"));
        }
        if (System.getProperty("monsia.topwindow.height") != null) {
            height = Integer.parseInt(System.getProperty("monsia.topwindow.height"));
        }
        if (System.getProperty("monsia.topwindow.x") != null) {
            x = Integer.parseInt(System.getProperty("monsia.topwindow.x"));
        }
        if (System.getProperty("monsia.topwindow.y") != null) {
            y = Integer.parseInt(System.getProperty("monsia.topwindow.y"));
        }

        this.setLocation(x, y);
        this.setSize(width, height);
        this.addComponentListener(this);
        this.setFocusCycleRoot(true);
        this.setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
    }

    private void Scale() {
        Insets insets = this.getInsets();
        hScale = (this.getWidth() * 1.0 - insets.left - insets.right) / (DEFAULT_WIDTH);
        // the bottom 24 pixel is not visible in glade
        vScale = (this.getHeight() * 1.0 - insets.top - insets.bottom)
                / (DEFAULT_HEIGHT - FOOTER);
    }

    public void ReScale() {
        Scale();
        this.xml.scaleWidget(hScale, vScale, this.getInsets());
    }

    public void setXml(Interface xml) {
        this.xml = xml;
    }

    @Override
    public void componentResized(ComponentEvent ce) {
        ReScale();
        prefs.putInt(this.getClass().getName() + ".width", this.getWidth());
        prefs.putInt(this.getClass().getName() + ".height", this.getHeight());
    }

    @Override
    public void componentMoved(ComponentEvent ce) {
        prefs.putInt(this.getClass().getName() + ".x", this.getX());
        prefs.putInt(this.getClass().getName() + ".y", this.getY());
    }

    @Override
    public void componentShown(ComponentEvent ce) {
        // do nothing
    }

    @Override
    public void componentHidden(ComponentEvent ce) {
        // do nothing
    }
}
