/*      PANDA -- a simple transaction monitor
                                                                                
Copyright (C) 1998-1999 Ogochan.
			  2000-2003 Ogochan & JMA (Japan Medical Association).
                                                                                
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
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import org.montsuqi.util.PrecisionScale;

public class NumberEntry extends JTextField {

	public NumberEntry(String text, int columns) {
		super(new NumberDocument(), text, columns);
		setHorizontalAlignment(SwingConstants.RIGHT);
	}


	public NumberEntry(int columns) {
		this(null, columns);
	}

	public NumberEntry(String text) {
		this(text, 0);
	}

	public NumberEntry() {
		this(null, 0);
	}	

	public void setValue(BigDecimal value) {
		NumberDocument doc = (NumberDocument)getDocument();
		doc.setValue(value);
		
	}

	public void setValue(double value) {
		setValue(new BigDecimal(value));	
	}

	public BigDecimal getValue() {
		NumberDocument doc = (NumberDocument)getDocument();
		return doc.getValue();
	}

	public void setFormat(String format) {
		NumberDocument doc = (NumberDocument)getDocument();
		doc.setFormat(format);
		setValue(getValue());
	}

	public static void main(String[] args) {
		final JFrame f = new JFrame("TestNumberEntry"); //$NON-NLS-1$
		final NumberEntry ne = new NumberEntry();
		ne.setForeground(Color.red);
		ne.setFormat("99.9"); //$NON-NLS-1$
		ne.setValue(NumberDocument.ZERO);
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(ne, BorderLayout.CENTER);
		f.setSize(200, 50);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		f.show();
	}

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

	protected static final String DEFAULT_FORMAT = "ZZZZZZZZZ9"; //$NON-NLS-1$
	static final BigDecimal ZERO = new BigDecimal(BigInteger.ZERO);
	static final BigDecimal ONE = new BigDecimal(BigInteger.ONE);

	NumberDocument() {
		setFormat(DEFAULT_FORMAT);
		expo = 0;
		scale = 0;
		value = ZERO;
	}
	
	synchronized void setValue(BigDecimal v) {
		if ( ! value.equals(v)) {
			PrecisionScale ps = new PrecisionScale(originalFormat);
			String t = formatValue(format, v.setScale(ps.precision + 1, ps.scale));
			value = ZERO;
			try {
				insertString(0, t, null);
			} catch (BadLocationException e) {	
				e.printStackTrace();
			}
			expo = 0;
			scale = 0;
		}
	}

	BigDecimal getValue() {
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
			case 'Z': case '-': case '+':
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
		}
		tmp.append(buf);
		if (negative) {
			tmp.append(';');
			tmp.append('-');
			tmp.append(buf);
		}
		return new DecimalFormat(tmp.toString());
	}

	String getFormat() {
		return originalFormat;
	}

	public void insertString(int offs, String str, AttributeSet a)
		throws BadLocationException {
		if (str.length() <= 0) {
			return;
		}

		PrecisionScale ps = new PrecisionScale(originalFormat);
		boolean minus = value.signum() < 0;
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
						if ( ! v.equals(ZERO)) {
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
		value = minus ? v.negate() : v;
		String formatted = formatValue(format, value);
		remove(0, getLength());
		// treat zero value representation specially
		if (formatted.equals("0") && leaveZeroAsBlank()) { //$NON-NLS-1$
			// do nothing
		} else {
			super.insertString(0, formatted, a);
		}
	}

	private boolean leaveZeroAsBlank() {
		int pos = originalFormat.indexOf('.');
		if (pos < 1) {
			pos = originalFormat.length();
		}
		char last = originalFormat.charAt(pos - 1);
		return last == 'Z' || last == '-' || last == '+';
	}

	private static String formatValue(NumberFormat format, BigDecimal v) {
		return format.format(v);
	}
}
