package org.montsuqi.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.math.BigInteger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Keymap;
import javax.swing.text.PlainDocument;
import org.montsuqi.util.PrecisionScale;

public class NumberEntry extends JTextField {
	final protected Action clearAction;
	
	public NumberEntry(String text, int columns) {
		super(new NumberDocument(), text, columns);
		setHorizontalAlignment(JTextField.RIGHT);

		clearAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				setValue(new BigDecimal(BigInteger.ZERO));
			}
		};

		Keymap km = getKeymap();
		int clearKeys[] = new int[] 
			{
				KeyEvent.VK_BACK_SPACE,
				KeyEvent.VK_DELETE,
				KeyEvent.VK_CLEAR,
				KeyEvent.VK_HOME,
				KeyEvent.VK_END
			};

		for (int i = 0; i < clearKeys.length; i++) {
			KeyStroke stroke = KeyStroke.getKeyStroke(clearKeys[i], 0);
			km.addActionForKeyStroke(stroke, clearAction);
		}
		setKeymap(km);
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

	public BigDecimal getValue() {
		NumberDocument doc = (NumberDocument)getDocument();
		return doc.getValue();
	}

	public void setFormat(String format) {
		NumberDocument doc = (NumberDocument)getDocument();
		doc.setFormat(format);
	}

	public static void main(String[] args) {
		final JFrame f = new JFrame("TestNumberEntry"); //$NON-NLS-1$
		final NumberEntry ne = new NumberEntry();
		ne.setForeground(Color.red);
		ne.setFormat("----,---.99"); //$NON-NLS-1$
		ne.setValue(new BigDecimal("100.0")); //$NON-NLS-1$
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
}

class NumberDocument extends PlainDocument {
	String format;

	BigDecimal value;
	int scale;
	int expo;

	protected static final String DEFAULT_FORMAT = "ZZZZZZZZZ9"; //$NON-NLS-1$

	public NumberDocument() {
		format = DEFAULT_FORMAT;
		expo = 0;
		scale = 0;
		value = new BigDecimal("0.0"); //$NON-NLS-1$
	}
	
	public synchronized void setValue(BigDecimal v) {
		if ( ! value.equals(v)) {
			PrecisionScale ps = new PrecisionScale(format);
			String t = formatValue(format, v.setScale(ps.precision + 1, ps.scale));
			value = new BigDecimal(BigInteger.ZERO);
			try {
				insertString(0, t, null);
			} catch (BadLocationException e) {	
				e.printStackTrace();
			}
			expo = 0;
			scale = 0;
		}
	}

	public BigDecimal getValue() {
		PrecisionScale ps = new PrecisionScale(format);
		value = value.setScale(ps.precision, ps.scale);
		return value;
	}

	public void setFormat(String format) {
		this.format = (format == null) ? DEFAULT_FORMAT : format;
	}
	
	public String getFormat() {
		return format;
	}

	public void insertString(int offs, String str, AttributeSet a)
		throws BadLocationException { 
		if (str.length() <= 0) {
			return;
		}

		PrecisionScale ps = new PrecisionScale(format);
		boolean minus = value.signum() < 0;
		BigDecimal v = value.abs();
		char[] p = str.toCharArray();
		for	(int i = 0; i < p.length; i++) {
			if (p[i] == '-') {
				minus = (minus) ? false : true;
			} else if (p[i] == '.') {
				if (format.indexOf('.') >= 0) {
					scale = 1;
				}
			} else if (Character.isDigit(p[i])) {
				BigDecimal input = new BigDecimal(new BigInteger(String.valueOf(p[i])), 0);
				if (scale == 0) {
					if (expo < (ps.precision - ps.scale)) {
						v = v.movePointRight(1);
						v = v.add(input);
						expo++;
					}
				} else {
					if (scale <= ps.scale) {
						BigDecimal one = new BigDecimal(BigInteger.ONE);
						BigDecimal small = one.movePointLeft(scale);
						scale++;
						v = v.add(small.multiply(input));
					}
				}
			}
		}
		value = minus ? v.negate() : v;
		remove(0, getLength());
		super.insertString(0, formatValue(format, value), a);
	}

	protected static String formatValue(String format, BigDecimal v) {
		boolean fMinus, fMark;
		if (v.signum() < 0) {
			fMinus = true;
			v = v.negate();
		} else {
			fMinus = false;
		}

		int buflen;

		boolean fPSign, fNSign, fSup, fSmall;
		fPSign = false;
		fNSign = false;
		fSup = false;
		fSmall = false;
		char sup = ' ';
		char[] fs = format.toCharArray();
		int f;

		for (f = 0; f < fs.length; f++) {
			switch (fs[f]) {
			case '\\':
				sup = fs[f]; fSup = true; break;
			case 'Z':
				sup = ' ';   fSup = true; break;
			case '-':
				if (fNSign) { fSup = true; } fNSign = true; break;
			case '+':
				if (fPSign) { fSup = true; } fPSign = true;	break;
			case '.':
				fSmall = true; break;
			default:
				break;
			}
		}

		int len, p;
		for (len = 0, p = 0; p < fs.length && fs[p] != '.'; len++, p++)
			;

		char[] vs = v.toString().toCharArray();
		int q, qq;
		for (q = 0; q < vs.length && vs[q] != '.'; q++)
			;
		qq = q - 1;
		q++;

		char[] buf = new char[format.length()];

		for (int i = 0; i < buf.length; i++) {
			buf[i] = '0';
		}
		
		if (fSmall) { // when a '.' exists
			p = len;
			f = len;
			while (f < fs.length) {
				switch (fs[f]) {
				case '9':
					if (q < vs.length) {
						buf[p] = vs[q];
						q++;
					}
					break;
				default:
					buf[p] = fs[f];
					break;
				}
				f++;
				p++;
			}
			buflen = p;
			len--;
		} else { // otherwise
			buflen = p + 1;
		}
		
		p = len;
		f = len;

		if ( ! fSup) {
			for (/**/; f >= 0; f--, p--) {
				switch (fs[f]) {
				case '9':
					if (qq < 0) {
						buf[p] = '0';
					} else {
						buf[p] = vs[qq];
						qq --;
					}
					break;
				case '-':
					if (fMinus) {
						buf[p] = '-';
					} else {
						buf[p] = ' ';
					}
					break;
				case '+':
					if (fMinus) {
						buf[p] = '-';
					} else {
						buf[p] = '+';
					}
					break;
				default:
					buf[p] = fs[f];
					break;
				}
			}
		} else {
			fMark = false;
			for (/**/; f >= 0; f--, p--) {
				if (qq < 0 || (qq == 0 && vs[qq] == '0')) {
					switch (fs[f]) {
					case 'Z': case '-': case '+': case '\\': case ',':
						if (fNSign && fMinus) {
							fNSign = false;
							buf[p] = '-';
							if (fs[f] == '-') {
								fMark = true;
							}
						} else if (fPSign && ! fMinus) {
							fPSign = false;
							buf[p] = '+';
							if (fs[f] == '+') {
								fMark = true;
							}
						} else if ( ! fMark) {
							buf[p] = sup;
							fMark = true;
						} else {
							buf[p] = ' ';
						}
						break;
					case '9':
						buf[p] = '0';
						break;
					default:
						buf[p] = fs[f];
						break;
					}
				} else {
					switch (fs[f]) {
					case 'Z': case '\\': case '9': case '-': case '+':
						buf[p] = vs[qq];
						qq--;
						break;
					default:
						buf[p] = fs[f];
						break;
					}
				}
			}
		}
		return new String(buf, 0, buflen);
	}
}
