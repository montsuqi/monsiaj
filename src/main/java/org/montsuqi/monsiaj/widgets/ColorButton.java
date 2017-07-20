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
import javax.swing.*;
import org.montsuqi.monsiaj.util.SafeColorDecoder;

/**
 * <p>A class that simulates Gtk+'s ColorButton.</p>
 *
 */
public class ColorButton extends JButton {

    private class ColorIcon implements Icon {

        public ColorIcon(int w, int h) {
        }

        public int getIconHeight() {
            return ColorButton.this.getBounds().height;
        }

        public int getIconWidth() {
            return ColorButton.this.getBounds().width;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Rectangle rect = ColorButton.this.getBounds();
            int xx = (int) (rect.width * 0.2);
            int yy = (int) (rect.height * 0.2);
            int w = (int) (rect.width * 0.6);
            int h = (int) (rect.height * 0.6);
            Graphics2D g2 = (Graphics2D) g;
            g2.translate(xx, yy);
            g2.setColor(color);
            g2.fillRect(0, 0, w, h);
            g2.translate(-xx, -yy);
        }
    }
    private Color color;
    private ColorIcon icon;

    public ColorButton() {
        this.color = Color.RED;
        this.icon = new ColorIcon(64, 64);
        this.setAction(new ColorChooseAction());
        this.setIcon(icon);
    }

    public String getColorStr() {
        if (color != null) {
            return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
        } else {
            return String.format("#%02X%02X%02X", 0,0,0);
        }
    }

    private class ColorChooseAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            Color color;
            color = JColorChooser.showDialog(null, Messages.getString("ColorButton.dialog_title"), ColorButton.this.color);
            if (color != null) {
                ColorButton.this.color = color;
            }
            ColorButton.this.validate();
        }
    }

    public void setColorStr(String colorStr) {
        this.color = SafeColorDecoder.decode(colorStr);
        this.validate();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("ColorButton");
        Container container = frame.getContentPane();
        container.setLayout(new BorderLayout(10, 5));

        final ColorButton cb = new ColorButton();
        container.add(cb, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        container.add(buttonPanel, BorderLayout.SOUTH);

        JButton button3 = new JButton(new AbstractAction("output") {

            public void actionPerformed(ActionEvent ev) {
                System.out.println(cb.getColorStr());
            }
        });

        buttonPanel.add(button3);

        cb.setColorStr("");

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setSize(200, 100);
        frame.setVisible(true);
    }
}
