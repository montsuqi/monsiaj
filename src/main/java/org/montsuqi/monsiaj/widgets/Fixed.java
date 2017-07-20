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

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JPanel;
import javax.swing.JViewport;

/**
 * <p>A container that lays out components in fixed position and size.</p>
 */
public class Fixed extends JPanel
        implements FocusListener {

    public Fixed() {
        super();
        super.setLayout(null);
    }

    public void focusGained(FocusEvent e) {
        final int FOCUS_MARGIN = 10;
        Component parent;
        parent = this.getParent();
        if (parent != null && parent instanceof JViewport) {
            Rectangle childRect = e.getComponent().getBounds();
            Rectangle viewRect = ((JViewport) parent).getViewRect();
            Point p = ((JViewport) parent).getViewPosition();
            int childTop = (int) childRect.getY();
            int childBottom = (int) (childRect.getY() + childRect.getHeight());
            int childLeft = (int) childRect.getX();
            int childRight = (int) (childRect.getX() + childRect.getWidth());
            int viewTop = (int) viewRect.getY();
            int viewBottom = (int) (viewRect.getY() + viewRect.getHeight());
            int viewLeft = (int) viewRect.getX();
            int viewRight = (int) (viewRect.getX() + viewRect.getWidth());
            int viewHeight = (int) viewRect.getHeight();
            int viewWidth = (int) viewRect.getWidth();
            int thisBottom = (int) this.getHeight();
            int thisRight = (int) this.getWidth();

            if (childTop < viewTop) {
                if (childTop - FOCUS_MARGIN < 0) {
                    p.move((int) p.getX(), 0);
                } else {
                    p.move((int) p.getX(), childTop - FOCUS_MARGIN);
                }
                ((JViewport) parent).setViewPosition(p);
            }
            if (childBottom > viewBottom) {
                if (childBottom + FOCUS_MARGIN > thisBottom) {
                    p.move((int) p.getX(), childBottom - viewHeight);
                } else {
                    p.move((int) p.getX(), childBottom + FOCUS_MARGIN - viewHeight);
                }
                ((JViewport) parent).setViewPosition(p);
            }
            if (childLeft < viewLeft) {
                if (childLeft - FOCUS_MARGIN < 0) {
                    p.move(0, (int) p.getY());
                } else {
                    p.move(childLeft - FOCUS_MARGIN, (int) p.getY());
                }
                ((JViewport) parent).setViewPosition(p);
            }
            if (childRight > viewRight) {
                if (childRight + FOCUS_MARGIN > thisRight) {
                    p.move(childRight - viewWidth, (int) p.getY());
                } else {
                    p.move(childRight + FOCUS_MARGIN - viewWidth, (int) p.getY());
                }
                ((JViewport) parent).setViewPosition(p);
            }
        }
    }

    public void focusLost(FocusEvent e) {
        // do nothing
    }

    @Override
    public void setLayout(LayoutManager layout) {
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        setPreferredSize(new Dimension(width, height));
    }

    @Override
    public void setBounds(Rectangle r) {
        super.setBounds(r);
        setPreferredSize(new Dimension(r.width, r.height));
    }

    @Override
    public void setSize(Dimension d) {
        super.setSize(d);
        setPreferredSize(d);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        setPreferredSize(new Dimension(width, height));
    }
}
