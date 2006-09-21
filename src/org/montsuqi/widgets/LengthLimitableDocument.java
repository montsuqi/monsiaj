package org.montsuqi.widgets;

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
		final int length = Math.min(str.length(), limit - getLength());
		if (length > 0) {
			super.insertString(offset, str.substring(0, length), a);
		}
    }
}
