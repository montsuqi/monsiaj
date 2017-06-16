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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.im.InputContext;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.montsuqi.monsiaj.util.PrecisionScale;
import org.montsuqi.monsiaj.util.SystemEnvironment;

/**
 * <
 * p>
 * A class that simulates Gtk+'s NumberEntry.</p>
 */
public class NumberEntry extends Entry {

    public NumberEntry() {
        super();
        enableInputMethods(false);
        setDocument(new NumberDocument());
        setHorizontalAlignment(SwingConstants.RIGHT);
        initListeners();
    }

    private void initListeners() {
        addFocusListener(new FocusListener() {
            // NOTE only works in japanese environment.
            // See
            // <a href="http://java-house.jp/ml/archive/j-h-b/024510.html">JHB:24510</a>
            // <a href="http://java-house.jp/ml/archive/j-h-b/024682.html">JHB:24682</a>

            public void focusGained(FocusEvent e) {
                if (SystemEnvironment.isWindows()) {
                    InputContext ic = getInputContext();
                    if (ic != null) {
                        ic.setCharacterSubsets(null);
                        ic.endComposition();
                        ic.selectInputMethod(Locale.ENGLISH);
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
    }

    public void setValue(BigDecimal value) {
        NumberDocument doc = (NumberDocument) getDocument();
        doc.setValue(value);
    }

    public void setValue(double value) {
        setValue(new BigDecimal(value));
    }

    public void setValue(int value) {
        setValue(new BigDecimal(value));
    }

    public BigDecimal getValue() {
        NumberDocument doc = (NumberDocument) getDocument();
        return doc.getValue();
    }

    public double getValueDouble() {
        NumberDocument doc = (NumberDocument) getDocument();
        return doc.getValue().doubleValue();
    }

    public void setFormat(String format) {
        NumberDocument doc = (NumberDocument) getDocument();
        doc.setFormat(format);
        setValue(getValue());
    }

    public static void main(String[] args) {
        final JFrame f = new JFrame("TestNumberEntry"); 
        final NumberEntry ne = new NumberEntry();
        ne.setFormat("-----"); 
        ne.setValue(new BigDecimal(0));
        f.getContentPane().setLayout(new BorderLayout());
        f.getContentPane().add(ne, BorderLayout.CENTER);
        JButton b = new JButton("show");
        b.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                System.out.println("[" + ne.getValue() + "]");
            }
        });
        f.getContentPane().add(b, BorderLayout.SOUTH);
        f.setSize(200, 120);
        f.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        f.setVisible(true);
    }

    @Override
    protected void processKeyEvent(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_BACK_SPACE: // fall through
            case KeyEvent.VK_DELETE: // fall through
            case KeyEvent.VK_CLEAR: // fall through
            case KeyEvent.VK_HOME: // fall through
            case KeyEvent.VK_END:
                if (e.getID() == KeyEvent.KEY_PRESSED || e.getID() == KeyEvent.KEY_RELEASED) {
                setValue(NumberDocument.ZERO);
            }
                e.consume();
                break;
            default:
                super.processKeyEvent(e);
                break;
        }
    }
}

class NumberDocument extends PlainDocument {

    private String originalFormat;
    private NumberFormat format;
    private BigDecimal value;
    private int scale;
    private int expo;
    private boolean minus = false;
    protected static final String DEFAULT_FORMAT = "ZZZZZZZZZ9"; 
    static final BigDecimal ZERO = new BigDecimal(BigInteger.ZERO);
    static final BigDecimal ONE = new BigDecimal(BigInteger.ONE);
    protected static final Logger logger = LogManager.getLogger(NumberDocument.class);

    NumberDocument() {
        setFormat(DEFAULT_FORMAT);
        expo = 0;
        scale = 0;
        value = ZERO;
        minus = value.signum() < 0;
    }

    synchronized void setValue(BigDecimal v) {
        PrecisionScale ps = new PrecisionScale(originalFormat);
        String t = formatValue(v.setScale(ps.precision + 1, ps.scale));
        value = ZERO;
        minus = false;
        try {
            expo = 0;
            scale = 0;
            insertString(0, t, null);
        } catch (BadLocationException e) {
            logger.warn(e);
        }
        expo = 0;
        scale = 0;
    }

    synchronized BigDecimal getValue() {
        PrecisionScale ps = new PrecisionScale(originalFormat);
        value = value.setScale(ps.precision, ps.scale);
        return value;
    }

    void setFormat(String format) {
        this.originalFormat = (format == null) ? DEFAULT_FORMAT : format;
        this.format = translateFormat(originalFormat);
    }

    private static NumberFormat translateFormat(String originalFormat) {
        char[] chars = originalFormat.toCharArray();
        StringBuffer buf = new StringBuffer(originalFormat.length());
        int i = 0;
        boolean negative = false;
        boolean positive = false;
        switch (chars[i]) {
            case '-':
                negative = true;
                i++;
                break;
            case '+':
                positive = true;
                i++;
                break;
        }
        for (/**/; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case 'Z':
                case '-':
                case '+':
                    buf.append('#');
                    break;
                case '9':
                    buf.append('0');
                    break;
                default:
                    buf.append(c);
                    break;
            }
        }
        StringBuffer tmp = new StringBuffer();
        if (positive) {
            tmp.append('+');
        } else {
            tmp.append('#');
        }
        tmp.append(buf);
        if (negative || positive) {
            tmp.append(';');
            tmp.append('-');
            tmp.append(buf);
        }
        return new DecimalFormat(tmp.toString());
    }

    String getFormat() {
        return originalFormat;
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        if (str.length() <= 0) {
            return;
        }

        PrecisionScale ps = new PrecisionScale(originalFormat);
        BigDecimal v = value.abs();
        char[] p = str.toCharArray();
        for (int i = 0; i < p.length; i++) {
            if (p[i] == '-') {
                minus = minus ? false : true;
            } else if (p[i] == '.') {
                if (originalFormat.indexOf('.') >= 0) {
                    scale = 1;
                }
            } else if (Character.isDigit(p[i])) {
                BigDecimal input = new BigDecimal(new BigInteger(String.valueOf(p[i])), 0);
                if (scale == 0) {
                    if (expo < (ps.precision - ps.scale)) {
                        v = v.movePointRight(1);
                        v = v.add(input);
                        if (!v.equals(ZERO)) {
                            expo++;
                        }
                    }
                } else {
                    if (scale <= ps.scale) {
                        BigDecimal small = ONE.movePointLeft(scale);
                        scale++;
                        v = v.add(small.multiply(input));
                    }
                }
            }
        }
        if (v.movePointRight(v.scale()).equals(ZERO)) {
            v = ZERO;
        }
        if (minus && wantsSign()) {
            value = v.negate();
        } else {
            value = v;
        }
        String formatted = formatValue(value);
        remove(0, getLength());
        // treat zero value representation specially
        if ((formatted.trim().equals("0") || formatted.trim().equals("+0")) && leaveZeroAsBlank()) { 
            // do nothing
        } else {
            super.insertString(0, formatted, a);
        }
    }

    /**
     * <
     * p>
     * Test if zero value should be printed or left blank.</p>
     *
     * @return true if zero should be blank. false if zero should be printed.
     */
    private boolean leaveZeroAsBlank() {
        int pos = originalFormat.indexOf('.');
        if (pos < 1) {
            pos = originalFormat.length();
        }
        char last = originalFormat.charAt(pos - 1);
        return last == 'Z' || last == '-' || last == '+';
    }

    private String formatValue(BigDecimal v) {
        if (v.signum() < 0 && !wantsSign()) {
            return format.format(v.negate());
        } else {
            return format.format(v);
        }
    }

    private boolean wantsSign() {
        return originalFormat.startsWith("-") || originalFormat.startsWith("+");
    }
}
