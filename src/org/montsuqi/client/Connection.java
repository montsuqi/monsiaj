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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Socket;
import java.text.MessageFormat;

class Connection {

	private String encoding;
	private Socket socket;
	protected DataInput in;
	protected DataOutput out;
	private int dataType;

	Connection(Socket s, String encoding, boolean networkByteOrder) throws IOException {
		this.socket = s;
		this.encoding = encoding;
		InputStream rawIn = new BufferedInputStream(socket.getInputStream());
		OutputStream rawOut = new BufferedOutputStream(socket.getOutputStream());
		if (networkByteOrder) {
			in = new DataInputStream(rawIn);
			out = new DataOutputStream(rawOut);
		} else {
			in = new LittleEndianDataInputStream(rawIn);
			out = new LittleEndianDataOutputStream(rawOut);
		}
	}

	public void sendPacketClass(int c) throws IOException {
		out.write((byte)c);
		((OutputStream)out).flush();
	}

	byte receivePacketClass() throws IOException {
		byte b = in.readByte();
		return b;
	}

	public void sendDataType(int c) throws IOException {
		out.write((byte)c);
		((OutputStream)out).flush();
	}

	int receiveDataType() throws IOException {
		dataType = in.readByte();
		if (dataType < 0) {
			dataType = 0x100 + dataType;
		}
		return dataType;
	}

	public int receiveDataTypeWithCheck(int expected) throws IOException {
		receiveDataType();
		if (dataType != expected) {
			throw new IOException("data type mismatch: expected " + Type.getName(expected) + ", but was " + Type.getName(dataType)); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return dataType;
	}

	public int getLastDataType() {
		return dataType;
	}

	void sendPacketDataType(int t) throws IOException {
		out.write((byte)t);
		((OutputStream)out).flush();
	}

	int receivePacketDataType() throws IOException {
		return in.readByte();
	}

	void sendLength(int size) throws IOException {
		out.writeInt(size);
		((OutputStream)out).flush();
	}

	int receiveLength() throws IOException {
		return in.readInt();
	}

	public void sendString(String s) throws IOException {
		byte[] bytes = s.getBytes(encoding);
		sendLength(bytes.length);
		out.write(bytes);
		((OutputStream)out).flush();
	}

	void sendFixedString(String s) throws IOException {
		sendString(s);
	}

	String receiveStringBody(int size) throws IOException {
		byte[] bytes = new byte[size];
		in.readFully(bytes);
		return new String(bytes, encoding);
	}

	public String receiveString() throws IOException {
		return receiveStringBody(receiveLength());
	}

	int receiveLong() throws IOException { // longs: 4-byte long
		return in.readInt();
	}

	void sendLong(int data) throws IOException {
		out.writeInt(data);
		((OutputStream)out).flush();
	}

	public int receiveInt() throws IOException {
		return receiveLong();
	}

	public void sendInt(int data) throws IOException {
		sendLong(data);
	}

	int receiveChar() throws IOException {
		return in.readUnsignedByte();
	}

	void sendChar(byte data) throws IOException {
		out.write(data);
		((OutputStream)out).flush();
	}

	double receiveFloat() throws IOException {
		return in.readDouble();
	}

	void sendFloat(double data) throws IOException {
		out.writeDouble(data);
		((OutputStream)out).flush();
	}

	private static final byte T_BYTE = 0x54;
	private static final byte F_BYTE = 0x46;

	boolean receiveBoolean() throws IOException {
		return in.readByte() == T_BYTE ? true : false;
	}

	void sendBoolean(boolean data) throws IOException {
		out.writeByte(data ? T_BYTE : F_BYTE);
		((OutputStream)out).flush();
	}

	void sendFixed(BigDecimal xval) throws IOException {
		String s;
		if (xval.signum() >= 0) {
			s = String.valueOf(xval.unscaledValue());
		} else {
			s = String.valueOf(xval.negate().unscaledValue());
			char[] chars = s.toCharArray();
			chars[0] |= 0x40;
			s = new String(chars);
		}
		sendLength(s.length() - xval.scale() - 1); // 1 for the dot
		sendLength(xval.scale());
		sendString(s);
	}

	private static final int NEGATIVE_FIXED_MASK = 0x40;
	private static final BigDecimal ZERO = new BigDecimal(BigInteger.ZERO);

	BigDecimal receiveFixed() throws IOException {
		/* int flen = */ receiveLength();
		int slen = receiveLength();
		String value = receiveString();
		if (value == null) {
			throw new IllegalArgumentException("empty Fixed value"); //$NON-NLS-1$
		}
		value = value.trim();
		if (value.length() == 0) {
			return ZERO;
		}
		// negative values are represented by masking the first
		// character with NEGATIVE_FIXED_MASK(0x40),
		// '0'(0x30) -> 'p'(0x70) for instance.
		char[] chars = value.toCharArray();
		boolean negative = (chars[0] & NEGATIVE_FIXED_MASK) != 0;
		if (negative) {
			chars[0] &= ~NEGATIVE_FIXED_MASK;
			value = new String(chars);
		}
		try {
			BigDecimal v =  new BigDecimal(value);
			if (negative) {
				v = v.negate();
			}
			return v.movePointLeft(slen);
		} catch (NumberFormatException e) {
			IllegalArgumentException iae = new IllegalArgumentException();
			iae.initCause(e);
			throw iae;
		}
	}

	public BigDecimal receiveFixedData() throws IOException {
		if (receiveDataType() == Type.NUMBER) {
			return receiveFixed();
		}
		throw new IllegalArgumentException("invalid data conversion"); //$NON-NLS-1$
	}

	public void sendFixedData(int type, BigDecimal xval) throws IOException {
		if (type == Type.NUMBER) {
			type = Type.TEXT;
		}
		sendDataType(type);
		switch (type) {
		case Type.CHAR:
		case Type.VARCHAR:
		case Type.DBCODE:
		case Type.TEXT:
			sendString(String.valueOf(xval));
			break;
		//case Type.NUMBER:
		//	sendFixed(xval);
		//	break;
		default:
			throw new IllegalArgumentException("invalid data conversion"); //$NON-NLS-1$
		}
	}

	public void sendStringData(int type, String str) throws IOException {
		sendDataType(type);
		switch (type) {
		case Type.CHAR:
		case Type.VARCHAR:
		case Type.DBCODE:
		case Type.TEXT:
			sendString(str);
			break;
		case Type.INT:
			sendInt(Integer.parseInt(str));
			break;
		case Type.FLOAT:
			sendFloat(Double.parseDouble(str));
			break;
		case Type.BOOL:
			sendBoolean(str.charAt(0) == 'T');
			break;
		default:
			break;
		}
	}

	public String receiveStringData() throws IOException {
		int type = receiveDataType();
		switch (type) {
		case Type.INT:
			return String.valueOf(receiveInt());
		case Type.CHAR:
		case Type.VARCHAR:
		case Type.DBCODE:
		case Type.TEXT:
			return receiveString();
		default:
			Object[] args = { Integer.toHexString(type) };
			throw new IllegalArgumentException(MessageFormat.format("invalid data type(0x{0})", args)); //$NON-NLS-1$
		}
	}

	public void sendIntegerData(int type, int value) throws IOException {
		sendDataType(type);
		switch (type) {
		case Type.CHAR:
		case Type.VARCHAR:
		case Type.DBCODE:
		case Type.TEXT:
			sendString(String.valueOf(value));
			break;
		case Type.NUMBER:
			sendFloat(value);
			break;
		case Type.INT:
			sendInt(value);
			break;
		case Type.FLOAT:
			sendFloat(value);
			break;
		case Type.BOOL:
			sendBoolean(value != 0);
			break;
		default:
			break;
		}
	}

	public int receiveIntData() throws IOException {
		switch (receiveDataType()) {
		case Type.INT:
			return receiveInt();
		case Type.CHAR:
		case Type.VARCHAR:
		case Type.DBCODE:
		case Type.TEXT:
			return Integer.parseInt(receiveString());
		default:
			throw new IllegalArgumentException();
		}
	}

	public void sendBooleanData(int type, boolean value) throws IOException {
		sendDataType(type);
		switch (type) {
		case Type.CHAR:
		case Type.VARCHAR:
		case Type.DBCODE:
		case Type.TEXT:
			sendString(value ? String.valueOf('T') : String.valueOf('F'));
			break;
		case Type.INT:
			sendInt(value ? 1 : 0);
			break;
		case Type.FLOAT:
			sendFloat(value ? 1.0 : 0.0);
			break;
		case Type.BOOL:
			sendBoolean(value);
			break;
		default:
			break;
		}
	}

	public boolean receiveBooleanData() throws IOException {
		switch (receiveDataType()) {
		case Type.INT:
			return receiveInt() != 0;
		case Type.BOOL:
			return receiveBoolean();
		case Type.CHAR:
		case Type.VARCHAR:
		case Type.DBCODE:
		case Type.TEXT:
			return receiveString().charAt(0) == 'T';
		default:
			return false;
		}
	}

	public byte[] receiveBinaryData() throws IOException {
		int type = receiveDataType();
		switch (type) {
		case Type.CHAR:
		case Type.VARCHAR:
		case Type.DBCODE:
		case Type.TEXT:
		case Type.BINARY:
		case Type.BYTE:
		case Type.OBJECT:
			return receiveBinary();
		default:
			Object[] args = { Integer.toHexString(type) };
			throw new IllegalArgumentException(MessageFormat.format("invalid data type(0x{0})", args)); //$NON-NLS-1$
		}
	}

	private byte[] receiveBinary() throws IOException {
		int size = receiveLength();
		byte[] bin = new byte[size];
		if (size > 0) {
			in.readFully(bin);
		}
		return bin;
	}

	void close() throws IOException {
		socket.shutdownInput(); 
		socket.shutdownOutput();
		((InputStream)in).close();
		((OutputStream)out).close();
		socket.close();
	}
}
