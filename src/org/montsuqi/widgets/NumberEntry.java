package org.montsuqi.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Keymap;
import javax.swing.text.PlainDocument;
import org.montsuqi.util.Logger;
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

	private String originalFormat;
	private NumberFormat format;
	private static Logger logger;

	private BigDecimal value;
	private int scale;
	private int expo;

	protected static final String DEFAULT_FORMAT = "ZZZZZZZZZ9"; //$NON-NLS-1$

	NumberDocument() {
		
		//setFormat(DEFAULT_FORMAT);
		setFormat("-ZZZ,ZZZ.99");
		expo = 0;
		scale = 0;
		value = new BigDecimal("0.0"); //$NON-NLS-1$
		logger = Logger.getLogger(NumberDocument.class);
	}
	
	synchronized void setValue(BigDecimal v) {
		if ( ! value.equals(v)) {
			PrecisionScale ps = new PrecisionScale(originalFormat);
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
			case 'Z':
				buf.append('#');
				break;
			case '9': case '-': case '+':
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
		for	(int i = 0; i < p.length; i++) {
			if (p[i] == '-') {
				minus = (minus) ? false : true;
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

	private static String formatValue(NumberFormat format, BigDecimal v) {
		return format.format(v);
	}
}
