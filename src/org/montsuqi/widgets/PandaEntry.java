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
		'　', '！', '”', '＃', '＄', '％', '＆', '’',
		'（', '）', '＊', '＋', '，', 'ー', '．', '／',
		'０', '１', '２', '３', '４', '５', '６', '７',
		'８', '９', '：', '；', '＜', '＝', '＞', '？',
		'＠', 'Ａ', 'Ｂ', 'Ｃ', 'Ｄ', 'Ｅ', 'Ｆ', 'Ｇ',
		'Ｈ', 'Ｉ', 'Ｊ', 'Ｋ', 'Ｌ', 'Ｍ', 'Ｎ', 'Ｏ',
		'Ｐ', 'Ｑ', 'Ｒ', 'Ｓ', 'Ｔ', 'Ｕ', 'Ｖ', 'Ｗ',
		'Ｘ', 'Ｙ', 'Ｚ', '［', '￥', '］', '＾', '＿',
		'‘', 'ア',    0,    0,    0, 'エ',    0,    0,
		   0, 'イ',    0,    0,    0,    0,    0, 'オ',
		   0,    0,    0,    0,    0, 'ウ',    0,    0,
		   0,    0,    0, '｛', '｜', '｝', '￣',    0
	};

	static final RuleEntry[] KANA_TABLE = {
		new RuleEntry("l",  "ァ", "ィ", "ゥ", "ェ", "ォ"),
		new RuleEntry("x",  "ァ", "ィ", "ゥ", "ェ", "ォ"),
		new RuleEntry("k",  "カ", "キ", "ク", "ケ", "コ"),
		new RuleEntry("ky", "キャ", "キィ", "キュ", "キェ", "キョ"),
		new RuleEntry("g",  "ガ", "ギ", "グ", "ゲ", "ゴ"),
		new RuleEntry("gy", "ギャ", "ギィ", "ギュ", "ギェ", "ギョ"),
		new RuleEntry("s",  "サ", "シ", "ス", "セ", "ソ"),
		new RuleEntry("sh", "シャ", "シ", "シュ", "シェ", "ショ"),
		new RuleEntry("sy", "シャ", "シィ", "シュ", "シェ", "ショ"),
		new RuleEntry("z",  "ザ", "ジ", "ズ", "ゼ", "ゾ"),
		new RuleEntry("j",  "ジャ", "ジ", "ジュ", "ジェ", "ジョ"),
		new RuleEntry("jy", "ジャ", "ジィ", "ジュ", "ジェ", "ジョ"),
		new RuleEntry("zy", "ジャ", "ジィ", "ジュ", "ジェ", "ジョ"),
		new RuleEntry("t",  "タ", "チ", "ツ", "テ", "ト"),
		new RuleEntry("ts", null, null, "ツ", null, null),
		new RuleEntry("lt", null, null, "ッ", null, null),
		new RuleEntry("xt", null, null, "ッ", null, null),
		new RuleEntry("ty", "チャ", "チィ", "チュ", "チェ", "チョ"),
		new RuleEntry("cy", "チャ", "チィ", "チュ", "チェ", "チョ"),
		new RuleEntry("ch", "チャ", "チ", "チュ", "チェ", "チョ"),
		new RuleEntry("th", "テャ", "ティ", "テュ", "テェ", "テョ"),
		new RuleEntry("d",  "ダ", "ヂ", "ヅ", "デ", "ド"),
		new RuleEntry("dy", "ヂャ", "ヂィ", "ヂュ", "ヂェ", "ヂョ"),
		new RuleEntry("dh", "デャ", "ディ", "デュ", "デェ", "デョ"),
		new RuleEntry("dw", "ドァ", "ドィ", "ドゥ", "ドェ", "ドォ"),
		new RuleEntry("n",  "ナ", "ニ", "ヌ", "ネ", "ノ"),
		new RuleEntry("ny", "ニャ", "ニィ", "ニュ", "ニェ", "ニョ"),
		new RuleEntry("h",  "ハ", "ヒ", "フ", "ヘ", "ホ"),
		new RuleEntry("f",  "ファ", "フィ", "フ", "フェ", "フォ"),
		new RuleEntry("hy", "ヒャ", "ヒィ", "ヒュ", "ヒェ", "ヒョ"),
		new RuleEntry("fy", "フャ", "フィ", "フュ", "フェ", "フョ"),
		new RuleEntry("b",  "バ", "ビ", "ブ", "ベ", "ボ"),
		new RuleEntry("by", "ビャ", "ビィ", "ビュ", "ビェ", "ビョ"),
		new RuleEntry("p",  "パ", "ピ", "プ", "ペ", "ポ"),
		new RuleEntry("py", "ピャ", "ピィ", "ピュ", "ピェ", "ピョ"),
		new RuleEntry("m",  "マ", "ミ", "ム", "メ", "モ"),
		new RuleEntry("my", "ミャ", "ミィ", "ミュ", "ミェ", "ミョ"),
		new RuleEntry("y",  "ヤ", "イ", "ユ", "イェ", "ヨ"),
		new RuleEntry("ly", "ャ", null, "ュ", null, "ョ"),
		new RuleEntry("xy", "ャ", null, "ュ", null, "ョ"),
		new RuleEntry("r",  "ラ", "リ", "ル", "レ", "ロ"),
		new RuleEntry("ry", "リャ", "リィ", "リュ", "リェ", "リョ"),
		new RuleEntry("w",  "ワ", "ヰ", "ウ", "ヱ", "ヲ"),
		new RuleEntry("wh", "ワ", "ウィ", "ウ", "ウェ", "ウォ"),
		new RuleEntry("lw", "ヮ", null, null, null, null),
		new RuleEntry("xw", "ヮ", null, null, null, null),
		new RuleEntry("v",  "ヴァ", "ヴィ", "ヴ", "ヴェ", "ヴォ"),
		new RuleEntry("q",  "クァ", "クィ", "ク", "クェ", "クォ"),
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
				// カナ
				String s = getKana(prefix, thisChar);
				if (s != null) {
					str = s;
				} else {
					super.insertString(offs, str, a);
					return;
				}
			} else if (prefix.charAt(0) == 'n' && thisChar != 'y') {
				// n -> ン
				str = "ン";
				if (thisChar != 'n' && thisChar != '\'') {
					str += thisChar;
				}
			} else if (thisChar == prefix.charAt(0)) {
				// xx -> ッ
				str = "ッ" + thisChar;
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

