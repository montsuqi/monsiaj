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
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Socket;
import java.text.MessageFormat;

import org.montsuqi.util.Logger;

class Connection {

	private String encoding;
	private Socket socket;
	protected LittleEndianDataInputStream in;
	protected LittleEndianDataOutputStream out;
	private int dataType;
	private Logger logger;

	Connection(Socket s, String encoding) throws IOException {
		this.socket = s;
		this.encoding = encoding;
		in = new LittleEndianDataInputStream(new BufferedInputStream(socket.getInputStream()));
		out = new LittleEndianDataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		logger = Logger.getLogger(Connection.class);
	}

	public void sendPacketClass(int c) throws IOException {
		out.write((byte)c);
		out.flush();
	}

	byte receivePacketClass() throws IOException {
		byte b = in.readByte();
		//logger.debug("receivePacketClass: 0x{0}", Integer.toHexString(b));
		return b;
	}

	public void sendDataType(int c) throws IOException {
		out.write((byte)c);
		out.flush();
	}

	int receiveDataType() throws IOException {
		dataType = in.readByte();
		if (dataType < 0) {
			dataType = 0x100 + dataType;
		}
		//logger.debug("receiveDataType: 0x{0}", Integer.toHexString(dataType));
		return dataType;
	}

	public int receiveDataTypeWithCheck(int expected) throws IOException {
		receiveDataType();
		if (dataType != expected) {
			throw new IOException(Messages.getString("Connection.data_type_mismatch")); //$NON-NLS-1$
		}
		return dataType;
	}
	
	public int getLastDataType() {
		return dataType;
	}

	void sendPacketDataType(int t) throws IOException {
		out.write((byte)t);
		out.flush();
	}

	int receivePacketDataType() throws IOException {
		return in.readByte();
	}

	void sendLength(int size) throws IOException {
		out.writeInt(size);
		out.flush();
	}

	int receiveLength() throws IOException {
		int length = in.readInt();
		//logger.debug("receiveLength: {0}", new Integer(length));
		return length;
	}

	public void sendString(String s) throws IOException {
		byte[] bytes = s.getBytes(encoding);
		sendLength(bytes.length);
		out.write(bytes);
		out.flush();
	}

	void sendFixedString(String s) throws IOException {
		sendString(s);
	}

	String receiveStringBody(int size) throws IOException {
		byte[] bytes = new byte[size];
		in.read(bytes);
		String s = new String(bytes, encoding);
		//logger.debug("receiveStringBody: {0}", s);
		return s;
	}

	public String receiveString() throws IOException {
		int size = receiveLength();
		return receiveStringBody(size);
	}

	int receiveLong() throws IOException { /* longs: 4-byte long */
		int l = in.readInt();
		//logger.debug("receiveLong: {0}", new Integer(l));
		return l;
	}

	void sendLong(int data) throws IOException {
		out.writeInt(data);
		out.flush();
	}

	public int receiveInt() throws IOException {
		return receiveLong();
	}

	public void sendInt(int data) throws IOException {
		sendLong(data);
	}

	int receiveChar() throws IOException {
		int c = in.readUnsignedByte();
		//logger.debug("receiveChar: {0}", new Character((char)c));
		return c;
	}

	void sendChar(byte data) throws IOException {
		out.write(data);
		out.flush();
	}

	double receiveFloat() throws IOException {
		double f = in.readDouble();
		//logger.debug("receiveFloat: {0}", new Double(f));
		return f;
	}

	void sendFloat(double data) throws IOException {
		out.writeDouble(data);
		out.flush();
	}

	private static final byte T_BYTE = 0x54;
	private static final byte F_BYTE = 0x46;

		boolean receiveBoolean() throws IOException {
		byte b = in.readByte();
		boolean value = b == T_BYTE ? true : false;
		//logger.debug("receiveBoolean: {0}", new Boolean(value));
		return value;
	}

	public void sendBoolean(boolean data) throws IOException {
		out.writeByte(data ? T_BYTE : F_BYTE);
		out.flush();
	}

	void sendFixed(BigDecimal xval) throws IOException {
		String s = String.valueOf(xval.unscaledValue());
		sendLength(s.length() - xval.scale() - 1); /* 1 for the dot */
		sendLength(xval.scale());
		sendString(s);
	}

	BigDecimal receiveFixed() throws IOException {
		int flen = receiveLength();
		int slen = receiveLength();
		String value = receiveString();
		BigInteger i = BigInteger.ZERO;
		if (value == null || value.length() == 0) {
			logger.warn("empty Fixed value");
		} else {
			try {
				i =  new BigInteger(value);
			} catch (NumberFormatException e) {
				logger.warn(e);
			}
		}
		BigDecimal result = (new BigDecimal(i)).movePointLeft(slen);
		return result;
	}

	public BigDecimal receiveFixedData() throws IOException {
		//logger.debug("receiveFixedData");
		if (receiveDataType() == Type.NUMBER) {
			return receiveFixed();
		} else {
			throw new IllegalArgumentException(Messages.getString("Connection.invalid_data_conversion")); //$NON-NLS-1$
		}
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
			throw new IllegalArgumentException(Messages.getString("Connection.invalid_data_conversion")); //$NON-NLS-1$
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
		//logger.debug("receiveStringData");
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
			String message = Messages.getString("Connection.invalid_data_type"); //$NON-NLS-1$
			message = MessageFormat.format(message, new Object[] { Integer.toHexString(type) });
			throw new IllegalArgumentException(message);
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
			sendFloat((double)value);
			break;
		case Type.INT:
			sendInt(value);
			break;
		case Type.FLOAT:
			sendFloat((double)value);
			break;
		case Type.BOOL:
			sendBoolean(value != 0);
			break;
		default:
			break;
		}
	}

	public int receiveIntData() throws IOException {
		//logger.debug("receiveIntData");
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
			sendString(value ? "T" : "F"); //$NON-NLS-1$ //$NON-NLS-2$
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
		//logger.debug("receiveBooleanData");
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

	void close() throws IOException {
		socket.shutdownInput(); 
		socket.shutdownOutput();
		in.close();
		out.close();
		socket.close();
	}
}
