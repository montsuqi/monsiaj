/*      PANDA -- a simple transaction monitor

Copyright (C) 1998-1999 Ogochan.
              2000-2003 Ogochan & JMA (Japan Medical Association).

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

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import org.montsuqi.util.ByteOrder;

public class LittleEndianDataOutputStream extends OutputStream implements DataOutput {

	private DataOutputStream dos;

	public LittleEndianDataOutputStream(OutputStream os) {
		super();
		this.dos = new DataOutputStream(os);
	}

	public void close() throws IOException {
		dos.close();
	}

	public void flush() throws IOException {
		dos.flush();
	}

	public void write(byte[] b) throws IOException {
		dos.write(b);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		dos.write(b, off, len);
	}

	public void write(int b) throws IOException {
		dos.write(b);
	}

	public void writeBoolean(boolean v) throws IOException {
		dos.writeBoolean(v);
	}

	public void writeByte(int b) throws IOException {
		dos.writeByte(b);
	}

	public void writeBytes(String s) throws IOException {
		dos.writeBytes(s);
	}

	public void writeChar(int c) throws IOException {
		dos.writeChar(ByteOrder.reverse(c));
	}

	public void writeChars(String s) throws IOException {
		char[] chars = s.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			dos.writeChar(chars[i]);
		}
	}

	public void writeDouble(double d) throws IOException {
		long bits = Double.doubleToLongBits(d);
		dos.writeDouble(Double.longBitsToDouble(ByteOrder.reverse(bits)));
	}

	public void writeFloat(float f) throws IOException {
		int bits = Float.floatToIntBits(f);
		dos.writeFloat(Float.intBitsToFloat(ByteOrder.reverse(bits)));
	}

	public void writeInt(int i) throws IOException {
		dos.writeInt(ByteOrder.reverse(i));
	}

	public void writeLong(long l) throws IOException {
		dos.writeLong(ByteOrder.reverse(l));
	}

	public void writeShort(int s) throws IOException {
		dos.writeShort(ByteOrder.reverse((short)s));
	}

	public void writeUTF(String s) throws IOException {
		dos.writeUTF(s);
	}
}
