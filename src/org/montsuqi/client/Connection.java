package org.montsuqi.client;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Socket;
import org.montsuqi.util.Logger;

public class Connection {

	private String encoding;
	private Socket socket;
	protected LittleEndianDataInputStream in;
	protected LittleEndianDataOutputStream out;
	int dataType;
	Logger logger;

	public Connection(Socket s, String encoding) throws IOException {
		this.socket = s;
		this.encoding = encoding;
		in = new LittleEndianDataInputStream(socket.getInputStream());
		out = new LittleEndianDataOutputStream(socket.getOutputStream());
		logger = Logger.getLogger(Connection.class);
	}

	public void sendPacketClass(int c) throws IOException {
		out.write((byte)c);
		out.flush();
	}

	public byte receivePacketClass() throws IOException {
		return in.readByte();
	}

	public void sendDataType(int c) throws IOException {
		out.write((byte)c);
		out.flush();
	}

	public synchronized int receiveDataType() throws IOException {
		dataType = in.readByte();
		if (dataType < 0) {
			dataType = 0x100 + dataType;
		}
		return dataType;
	}

	public synchronized int receiveDataType(int expected) throws IOException {
		receiveDataType();
		if (dataType != expected) {
			throw new IOException(Messages.getString("Connection.data_type_mismatch")); //$NON-NLS-1$
		}
		return dataType;
	}
	
	public int getLastDataType() {
		return dataType;
	}

	public void sendPacketDataType(int t) throws IOException {
		out.write((byte)t);
		out.flush();
	}

	public int receivePacketDataType() throws IOException {
		return in.readByte();
	}

	public void sendLength(int size) throws IOException {
		out.writeInt(size);
		out.flush();
	}

	public int receiveLength() throws IOException {
		return in.readInt();
	}

	public void sendString(String s) throws IOException {
		byte[] bytes = s.getBytes();
		sendLength(bytes.length);
		out.write(bytes);
		out.flush();
	}

	public void sendFixedString(String s) throws IOException {
		sendString(s);
	}

	public String receiveStringBody(int size) throws IOException {
		byte[] bytes = new byte[size];
		in.read(bytes);
		return new String(bytes, encoding);
	}

	public String receiveString() throws IOException {
		int size = receiveLength();
		return receiveStringBody(size);
	}

	public int receiveLong() throws IOException { /* longs: 4-byte long */
		return in.readInt();
	}

	public void sendLong(int data) throws IOException {
		out.writeInt(data);
		out.flush();
	}

	public int receiveInt() throws IOException {
		return receiveLong();
	}

	public void sendInt(int data) throws IOException {
		sendLong(data);
	}

	public int receiveChar() throws IOException {
		return in.readUnsignedByte();
	}

	public void sendChar(byte data) throws IOException {
		out.write(data);
		out.flush();
	}

	public double receiveFloat() throws IOException {
		return in.readDouble();
	}

	public void sendFloat(double data) throws IOException {
		out.writeDouble(data);
		out.flush();
	}

	static final byte T_BYTE = 0x54;
	static final byte F_BYTE = 0x46;
	
	public boolean receiveBoolean() throws IOException {
		byte b = in.readByte();
		return b == T_BYTE ? true : false;
	}

	
	public void sendBoolean(boolean data) throws IOException {
		out.writeByte(data ? T_BYTE : F_BYTE);
		out.flush();
	}

	public void sendFixed(BigDecimal xval) throws IOException {
		String s = String.valueOf(xval.unscaledValue());
		sendLength(s.length() - xval.scale() - 1); /* 1 for the dot */
		sendLength(xval.scale());
		sendString(s);
	}

	public BigDecimal receiveFixed() throws IOException {
		int flen = receiveLength();
		int slen = receiveLength();
		BigInteger i =  new BigInteger(receiveString());
		return (new BigDecimal(i)).movePointLeft(slen);
	}

	public BigDecimal receiveFixedData() throws IOException {
		if (receiveDataType() == Type.NUMBER) {
			return receiveFixed();
		} else {
			throw new IllegalArgumentException(Messages.getString("Connection.invalid_data_conversion")); //$NON-NLS-1$
		}
	}

	public void sendFixedData(int type, BigDecimal xval) throws IOException {
		sendDataType(type);
		switch (type) {
		case Type.CHAR:
		case Type.VARCHAR:
		case Type.DBCODE:
		case Type.TEXT:
			sendString(String.valueOf(xval));
			break;
		case Type.NUMBER:
			sendFixed(xval);
			break;
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
		switch (receiveDataType()) {
		case Type.INT:
			return String.valueOf(receiveInt());
		case Type.CHAR:
		case Type.VARCHAR:
		case Type.DBCODE:
		case Type.TEXT:
			return receiveString();
		default:
			throw new IllegalArgumentException(Messages.getString("Connection.invalid_data_type")); //$NON-NLS-1$
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
		switch (receiveDataType()) {
		case Type.INT:
			return receiveInt();
		case	Type.CHAR:
		case	Type.VARCHAR:
		case	Type.DBCODE:
		case	Type.TEXT:
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

	public void close() throws IOException {
		socket.shutdownInput(); 
		socket.shutdownOutput();
		in.close();
		out.close();
		if (socket.isConnected()) {
			socket.close();
		}
	}
}

