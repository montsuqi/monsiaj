package org.montsuqi.widgets;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class PandaEntry extends JTextField {
	public PandaEntry(String text, int columns) {
		super(new PandaDocument(), text, columns);
	}
	public PandaEntry(int columns) {
		this(null, columns);
	}
	public PandaEntry(String text) {
		this(text, 0);
	}
	public PandaEntry() {
		this(null, 0);
	}	

	public void setInputMode(int mode) {
		PandaDocument doc = (PandaDocument)getDocument();
		doc.setInputMode(mode);
	}

	public int getInputMode() {
		PandaDocument doc = (PandaDocument)getDocument();
		return doc.getInputMode();
	}

	public static void main(String[] args) {
		final JFrame f = new JFrame("TestPandaEntry");
		PandaEntry pe = new PandaEntry();
		pe.setInputMode(PandaDocument.KANA);
		System.out.println(pe.getInputMode());
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(pe, BorderLayout.CENTER);
		f.setSize(200, 50);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		f.show();
	}
}

class PandaDocument extends PlainDocument {
	public static final int KANA = 1;
	public static final int XIM = 2;
	public static final int ASCII = 3;

	protected int mode;

	static final char[] SYMBOL_TABLE = {
		'\u3000', '\uff01', '\u201d', '\uff03', '\uff04', '\uff05', '\uff06', '\u2019',
		'\uff08', '\uff09', '\uff0a', '\uff0b', '\uff0c', '\u30fc', '\uff0e', '\uff0f',
		'\uff10', '\uff11', '\uff12', '\uff13', '\uff14', '\uff15', '\uff16', '\uff17',
		'\uff18', '\uff19', '\uff1a', '\uff1b', '\uff1c', '\uff1d', '\uff1e', '\uff1f',
		'\uff20', '\uff21', '\uff22', '\uff23', '\uff24', '\uff25', '\uff26', '\uff27',
		'\uff28', '\uff29', '\uff2a', '\uff2b', '\uff2c', '\uff2d', '\uff2e', '\uff2f',
		'\uff30', '\uff31', '\uff32', '\uff33', '\uff34', '\uff35', '\uff36', '\uff37',
		'\uff38', '\uff39', '\uff3a', '\uff3b', '\uffe5', '\uff3d', '\uff3e', '\uff3f',
		'\u2018', '\u30a2',    0,    0,    0, '\u30a8',    0,    0,
		   0, '\u30a4',    0,    0,    0,    0,    0, '\u30aa',
		   0,    0,    0,    0,    0, '\u30a6',    0,    0,
		   0,    0,    0, '\uff5b', '\uff5c', '\uff5d', '\uffe3',    0
	};

	static final RuleEntry[] KANA_TABLE = {
		new RuleEntry("l",  "\u30a1", "\u30a3", "\u30a5", "\u30a7", "\u30a9"),
		new RuleEntry("x",  "\u30a1", "\u30a3", "\u30a5", "\u30a7", "\u30a9"),
		new RuleEntry("k",  "\u30ab", "\u30ad", "\u30af", "\u30b1", "\u30b3"),
		new RuleEntry("ky", "\u30ad\u30e3", "\u30ad\u30a3", "\u30ad\u30e5", "\u30ad\u30a7", "\u30ad\u30e7"),
		new RuleEntry("g",  "\u30ac", "\u30ae", "\u30b0", "\u30b2", "\u30b4"),
		new RuleEntry("gy", "\u30ae\u30e3", "\u30ae\u30a3", "\u30ae\u30e5", "\u30ae\u30a7", "\u30ae\u30e7"),
		new RuleEntry("s",  "\u30b5", "\u30b7", "\u30b9", "\u30bb", "\u30bd"),
		new RuleEntry("sh", "\u30b7\u30e3", "\u30b7", "\u30b7\u30e5", "\u30b7\u30a7", "\u30b7\u30e7"),
		new RuleEntry("sy", "\u30b7\u30e3", "\u30b7\u30a3", "\u30b7\u30e5", "\u30b7\u30a7", "\u30b7\u30e7"),
		new RuleEntry("z",  "\u30b6", "\u30b8", "\u30ba", "\u30bc", "\u30be"),
		new RuleEntry("j",  "\u30b8\u30e3", "\u30b8", "\u30b8\u30e5", "\u30b8\u30a7", "\u30b8\u30e7"),
		new RuleEntry("jy", "\u30b8\u30e3", "\u30b8\u30a3", "\u30b8\u30e5", "\u30b8\u30a7", "\u30b8\u30e7"),
		new RuleEntry("zy", "\u30b8\u30e3", "\u30b8\u30a3", "\u30b8\u30e5", "\u30b8\u30a7", "\u30b8\u30e7"),
		new RuleEntry("t",  "\u30bf", "\u30c1", "\u30c4", "\u30c6", "\u30c8"),
		new RuleEntry("ts", null, null, "\u30c4", null, null),
		new RuleEntry("lt", null, null, "\u30c3", null, null),
		new RuleEntry("xt", null, null, "\u30c3", null, null),
		new RuleEntry("ty", "\u30c1\u30e3", "\u30c1\u30a3", "\u30c1\u30e5", "\u30c1\u30a7", "\u30c1\u30e7"),
		new RuleEntry("cy", "\u30c1\u30e3", "\u30c1\u30a3", "\u30c1\u30e5", "\u30c1\u30a7", "\u30c1\u30e7"),
		new RuleEntry("ch", "\u30c1\u30e3", "\u30c1", "\u30c1\u30e5", "\u30c1\u30a7", "\u30c1\u30e7"),
		new RuleEntry("th", "\u30c6\u30e3", "\u30c6\u30a3", "\u30c6\u30e5", "\u30c6\u30a7", "\u30c6\u30e7"),
		new RuleEntry("d",  "\u30c0", "\u30c2", "\u30c5", "\u30c7", "\u30c9"),
		new RuleEntry("dy", "\u30c2\u30e3", "\u30c2\u30a3", "\u30c2\u30e5", "\u30c2\u30a7", "\u30c2\u30e7"),
		new RuleEntry("dh", "\u30c7\u30e3", "\u30c7\u30a3", "\u30c7\u30e5", "\u30c7\u30a7", "\u30c7\u30e7"),
		new RuleEntry("dw", "\u30c9\u30a1", "\u30c9\u30a3", "\u30c9\u30a5", "\u30c9\u30a7", "\u30c9\u30a9"),
		new RuleEntry("n",  "\u30ca", "\u30cb", "\u30cc", "\u30cd", "\u30ce"),
		new RuleEntry("ny", "\u30cb\u30e3", "\u30cb\u30a3", "\u30cb\u30e5", "\u30cb\u30a7", "\u30cb\u30e7"),
		new RuleEntry("h",  "\u30cf", "\u30d2", "\u30d5", "\u30d8", "\u30db"),
		new RuleEntry("f",  "\u30d5\u30a1", "\u30d5\u30a3", "\u30d5", "\u30d5\u30a7", "\u30d5\u30a9"),
		new RuleEntry("hy", "\u30d2\u30e3", "\u30d2\u30a3", "\u30d2\u30e5", "\u30d2\u30a7", "\u30d2\u30e7"),
		new RuleEntry("fy", "\u30d5\u30e3", "\u30d5\u30a3", "\u30d5\u30e5", "\u30d5\u30a7", "\u30d5\u30e7"),
		new RuleEntry("b",  "\u30d0", "\u30d3", "\u30d6", "\u30d9", "\u30dc"),
		new RuleEntry("by", "\u30d3\u30e3", "\u30d3\u30a3", "\u30d3\u30e5", "\u30d3\u30a7", "\u30d3\u30e7"),
		new RuleEntry("p",  "\u30d1", "\u30d4", "\u30d7", "\u30da", "\u30dd"),
		new RuleEntry("py", "\u30d4\u30e3", "\u30d4\u30a3", "\u30d4\u30e5", "\u30d4\u30a7", "\u30d4\u30e7"),
		new RuleEntry("m",  "\u30de", "\u30df", "\u30e0", "\u30e1", "\u30e2"),
		new RuleEntry("my", "\u30df\u30e3", "\u30df\u30a3", "\u30df\u30e5", "\u30df\u30a7", "\u30df\u30e7"),
		new RuleEntry("y",  "\u30e4", "\u30a4", "\u30e6", "\u30a4\u30a7", "\u30e8"),
		new RuleEntry("ly", "\u30e3", null, "\u30e5", null, "\u30e7"),
		new RuleEntry("xy", "\u30e3", null, "\u30e5", null, "\u30e7"),
		new RuleEntry("r",  "\u30e9", "\u30ea", "\u30eb", "\u30ec", "\u30ed"),
		new RuleEntry("ry", "\u30ea\u30e3", "\u30ea\u30a3", "\u30ea\u30e5", "\u30ea\u30a7", "\u30ea\u30e7"),
		new RuleEntry("w",  "\u30ef", "\u30f0", "\u30a6", "\u30f1", "\u30f2"),
		new RuleEntry("wh", "\u30ef", "\u30a6\u30a3", "\u30a6", "\u30a6\u30a7", "\u30a6\u30a9"),
		new RuleEntry("lw", "\u30ee", null, null, null, null),
		new RuleEntry("xw", "\u30ee", null, null, null, null),
		new RuleEntry("v",  "\u30f4\u30a1", "\u30f4\u30a3", "\u30f4", "\u30f4\u30a7", "\u30f4\u30a9"),
		new RuleEntry("q",  "\u30af\u30a1", "\u30af\u30a3", "\u30af", "\u30af\u30a7", "\u30af\u30a9"),
	};

	protected static final String LOW_ALPHABETS = "abcdefghijklmnopqrstuvwxyz";
	protected static final String AEIOU = "aeiou";

	PandaDocument() {
		mode = ASCII;
	}

	public void setInputMode(int mode) {
		if (mode != KANA && mode != XIM && mode != ASCII) {
			throw new IllegalArgumentException();
		}
		this.mode = mode;
	}

	public int getInputMode(){
		return mode;
	}

	public void insertString(int offs, String str, AttributeSet a)
		throws BadLocationException {

		// Handle kana input
		if (mode == KANA) {
			// Do nothing if there is no input
			if (str.length() < 1) {
				return;
			}

			// Just insert for non-keyboard input
			if (str.length() > 1) {
				super.insertString(offs, str, a);
				return;
			}

			if (str.length() != 1) {
				throw new IllegalStateException("cannot hapen");
			}

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
				char c = getSymbol(thisChar);
				if (c != 0) {
					str = String.valueOf(c);
				} else {
					super.insertString(offs, str, a);
					return;
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
				if (thisChar != 'n' && thisChar != '\'') {
					str += thisChar;
				}
			} else if (thisChar == prefix.charAt(0)) {
				// xx -> \u30c3
				str = "\u30c3" + thisChar;
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
				case 'a': return KANA_TABLE[i].a;
				case 'i': return KANA_TABLE[i].i;
				case 'u': return KANA_TABLE[i].u;
				case 'e': return KANA_TABLE[i].e;
				case 'o': return KANA_TABLE[i].o;
				default:  return null;
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

