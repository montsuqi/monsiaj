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

package org.montsuqi.monsiaj.monsia;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/** <p>An InputStream to read glade 1.0 screen definition file.</p>
 * <p>Since glade 1.0's screen definitions are written in EUC-JP but does not
 * have encoding in xml declaration, normal xml parser cannot read them correctly.</p>
 * <p>This class fakes xml encoding parameter to solve this problem.</p>
 */
final class FakeEncodingInputStream extends InputStream {
	private final InputStream in;
	byte[] headerBytes;
	private int index;

	private static final String REAL_HEADER = "<?xml version=\"1.0\"?>"; 
	private static final String FAKE_HEADER = "<?xml version=\"1.0\" encoding=\"euc-jp\"?>"; 

	public FakeEncodingInputStream(InputStream in) throws IOException {
		this.in = in;
		in.skip(REAL_HEADER.length());
		try {
			headerBytes = FAKE_HEADER.getBytes("euc-jp"); 
		} catch (UnsupportedEncodingException e) {
			throw new IOException(e);
		}
		index = 0;
	}

	/** <p>To fake xml encoding, this method returns bytes in FAKE_HEADER string
	 * while the index is less than FAKE_HEADER's length.  When it read all from
	 * FAKE_HEADER, it returns bytes actually read.</p>
	 */
        @Override
	public int read() throws IOException {
		int b;
		if (index < headerBytes.length) {
			b = headerBytes[index++];
			if (b < 0) {
				b = 0x100 + b;
			}
		} else {
			b = in.read();
		}
		return b;
	}
}
