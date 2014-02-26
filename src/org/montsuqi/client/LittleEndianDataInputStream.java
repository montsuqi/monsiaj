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

package org.montsuqi.client;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;
import org.montsuqi.util.ByteOrder;

/** <p>A DataInputStream decorator that reads data in little endian.</p> */
public class LittleEndianDataInputStream extends InputStream implements DataInput {

	private DataInputStream dis;

	public LittleEndianDataInputStream(InputStream is) {
		super();
		this.dis = new DataInputStream(is);
	}

	public int read() throws IOException {
		return dis.read();
	}

	public int read(byte[] b) throws IOException {
		return dis.read(b);
	}

	public int read(byte[] b, int off, int len) throws IOException {
		return dis.read(b, off, len);
	}

	public boolean readBoolean() throws IOException {
		return dis.readBoolean();
	}

	public byte readByte() throws IOException {
		return dis.readByte();
	}

	public char readChar() throws IOException {
		return ByteOrder.reverse(dis.readChar());
	}

	public double readDouble() throws IOException {
		return ByteOrder.reverse(dis.readDouble());
	}

	public float readFloat() throws IOException {
		return ByteOrder.reverse(dis.readFloat());
	}

	public void readFully(byte[] b) throws IOException {
		dis.readFully(b);
	}

	public void readFully(byte[] b, int off, int len) throws IOException {
		dis.readFully(b, off, len);
	}

	public int readInt() throws IOException {
		return ByteOrder.reverse(dis.readInt());
	}

	public String readLine() throws IOException {
		throw new UnsupportedOperationException("deprecated DataInput#readLine() is not impremented"); 
	}

	public long readLong() throws IOException {
		return ByteOrder.reverse(dis.readLong());
	}

	public short readShort() throws IOException {
		return ByteOrder.reverse(dis.readShort());
	}

	public int readUnsignedByte() throws IOException {
		return ByteOrder.reverse((int)dis.readByte());
	}

	public int readUnsignedShort() throws IOException {
		return ByteOrder.reverse((int)dis.readShort());
	}

	public String readUTF() throws IOException {
		return dis.readUTF();
	}

	public int skipBytes(int n) throws IOException {
		return dis.skipBytes(n);
	}
}
