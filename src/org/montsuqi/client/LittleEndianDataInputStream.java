package org.montsuqi.client;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;
import org.montsuqi.util.ByteOrder;

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
		throw new UnsupportedOperationException("DataInput#readLine() which is deprecated is not imprementad.");
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
