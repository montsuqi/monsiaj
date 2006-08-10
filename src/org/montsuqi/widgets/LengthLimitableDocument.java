package org.montsuqi.widgets;

import java.io.UnsupportedEncodingException;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;


public class LengthLimitableDocument extends PlainDocument {

	private int limit;

	public LengthLimitableDocument() {
		super();
		limit = Integer.MAX_VALUE;
	}
	public void setLimit(int limit) {
		if (limit < 0) {
			throw new IllegalArgumentException("negative limit");
		}
		this.limit = limit;
	}

	public int getLimit() {
		return limit;
	}

	public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
		int curLength = getLength();
		String curText = getText(0, curLength);
		try {
			final byte[] curBytes = curText.getBytes("EUC-JP");
			int left = limit - curBytes.length;
			if (left > 0) {
				final char[] chars = str.toCharArray();
				final StringBuffer buf = new StringBuffer();
				for (int i = 0; i < chars.length; i++) {
					final String oneChar = String.valueOf(chars[i]);
					final byte[] bytes = oneChar.getBytes("EUC-JP");
					if (left < bytes.length) {
						break;
					}
					buf.append(chars[i]);
					left -= bytes.length;
	        	}
				super.insertString(offset, buf.toString(), a);
	        }
        } catch (UnsupportedEncodingException e) {
        	// ignore
        }
    }
}
