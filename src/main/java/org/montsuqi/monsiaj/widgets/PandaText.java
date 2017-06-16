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

import java.awt.GridLayout;
import java.awt.event.*;
import java.awt.im.InputContext;
import java.awt.im.InputSubset;
import java.util.Locale;
import javax.swing.*;
import org.montsuqi.monsiaj.util.SystemEnvironment;

/** <p>A class that simulates GtkPandaButton widget.</p>
 */
public class PandaText extends JTextArea {

    boolean ximEnabled;

    public PandaText() {
        super();
        initListeners();
    }

    private void initListeners() {

        // If ximEnabled is true, enable/disable Japanese input on focus gained/lost.
        addFocusListener(new FocusListener() {
            // NOTE only works in japanese environment.
            // See
            // <a href="http://java-house.jp/ml/archive/j-h-b/024510.html">JHB:24510</a>
            // <a href="http://java-house.jp/ml/archive/j-h-b/024682.html">JHB:24682</a>

            @Override
            public void focusGained(FocusEvent e) {
                if (SystemEnvironment.isWindows()) {
                    if (ximEnabled) {
                        InputContext ic = getInputContext();
                        if (ic != null) {
                            ic.setCharacterSubsets(new Character.Subset[]{InputSubset.KANJI});
                            ic.selectInputMethod(Locale.JAPANESE);
                        }
                    } else {
                        InputContext ic = getInputContext();
                        if (ic != null) {
                            ic.setCharacterSubsets(null);
                            ic.endComposition();
                            ic.selectInputMethod(Locale.ENGLISH);
                        }
                    }
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (SystemEnvironment.isWindows()) {
                    InputContext ic = getInputContext();
                    if (ic != null) {
                        ic.setCharacterSubsets(null);
                        ic.endComposition();
                        ic.selectInputMethod(Locale.ENGLISH);
                    }
                }
            }
        });
    }


    /** <p>Sets xim enabled.</p>
     * @param enabled
     */
    public void setXIMEnabled(boolean enabled) {
        ximEnabled = enabled;
    }

    /** <p>Gets xim enabled.</p>
     * @return 
     */
    public boolean getXIMEnabled() {
        return ximEnabled;
    }

    public static void main(String[] args) {
        final JFrame f = new JFrame("TestPandaText"); 

        PandaText pe = new PandaText();
        pe.setXIMEnabled(true);

        PandaText pe2 = new PandaText();
        pe2.setXIMEnabled(false);

        f.getContentPane().setLayout(new GridLayout(2, 1));
        f.getContentPane().add(pe);
        f.getContentPane().add(pe2);
        f.setSize(300, 400);
        f.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        f.setVisible(true);
    }
}