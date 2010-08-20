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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

/** <p>A JFrame wrapper.</p>
 */
public class Window extends JFrame {

    private String title = "";
    private boolean allow_grow;
    private boolean allow_shrink;
    private Component child = null;
    private JDialog dialog = null;

    public void destroyDialog() {
        if (dialog != null) {
            child.setEnabled(false);
            child.setVisible(false);
            dialog.setEnabled(false);
            dialog.setVisible(false);
            //dialog.removeAll();
            //dialog.dispose();
            //dialog = null;
        }
    }

    public JDialog createDialog(Component parent,int tx, int ty) {
        if (dialog == null) {
            if (parent instanceof Frame) {
                dialog = new JDialog((Frame) parent, this.getTitle(), false);
            } else if (parent instanceof Dialog) {
                dialog = new JDialog((Dialog) parent, this.getTitle(), false);
            } else {
                dialog = new JDialog();
                dialog.setModal(false);
            }
            dialog.setName(this.getName());
            dialog.getContentPane().add(child);
            dialog.setResizable(this.getAllow_Grow() && this.getAllow_Shrink());
            dialog.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            dialog.setLocation(tx + this.getX(),ty + this.getY());
        }
        dialog.setTitle(this.getTitle());
        if (!dialog.isEnabled()) {
            dialog.setEnabled(true);
        }
        if (!dialog.isVisible()) {
            dialog.setVisible(true);
        }
        if (!child.isEnabled()) {
            child.setEnabled(true);
        }
        if (!child.isVisible()) {
            child.setVisible(true);
        }
        dialog.setSize(this.getSize());
        dialog.validate();
        return dialog;
    }

    public JDialog getDialog() {
        return dialog;
    }

    public Component getChild() {
        return child;
    }

    public void setChild(Component child) {
        this.child = child;
    }

    public boolean isDialog() {
        return isDialog;
    }

    public void setIsDialog(boolean isDialog) {
        this.isDialog = isDialog;
    }
    private boolean isDialog = false;

    /** <p>Constructs a Window instance.</p>
     */
    public Window() {
        super();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Prepare a glass pane with wait cursor.
        // This pane is usually invisible, but is visible and gains focus to
        // disalbe this window when it is busy.
        setGlassPane(new JPanel() {

            {
                // Make the pane transparent.
                setOpaque(false);
                Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
                setCursor(waitCursor);
                addMouseListener(new MouseAdapter() {
                    // nothing overridden
                });
                addKeyListener(new KeyAdapter() {
                    // nothing overridden
                });
            }
        });
        getGlassPane().setVisible(false);
    }

    /** <p>Show the window is busy by changing the mouse cursor to wait cursor.
     * Accepts no input.</p>
     */
    public void showBusyCursor() {
        getGlassPane().setVisible(true);
    }

    /** <p>Cancel the busy state of this window.</p>
     */
    public void hideBusyCursor() {
        getGlassPane().setVisible(false);
    }

    /** <p>Test if this window is active(=accpets input)</p>
     *
     * @return true if this window is active. false otherwise.
     */
    @Override
    public boolean isActive() {
        return !getGlassPane().isVisible();
    }

    /** <p>Make <em>all</em> window busy.</p>
     */
    public static void busyAllWindows() {
        Window[] windows = getMontsuqiWindows();
        for (int i = 0; i < windows.length; i++) {
            Window w = windows[i];
            w.showBusyCursor();
        }
    }

    /** <p>Returns all java.awt.Frames which are instances of this class.</p>
     *
     * @return array of Frames which are instances of this class.
     */
    public static Window[] getMontsuqiWindows() {
        Frame[] frames = Frame.getFrames();
        List list = new ArrayList();
        for (int i = 0; i < frames.length; i++) {
            Frame f = frames[i];
            if (f instanceof Window) {
                list.add(f);
            }
        }
        return (Window[]) list.toArray(new Window[list.size()]);
    }

    public void setTitleString(String title) {
        this.title = title;
    }

    public void setSessionTitle(String sessionTitle) {
        Frame frame = (Frame) this;
        if (sessionTitle.equals("")) {
            frame.setTitle(title);
        } else {
            frame.setTitle(title + " - " + sessionTitle);
        }
    }

    public void setAllow_Grow(boolean value) {
        this.allow_grow = value;
    }

    public boolean getAllow_Grow() {
        return allow_grow;
    }

    public void setAllow_Shrink(boolean value) {
        this.allow_shrink = value;
    }

    public boolean getAllow_Shrink() {
        return allow_shrink;
    }
}
