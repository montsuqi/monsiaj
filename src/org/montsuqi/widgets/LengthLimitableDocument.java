package org.montsuqi.widgets;

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
}
