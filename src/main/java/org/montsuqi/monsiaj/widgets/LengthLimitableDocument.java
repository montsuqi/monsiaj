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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;


/** <p>A Document model that can restrict length of the document.</p>
 */
public class LengthLimitableDocument extends PlainDocument {

	private int limit;

	/** <p>Constructs a LengthLimitableDocument instance.</p>
	 */
	public LengthLimitableDocument() {
		super();
		limit = Integer.MAX_VALUE;
	}

	/** <p>Sets the limit of document length.</p>
	 * 
	 * @param limit the limit.
	 * @throws IllegalArgumentException if the limit is negative.
	 */
	public void setLimit(int limit) {
		if (limit < 0) {
			throw new IllegalArgumentException("negative limit");
		}
		this.limit = limit;
	}

	/** <p>Gets the limit of this document.</p>
	 * 
	 * @return the limit.
	 */
	public int getLimit() {
		return limit;
	}

	/** <p>Inserts given string into this document.</p>
	 * <p>Text beyond the limit is simply omitted.</p>
	 */
	public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
		final int length = Math.min(str.length(), limit - getLength());
		if (length > 0) {
			super.insertString(offset, str.substring(0, length), a);
		}
    }
}
