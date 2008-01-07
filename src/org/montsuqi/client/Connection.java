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

import org.montsuqi.util.SystemEnvironment;

/** <p>A class that represents client/server connection.</p>
 * <p>This class implements methods for sending/receiving basic data types such as
 * booleans, integers and strings.</p>
 */
class Connection {

	private String encoding;
	private Socket socket;
	protected DataInput in;
	protected DataOutput out;
	private int dataType;

	/** <p>Constructs a Connection instance.</p>
	 * <p>Input/Output streams are initialized correctly by <var>networkByteOrder</var>.
	 * @param s the socket to communicate.
	 * @param encoding the encoding to use.
	 * @param networkByteOrder true if networkByteOrder(Big Endian) is used. false otherwise(Litle Endian).
	 */
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

	/** <p>Sends a packet class.</p>
	 * @param c packet class. should use constants defined in PacketClass.
	 * @throws IOException on I/O error.
	 */
	public synchronized void sendPacketClass(int c) throws IOException {
		out.write((byte)c);
		((OutputStream)out).flush();
	}

	/** <p>Receives a packet class.</p>
	 * @return a byte representing packet class.
	 * @throws IOException on I/O error.
	 */
	synchronized byte receivePacketClass() throws IOException {
		byte b = in.readByte();
		return b;
	}

	/** <p>Sends a data type.</p>
	 * @param c data type. should use constants defined in Type.
	 * @throws IOException on I/O error.
	 */
	public synchronized void sendDataType(int c) throws IOException {
		out.write((byte)c);
		((OutputStream)out).flush();
	}

	/** <p>Receives a data type.</p>
	 * @return a byte representing data type.
	 * @throws IOException on I/O error.
	 */
	synchronized int receiveDataType() throws IOException {
		dataType = in.readByte();
		if (dataType < 0) {
			dataType = 0x100 + dataType;
		}
		return dataType;
	}

	/** <p>Receives a data type.  If the type does not match to <var>expected</var> type,
	 * an IOException is thrown.</p>
	 * @param expected data type that should have been sent from the server.
	 * @return the type received.
	 * @throws IOException on I/O error or type mismatch.
	 */
	public synchronized int receiveDataTypeWithCheck(int expected) throws IOException {
		receiveDataType();
		if (dataType != expected) {
			throw new IOException("data type mismatch: expected " + Type.getName(expected) + ", but was " + Type.getName(dataType)); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return dataType;
	}

	/** <p>Returns the last(latest) data type received.</p>
	 * @return the latest result of recevieDataType().
	 */
	public synchronized int getLastDataType() {
		return dataType;
	}

	/** <p>Sends length of 4-byte long.</p>
	 * 
	 * @param size the length to send.
	 * @throws IOException on I/O error.
	 */
	synchronized void sendLength(int size) throws IOException {
		out.writeInt(size);
		((OutputStream)out).flush();
	}

	/** <p>Receives length of 4-byte long.</p>
	 * 
	 * @return the length received.
	 * @throws IOException on I/O error.
	 */
	synchronized int receiveLength() throws IOException {
		return in.readInt();
	}

	/** <p>Sends a string.</p>
	 * <p>If the client environment uses MS932 characters, incompatible
	 * characters are converted to compatible ones.</p>
	 * @param s the string to send.
	 * @throws IOException on I/O error.
	 */
	public synchronized void sendString(String s) throws IOException {
		if (SystemEnvironment.isMS932()) {
			s = fromMS932(s);
		}
		byte[] bytes = s.getBytes(encoding);
		sendLength(bytes.length);
		out.write(bytes);
		((OutputStream)out).flush();
	}

	/** <p>Converts MS932 characters to compatible characters.</p>
	 */
	private String fromMS932(String s) {
		char[] chars = s.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			switch (chars[i]) {
			case 0xFF0D: // FULLWIDTH HYPHEN-MINUS
				chars[i] = 0x2212; // MINUS SIGN
				break;
			case 0xFF5E: // FULLWIDTH TILDE
				chars[i] = 0x301C; // WAVE DASH
				break;
			case 0x2225: // PARALLEL TO
				chars[i] = 0x2016; // DOUBLE VERTICAL LINE
				break;
			case 0xFFE0: // FULLWIDTH CENT SIGN
				chars[i] = 0x00A2; // CENT SIGN
				break;
			case 0xFFE1: // FULLWIDTH POUND SIGN
				chars[i] = 0x00A3; // POUND SIGN
				break;
			case 0xFFE2: // FULLWIDTH NOT SIGN
				chars[i] = 0x00AC; // NOT SIGN
				break;
			}
		}
		return new String(chars);
	}

	/** <p>Converts characters to MS932 characters.</p>
	 */
	private String toMS932(String s) {
		char[] chars = s.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			switch (chars[i]) {
			case 0x2212: // MINUS SIGN
				chars[i] = 0xFF0D; // FULLWIDTH HYPHEN-MINUS
				break;
			case 0x301C: // WAVE DASH
				chars[i] = 0xFF5E; // FULLWIDTH TILDE
				break;
			case 0x2016: // DOUBLE VERTICAL LINE
				chars[i] = 0x2225; // PARALLEL TO
				break;
			case 0x00A2: // CENT SIGN
				chars[i] = 0xFFE0; // FULLWIDTH CENT SIGN
				break;
			case 0x00A3: // POUND SIGN
				chars[i] = 0xFFE1; // FULLWIDTH POUND SIGN
				break;
			case 0x00AC: // NOT SIGN
				chars[i] = 0xFFE2; // FULLWIDTH NOT SIGN
				break;
			}
		}
		return new String(chars);
	}

	/** <p>Receives string data part of given size.</p>
	 * 
	 * @param size the size of the data to be received.
	 * @return a String instance.
	 * @throws IOException on I/O error.
	 */
	synchronized String receiveStringBody(int size) throws IOException {
		byte[] bytes = new byte[size];
		in.readFully(bytes);
		final String s = new String(bytes, encoding);
		return SystemEnvironment.isMS932() ? toMS932(s) : s;
	}

	/** <p>Receives a string </p>
	 * <p>At first, the length of the string is received. Then
	 * the string's data of that length are received and built into a String.
	 * 
	 * @return a String instance.
	 * @throws IOException on I/O error.
	 */
	public synchronized String receiveString() throws IOException {
		return receiveStringBody(receiveLength());
	}

	/** <p>Receives an integer of 4-byte long.</p>
	 * @return the integer received.
	 * @throws IOException on I/O error.
	 */
	synchronized int receiveLong() throws IOException { // longs: 4-byte long
		int i = in.readInt();
		// WORKAROUND:
		// some application returns "    "(4 spaces) for
		// integer 0 even it sends numeric data type beforehand.
		if (i == 0x20202020) { // " " is 0x20
			i = 0;
		}
		return i;
	}

	synchronized void sendLong(int data) throws IOException {
		out.writeInt(data);
		((OutputStream)out).flush();
	}

	synchronized public int receiveInt() throws IOException {
		return receiveLong();
	}

	synchronized public void sendInt(int data) throws IOException {
		sendLong(data);
	}

	synchronized int receiveChar() throws IOException {
		return in.readUnsignedByte();
	}

	synchronized void sendChar(byte data) throws IOException {
		out.write(data);
		((OutputStream)out).flush();
	}

	synchronized double receiveFloat() throws IOException {
		return in.readDouble();
	}

	synchronized void sendFloat(double data) throws IOException {
		out.writeDouble(data);
		((OutputStream)out).flush();
	}

	private static final byte T_BYTE = 0x54;
	private static final byte F_BYTE = 0x46;

	synchronized boolean receiveBoolean() throws IOException {
		return in.readByte() == T_BYTE ? true : false;
	}

	synchronized void sendBoolean(boolean data) throws IOException {
		out.writeByte(data ? T_BYTE : F_BYTE);
		((OutputStream)out).flush();
	}

	private static final BigDecimal ZERO = new BigDecimal(BigInteger.ZERO);
	private static final int NEGATIVE_FIXED_MASK = 0x40;

	synchronized void sendFixed(BigDecimal xval) throws IOException {
		String s;
		if (xval.equals(ZERO)) {
			sendString("0"); //$NON-NLS-1$
			return;
		}
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

	synchronized BigDecimal receiveFixed() throws IOException {
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

	public synchronized BigDecimal receiveFixedData() throws IOException {
		if (receiveDataType() == Type.NUMBER) {
			return receiveFixed();
		}
		throw new IllegalArgumentException("invalid data conversion"); //$NON-NLS-1$
	}

	public synchronized void sendFixedData(int type, BigDecimal xval) throws IOException {
		if (type == Type.NUMBER) {
			type = Type.TEXT;
			if (xval.movePointRight(xval.scale()).equals(ZERO)) {
				xval = ZERO;
			}
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

	public synchronized void sendStringData(int type, String str) throws IOException {
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

	public synchronized String receiveStringData() throws IOException {
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

	public synchronized void sendIntegerData(int type, int value) throws IOException {
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

	public synchronized int receiveIntData() throws IOException {
		switch (receiveDataType()) {
		case Type.INT:
			return receiveInt();
		case Type.CHAR:
		case Type.VARCHAR:
		case Type.DBCODE:
		case Type.TEXT:
			String value = receiveString().trim();
			return value.length() == 0 ? 0 : Integer.parseInt(value);
		default:
			throw new IllegalArgumentException();
		}
	}

	public synchronized void sendBooleanData(int type, boolean value) throws IOException {
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

	public synchronized boolean receiveBooleanData() throws IOException {
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

	public synchronized byte[] receiveBinaryData() throws IOException {
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

	private synchronized byte[] receiveBinary() throws IOException {
		int size = receiveLength();
		byte[] bin = new byte[size];
		if (size > 0) {
			in.readFully(bin);
		}
		return bin;
	}

	public synchronized void sendBinaryData(int type, byte [] binary) throws IOException {
		sendDataType(type);
		switch (type) {
		case Type.CHAR:
		case Type.VARCHAR:
		case Type.DBCODE:
		case Type.TEXT:
		case Type.BINARY:
		case Type.BYTE:
		case Type.OBJECT:
			sendBinary(binary);
			break;
		default:
			Object[] args = { Integer.toHexString(type) };
			throw new IllegalArgumentException(MessageFormat.format("invalid data type(0x{0})", args)); //$NON-NLS-1$
		}
	}

	synchronized void sendBinary(byte [] binary) throws IOException {
		sendLength(binary.length);
		if (binary.length > 0) {
			out.write(binary);
		}
	}
	
	synchronized void close() throws IOException {
		socket.shutdownInput(); 
		socket.shutdownOutput();
		((InputStream)in).close();
		((OutputStream)out).close();
		socket.close();
	}
}
