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
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.*;
import java.awt.im.InputContext;
import java.awt.im.InputSubset;
import java.util.Locale;
import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.montsuqi.monsiaj.util.SystemEnvironment;

/** <p>A class that simulates GtkPandaButton widget.</p>
 */
public class PandaEntry extends Entry {

    final class InsertEnterAsText extends AbstractAction {

        public void actionPerformed(ActionEvent evt) {
            Object source = evt.getSource();
            if (source instanceof Entry) {
                Entry entry = (Entry) source;
                String text = entry.getText();
                if (text.endsWith("n")) { 
                    Document doc = entry.getDocument();
                    try {
                        doc.insertString(doc.getLength(), "\n", null); 
                    } catch (BadLocationException e) {
                        throw new RuntimeException(e);
                    }
                }
                entry.postActionEvent();
            }
        }
    }
    boolean ximEnabled;
    public static final int KANA = PandaDocument.KANA;
    public static final int XIM = PandaDocument.XIM;
    public static final int ASCII = PandaDocument.ASCII;

    public PandaEntry() {
        super();
        setDocument(new PandaDocument());
        initListeners();
    }

    public PandaEntry(String value, int n) {
        super();
        setDocument(new PandaDocument());
        setText(value);
        setColumns(n);
        initListeners();
    }

    private void initListeners() {

        // If ximEnabled is true, enable/disable Japanese input on focus gained/lost.
        addFocusListener(new FocusListener() {
            // NOTE only works in japanese environment.
            // See
            // <a href="http://java-house.jp/ml/archive/j-h-b/024510.html">JHB:24510</a>
            // <a href="http://java-house.jp/ml/archive/j-h-b/024682.html">JHB:24682</a>

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

        // When this component hides a button under itself, propagete mouse click to the button.
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                Component source = (Component) e.getSource();
                Point p = e.getPoint();
                Container parent = source.getParent();
                for (int i = 0, n = parent.getComponentCount(); i < n; i++) {
                    Component c = parent.getComponent(i);
                    if (c instanceof AbstractButton && c.contains(SwingUtilities.convertPoint(source, p, c))) {
                        AbstractButton button = (AbstractButton) c;
                        button.setVisible(false);
                        button.doClick();
                    }
                }
            }
        });
    }

    /** <p>Sets the input mode to KANA, XIM or ASCII.</p>
     *
     * @param mode one of PandaEntry.KANA, PandaEntry.XIM or PandaEntry.ASCII.
     */
    public void setInputMode(int mode) {
        PandaDocument doc = (PandaDocument) getDocument();
        doc.setInputMode(mode);
        setEnterMode();
    }

    /** <p>When the input mode == KANA and xim is not enabled,
     * treat Enter as text commit key. Otherwise treat it as action trigger.</p>
     */
    private void setEnterMode() {
        final KeyStroke pressEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        final InputMap inputMap = getInputMap();
        if (getInputMode() == KANA && !ximEnabled) {
            inputMap.put(pressEnter, new InsertEnterAsText());
        } else {
            inputMap.put(pressEnter, getActionMap().get(notifyAction));
        }
    }

    /** <p>Gets current input mode.</p>
     */
    public int getInputMode() {
        PandaDocument doc = (PandaDocument) getDocument();
        return doc.getInputMode();
    }

    /** <p>Sets xim enabled.</p>
     */
    public void setXIMEnabled(boolean enabled) {
        ximEnabled = enabled;
        setEnterMode();
    }

    /** <p>Gets xim enabled.</p>
     */
    public boolean getXIMEnabled() {
        return ximEnabled;
    }

    public static void main(String[] args) {
        final JFrame f = new JFrame("TestPandaEntry"); 
        java.awt.event.ActionListener al = new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent ev) {
                System.out.println("evt"); 
            }
        };
        PandaEntry pe = new PandaEntry();
        pe.setInputMode(XIM);
        pe.setXIMEnabled(true);
        pe.addActionListener(al);

        PandaEntry pe2 = new PandaEntry();
        pe2.setInputMode(KANA);
        pe2.setXIMEnabled(false);
        pe2.addActionListener(al);

        f.getContentPane().setLayout(new GridLayout(2, 1));
        f.getContentPane().add(pe);
        f.getContentPane().add(pe2);
        f.setSize(200, 100);
        f.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        f.setVisible(true);
    }
}

class PandaDocument extends LengthLimitableDocument {

    public static final int KANA = 1;
    public static final int XIM = 2;
    public static final int ASCII = 3;
    private int mode;
    private static final char[] SYMBOL_TABLE = {
        '\u3000', '\uff01', '\u201d', '\uff03', '\uff04', '\uff05', '\uff06', '\u2019',
        '\uff08', '\uff09', '\uff0a', '\uff0b', '\uff0c', '\u30fc', '\uff0e', '\uff0f',
        '\uff10', '\uff11', '\uff12', '\uff13', '\uff14', '\uff15', '\uff16', '\uff17',
        '\uff18', '\uff19', '\uff1a', '\uff1b', '\uff1c', '\uff1d', '\uff1e', '\uff1f',
        '\uff20', '\uff21', '\uff22', '\uff23', '\uff24', '\uff25', '\uff26', '\uff27',
        '\uff28', '\uff29', '\uff2a', '\uff2b', '\uff2c', '\uff2d', '\uff2e', '\uff2f',
        '\uff30', '\uff31', '\uff32', '\uff33', '\uff34', '\uff35', '\uff36', '\uff37',
        '\uff38', '\uff39', '\uff3a', '\uff3b', '\uffe5', '\uff3d', '\uff3e', '\uff3f',
        '\u2018', '\u30a2', 0, 0, 0, '\u30a8', 0, 0,
        0, '\u30a4', 0, 0, 0, 0, 0, '\u30aa',
        0, 0, 0, 0, 0, '\u30a6', 0, 0,
        0, 0, 0, '\uff5b', '\uff5c', '\uff5d', '\uffe3', 0,};
    private static final RuleEntry[] KANA_TABLE = {
        new RuleEntry("l", "\u30a1", "\u30a3", "\u30a5", "\u30a7", "\u30a9"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("x", "\u30a1", "\u30a3", "\u30a5", "\u30a7", "\u30a9"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("k", "\u30ab", "\u30ad", "\u30af", "\u30b1", "\u30b3"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("ky", "\u30ad\u30e3", "\u30ad\u30a3", "\u30ad\u30e5", "\u30ad\u30a7", "\u30ad\u30e7"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("g", "\u30ac", "\u30ae", "\u30b0", "\u30b2", "\u30b4"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("gy", "\u30ae\u30e3", "\u30ae\u30a3", "\u30ae\u30e5", "\u30ae\u30a7", "\u30ae\u30e7"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("s", "\u30b5", "\u30b7", "\u30b9", "\u30bb", "\u30bd"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("sh", "\u30b7\u30e3", "\u30b7", "\u30b7\u30e5", "\u30b7\u30a7", "\u30b7\u30e7"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("sy", "\u30b7\u30e3", "\u30b7\u30a3", "\u30b7\u30e5", "\u30b7\u30a7", "\u30b7\u30e7"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("z", "\u30b6", "\u30b8", "\u30ba", "\u30bc", "\u30be"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("j", "\u30b8\u30e3", "\u30b8", "\u30b8\u30e5", "\u30b8\u30a7", "\u30b8\u30e7"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("jy", "\u30b8\u30e3", "\u30b8\u30a3", "\u30b8\u30e5", "\u30b8\u30a7", "\u30b8\u30e7"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("zy", "\u30b8\u30e3", "\u30b8\u30a3", "\u30b8\u30e5", "\u30b8\u30a7", "\u30b8\u30e7"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("t", "\u30bf", "\u30c1", "\u30c4", "\u30c6", "\u30c8"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("ts", null, null, "\u30c4", null, null),  //$NON-NLS-2$
        new RuleEntry("lt", null, null, "\u30c3", null, null),  //$NON-NLS-2$
        new RuleEntry("xt", null, null, "\u30c3", null, null),  //$NON-NLS-2$
        new RuleEntry("ty", "\u30c1\u30e3", "\u30c1\u30a3", "\u30c1\u30e5", "\u30c1\u30a7", "\u30c1\u30e7"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("cy", "\u30c1\u30e3", "\u30c1\u30a3", "\u30c1\u30e5", "\u30c1\u30a7", "\u30c1\u30e7"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("ch", "\u30c1\u30e3", "\u30c1", "\u30c1\u30e5", "\u30c1\u30a7", "\u30c1\u30e7"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("th", "\u30c6\u30e3", "\u30c6\u30a3", "\u30c6\u30e5", "\u30c6\u30a7", "\u30c6\u30e7"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("d", "\u30c0", "\u30c2", "\u30c5", "\u30c7", "\u30c9"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("dy", "\u30c2\u30e3", "\u30c2\u30a3", "\u30c2\u30e5", "\u30c2\u30a7", "\u30c2\u30e7"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("dh", "\u30c7\u30e3", "\u30c7\u30a3", "\u30c7\u30e5", "\u30c7\u30a7", "\u30c7\u30e7"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("dw", "\u30c9\u30a1", "\u30c9\u30a3", "\u30c9\u30a5", "\u30c9\u30a7", "\u30c9\u30a9"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("n", "\u30ca", "\u30cb", "\u30cc", "\u30cd", "\u30ce"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("ny", "\u30cb\u30e3", "\u30cb\u30a3", "\u30cb\u30e5", "\u30cb\u30a7", "\u30cb\u30e7"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("h", "\u30cf", "\u30d2", "\u30d5", "\u30d8", "\u30db"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("f", "\u30d5\u30a1", "\u30d5\u30a3", "\u30d5", "\u30d5\u30a7", "\u30d5\u30a9"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("hy", "\u30d2\u30e3", "\u30d2\u30a3", "\u30d2\u30e5", "\u30d2\u30a7", "\u30d2\u30e7"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("fy", "\u30d5\u30e3", "\u30d5\u30a3", "\u30d5\u30e5", "\u30d5\u30a7", "\u30d5\u30e7"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("b", "\u30d0", "\u30d3", "\u30d6", "\u30d9", "\u30dc"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("by", "\u30d3\u30e3", "\u30d3\u30a3", "\u30d3\u30e5", "\u30d3\u30a7", "\u30d3\u30e7"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("p", "\u30d1", "\u30d4", "\u30d7", "\u30da", "\u30dd"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("py", "\u30d4\u30e3", "\u30d4\u30a3", "\u30d4\u30e5", "\u30d4\u30a7", "\u30d4\u30e7"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("m", "\u30de", "\u30df", "\u30e0", "\u30e1", "\u30e2"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("my", "\u30df\u30e3", "\u30df\u30a3", "\u30df\u30e5", "\u30df\u30a7", "\u30df\u30e7"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("y", "\u30e4", "\u30a4", "\u30e6", "\u30a4\u30a7", "\u30e8"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("ly", "\u30e3", null, "\u30e5", null, "\u30e7"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        new RuleEntry("xy", "\u30e3", null, "\u30e5", null, "\u30e7"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        new RuleEntry("r", "\u30e9", "\u30ea", "\u30eb", "\u30ec", "\u30ed"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("ry", "\u30ea\u30e3", "\u30ea\u30a3", "\u30ea\u30e5", "\u30ea\u30a7", "\u30ea\u30e7"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("w", "\u30ef", "\u30f0", "\u30a6", "\u30f1", "\u30f2"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("wh", "\u30ef", "\u30a6\u30a3", "\u30a6", "\u30a6\u30a7", "\u30a6\u30a9"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("lw", "\u30ee", null, null, null, null),  //$NON-NLS-2$
        new RuleEntry("xw", "\u30ee", null, null, null, null),  //$NON-NLS-2$
        new RuleEntry("v", "\u30f4\u30a1", "\u30f4\u30a3", "\u30f4", "\u30f4\u30a7", "\u30f4\u30a9"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        new RuleEntry("q", "\u30af\u30a1", "\u30af\u30a3", "\u30af", "\u30af\u30a7", "\u30af\u30a9"),  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    };
    private static final String LOW_ALPHABETS = "abcdefghijklmnopqrstuvwxyz"; 
    private static final String AEIOU = "aeiou"; 

    PandaDocument() {
        mode = ASCII;
    }

    void setInputMode(int mode) {
        if (mode != KANA && mode != XIM && mode != ASCII) {
            throw new IllegalArgumentException();
        }
        this.mode = mode;
    }

    int getInputMode() {
        return mode;
    }

    public void insertString(int offs, String str, AttributeSet a)
            throws BadLocationException {

        // Handle kana input
        if (mode == KANA) {
            // Do nothing if there is no input
            int length = str.length();
            if (length < 1) {
                return;
            }

            // Just insert for non-keyboard input
            if (length > 1) {
                super.insertString(offs, str, a);
                return;
            }

            assert length == 1;

            // Find the prefix
            int prefixStart;
            int prefixEnd = offs;

            for (prefixStart = prefixEnd; prefixStart > 0; prefixStart--) {
                String s = getText(prefixStart - 1, 1);
                if (LOW_ALPHABETS.indexOf(s.charAt(0)) < 0) {
                    break;
                }
            }

            String prefix = getText(prefixStart, prefixEnd - prefixStart);
            char thisChar = str.charAt(0);
            if (prefix.length() == 0) {
                // single char
                if (thisChar == '\n') {
                    // ignore plain ENTER
                    return;
                } else {
                    char c = getSymbol(thisChar);
                    if (c != 0) {
                        str = String.valueOf(c);
                    } else {
                        super.insertString(offs, str, a);
                        return;
                    }
                }
            } else if (AEIOU.indexOf(thisChar) >= 0) {
                // \u30ab\u30ca
                String s = getKana(prefix, thisChar);
                if (s != null) {
                    str = s;
                } else {
                    super.insertString(offs, str, a);
                    return;
                }
            } else if (prefix.charAt(0) == 'n' && thisChar != 'y') {
                // n -> \u30f3
                str = "\u30f3"; 
                if (thisChar != 'n' && thisChar != '\n' && thisChar != '\'') {
                    str += thisChar;
                }
            } else if (thisChar == prefix.charAt(0)) {
                // xx -> \u30c3
                str = "\u30c3" + thisChar; 
            } else if (thisChar == '\n') {
                // ignore ENTER after prefix
                return;
            } else {
                super.insertString(offs, str, a);
                return;
            }

            super.remove(prefixStart, prefixEnd - prefixStart);
            offs = prefixStart;
        }
        super.insertString(offs, str, a);
    }

    private static char getSymbol(int key) {
        return 0x20 <= key && key <= 0x80 ? SYMBOL_TABLE[key - 0x20] : 0;
    }

    private static String getKana(String prefix, char key) {
        for (int i = 0; i < KANA_TABLE.length; i++) {
            if (prefix.equals(KANA_TABLE[i].prefix)) {
                switch (key) {
                    case 'a':
                        return KANA_TABLE[i].a;
                    case 'i':
                        return KANA_TABLE[i].i;
                    case 'u':
                        return KANA_TABLE[i].u;
                    case 'e':
                        return KANA_TABLE[i].e;
                    case 'o':
                        return KANA_TABLE[i].o;
                    default:
                        return null;
                }
            }
        }
        return null;
    }
}

class RuleEntry {

    final String prefix;
    final String a, i, u, e, o;

    RuleEntry(String prefix, String a, String i, String u, String e, String o) {
        this.prefix = prefix;
        this.a = a;
        this.i = i;
        this.u = u;
        this.e = e;
        this.o = o;
    }
}
